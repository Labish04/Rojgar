package com.example.rojgar.utils

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CallInvitation(
    val callId: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val isVideoCall: Boolean = true,
    val timestamp: Long = 0L
)

object CallInvitationManager {

    private val TAG = "CallInvitationManager"

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val _incomingCallInvitation = MutableStateFlow<CallInvitation?>(null)
    val incomingCallInvitation: StateFlow<CallInvitation?> = _incomingCallInvitation

    private var currentUserId: String? = null
    private var invitationListener: ValueEventListener? = null
    private var callSessionListener: ValueEventListener? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun initialize(context: Context) {
        // This method is now called from Application.onCreate()
        // The actual listening will be started when startListeningForInvitations() is called
        Log.d(TAG, "CallInvitationManager initialized")
    }

    fun startListening() {
        currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            startListeningForInvitations()
        } else {
            Log.w(TAG, "User not authenticated, cannot listen for call invitations")
        }
    }

    private fun startListeningForInvitations() {
        currentUserId?.let { userId ->
            val invitationsRef = database.getReference("call_invitations").child(userId)

            invitationListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val invitation = snapshot.getValue(CallInvitation::class.java)
                        if (invitation != null) {
                            Log.d(TAG, "Incoming call invitation received: ${invitation.callId}")
                            _incomingCallInvitation.value = invitation
                        }
                    } else {
                        // No invitation data
                        _incomingCallInvitation.value = null
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error listening for call invitations", error.toException())
                }
            }

            invitationsRef.addValueEventListener(invitationListener!!)

            Log.d(TAG, "Started listening for call invitations for user: $userId")
        }
    }

    fun acceptCall(invitation: CallInvitation) {
        scope.launch {
            try {
                // Remove the invitation from Firebase
                removeInvitation(invitation.callId)

                // Update call session status
                updateCallStatus(invitation.callId, "accepted")

                Log.d(TAG, "Call accepted: ${invitation.callId}")

            } catch (e: Exception) {
                Log.e(TAG, "Error accepting call", e)
            }
        }
    }

    fun rejectCall(invitation: CallInvitation) {
        scope.launch {
            try {
                // Remove the invitation from Firebase
                removeInvitation(invitation.callId)

                // Update call session status
                updateCallStatus(invitation.callId, "rejected")

                // Clear the current invitation
                _incomingCallInvitation.value = null

                Log.d(TAG, "Call rejected: ${invitation.callId}")

            } catch (e: Exception) {
                Log.e(TAG, "Error rejecting call", e)
            }
        }
    }

    private fun removeInvitation(callId: String) {
        currentUserId?.let { userId ->
            database.getReference("call_invitations").child(userId).removeValue()
        }
    }

    private fun updateCallStatus(callId: String, status: String) {
        database.getReference("call_sessions").child(callId)
            .updateChildren(mapOf("status" to status))
    }

    fun clearCurrentInvitation() {
        _incomingCallInvitation.value = null
    }

    fun stopListening() {
        currentUserId?.let { userId ->
            val invitationsRef = database.getReference("call_invitations").child(userId)
            invitationListener?.let { invitationsRef.removeEventListener(it) }
        }

        invitationListener = null
        callSessionListener = null
        _incomingCallInvitation.value = null

        Log.d(TAG, "Stopped listening for call invitations")
    }

    fun sendCallInvitation(
        callId: String,
        callerId: String,
        callerName: String,
        receiverId: String,
        isVideoCall: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val invitation = CallInvitation(
            callId = callId,
            callerId = callerId,
            callerName = callerName,
            isVideoCall = isVideoCall,
            timestamp = System.currentTimeMillis()
        )

        // Send invitation to receiver
        database.getReference("call_invitations").child(receiverId)
            .setValue(invitation)
            .addOnSuccessListener {
                // Create call session
                val callSession = mapOf(
                    "callId" to callId,
                    "callerId" to callerId,
                    "calleeId" to receiverId,
                    "status" to "ringing",
                    "timestamp" to System.currentTimeMillis()
                )

                database.getReference("call_sessions").child(callId)
                    .setValue(callSession)
                    .addOnSuccessListener {
                        Log.d(TAG, "Call invitation sent successfully: $callId")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to create call session", e)
                        onFailure("Failed to create call session: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to send call invitation", e)
                onFailure("Failed to send invitation: ${e.message}")
            }
    }
}