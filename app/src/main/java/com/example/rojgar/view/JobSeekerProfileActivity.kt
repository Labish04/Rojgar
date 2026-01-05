package com.example.rojgar.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.ui.theme.RojgarTheme
import android.net.Uri
import android.provider.MediaStore
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.rojgar.R
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.FollowRepoImpl
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.livedata.observeAsState
import coil.compose.AsyncImage
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.example.rojgar.viewmodel.FollowViewModel


class JobSeekerProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val jobSeekerId = intent.getStringExtra("JOB_SEEKER_ID") ?: ""
        val isOwnProfile = jobSeekerId.isEmpty() || jobSeekerId == getCurrentUserId()

        setContent {
            RojgarTheme {
                JobSeekerProfileBody(
                    targetJobSeekerId = jobSeekerId,
                    isOwnProfile = isOwnProfile
                )
            }
        }
    }

    private fun getCurrentUserId(): String {
        return JobSeekerRepoImpl().getCurrentJobSeeker()?.uid ?: ""
    }
}

@Composable
fun JobSeekerProfileBody(
    targetJobSeekerId: String = "",
    isOwnProfile: Boolean = true
) {
    val context = LocalContext.current
    val activity = context as Activity

    val repository = remember { JobSeekerRepoImpl() }
    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val followViewModel = remember { FollowViewModel(FollowRepoImpl()) }

    val jobSeekerState by jobSeekerViewModel.jobSeeker.observeAsState(initial = null)
    val isFollowingState by followViewModel.isFollowing.observeAsState(initial = false)
    val followersCountState by followViewModel.followersCount.observeAsState(initial = 0)
    val followingCountState by followViewModel.followingCount.observeAsState(initial = 0)

    val currentJobSeeker = remember { JobSeekerRepoImpl().getCurrentJobSeeker() }
    val currentCompany = remember { CompanyRepoImpl().getCurrentCompany() }

    val currentUserId = remember {
        if (currentJobSeeker != null) currentJobSeeker.uid
        else if (currentCompany != null) currentCompany.uid
        else ""
    }

    val currentUserType = remember {
        when {
            currentJobSeeker != null -> "JobSeeker"
            currentCompany != null -> "Company"
            else -> ""
        }
    }

    val displayJobSeekerId = if (targetJobSeekerId.isNotEmpty()) targetJobSeekerId else currentUserId

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoThumbnail by remember { mutableStateOf<Bitmap?>(null) }
    var showMoreDialog by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedVideoUri = uri
            val filePath = getRealPathFromURI(context, uri)
            if (filePath != null) {
                videoThumbnail = ThumbnailUtils.createVideoThumbnail(
                    filePath,
                    MediaStore.Video.Thumbnails.MINI_KIND
                )
            }
            Toast.makeText(context, "Video Selected!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(displayJobSeekerId) {
        if (displayJobSeekerId.isNotEmpty()) {
            if (isOwnProfile) {
                jobSeekerViewModel.fetchCurrentJobSeeker()
            } else {
                jobSeekerViewModel.getJobSeekerDetails(displayJobSeekerId)
            }
        } else {
            jobSeekerViewModel.fetchCurrentJobSeeker()
        }
    }

    LaunchedEffect(jobSeekerState, currentUserId, displayJobSeekerId) {
        jobSeekerState?.let {
            if (!isOwnProfile && currentUserId.isNotEmpty() && displayJobSeekerId.isNotEmpty()) {
                followViewModel.checkFollowStatus(currentUserId, displayJobSeekerId)
                followViewModel.getFollowersCount(displayJobSeekerId)
                followViewModel.getFollowingCount(displayJobSeekerId)
            } else if (isOwnProfile && displayJobSeekerId.isNotEmpty()) {
                followViewModel.getFollowersCount(displayJobSeekerId)
                followViewModel.getFollowingCount(displayJobSeekerId)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFFBBDEFB),
                            Color(0xFF90CAF9)
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = "Back",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier
                                .clickable { activity.finish() }
                                .padding(14.dp)
                        )
                    }

                    Text(
                        text = "Profile",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    )

                    if (isOwnProfile) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_more_vert_24),
                                contentDescription = "Menu",
                                tint = Color(0xFF1976D2),
                                modifier = Modifier
                                    .clickable { isDrawerOpen = true }
                                    .padding(14.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(140.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF42A5F5).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(130.dp)
                        .border(4.dp, Color.White, CircleShape),
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    if (jobSeekerState?.profilePhoto != null) {
                        AsyncImage(
                            model = jobSeekerState?.profilePhoto,
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.profileemptypic),
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = jobSeekerState?.fullName ?: "User",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0D47A1),
                        letterSpacing = 0.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.8f),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = jobSeekerState?.profession ?: "No Profession Added",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1976D2)
                        ),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    count = followersCountState,
                    label = "Followers",
                    onClick = {
                        Toast.makeText(context, "Followers: $followersCountState", Toast.LENGTH_SHORT).show()
                    }
                )

                StatCard(
                    count = followingCountState,
                    label = "Following",
                    onClick = {
                        Toast.makeText(context, "Following: $followingCountState", Toast.LENGTH_SHORT).show()
                    }
                )

                StatCard(
                    count = 156,
                    label = "Posts",
                    onClick = {
                        Toast.makeText(context, "Posts: 156", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column {
                    if (!isOwnProfile) {
                        if (!isFollowingState) {
                            Button(
                                onClick = {
                                    if (currentUserId.isNotEmpty() && currentUserType.isNotEmpty()) {
                                        followViewModel.follow(
                                            followerId = currentUserId,
                                            followerType = currentUserType,
                                            followingId = displayJobSeekerId,
                                            followingType = "JobSeeker",
                                            onComplete = { success, message ->
                                                if (success) {
                                                    Toast.makeText(context, "Followed!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Failed: $message", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    } else {
                                        Toast.makeText(context, "Please login to follow", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3),
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.follow_icon),
                                        contentDescription = "Follow",
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Follow",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Message clicked", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3),
                                        contentColor = Color.White
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.round_info_outline_24),
                                            contentDescription = "Message",
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Message",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        showMoreDialog = !showMoreDialog
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF1976D2)
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.following_icon),
                                            contentDescription = "Following",
                                            modifier = Modifier.size(24.dp),
                                            tint = Color(0xFF1976D2)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Following",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            AnimatedVisibility(
                                visible = showMoreDialog,
                                enter = fadeIn(animationSpec = tween(200)) +
                                        slideInVertically(
                                            initialOffsetY = { -20 },
                                            animationSpec = tween(300)
                                        ),
                                exit = fadeOut(animationSpec = tween(200)) +
                                        slideOutVertically(
                                            targetOffsetY = { -20 },
                                            animationSpec = tween(300)
                                        )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        ),
                                        elevation = CardDefaults.cardElevation(16.dp),
                                        modifier = Modifier.width(180.dp)
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = "Unfollow",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.Red,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        showMoreDialog = false
                                                        if (currentUserId.isNotEmpty() && currentUserType.isNotEmpty()) {
                                                            followViewModel.unfollow(
                                                                followerId = currentUserId,
                                                                followerType = currentUserType,
                                                                followingId = displayJobSeekerId,
                                                                followingType = "JobSeeker",
                                                                onComplete = { success, message ->
                                                                    if (success) {
                                                                        Toast.makeText(context, "Unfollowed!", Toast.LENGTH_SHORT).show()
                                                                    } else {
                                                                        Toast.makeText(context, "Failed: $message", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                }
                                                            )
                                                        }
                                                    }
                                                    .padding(18.dp)
                                            )

                                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                                            Text(
                                                text = "Block",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF263238),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        Toast.makeText(context, "Block clicked", Toast.LENGTH_SHORT).show()
                                                        showMoreDialog = false
                                                    }
                                                    .padding(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(Color.White)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "About",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = jobSeekerState?.bio ?: "No Bio Added",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = Color(0xFF546E7A),
                            lineHeight = 24.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isOwnProfile) {
                        Text(
                            text = "Introduction Video",
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .shadow(12.dp, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (isPlaying && selectedVideoUri != null) {
                                    VideoPlayer(uri = selectedVideoUri!!, modifier = Modifier.fillMaxSize())
                                } else {
                                    if (videoThumbnail != null) {
                                        Image(
                                            bitmap = videoThumbnail!!.asImageBitmap(),
                                            contentDescription = "Video Thumbnail",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                        colors = listOf(
                                                            Color(0xFF42A5F5),
                                                            Color(0xFF2196F3)
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.baseline_upload_24),
                                                    contentDescription = "Upload",
                                                    tint = Color.White.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    "No video selected",
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp)
                                        .size(56.dp),
                                    shape = CircleShape,
                                    color = Color(0xFF2196F3),
                                    shadowElevation = 12.dp
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_upload_24),
                                        contentDescription = "Upload",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .clickable { videoPickerLauncher.launch("video/*") }
                                            .padding(16.dp)
                                    )
                                }

                                if (!isPlaying) {
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(72.dp),
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.95f),
                                        shadowElevation = 16.dp
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_play_arrow_24),
                                            contentDescription = "Play",
                                            tint = Color(0xFF1976D2),
                                            modifier = Modifier
                                                .clickable {
                                                    if (selectedVideoUri != null) {
                                                        isPlaying = true
                                                    } else {
                                                        Toast.makeText(context, "Select a video first!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .padding(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (isOwnProfile) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(context, JobSeekerProfileDetailsActivity::class.java)
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE3F2FD),
                                    contentColor = Color(0xFF1976D2)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.round_info_outline_24),
                                        contentDescription = "Details",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Details",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(context, CvViewActivity::class.java)
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .shadow(12.dp, RoundedCornerShape(16.dp)),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Text(
                                    "View CV",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        if (isOwnProfile) {
            AnimatedVisibility(
                visible = isDrawerOpen,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeIn(),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                ) + fadeOut(),
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { isDrawerOpen = false }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(340.dp)
                            .align(Alignment.CenterEnd)
                            .shadow(24.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFAFAFA),
                                        Color.White
                                    )
                                )
                            )
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF1E88E5),
                                                Color(0xFF42A5F5)
                                            )
                                        )
                                    )
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(16.dp)
                                        .size(36.dp),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                                        contentDescription = "Close",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .graphicsLayer(rotationZ = 180f)
                                            .clickable { isDrawerOpen = false }
                                            .padding(8.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.CenterStart)
                                        .padding(start = 20.dp, end = 20.dp, bottom = 16.dp, top = 56.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .border(3.dp, Color.White, CircleShape),
                                        shape = CircleShape,
                                        elevation = CardDefaults.cardElevation(8.dp)
                                    ) {
                                        if (jobSeekerState?.profilePhoto != null) {
                                            AsyncImage(
                                                model = jobSeekerState?.profilePhoto,
                                                contentDescription = "Profile",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.profileemptypic),
                                                contentDescription = "Profile",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = jobSeekerState?.fullName ?: "User",
                                            style = TextStyle(
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            ),
                                            maxLines = 1
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = jobSeekerState?.profession ?: "No Profession",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontWeight = FontWeight.Medium
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                ModernDrawerMenuItem(
                                    icon = R.drawable.save_outline,
                                    text = "Saved Jobs",
                                    subtitle = "View bookmarked opportunities",
                                    iconColor = Color(0xFF4CAF50),
                                    onClick = {
                                        isDrawerOpen = false
                                        Toast.makeText(context, "Saved Jobs clicked", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                ModernDrawerMenuItem(
                                    icon = R.drawable.appliedjob,
                                    text = "Applied Jobs",
                                    subtitle = "Track your applications",
                                    iconColor = Color(0xFF2196F3),
                                    onClick = {
                                        isDrawerOpen = false
                                        Toast.makeText(context, "Applied Jobs clicked", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                ModernDrawerMenuItem(
                                    icon = R.drawable.feedback,
                                    text = "Feedback",
                                    subtitle = "Help you to improve",
                                    iconColor = Color(0xFF2196F3),
                                    onClick = {
                                        isDrawerOpen = false
                                        Toast.makeText(context, "Feedback clicked", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                ModernDrawerMenuItem(
                                    icon = R.drawable.settings,
                                    text = "Settings",
                                    subtitle = "Manage preferences",
                                    iconColor = Color(0xFF9C27B0),
                                    onClick = {
                                        showSettingsDialog = true
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    color = Color.Gray.copy(alpha = 0.2f)
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 24.dp)
                                    .clickable {
                                        isDrawerOpen = false
                                        repository.logout(currentUserId) { success, message ->
                                            if (success) {
                                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                                val intent = Intent(context, LoginActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                context.startActivity(intent)
                                                activity.finish()
                                            } else {
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                ),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_logout_24),
                                        contentDescription = "Logout",
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Logout",
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD32F2F)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showSettingsDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(20f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .clickable { showSettingsDialog = false }
                    )

                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.85f)
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Settings",
                                    style = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0)
                                    )
                                )

                                Surface(
                                    modifier = Modifier.size(32.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFF5F5F5)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                                        contentDescription = "Close",
                                        tint = Color(0xFF546E7A),
                                        modifier = Modifier
                                            .clickable { showSettingsDialog = false }
                                            .padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            SettingsOption(
                                icon = R.drawable.outline_lock_24,
                                title = "Change Password",
                                subtitle = "Update your password",
                                iconColor = Color(0xFF4CAF50),
                                onClick = {
                                    showSettingsDialog = false
                                    Toast.makeText(context, "Change Password", Toast.LENGTH_SHORT).show()
                                }
                            )

                            var isDarkMode by remember { mutableStateOf(false) }
                            SettingsOptionWithSwitch(
                                icon = R.drawable.darkmode,
                                title = "Dark Mode",
                                subtitle = if (isDarkMode) "Dark theme enabled" else "Light theme enabled",
                                iconColor = Color(0xFF9C27B0),
                                isChecked = isDarkMode,
                                onCheckedChange = { isDarkMode = it }
                            )

                            var isPrivateProfile by remember { mutableStateOf(false) }
                            SettingsOptionWithSwitch(
                                icon = R.drawable.privateprofile,
                                title = "Private Profile",
                                subtitle = if (isPrivateProfile) "Profile is private" else "Profile is public",
                                iconColor = Color(0xFF2196F3),
                                isChecked = isPrivateProfile,
                                onCheckedChange = { isPrivateProfile = it }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                            Spacer(modifier = Modifier.height(8.dp))

                            SettingsOption(
                                icon = R.drawable.deactivateaccount,
                                title = "Deactivate Account",
                                subtitle = "Temporarily disable your account",
                                iconColor = Color(0xFFFF9800),
                                onClick = {
                                    showSettingsDialog = false
                                    Toast.makeText(context, "Deactivate Account", Toast.LENGTH_SHORT).show()
                                }
                            )

                            SettingsOption(
                                icon = R.drawable.deleteaccount,
                                title = "Delete Account",
                                subtitle = "Permanently remove your account",
                                iconColor = Color(0xFFF44336),
                                onClick = {
                                    showSettingsDialog = false
                                    Toast.makeText(context, "Delete Account", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(count: Int, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatCount(count),
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1976D2).copy(alpha = 0.8f)
                )
            )
        }
    }
}

fun formatCount(count: Int): String {
    return when {
        count >= 1000 -> "${count / 1000}.${(count % 1000) / 100}K"
        else -> count.toString()
    }
}

@Composable
fun ModernDrawerMenuItem(
    icon: Int,
    text: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = text,
                    tint = iconColor,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xFF78909C)
                    )
                )
            }

            Icon(
                painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                contentDescription = "Navigate",
                tint = Color(0xFFBDBDBD),
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer(rotationZ = 180f)
            )
        }
    }
}

@Preview
@Composable
fun PreviewJobSeekerProfile() {
    RojgarTheme {
        JobSeekerProfileBody()
    }
}

@Suppress("DEPRECATION")
fun getRealPathFromURI(context: Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.Video.Media.DATA)
    val cursor = context.contentResolver.query(uri, projection, null, null, null)
    cursor?.use {
        val columnIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        if (it.moveToFirst()) {
            return it.getString(columnIndex)
        }
    }
    return null
}

@Composable
fun VideoPlayer(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
    }

    AndroidView(
        factory = { PlayerView(context).apply { player = exoPlayer } },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@Composable
fun SettingsOption(
    icon: Int,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFAFAFA)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xFF78909C)
                    )
                )
            }
        }
    }
}

@Composable
fun SettingsOptionWithSwitch(
    icon: Int,
    title: String,
    subtitle: String,
    iconColor: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFAFAFA)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xFF78909C)
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = iconColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFBDBDBD)
                )
            )
        }
    }
}