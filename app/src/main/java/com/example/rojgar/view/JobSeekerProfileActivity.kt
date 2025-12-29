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
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue
import com.example.rojgar.ui.theme.RojgarTheme
import com.example.rojgar.ui.theme.SkyBlue
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
import com.example.rojgar.ui.theme.Purple
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.zIndex


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

    // Repository instance
    val repository = remember { JobSeekerRepoImpl() }

    // Get current user ID
    val currentUserId = repository.getCurrentJobSeeker()?.uid ?: ""

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoThumbnail by remember { mutableStateOf<Bitmap?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    var showMoreDialog by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isDrawerOpen by remember { mutableStateOf(false) }

    // Check if currently following when screen loads
    LaunchedEffect(targetJobSeekerId) {
        if (targetJobSeekerId.isNotEmpty() && currentUserId.isNotEmpty()) {
            repository.isFollowing(currentUserId, targetJobSeekerId) { following ->
                isFollowing = following
            }
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding()
                    .background(Blue)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable { activity.finish() }
                    )

                    Icon(
                        painter = painterResource(R.drawable.outline_more_vert_24),
                        contentDescription = "Menu",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                isDrawerOpen = true
                            }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // LEFT SIDE TEXT
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp)
                    ) {
                        Spacer(modifier = Modifier.height(110.dp))
                        Text(
                            text = "Sarah Johnson",
                            style = TextStyle(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        Text(
                            text = "I am a dedicated IT student eager to learn new skills, gain experience, and grow in the field of technology.",
                            style = TextStyle(fontSize = 13.sp)
                        )
                    }

                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .height(340.dp)
                            .background(DarkBlue)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.profilepicture),
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = 0.dp, y = -8.dp),
                    colors = CardDefaults.cardColors(
                        contentColor = DarkBlue
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBlue)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(
                                        context,
                                        JobSeekerProfileDetailsActivity::class.java
                                    )
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(25.dp),
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(45.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SkyBlue,
                                    contentColor = Color.Black
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.round_info_outline_24),
                                        contentDescription = "Details Icon",
                                        modifier = Modifier.size(27.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Details",
                                        style = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // ---------- FOLLOW BUTTON WITH DATABASE ----------
                            Button(
                                onClick = {
                                    if (isFollowing) {
                                        showMoreDialog = !showMoreDialog
                                    } else {
                                        // Follow the user
                                        repository.followJobSeeker(
                                            currentUserId,
                                            targetJobSeekerId
                                        ) { success, message ->
                                            if (success) {
                                                isFollowing = true
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                                    .show()
                                            } else {
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                                    .show()
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(25.dp),
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(45.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SkyBlue,
                                    contentColor = Color.Black
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (isFollowing) R.drawable.following_icon else R.drawable.follow_icon
                                        ),
                                        contentDescription = "Follow Icon",
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isFollowing) "Following" else "Follow",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // ---------- MORE OPTIONS MENU ----------
                        if (showMoreDialog) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 70.dp, end = 20.dp)
                                    .background(SkyBlue, RoundedCornerShape(12.dp))
                                    .width(150.dp)
                            ) {
                                Text(
                                    text = "Unfollow",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Unfollow the user
                                            repository.unfollowJobSeeker(
                                                currentUserId,
                                                targetJobSeekerId
                                            ) { success, message ->
                                                if (success) {
                                                    isFollowing = false
                                                    showMoreDialog = false
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            message,
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                } else {
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            message,
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                }
                                            }
                                        }
                                        .padding(16.dp)
                                )

                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color.Gray)
                                )

                                Text(
                                    text = "Message",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Message clicked",
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                            showMoreDialog = false
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = 180.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Card(
                                modifier = Modifier
                                    .height(200.dp)
                                    .width(350.dp),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 10.dp
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (isPlaying && selectedVideoUri != null) {
                                        VideoPlayer(
                                            uri = selectedVideoUri!!,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        if (videoThumbnail != null) {
                                            Image(
                                                bitmap = videoThumbnail!!.asImageBitmap(),
                                                contentDescription = "Video Thumbnail",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    Icon(
                                        painter = painterResource(R.drawable.baseline_upload_24),
                                        contentDescription = "Upload Video",
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(16.dp)
                                            .size(40.dp)
                                            .clickable {
                                                videoPickerLauncher.launch("video/*")
                                            }
                                    )

                                    Icon(
                                        painter = painterResource(R.drawable.baseline_play_arrow_24),
                                        contentDescription = "Play Video",
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(40.dp)
                                            .clickable {
                                                if (selectedVideoUri != null) {
                                                    isPlaying = true
                                                } else {
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            "Select a video first!",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                }
                                            }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp)
                                .offset(y = 400.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(context, CvViewActivity::class.java)
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Purple,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(
                                    "View CV", style = TextStyle(
                                        fontSize = 20.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Stylish Right-side Drawer
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { isDrawerOpen = false }
                )

                // Drawer content
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(320.dp)
                        .align(Alignment.CenterEnd)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    DarkBlue,
                                    Blue.copy(alpha = 0.9f),
                                    DarkBlue
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Stylish Header with Profile
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            SkyBlue.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        ) {
                            // Close button
                            Icon(
                                painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(20.dp)
                                    .size(24.dp)
                                    .graphicsLayer(rotationZ = 180f)
                                    .clickable { isDrawerOpen = false }
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Profile Image Circle with glow effect
                                Card(
                                    modifier = Modifier.size(90.dp),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    elevation = CardDefaults.cardElevation(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = SkyBlue.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp)
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxSize(),
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.profilepicture),
                                                contentDescription = "Profile",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "Sarah Johnson",
                                    style = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "IT Student",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Light
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Menu Items with Stylish Cards
                        StylishDrawerMenuItem(
                            icon = R.drawable.round_info_outline_24,
                            text = "Saved Jobs",
                            subtitle = "View bookmarked opportunities",
                            onClick = {
                                isDrawerOpen = false
                                Toast.makeText(context, "Saved Jobs clicked", Toast.LENGTH_SHORT)
                                    .show()
                                // Navigate to Saved Jobs Activity
                                // val intent = Intent(context, SavedJobsActivity::class.java)
                                // context.startActivity(intent)
                            }
                        )

                        StylishDrawerMenuItem(
                            icon = R.drawable.round_info_outline_24,
                            text = "Applied Jobs",
                            subtitle = "Track your applications",
                            onClick = {
                                isDrawerOpen = false
                                Toast.makeText(context, "Applied Jobs clicked", Toast.LENGTH_SHORT)
                                    .show()
                                // Navigate to Applied Jobs Activity
                                // val intent = Intent(context, AppliedJobsActivity::class.java)
                                // context.startActivity(intent)
                            }
                        )

                        StylishDrawerMenuItem(
                            icon = R.drawable.round_info_outline_24,
                            text = "Settings",
                            subtitle = "Manage preferences",
                            onClick = {
                                isDrawerOpen = false
                                Toast.makeText(context, "Settings clicked", Toast.LENGTH_SHORT)
                                    .show()
                                // Navigate to Settings Activity
                                // val intent = Intent(context, SettingsActivity::class.java)
                                // context.startActivity(intent)
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Logout with Special Styling
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                                .clickable {
                                    isDrawerOpen = false
                                    repository
                                        .getCurrentJobSeeker()
                                        ?.let {
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Logging out...",
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                            activity.finish()
                                        }
                                },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Red.copy(alpha = 0.15f)
                            ),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.round_info_outline_24),
                                    contentDescription = "Logout",
                                    tint = Color.Red,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Logout",
                                    style = TextStyle(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StylishDrawerMenuItem(
    icon: Int,
    text: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SkyBlue.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        SkyBlue.copy(alpha = 0.4f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = text,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                )
            }

            Icon(
                painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                contentDescription = "Navigate",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer(rotationZ = 180f)
            )
        }
    }
}

@Preview()
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