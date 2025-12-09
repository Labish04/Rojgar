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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import androidx.compose.ui.graphics.asImageBitmap
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
    var isPlaying by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeactivateConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var passwordDeactivate by remember { mutableStateOf("") }
    var passwordDelete by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
                .padding(padding)
                .background(color = Blue)
        ) {
            // Top Bar with Menu
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

                // 3-Dot Menu with Dropdown
                Box {
                    Icon(
                        painter = painterResource(R.drawable.outline_more_vert_24),
                        contentDescription = "Menu",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable { showMenu = true }
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_settings_24),
                                        contentDescription = "Settings",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Settings")
                                }
                            },
                            onClick = {
                                showMenu = false
                                showSettingsDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_edit_24),
                                        contentDescription = "Edit Profile",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Edit Profile")
                                }
                            },
                            onClick = {
                                showMenu = false
                                Toast.makeText(context, "Edit Profile", Toast.LENGTH_SHORT).show()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_share_24),
                                        contentDescription = "Share",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Share Profile")
                                }
                            },
                            onClick = {
                                showMenu = false
                                Toast.makeText(context, "Share Profile", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Divider()

                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_logout_24),
                                        contentDescription = "Logout",
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Red
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Logout", color = Color.Red)
                                }
                            },
                            onClick = {
                                showMenu = false
                                Toast.makeText(context, "Logging out...", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 1.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
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

            Card(
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 0.dp, y = (-8).dp),
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

                                if (!isPlaying) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_play_arrow_24),
                                        contentDescription = "Play Video",
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(60.dp)
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

        // Settings Dialog
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Account Settings") },
                text = {
                    Column {
                        Text(
                            "Manage your account settings",
                            style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Deactivate Account Option
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showSettingsDialog = false
                                    showDeactivateDialog = true
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3CD)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_pause_circle_24),
                                    contentDescription = "Deactivate",
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Deactivate Account",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        "Temporarily disable your account",
                                        style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Delete Account Option
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showSettingsDialog = false
                                    showDeleteDialog = true
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEE2E2)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.delete),
                                    contentDescription = "Delete",
                                    tint = Color.Red,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Delete Account",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Red
                                        )
                                    )
                                    Text(
                                        "Permanently delete your account",
                                        style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
        // Deactivate Account - Password Confirmation Dialog
        if (showDeactivateConfirm) {
            AlertDialog(
                onDismissRequest = { showDeactivateConfirm = false },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_lock_24),
                        contentDescription = "Password",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { Text("Confirm Password") },
                text = {
                    Column {
                        Text(
                            "Please enter your password to deactivate your account",
                            style = TextStyle(fontSize = 14.sp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = passwordDeactivate,
                            onValueChange = { passwordDeactivate = it },
                            label = { Text("Password") },
                            placeholder = { Text("Enter your password") },
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        painter = painterResource(
                                            if (passwordVisible)
                                                R.drawable.visibility
                                            else
                                                R.drawable.visibility_off
                                        ),
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Are you sure you want to deactivate your account?",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (passwordDeactivate.isNotEmpty()) {
                                showDeactivateConfirm = false
                                Toast.makeText(
                                    context,
                                    "Account Deactivated Successfully",
                                    Toast.LENGTH_LONG
                                ).show()
                                activity.finish()

                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter your password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        ),
                        enabled = passwordDeactivate.isNotEmpty()
                    ) {
                        Text("Deactivate Account")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeactivateConfirm = false
                        passwordDeactivate = ""
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Account - Password Confirmation Dialog
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_lock_24),
                        contentDescription = "Password",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { Text("Confirm Password", color = Color.Red) },
                text = {
                    Column {
                        Text(
                            "⚠️ This action is PERMANENT and IRREVERSIBLE!",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Please enter your password to permanently delete your account",
                            style = TextStyle(fontSize = 14.sp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = passwordDelete,
                            onValueChange = { passwordDelete = it },
                            label = { Text("Password") },
                            placeholder = { Text("Enter your password") },
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        painter = painterResource(
                                            if (passwordVisible)
                                                R.drawable.visibility
                                            else
                                                R.drawable.visibility_off
                                        ),
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Red,
                                focusedLabelColor = Color.Red
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Are you absolutely sure you want to delete your account?",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (passwordDelete.isNotEmpty()) {
                                showDeleteConfirm = false
                                Toast.makeText(
                                    context,
                                    "Account Deleted Successfully",
                                    Toast.LENGTH_LONG
                                ).show()

                                activity.finish()

                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter your password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        enabled = passwordDelete.isNotEmpty()
                    ) {
                        Text("Delete Permanently")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        passwordDelete = ""
                    }) {
                        Text("Cancel")
                    }
                }
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
                playWhenReady = true
            }
        }

        AndroidView(
            factory = { PlayerView(context).apply { player = exoPlayer } },
            modifier = modifier
        )

        DisposableEffect(uri) {
            onDispose {
                exoPlayer.stop()
                exoPlayer.release()
            }
        }
    }


