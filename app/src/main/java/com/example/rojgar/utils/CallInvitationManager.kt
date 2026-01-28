package com.example.rojgar.utils

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Data class representing an incoming call invitation
 */
data class CallInvitation(
    val callId: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val receiverId: String = "",
    val isVideoCall: Boolean = true,
    val timestamp: Long = 0L
) {
    constructor() : this("", "", "", "", true, 0L)
}

/**
 * Object for managing call invitations and call state through Firebase Realtime Database
 * Handles incoming/outgoing call lifecycle and Firebase synchronization
 */
object CallInvitationManager {

    private const val TAG = "CallInvitationManager"
    
    private const val CALL_INVITATIONS_PATH = "call_invitations"
    private const val CALL_SESSIONS_PATH = "call_sessions"

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // StateFlow for UI to observe incoming calls
    private val _incomingCallInvitation = MutableStateFlow<CallInvitation?>(null)
    val incomingCallInvitation: StateFlow<CallInvitation?> = _incomingCallInvitation

    // Coroutine scope for background operations
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Track current user and listeners
    private var currentUserId: String? = null
    private var invitationListener: ValueEventListener? = null

    /**
     * Initialize the CallInvitationManager (called from Application.onCreate)
     */
    fun initialize(application: Application) {
        Log.d(TAG, "CallInvitationManager initialized")
        // Update current user ID if available
        updateCurrentUser()
    }

    /**
     * Update the current user ID from Firebase Auth
     */
    private fun updateCurrentUser() {
        currentUserId = auth.currentUser?.uid
        Log.d(TAG, "Current user ID: $currentUserId")
    }

    /**
     * Start listening for incoming call invitations
     * Should be called when user logs in or activity is created
     */
    fun startListening() {
        updateCurrentUser()
        currentUserId?.let { userId ->
            if (invitationListener == null) {
                startListeningForInvitations(userId)
            }
        } ?: run {
            Log.w(TAG, "User not authenticated, cannot start listening for calls")
        }
    }

    /**
     * Private function to set up the ValueEventListener for incoming calls
     */
    private fun startListeningForInvitations(userId: String) {
        try {
            val invitationsRef = database.getReference(CALL_INVITATIONS_PATH).child(userId)

            invitationListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        if (snapshot.exists()) {
                            val invitation = snapshot.getValue(CallInvitation::class.java)
                            if (invitation != null && invitation.callId.isNotEmpty()) {
                                Log.d(TAG, "Incoming call invitation received: ${invitation.callId} from ${invitation.callerName}")
                                _incomingCallInvitation.value = invitation
                            } else {
                                Log.d(TAG, "Invalid invitation data received")
                                _incomingCallInvitation.value = null
                            }
                        } else {
                            Log.d(TAG, "No invitation data available")
                            _incomingCallInvitation.value = null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing call invitation", e)
                        _incomingCallInvitation.value = null
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error listening for call invitations: ${error.message}", error.toException())
                    _incomingCallInvitation.value = null
                }
            }

            invitationsRef.addValueEventListener(invitationListener!!)
            Log.d(TAG, "Started listening for call invitations for user: $userId")

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up invitation listener", e)
        }
    }

    /**
     * Accept an incoming call invitation
     */
    fun acceptCall(invitation: CallInvitation) {
        scope.launch {
            try {
                // Update call session status to accepted
                updateCallStatus(invitation.callId, "accepted")
                
                // Remove the invitation from Firebase
                removeInvitation(invitation.callId)

                Log.d(TAG, "Call accepted: ${invitation.callId}")

            } catch (e: Exception) {
                Log.e(TAG, "Error accepting call", e)
            }
        }
    }

    /**
     * Reject an incoming call invitation
     */
    fun rejectCall(invitation: CallInvitation) {
        scope.launch {
            try {
                // Update call session status to rejected
                updateCallStatus(invitation.callId, "rejected")

                // Remove the invitation from Firebase
                removeInvitation(invitation.callId)

                // Clear the current invitation UI
                _incomingCallInvitation.value = null

                Log.d(TAG, "Call rejected: ${invitation.callId}")

            } catch (e: Exception) {
                Log.e(TAG, "Error rejecting call", e)
            }
        }
    }

    /**
     * End an active call
     */
    fun endCall(callId: String) {
        scope.launch {
            try {
                updateCallStatus(callId, "ended")
                Log.d(TAG, "Call ended: $callId")
            } catch (e: Exception) {
                Log.e(TAG, "Error ending call", e)
            }
        }
    }

    /**
     * Remove invitation from Firebase database
     */
    private fun removeInvitation(callId: String) {
        try {
            currentUserId?.let { userId ->
                database.getReference(CALL_INVITATIONS_PATH).child(userId).removeValue()
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to remove invitation from Firebase", e)
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing invitation", e)
        }
    }

    /**
     * Update the call session status in Firebase
     */
    private fun updateCallStatus(callId: String, status: String) {
        try {
            database.getReference(CALL_SESSIONS_PATH).child(callId)
                .updateChildren(mapOf(
                    "status" to status,
                    "updatedAt" to System.currentTimeMillis()
                ))
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update call status: $e")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating call status", e)
        }
    }

    /**
     * Clear the current incoming call invitation from the UI
     */
    fun clearCurrentInvitation() {
        _incomingCallInvitation.value = null
    }

    /**
     * Stop listening for incoming calls
     * Should be called when user logs out or activity is destroyed
     */
    fun stopListening() {
        try {
            currentUserId?.let { userId ->
                val invitationsRef = database.getReference(CALL_INVITATIONS_PATH).child(userId)
                invitationListener?.let {
                    invitationsRef.removeEventListener(it)
                    Log.d(TAG, "Removed invitation listener")
                }
            }

            invitationListener = null
            _incomingCallInvitation.value = null

            Log.d(TAG, "Stopped listening for call invitations")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping listeners", e)
        }
    }

    /**
     * Send a call invitation to another user
     */
    fun sendCallInvitation(
        callId: String,
        callerId: String,
        callerName: String,
        receiverId: String,
        isVideoCall: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            // Validate inputs
            if (callId.isEmpty() || callerId.isEmpty() || receiverId.isEmpty()) {
                onFailure("Invalid call parameters: callId, callerId, or receiverId is empty")
                Log.e(TAG, "Invalid call parameters")
                return
            }

            if (callerId == receiverId) {
                onFailure("Cannot call yourself")
                Log.e(TAG, "Attempting to call own user ID")
                return
            }

            val invitation = CallInvitation(
                callId = callId,
                callerId = callerId,
                callerName = callerName,
                receiverId = receiverId,
                isVideoCall = isVideoCall,
                timestamp = System.currentTimeMillis()
            )

            Log.d(TAG, "Sending call invitation: $callId from $callerName to $receiverId")

            // Send invitation to receiver
            database.getReference(CALL_INVITATIONS_PATH).child(receiverId)
                .setValue(invitation)
                .addOnSuccessListener {
                    Log.d(TAG, "Call invitation sent to receiver: $receiverId")

                    // Create call session in database
                    val callSession = mapOf(
                        "callId" to callId,
                        "callerId" to callerId,
                        "callerName" to callerName,
                        "receiverId" to receiverId,
                        "isVideoCall" to isVideoCall,
                        "status" to "ringing",
                        "createdAt" to System.currentTimeMillis(),
                        "updatedAt" to System.currentTimeMillis()
                    )

                    database.getReference(CALL_SESSIONS_PATH).child(callId)
                        .setValue(callSession)
                        .addOnSuccessListener {
                            Log.d(TAG, "Call session created successfully: $callId")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to create call session", e)
                            // Clean up the invitation we just sent
                            database.getReference(CALL_INVITATIONS_PATH).child(receiverId).removeValue()
                            onFailure("Failed to create call session: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to send call invitation", e)
                    onFailure("Failed to send invitation: ${e.message}")
                }

        } catch (e: Exception) {
            Log.e(TAG, "Error sending call invitation", e)
            onFailure("Error: ${e.message}")
        }
    }
}