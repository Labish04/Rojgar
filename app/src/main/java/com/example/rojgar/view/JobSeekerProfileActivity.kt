package com.example.rojgar.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.rojgar.R
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue
import com.example.rojgar.ui.theme.RojgarTheme
import com.example.rojgar.ui.theme.SkyBlue

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerProfileBody(targetJobSeekerId: String = "") {
    val context = LocalContext.current
    val activity = context as Activity
    val repository = remember { JobSeekerRepoImpl() }
    val currentUserId = repository.getCurrentJobSeeker()?.uid ?: ""

    // UI States
    var showMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showFollowMoreOptions by remember { mutableStateOf(false) }

    // Video and Interaction States
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoThumbnail by remember { mutableStateOf<Bitmap?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    // Check following status
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
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(painterResource(R.drawable.outline_arrow_back_ios_24), "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Settings Menu")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMenu = false
                                    showSettingsDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    showMenu = false
                                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(color = Blue)
        ) {
            // Profile Info Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = "Sarah Johnson",
                        style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "I am a dedicated IT student eager to learn new skills and grow in technology.",
                        style = TextStyle(fontSize = 14.sp)
                    )
                }

                Card(
                    modifier = Modifier
                        .width(180.dp)
                        .height(250.dp)
                        .padding(top = 20.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profilepicture),
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Lower Content Area
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = DarkBlue)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Action Buttons (Details & Follow)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Details Button
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, JobSeekerProfileDetailsActivity::class.java))
                            },
                            modifier = Modifier.width(150.dp).height(45.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue, contentColor = Color.Black)
                        ) {
                            Icon(painterResource(R.drawable.round_info_outline_24), null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Details")
                        }

                        // Follow Button
                        Button(
                            onClick = {
                                if (isFollowing) {
                                    showFollowMoreOptions = !showFollowMoreOptions
                                } else {
                                    repository.followJobSeeker(currentUserId, targetJobSeekerId) { success, msg ->
                                        if (success) isFollowing = true
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.width(150.dp).height(45.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue, contentColor = Color.Black)
                        ) {
                            Icon(
                                painter = painterResource(if (isFollowing) R.drawable.following_icon else R.drawable.follow_icon),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (isFollowing) "Following" else "Follow")
                        }
                    }

                    // Follow Options Dropdown (Unfollow/Message)
                    if (showFollowMoreOptions) {
                        Card(
                            modifier = Modifier.align(Alignment.TopEnd).padding(top = 70.dp, end = 20.dp).width(150.dp),
                            colors = CardDefaults.cardColors(containerColor = SkyBlue)
                        ) {
                            Column {
                                Text("Unfollow", modifier = Modifier.fillMaxWidth().clickable {
                                    repository.unfollowJobSeeker(currentUserId, targetJobSeekerId) { success, msg ->
                                        if (success) isFollowing = false
                                        showFollowMoreOptions = false
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }.padding(16.dp))
                                Divider(color = Color.Gray)
                                Text("Message", modifier = Modifier.fillMaxWidth().clickable {
                                    showFollowMoreOptions = false
                                }.padding(16.dp))
                            }
                        }
                    }

                    // Video Section
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Card(
                            modifier = Modifier.height(200.dp).width(340.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (isPlaying && selectedVideoUri != null) {
                                    VideoPlayer(uri = selectedVideoUri!!, modifier = Modifier.fillMaxSize())
                                } else {
                                    videoThumbnail?.let {
                                        Image(bitmap = it.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    }
                                }

                                Row(modifier = Modifier.align(Alignment.Center)) {
                                    IconButton(onClick = { if(selectedVideoUri != null) isPlaying = true }) {
                                        Icon(painterResource(R.drawable.baseline_play_arrow_24), "Play", Modifier.size(48.dp), tint = Color.White)
                                    }
                                }

                                IconButton(
                                    onClick = { videoPickerLauncher.launch("video/*") },
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                                ) {
                                    Icon(painterResource(R.drawable.baseline_upload_24), "Upload", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs Logic
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onDelete = { showSettingsDialog = false; showDeleteDialog = true },
            onDeactivate = { showSettingsDialog = false; showDeactivateDialog = true }
        )
    }

    if (showDeleteDialog) {
        PasswordDialog("Delete Account", onConfirm = { showDeleteDialog = false }, onDismiss = { showDeleteDialog = false })
    }

    if (showDeactivateDialog) {
        PasswordDialog("Deactivate Account", onConfirm = { showDeactivateDialog = false }, onDismiss = { showDeactivateDialog = false })
    }
}

/* --- Helper Components --- */

@Composable
fun SettingsDialog(onDismiss: () -> Unit, onDelete: () -> Unit, onDeactivate: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.fillMaxWidth()) {
                    Text("Delete Account", color = Color.White)
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = onDeactivate,colors = ButtonDefaults.buttonColors(containerColor = Color.Blue), modifier = Modifier.fillMaxWidth()) {
                    Text("Deactivate Account")
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    }
}

@Composable
fun PasswordDialog(title: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onConfirm(password) }) { Text("Confirm") }
                }
            }
        }
    }
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
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
}

fun getRealPathFromURI(context: Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.Video.Media.DATA)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
        }
    }
    return null
}