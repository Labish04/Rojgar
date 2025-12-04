package com.example.rojgar

import android.app.Activity
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
fun JobSeekerProfileBody() {

    val context = LocalContext.current
    val activity = context as Activity

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoThumbnail by remember { mutableStateOf<Bitmap?>(null) }


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


    var isPlaying by remember { mutableStateOf(false) }




    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(color = Blue)
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
                    modifier = Modifier.size(30.dp)
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
                    modifier = Modifier.weight(1f)
                        .padding(10.dp)
                )
                {
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
                        .background(Blue)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profilepicture),
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

            }
            Card (
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 0.dp, y = -8.dp),
                colors = CardDefaults.cardColors(
                    contentColor = DarkBlue
                )
            ){
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBlue)


                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {

                        Button (
                            onClick = {
                                val intent = Intent(context,
                                    JobSeekerProfileDetailsActivity::class.java)
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
                    }
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = 200.dp),
                        horizontalArrangement = Arrangement.Center,
                    ){
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
                                    // Show video using ExoPlayer
                                    VideoPlayer(
                                        uri = selectedVideoUri!!,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // If video selected → show thumbnail
                                    if (videoThumbnail != null) {
                                        Image(
                                            bitmap = videoThumbnail!!.asImageBitmap(),
                                            contentDescription = "Video Thumbnail",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                // Upload icon (always visible)
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

                                // Play icon
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
                                                Toast.makeText(context, "Select a video first!", Toast.LENGTH_SHORT).show()
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
fun getRealPathFromURI(context: android.content.Context, uri: Uri): String? {
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

    AndroidView(factory = { PlayerView(context).apply { player = exoPlayer } }, modifier = modifier)

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}

