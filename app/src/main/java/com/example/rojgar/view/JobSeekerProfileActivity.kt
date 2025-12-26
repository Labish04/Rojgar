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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
                    modifier = Modifier.size(30.dp)
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
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
                                                    .makeText(context, message, Toast.LENGTH_SHORT)
                                                    .show()
                                            } else {
                                                Toast
                                                    .makeText(context, message, Toast.LENGTH_SHORT)
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
                            .offset(y = 200.dp),
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
                }
            }
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