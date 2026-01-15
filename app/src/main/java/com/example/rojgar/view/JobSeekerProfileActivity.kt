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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.rojgar.R
import com.example.rojgar.repository.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.zIndex
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import coil.compose.AsyncImage
import com.example.rojgar.viewmodel.JobSeekerViewModel

class JobSeekerProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RojgarTheme {
                JobSeekerProfileBody()
            }
        }
    }
}

@Composable
fun JobSeekerProfileBody(targetJobSeekerId: String = "") {
    val context = LocalContext.current
    val activity = context as Activity

    // Initialize repositories
    val repository = remember { JobSeekerRepoImpl() }
    val followRepository = remember { FollowRepoImpl() }

    // Initialize JobSeeker ViewModel
    val jobSeekerViewModel = remember { JobSeekerViewModel(repository) }

    val jobSeekerState by jobSeekerViewModel.jobSeeker.observeAsState(initial = null)
    val currentUserId = repository.getCurrentJobSeeker()?.uid ?: ""
    val currentUserType = "JobSeeker"

    val intentJobSeekerId = remember {
        (activity as? JobSeekerProfileActivity)?.intent?.getStringExtra("JOB_SEEKER_ID") ?: ""
    }
    val finalTargetJobSeekerId = targetJobSeekerId.ifEmpty { intentJobSeekerId }

    // Check if viewing own profile or other's profile
    val isOwnProfile = finalTargetJobSeekerId.isEmpty() || currentUserId == finalTargetJobSeekerId

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoThumbnail by remember { mutableStateOf<Bitmap?>(null) }
    var showMoreDialog by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isDrawerOpen by remember { mutableStateOf(false) }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showConfirmPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    // Follow states
    var isFollowing by remember { mutableStateOf(false) }
    var followersCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }
    var isLoadingFollow by remember { mutableStateOf(false) }

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

    fun shareProfile(context: Context, jobSeekerId: String, fullName: String, profession: String) {
        val shareText = """
        Check out ${fullName}'s profile on Rojgar!
        
        Profession: $profession
        
        View full profile: https://rojgar.app/profile/$jobSeekerId
        
        Download Rojgar App to connect with talented professionals!
    """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "${fullName}'s Professional Profile")
            type = "text/plain"
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share Profile via")
        context.startActivity(chooserIntent)
    }

    LaunchedEffect(isFollowing) {
        val profileIdToLoad = if (finalTargetJobSeekerId.isNotEmpty()) {
            finalTargetJobSeekerId
        } else {
            currentUserId
        }

        if (profileIdToLoad.isNotEmpty()) {
            // Refresh counts when follow status changes
            followRepository.getFollowersCount(profileIdToLoad) { count ->
                followersCount = count
            }

            followRepository.getFollowingCount(profileIdToLoad) { count ->
                followingCount = count
            }
        }
    }

    // Load job seeker data
    LaunchedEffect(finalTargetJobSeekerId) {
        if (finalTargetJobSeekerId.isNotEmpty()) {
            jobSeekerViewModel.fetchJobSeekerById(finalTargetJobSeekerId)
        } else {
            jobSeekerViewModel.fetchCurrentJobSeeker()
        }
    }

    // Load follow data
    LaunchedEffect(finalTargetJobSeekerId, currentUserId) {
        // Determine which profile ID to use for loading counts
        val profileIdToLoad = if (finalTargetJobSeekerId.isNotEmpty()) {
            finalTargetJobSeekerId
        } else {
            currentUserId
        }

        if (profileIdToLoad.isNotEmpty()) {
            // Check follow status if not own profile
            if (!isOwnProfile && currentUserId.isNotEmpty()) {
                isLoadingFollow = true
                followRepository.isFollowing(currentUserId, profileIdToLoad) { following ->
                    isFollowing = following
                    isLoadingFollow = false
                }
            }

            // Get followers and following counts
            followRepository.getFollowersCount(profileIdToLoad) { count ->
                followersCount = count
            }

            followRepository.getFollowingCount(profileIdToLoad) { count ->
                followingCount = count
            }
        }
    }

    // Handle follow action
    fun handleFollow() {
        if (currentUserId.isEmpty() || finalTargetJobSeekerId.isEmpty()) {
            Toast.makeText(context, "Unable to follow", Toast.LENGTH_SHORT).show()
            return
        }

        isLoadingFollow = true
        followRepository.follow(
            followerId = currentUserId,
            followerType = currentUserType,
            followingId = finalTargetJobSeekerId,
            followingType = "JobSeeker"
        ) { success, message ->
            isLoadingFollow = false
            if (success) {
                isFollowing = true
                followersCount++
                Toast.makeText(context, "Followed successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to follow: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle unfollow action
    fun handleUnfollow() {
        if (currentUserId.isEmpty() || finalTargetJobSeekerId.isEmpty()) {
            Toast.makeText(context, "Unable to unfollow", Toast.LENGTH_SHORT).show()
            return
        }

        isLoadingFollow = true
        followRepository.unfollow(
            followerId = currentUserId,
            followerType = currentUserType,
            followingId = finalTargetJobSeekerId,
            followingType = "JobSeeker"
        ) { success, message ->
            isLoadingFollow = false
            if (success) {
                isFollowing = false
                followersCount = maxOf(0, followersCount - 1)
                Toast.makeText(context, "Unfollowed successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to unfollow: $message", Toast.LENGTH_SHORT).show()
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                shareProfile(
                                    context = context,
                                    jobSeekerId = finalTargetJobSeekerId.ifEmpty { currentUserId },
                                    fullName = jobSeekerState?.fullName ?: "User",
                                    profession = jobSeekerState?.profession ?: "Professional"
                                )
                            },
                            modifier = Modifier
                                .shadow(4.dp, CircleShape)
                                .background(Color.White.copy(alpha = 0.95f), CircleShape)
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color(0xFF1976D2)
                            )
                        }

                        // Show Menu button only if viewing own profile
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
                                        .padding(16.dp)
                                )
                            }
                        } else {
                            // Dummy space to keep layout consistent
                            Spacer(modifier = Modifier.size(48.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PROFILE IMAGE
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

            // NAME & TITLE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = jobSeekerState?.fullName?:"User",
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
                        text = jobSeekerState?.profession?:"No Profession Added",
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

            // STAT CARDS - Using real follow data
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    count = followersCount,
                    label = "Followers",
                    onClick = {
                        val profileIdToLoad = if (finalTargetJobSeekerId.isNotEmpty()) {
                            finalTargetJobSeekerId
                        } else {
                            currentUserId
                        }

                        if (profileIdToLoad.isNotEmpty()) {
                            val intent = Intent(context, FollowersListActivity::class.java)
                            intent.putExtra("USER_ID", profileIdToLoad)
                            intent.putExtra("USER_TYPE", "JobSeeker")
                            intent.putExtra("IS_OWN_PROFILE", isOwnProfile)
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Unable to load followers", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                StatCard(
                    count = followingCount,
                    label = "Following",
                    onClick = {
                        val profileIdToLoad = if (finalTargetJobSeekerId.isNotEmpty()) {
                            finalTargetJobSeekerId
                        } else {
                            currentUserId
                        }

                        if (profileIdToLoad.isNotEmpty()) {
                            val intent = Intent(context, FollowingListActivity::class.java)
                            intent.putExtra("USER_ID", profileIdToLoad)
                            intent.putExtra("USER_TYPE", "JobSeeker")
                            intent.putExtra("IS_OWN_PROFILE", isOwnProfile)
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Unable to load followers", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                StatCard(
                    count = 156, // Posts count (can be updated later with real data)
                    label = "Posts",
                    onClick = { Toast.makeText(context, "Posts: 156", Toast.LENGTH_SHORT).show() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ACTION BUTTONS - Show follow options only when viewing others' profiles
            if (!isOwnProfile) {
                if (!isFollowing) {
                    // Single Follow Button (for other users you're not following)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!isLoadingFollow) {
                                    handleFollow()
                                }
                            },
                            enabled = !isLoadingFollow,
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
                            if (isLoadingFollow) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
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
                        }
                    }
                } else {
                    // Split into Message and Following buttons (for other users you're following)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
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
                                containerColor = if (isFollowing) Color(0xFF10B981) else Color(0xFF6366F1)
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
                                    tint = Color(0xFFFFFFFF)
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
                }
            }

            // MORE OPTIONS DIALOG - Only show when viewing others' profile and following (to unfollow/block)
            if (showMoreDialog && !isOwnProfile) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(16.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Column(modifier = Modifier.width(180.dp)) {
                            Text(
                                text = "Unfollow",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Red,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        handleUnfollow()
                                        showMoreDialog = false
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

            Spacer(modifier = Modifier.height(20.dp))

            // CONTENT CARD
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
                    // ABOUT SECTION
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
                        text = jobSeekerState?.bio?:"No Bio Added",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = Color(0xFF546E7A),
                            lineHeight = 24.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // INTRODUCTION VIDEO
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

                            // Only show video picker/upload option for own profile
                            if (isOwnProfile) {
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
                            }

                            // Play button
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // View Details button
                        Button(
                            onClick = {
                                val intent = Intent(context, JobSeekerProfileDetailsActivity::class.java).apply {
                                    putExtra("JOB_SEEKER_ID", finalTargetJobSeekerId)
                                }
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
                                    contentDescription = "View Details",
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

                        // View CV button
                        Button(
                            onClick = {
                                val intent = Intent(context, CvViewActivity::class.java).apply {
                                    putExtra("JOB_SEEKER_ID", finalTargetJobSeekerId)
                                }
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.document),
                                    contentDescription = "View CV",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "View CV",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // DRAWER - Only available for owner
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

                                // Profile Section
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

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
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

                            // MENU ITEMS
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
                                        val intent = Intent(context, AppliedJobsActivity::class.java)
                                        context.startActivity(intent)
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

                            // LOGOUT BUTTON
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 24.dp)
                                    .clickable {
                                        isDrawerOpen = false
                                        repository.logout(currentUserId) { success, message ->
                                            if (success) {
                                                Toast
                                                    .makeText(context, "Logged out successfully", Toast.LENGTH_SHORT)
                                                    .show()
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
        }

        // SETTINGS DIALOG
        if (showSettingsDialog && isOwnProfile) {
            SettingsDialog(
                showSettingsDialog = showSettingsDialog,
                onDismiss = { showSettingsDialog = false },
                onDeactivateClick = {
                    showSettingsDialog = false
                    showConfirmPasswordDialog = true
                },
                onChangePasswordClick = {
                    showSettingsDialog = false
                    showChangePasswordDialog = true
                },
                onDeleteAccountClick = {
                    showSettingsDialog = false
                    showDeleteAccountDialog = true
                },
                context = context
            )
        }

        // CHANGE PASSWORD DIALOG
        if (showChangePasswordDialog && isOwnProfile) {
            ChangePasswordDialog(
                onDismiss = { showChangePasswordDialog = false },
                repository = repository,
                context = context
            )
        }

        // CONFIRM PASSWORD FOR DEACTIVATE DIALOG
        if (showConfirmPasswordDialog && isOwnProfile) {
            ConfirmPasswordForDeactivateDialog(
                onDismiss = {
                    showConfirmPasswordDialog = false
                },
                onConfirm = { password ->
                    // Verify password first
                    val currentUser = repository.getCurrentJobSeeker()
                    if (currentUser != null && currentUser.email != null) {
                        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
                            currentUser.email!!,
                            password
                        )

                        currentUser.reauthenticate(credential)
                            .addOnSuccessListener {
                                // Password is correct, proceed with deactivation
                                jobSeekerViewModel.deactivateAccount(currentUserId) { success, message ->
                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            "Account deactivated successfully",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        // Navigate to login screen
                                        val intent = Intent(context, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                        activity.finish()
                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                    showConfirmPasswordDialog = false
                                }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    context,
                                    "Incorrect password. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            context,
                            "Unable to verify user. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                context = context
            )
        }
        if (showDeleteAccountDialog && isOwnProfile) {
            DeleteAccountConfirmationDialog(
                onDismiss = { showDeleteAccountDialog = false },
                onConfirm = { password ->
                    // Verify password first
                    val currentUser = repository.getCurrentJobSeeker()
                    if (currentUser != null && currentUser.email != null) {
                        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
                            currentUser.email!!,
                            password
                        )

                        currentUser.reauthenticate(credential)
                            .addOnSuccessListener {
                                // Password is correct, proceed with deletion
                                jobSeekerViewModel.deleteAccount(currentUserId) { success, message ->
                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            "Account deleted permanently",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        // Navigate to login screen
                                        val intent = Intent(context, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                        activity.finish()
                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                    showDeleteAccountDialog = false
                                }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    context,
                                    "Incorrect password. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            context,
                            "Unable to verify user. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                context = context
            )
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
        count >= 1000000 -> {
            val millions = count / 1000000.0
            if (millions % 1 == 0.0) {
                "${count / 1000000}M"
            } else {
                "%.1fM".format(millions)
            }
        }
        count >= 1000 -> {
            val thousands = count / 1000.0
            if (thousands % 1 == 0.0) {
                "${count / 1000}K"
            } else {
                "%.1fK".format(thousands)
            }
        }
        else -> count.toString()
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

@Composable
fun SettingsDialog(
    showSettingsDialog: Boolean,
    onDismiss: () -> Unit,
    onDeactivateClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    context: Context
) {
    var isDarkMode by remember { mutableStateOf(false) }
    var isPrivateProfile by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onDismiss() }
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
                                .clickable { onDismiss() }
                                .padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SETTINGS OPTIONS
                SettingsOption(
                    icon = R.drawable.outline_lock_24,
                    title = "Change Password",
                    subtitle = "Update your password",
                    iconColor = Color(0xFF4CAF50),
                    onClick = onChangePasswordClick
                )

                SettingsOptionWithSwitch(
                    icon = R.drawable.darkmode,
                    title = "Dark Mode",
                    subtitle = if (isDarkMode) "Dark theme enabled" else "Light theme enabled",
                    iconColor = Color(0xFF9C27B0),
                    isChecked = isDarkMode,
                    onCheckedChange = { isDarkMode = it }
                )

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
                    onClick = onDeactivateClick
                )

                SettingsOption(
                    icon = R.drawable.deleteaccount,
                    title = "Delete Account",
                    subtitle = "Permanently remove your account",
                    iconColor = Color(0xFFF44336),
                    onClick = onDeleteAccountClick
                )
            }
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

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    repository: JobSeekerRepoImpl,
    context: Context
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var currentPasswordError by remember { mutableStateOf(false) }
    var newPasswordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Password validation
    fun validatePasswords(): Boolean {
        currentPasswordError = currentPassword.isEmpty()
        newPasswordError = newPassword.isEmpty() || newPassword.length < 6
        confirmPasswordError = confirmPassword != newPassword

        when {
            currentPassword.isEmpty() -> {
                errorMessage = "Current password is required"
                return false
            }
            newPassword.isEmpty() -> {
                errorMessage = "New password is required"
                return false
            }
            newPassword.length < 6 -> {
                errorMessage = "Password must be at least 6 characters"
                return false
            }
            confirmPassword != newPassword -> {
                errorMessage = "Passwords do not match"
                return false
            }
            currentPassword == newPassword -> {
                errorMessage = "New password must be different from current"
                return false
            }
        }
        return true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(30f)
    ) {
        // Background overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() }
        )

        // Dialog card
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(28.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_lock_24),
                                contentDescription = "Lock",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Change Password",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                        )
                    }

                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = Color(0xFFF5F5F5)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = "Close",
                            tint = Color(0xFF546E7A),
                            modifier = Modifier
                                .clickable { onDismiss() }
                                .padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description
                Text(
                    text = "Create a strong password to keep your account secure",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF78909C),
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Current Password Field
                Text(
                    text = "Current Password",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        currentPasswordError = false
                        errorMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (currentPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.outline_lock_24),
                            contentDescription = null,
                            tint = if (currentPasswordError) Color(0xFFF44336) else Color(0xFF1976D2),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (currentPasswordVisible)
                                        R.drawable.baseline_visibility_24
                                    else
                                        R.drawable.baseline_visibility_off_24
                                ),
                                contentDescription = null,
                                tint = Color(0xFF78909C),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    isError = currentPasswordError,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA),
                        focusedIndicatorColor = Color(0xFF2196F3),
                        unfocusedIndicatorColor = Color(0xFFE0E0E0),
                        errorContainerColor = Color(0xFFFFF5F5),
                        errorIndicatorColor = Color(0xFFF44336)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // New Password Field
                Text(
                    text = "New Password",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        newPasswordError = false
                        errorMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (newPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.outline_lock_24),
                            contentDescription = null,
                            tint = if (newPasswordError) Color(0xFFF44336) else Color(0xFF4CAF50),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (newPasswordVisible)
                                        R.drawable.baseline_visibility_24
                                    else
                                        R.drawable.baseline_visibility_off_24
                                ),
                                contentDescription = null,
                                tint = Color(0xFF78909C),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    isError = newPasswordError,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA),
                        focusedIndicatorColor = Color(0xFF2196F3),
                        unfocusedIndicatorColor = Color(0xFFE0E0E0),
                        errorContainerColor = Color(0xFFFFF5F5),
                        errorIndicatorColor = Color(0xFFF44336)
                    ),
                    singleLine = true
                )

                // Password strength indicator
                if (newPassword.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    val strength = when {
                        newPassword.length < 6 -> "Weak"
                        newPassword.length < 10 -> "Medium"
                        else -> "Strong"
                    }

                    val strengthColor = when (strength) {
                        "Weak" -> Color(0xFFF44336)
                        "Medium" -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .background(
                                    if (newPassword.length >= 6) strengthColor else Color(0xFFE0E0E0),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .background(
                                    if (newPassword.length >= 10) strengthColor else Color(0xFFE0E0E0),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .background(
                                    if (newPassword.length >= 12) strengthColor else Color(0xFFE0E0E0),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = strength,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = strengthColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Confirm Password Field
                Text(
                    text = "Confirm New Password",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = false
                        errorMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (confirmPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.outline_lock_24),
                            contentDescription = null,
                            tint = if (confirmPasswordError) Color(0xFFF44336)
                            else if (confirmPassword.isNotEmpty() && confirmPassword == newPassword) Color(0xFF4CAF50)
                            else Color(0xFF1976D2),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (confirmPasswordVisible)
                                        R.drawable.baseline_visibility_24
                                    else
                                        R.drawable.baseline_visibility_off_24
                                ),
                                contentDescription = null,
                                tint = Color(0xFF78909C),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    isError = confirmPasswordError,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA),
                        focusedIndicatorColor = Color(0xFF2196F3),
                        unfocusedIndicatorColor = Color(0xFFE0E0E0),
                        errorContainerColor = Color(0xFFFFF5F5),
                        errorIndicatorColor = Color(0xFFF44336)
                    ),
                    singleLine = true
                )

                // Error message
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF5F5)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.round_info_outline_24),
                                contentDescription = null,
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = Color(0xFFF44336)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    Button(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF5F5F5),
                            contentColor = Color(0xFF546E7A)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Confirm Button
                    Button(
                        onClick = {
                            if (validatePasswords()) {
                                isLoading = true
                                repository.changePassword(
                                    currentPassword = currentPassword,
                                    newPassword = newPassword
                                ) { success, message ->
                                    isLoading = false
                                    if (success) {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    } else {
                                        // Show error in the dialog
                                        currentPasswordError = true
                                        errorMessage = message
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isLoading) "Updating..." else "Update",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmPasswordForDeactivateDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    context: Context
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(40f) // Increased zIndex to ensure it's on top
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onDismiss() }
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
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
                        text = "Confirm Password",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
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
                                .clickable { onDismiss() }
                                .padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Please enter your password to deactivate your account. This action will temporarily disable your account.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF546E7A),
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_lock_24),
                            contentDescription = null,
                            tint = Color(0xFF2196F3)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (passwordVisible)
                                        R.drawable.baseline_visibility_24
                                    else
                                        R.drawable.baseline_visibility_off_24
                                ),
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color(0xFF2196F3)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color(0xFF2196F3),
                        unfocusedIndicatorColor = Color(0xFFBDBDBD)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onDismiss() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0E0E0),
                            contentColor = Color(0xFF424242)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            "Cancel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (password.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Please enter your password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onConfirm(password)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            "Deactivate",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteAccountConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    context: Context
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(50f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() }
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(80.dp)
                        .background(
                            Color(0xFFF44336).copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_info_outline_24),
                        contentDescription = "Warning",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Delete Account Permanently?",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF5F5)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ Warning: This action cannot be undone!",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF44336)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "• All your data will be permanently deleted\n• Your profile will be removed\n• You cannot recover this account\n• All your applications will be lost",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = Color(0xFF546E7A),
                                lineHeight = 22.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Password Confirmation
                Text(
                    text = "Enter your password to confirm",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password", color = Color(0xFFBDBDBD)) },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_lock_24),
                            contentDescription = null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (passwordVisible)
                                        R.drawable.baseline_visibility_24
                                    else
                                        R.drawable.baseline_visibility_off_24
                                ),
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color(0xFF78909C),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA),
                        focusedIndicatorColor = Color(0xFFF44336),
                        unfocusedIndicatorColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    Button(
                        onClick = { onDismiss() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF5F5F5),
                            contentColor = Color(0xFF546E7A)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            "Cancel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Delete Button
                    Button(
                        onClick = {
                            if (password.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Please enter your password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onConfirm(password)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.deleteaccount),
                                contentDescription = "Delete",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Delete",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

