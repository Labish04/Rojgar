package com.example.rojgar.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.example.rojgar.R
import com.example.rojgar.utils.CallInvitationManager
import com.example.rojgar.utils.ZegoCloudConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment

/**
 * Activity for managing ZEGO video/audio calls
 * Handles both incoming and outgoing calls with proper fragment integration
 */
class ZegoCallActivity : AppCompatActivity() {

    private var ringtone: Ringtone? = null
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private var callId: String? = null
    private var callerId: String? = null
    private var callerName: String? = null
    private var isVideoCall: Boolean = true
    private var isIncoming: Boolean = false

    // Permission launcher for requesting microphone and camera permissions
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d("ZegoCallActivity", "All permissions granted")
            initializeZegoCall()
        } else {
            Log.w("ZegoCallActivity", "Some permissions were denied")
            val deniedPermissions = permissions.filter { !it.value }.keys.joinToString(", ")
            Toast.makeText(
                this,
                "Required permissions denied: $deniedPermissions",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zego_call)

        // Extract data from intent
        callId = intent.getStringExtra("callId")
        callerId = intent.getStringExtra("callerId")
        callerName = intent.getStringExtra("callerName")
        isVideoCall = intent.getBooleanExtra("isVideoCall", true)
        isIncoming = intent.getBooleanExtra("isIncoming", false)

        // Validate required parameters
        if (callId == null) {
            Log.e("ZegoCallActivity", "Missing callId in intent")
            Toast.makeText(this, "Invalid call parameters", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(
            "ZegoCallActivity",
            "onCreate: callId=$callId, isIncoming=$isIncoming, isVideoCall=$isVideoCall"
        )

        // Request necessary permissions before initializing ZEGO
        requestCallPermissions()
    }

    /**
     * Request microphone and camera permissions based on call type
     */
    private fun requestCallPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        // Always need microphone
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
        }

        // Need camera for video calls
        if (isVideoCall && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        // Request permissions if any are needed
        if (permissionsNeeded.isNotEmpty()) {
            Log.d("ZegoCallActivity", "Requesting permissions: $permissionsNeeded")
            permissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            Log.d("ZegoCallActivity", "All permissions already granted")
            initializeZegoCall()
        }
    }

    /**
     * Initialize the ZEGO call by creating and adding the call fragment
     */
    private fun initializeZegoCall() {
        try {
            val currentUserId = auth.currentUser?.uid
            val currentUserName = auth.currentUser?.displayName

            if (currentUserId == null) {
                Log.e("ZegoCallActivity", "User not authenticated")
                Toast.makeText(this, "User authentication failed", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            Log.d(
                "ZegoCallActivity",
                "Initializing ZEGO call: currentUserId=$currentUserId, currentUserName=$currentUserName"
            )

            // Create ZEGO call configuration based on call type
            val config = if (isVideoCall) {
                ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
            } else {
                ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()
            }

            // Configure the call settings
            config.turnOnCameraWhenJoining = isVideoCall
            config.turnOnMicrophoneWhenJoining = true
            config.useSpeakerWhenJoining = true
            config.audioVideoViewType = 0 // Picture-in-picture mode

            Log.d("ZegoCallActivity", "ZEGO config created: video=$isVideoCall")

            // Create the ZEGO call fragment
            val callFragment = ZegoUIKitPrebuiltCallFragment.newInstance(
                ZegoCloudConstants.APP_ID,
                ZegoCloudConstants.APP_SIGN,
                currentUserId,
                currentUserName ?: "User",
                callId ?: "",
                config
            )

            // Add the fragment to the container
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, callFragment)
                .commitNow()

            Log.d("ZegoCallActivity", "ZEGO call fragment added successfully")

            // Update call session in Firebase
            updateCallStatus("active")

            // Play ringtone for incoming calls
            if (!isIncoming) {
                playRingtone()
            }

        } catch (e: Exception) {
            Log.e("ZegoCallActivity", "Error initializing ZEGO call", e)
            Toast.makeText(this, "Failed to initialize call: ${e.message}", Toast.LENGTH_SHORT)
                .show()
            finish()
        }
    }

    /**
     * Play ringtone for outgoing calls
     */
    private fun playRingtone() {
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
            ringtone?.play()
            Log.d("ZegoCallActivity", "Ringtone started")
        } catch (e: Exception) {
            Log.e("ZegoCallActivity", "Error playing ringtone", e)
        }
    }

    /**
     * Stop the ringtone
     */
    private fun stopRingtone() {
        try {
            ringtone?.stop()
            ringtone = null
            Log.d("ZegoCallActivity", "Ringtone stopped")
        } catch (e: Exception) {
            Log.e("ZegoCallActivity", "Error stopping ringtone", e)
        }
    }

    /**
     * Update the call session status in Firebase
     */
    private fun updateCallStatus(status: String) {
        callId?.let { id ->
            try {
                database.getReference("call_sessions").child(id)
                    .updateChildren(mapOf(
                        "status" to status,
                        "updatedAt" to System.currentTimeMillis()
                    ))
                    .addOnSuccessListener {
                        Log.d("ZegoCallActivity", "Call status updated: $status")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ZegoCallActivity", "Failed to update call status", e)
                    }
            } catch (e: Exception) {
                Log.e("ZegoCallActivity", "Error updating call status", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ZegoCallActivity", "onDestroy called")

        stopRingtone()

        // Update call session status to ended
        if (callId != null) {
            updateCallStatus("ended")
        }

        // Clear the call invitation from UI
        CallInvitationManager.clearCurrentInvitation()
    }

    override fun onPause() {
        super.onPause()
        Log.d("ZegoCallActivity", "onPause called")
    }

    override fun onResume() {
        super.onResume()
        Log.d("ZegoCallActivity", "onResume called")
    }
}

/**
 * Composable for displaying an incoming call overlay
 * Can be used in any activity that wants to show incoming calls
 */
@Composable
fun IncomingCallOverlay(
    modifier: Modifier = Modifier
) {
    val incomingCallInvitation = CallInvitationManager.incomingCallInvitation.collectAsState().value
    val context = LocalContext.current

    incomingCallInvitation?.let { invitation ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .zIndex(999f),
            contentAlignment = Alignment.Center
        ) {
            // Semi-transparent background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // Call overlay card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 16.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Call type icon
                    Icon(
                        painter = painterResource(
                            if (invitation.isVideoCall) R.drawable.videocall else R.drawable.call
                        ),
                        contentDescription = if (invitation.isVideoCall) "Video Call" else "Audio Call",
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF1976D2)
                    )

                    // Caller name
                    Text(
                        text = invitation.callerName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // Call type text
                    Text(
                        text = if (invitation.isVideoCall) "Video Call" else "Audio Call",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    // Accept/Reject buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reject button
                        Button(
                            onClick = {
                                CallInvitationManager.rejectCall(invitation)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.call),
                                contentDescription = "Reject",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reject",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Accept button
                        Button(
                            onClick = {
                                // Start ZegoCallActivity for incoming call
                                val intent = Intent(context, ZegoCallActivity::class.java).apply {
                                    putExtra("isIncoming", true)
                                    putExtra("callId", invitation.callId)
                                    putExtra("callerId", invitation.callerId)
                                    putExtra("callerName", invitation.callerName)
                                    putExtra("isVideoCall", invitation.isVideoCall)
                                }
                                context.startActivity(intent)
                                CallInvitationManager.acceptCall(invitation)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Green.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (invitation.isVideoCall) R.drawable.videocall else R.drawable.call
                                ),
                                contentDescription = "Accept",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Accept",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}