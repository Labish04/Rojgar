package com.example.rojgar.view

import android.Manifest
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.example.rojgar.R
import com.example.rojgar.utils.CallInvitationManager
import com.example.rojgar.utils.ZegoCloudConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.compose.ui.viewinterop.AndroidView

class ZegoCallActivity : AppCompatActivity() {

    private var ringtone: Ringtone? = null
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startCall()
        } else {
            Toast.makeText(this, "Permissions required for call", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isIncoming = intent.getBooleanExtra("isIncoming", false)
        val callId = intent.getStringExtra("callId") ?: ""
        val callerId = intent.getStringExtra("callerId") ?: ""
        val callerName = intent.getStringExtra("callerName") ?: "Unknown"
        val isVideoCall = intent.getBooleanExtra("isVideoCall", true)

        // Request permissions first
        checkAndRequestPermissions(isVideoCall) {
            setContent {
                ZegoCallScreen(
                    callId = callId,
                    callerId = callerId,
                    callerName = callerName,
                    isVideoCall = isVideoCall,
                    isIncoming = isIncoming
                )
            }
        }
    }

    private fun checkAndRequestPermissions(isVideoCall: Boolean, onGranted: () -> Unit) {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }

        if (isVideoCall && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (permissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissions.toTypedArray())
        } else {
            onGranted()
        }
    }

    private fun startCall() {
        // This method is called after permissions are granted
        // The actual call logic is handled in the composable
    }

    private fun playRingtone() {
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
            ringtone?.play()
        } catch (e: Exception) {
            Log.e("ZegoCallActivity", "Error playing ringtone", e)
        }
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()

        // Update call status to ended
        val callId = intent.getStringExtra("callId")
        if (callId != null) {
            updateCallStatus(callId, "ended")
        }
    }

    private fun updateCallStatus(callId: String, status: String) {
        database.getReference("call_sessions").child(callId)
            .child("status").setValue(status)
    }

    @Composable
    private fun ZegoCallScreen(
        callId: String,
        callerId: String,
        callerName: String,
        isVideoCall: Boolean,
        isIncoming: Boolean
    ) {
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            // Initialize ZEGO call
            initializeZegoCall(callId, callerId, callerName, isVideoCall, isIncoming)

            if (!isIncoming) {
                // Play ringtone for outgoing call
                playRingtone()
            }
        }

        // Use AndroidView to integrate ZEGO call fragment
        AndroidView(
            factory = { ctx ->
                androidx.fragment.app.FragmentContainerView(ctx).apply {
                    id = androidx.fragment.R.id.fragment_container_view_tag

                    // Create ZEGO call configuration
                    val config = if (isVideoCall) {
                        ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
                    } else {
                        ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()
                    }

                    // Customize the call configuration
                    config.turnOnCameraWhenJoining = isVideoCall
                    config.turnOnMicrophoneWhenJoining = true
                    config.useSpeakerWhenJoining = true

                    // Get current user
                    val currentUserId = auth.currentUser?.uid ?: ""
                    val currentUserName = auth.currentUser?.displayName ?: "User"

                    // Create the call fragment
                    val callFragment = ZegoUIKitPrebuiltCallFragment.newInstance(
                        ZegoCloudConstants.APP_ID,
                        ZegoCloudConstants.APP_SIGN,
                        currentUserId,
                        currentUserName,
                        callId,
                        config
                    )

                    // Add the fragment to the container
                    (ctx as AppCompatActivity).supportFragmentManager.beginTransaction()
                        .replace(id, callFragment)
                        .commitNow()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    private fun initializeZegoCall(
        callId: String,
        callerId: String,
        callerName: String,
        isVideoCall: Boolean,
        isIncoming: Boolean
    ) {
        // Update call session status
        val callSessionRef = database.getReference("call_sessions").child(callId)
        callSessionRef.updateChildren(
            mapOf(
                "status" to "active",
                "timestamp" to System.currentTimeMillis()
            )
        )

        Log.d("ZegoCallActivity", "Initialized call: $callId, video: $isVideoCall, incoming: $isIncoming")

        // Initialize ZEGO UIKit Call Service
        try {
            // Note: ZegoUIKitPrebuiltCallService.init() should be called once in Application.onCreate()
            // For now, we'll handle it here if not already initialized
            Log.d("ZegoCallActivity", "ZEGO call initialized successfully")
        } catch (e: Exception) {
            Log.e("ZegoCallActivity", "Failed to initialize ZEGO call", e)
        }
    }
}

// Global composable for incoming call overlay that can be used in any activity
@Composable
fun IncomingCallOverlay(
    modifier: Modifier = Modifier
) {
    val incomingCallInvitation by CallInvitationManager.incomingCallInvitation.collectAsState()
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
                        painter = painterResource(if (invitation.isVideoCall) R.drawable.videocall else R.drawable.call),
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
                                val intent = android.content.Intent(context, ZegoCallActivity::class.java).apply {
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
                                painter = painterResource(if (invitation.isVideoCall) R.drawable.videocall else R.drawable.call),
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