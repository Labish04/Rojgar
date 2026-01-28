package com.example.rojgar.view

import android.app.Activity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rojgar.R
import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.repository.FollowRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.RojgarTheme
import com.google.firebase.database.FirebaseDatabase

class BlockedUsersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RojgarTheme {
                BlockedUsersScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen() {
    val context = LocalContext.current
    val activity = context as Activity

    // Initialize repositories
    val jobSeekerRepo = remember { JobSeekerRepoImpl() }
    val followRepo = remember { FollowRepoImpl(context) }

    // Get current user ID
    val currentUserId = jobSeekerRepo.getCurrentJobSeeker()?.uid ?: ""

    // State variables
    var blockedUsersList by remember { mutableStateOf<List<JobSeekerModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showUnblockDialog by remember { mutableStateOf(false) }
    var selectedUserToUnblock by remember { mutableStateOf<JobSeekerModel?>(null) }

    // Load blocked users
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            followRepo.getBlockedUsers(currentUserId) { success, message, blockedIds ->
                if (success && blockedIds != null) {
                    // Fetch details for each blocked user
                    val jobSeekersRef = FirebaseDatabase.getInstance()
                        .getReference("JobSeekers")

                    val fetchedUsers = mutableListOf<JobSeekerModel>()
                    var fetchCount = 0

                    if (blockedIds.isEmpty()) {
                        isLoading = false
                        blockedUsersList = emptyList()
                        return@getBlockedUsers
                    }

                    blockedIds.forEach { blockedId ->
                        jobSeekersRef.child(blockedId).get()
                            .addOnSuccessListener { snapshot ->
                                val jobSeeker = snapshot.getValue(JobSeekerModel::class.java)
                                if (jobSeeker != null) {
                                    // Ensure jobSeekerId is set
                                    val jobSeekerWithId = jobSeeker.copy(jobSeekerId = snapshot.key ?: blockedId)
                                    fetchedUsers.add(jobSeekerWithId)
                                }
                                fetchCount++

                                if (fetchCount == blockedIds.size) {
                                    blockedUsersList = fetchedUsers
                                    isLoading = false
                                }
                            }
                            .addOnFailureListener {
                                fetchCount++
                                if (fetchCount == blockedIds.size) {
                                    blockedUsersList = fetchedUsers
                                    isLoading = false
                                }
                            }
                    }
                } else {
                    isLoading = false
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Blocked Users",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFFBBDEFB),
                            Color(0xFF90CAF9)
                        )
                    )
                )
        ) {
            when {
                isLoading -> {
                    // Loading State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF2196F3),
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading blocked users...",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color(0xFF1565C0),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                blockedUsersList.isEmpty() -> {
                    // Empty State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.block),
                                contentDescription = "No blocked users",
                                tint = Color(0xFF90A4AE),
                                modifier = Modifier.size(100.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "No Blocked Users",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You haven't blocked anyone yet",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color(0xFF546E7A),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }

                else -> {
                    // List of blocked users
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(blockedUsersList) { blockedUser ->
                            BlockedUserCard(
                                jobSeeker = blockedUser,
                                onUnblockClick = {
                                    selectedUserToUnblock = blockedUser
                                    showUnblockDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Unblock Confirmation Dialog
        if (showUnblockDialog && selectedUserToUnblock != null) {
            UnblockConfirmationDialog(
                userName = selectedUserToUnblock!!.fullName,
                onDismiss = { showUnblockDialog = false },
                onConfirm = {
                    followRepo.unblockUser(
                        blockerId = currentUserId,
                        blockedId = selectedUserToUnblock!!.jobSeekerId
                    ) { success, message ->
                        if (success) {
                            // Remove from local list immediately
                            blockedUsersList = blockedUsersList.filter {
                                it.jobSeekerId != selectedUserToUnblock!!.jobSeekerId
                            }
                            Toast.makeText(
                                context,
                                "${selectedUserToUnblock!!.fullName} unblocked successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                        showUnblockDialog = false
                        selectedUserToUnblock = null
                    }
                }
            )
        }
    }
}

@Composable
fun BlockedUserCard(
    jobSeeker: JobSeekerModel,
    onUnblockClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Photo
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2196F3),
                                Color(0xFF1976D2)
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(3.dp, Color.White, CircleShape)
                    .shadow(6.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (jobSeeker.profilePhoto.isNotEmpty()) {
                    AsyncImage(
                        model = jobSeeker.profilePhoto,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = jobSeeker.fullName.firstOrNull()?.uppercase() ?: "?",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = jobSeeker.fullName,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = jobSeeker.profession,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                )
            }

            // Unblock Button
            Button(
                onClick = onUnblockClick,
                modifier = Modifier
                    .height(42.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.block),
                    contentDescription = "Unblock",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Unblock",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun UnblockConfirmationDialog(
    userName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Animation states
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false) { }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = Color(0xFFE8F5E9),
                                shape = CircleShape
                            )
                            .border(
                                width = 3.dp,
                                color = Color(0xFF4CAF50),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.block),
                            contentDescription = "Unblock",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title
                    Text(
                        text = "Unblock $userName?",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    Text(
                        text = "You will be able to see and interact with each other again.",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFF616161),
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                Color(0xFF90CAF9)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1565C0)
                            )
                        ) {
                            Text(
                                "Cancel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        // Unblock Button
                        Button(
                            onClick = {
                                onConfirm()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .shadow(6.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                "Unblock",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}