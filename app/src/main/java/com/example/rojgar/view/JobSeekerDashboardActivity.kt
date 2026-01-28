package com.example.rojgar.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.rojgar.R
import com.example.rojgar.model.PreferenceModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.repository.SavedJobRepoImpl
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.RojgarTheme
import com.example.rojgar.utils.CallInvitationManager
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.example.rojgar.viewmodel.SavedJobViewModel
import com.google.firebase.auth.FirebaseAuth

class JobSeekerDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start listening for call invitations
        CallInvitationManager.startListening()

        setContent {
            RojgarTheme {
                JobSeekerDashboardBody()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop listening for call invitations when activity is destroyed
        CallInvitationManager.stopListening()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerDashboardBody() {

    val context = LocalContext.current
    val activity = context as Activity

    val companyViewModel: CompanyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val savedViewModel = remember { SavedJobViewModel(SavedJobRepoImpl()) }

    val jobSeeker = jobSeekerViewModel.jobSeeker.observeAsState(initial = null)

    // Observe incoming call invitations
    val incomingCallInvitation by CallInvitationManager.incomingCallInvitation.collectAsState()

    data class NavItem(
        val label: String,
        val selectedIcon: Int,
        val unselectedIcon: Int
    )

    var selectedIndex by remember { mutableStateOf(0) }

    val listItem = listOf(
        NavItem(
            label = "Home",
            selectedIcon = R.drawable.home_filled,
            unselectedIcon = R.drawable.home
        ),
        NavItem(
            label = "Post",
            selectedIcon = R.drawable.jobpost_filled,
            unselectedIcon = R.drawable.jobpost
        ),
        NavItem(
            label = "Map",
            selectedIcon = R.drawable.map_filled,
            unselectedIcon = R.drawable.map
        ),
        NavItem(
            label = "Profile",
            selectedIcon = R.drawable.profile_filled,
            unselectedIcon = R.drawable.profile
        )
    )

    LaunchedEffect(Unit) {

        // Get current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val jobSeekerId = currentUser.uid

            // Fetch all profile data
            jobSeekerViewModel.fetchCurrentJobSeeker()
        }
    }


    Scaffold(
        bottomBar = {
            Surface (
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .fillMaxWidth(),
                color = Color.White,
                shadowElevation = 10.dp
            ) {
                NavigationBar(
                    containerColor = Color.Transparent
                ) {
                    listItem.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        if (selectedIndex == index) item.selectedIcon else item.unselectedIcon
                                    ),
                                    contentDescription = item.label,
                                    modifier = Modifier.size(25.dp)
                                )
                            },
                            label = { Text(item.label) },
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index }
                        )
                    }
                }
            }
        }

    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding()
            ) {
                when (selectedIndex) {
                    0 -> JobSeekerHomeScreenBody()
                    1 -> JobSeekerViewPostBody(savedViewModel)
                    2 -> MapScreen(viewModel = companyViewModel, context = context)
                    3 -> JobSeekerProfileBody()
                }
            }

            // Incoming call overlay
            incomingCallInvitation?.let { invitation ->
                IncomingCallOverlay(
                    invitation = invitation,
                    onAccept = {
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
                    onReject = {
                        CallInvitationManager.rejectCall(invitation)
                    }
                )
            }
        }
    }
}

@Composable
private fun IncomingCallOverlay(
    invitation: com.example.rojgar.utils.CallInvitation,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Box(
        modifier = Modifier
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
                    tint = Blue
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
                        onClick = onReject,
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
                        onClick = onAccept,
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

@Preview(showSystemUi = true)
@Composable
fun JobSeekerDashboardBodyPreview() {
    RojgarTheme {
        JobSeekerDashboardBody()
    }
}
