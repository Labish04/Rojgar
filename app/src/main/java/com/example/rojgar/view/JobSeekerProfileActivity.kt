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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.rojgar.R
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.Purple




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
    var showMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf("") } // "delete" or "deactivate"


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
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            activity.finish()
                        }
                )

                Box {
                    Icon(
                        painter = painterResource(R.drawable.outline_more_vert_24),
                        contentDescription = "Menu",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                showMenu = true
                            }
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
                                    Text(
                                        text = "Settings",
                                        style = TextStyle(fontSize = 16.sp)
                                    )
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
                                        painter = painterResource(R.drawable.baseline_logout_24),
                                        contentDescription = "Logout",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Logout",
                                        style = TextStyle(fontSize = 16.sp)
                                    )
                                }
                            },
                            onClick = {
                                showMenu = false
                                showLogoutDialog = true
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

                // LEFT SIDE TEXT
                Column(
                    modifier = Modifier
                        .weight(1f)
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
                    Spacer(modifier = Modifier.height(10.dp))
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
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onDeleteClick = {
                showSettingsDialog = false
                showDeleteDialog = true
            },
            onDeactivateClick = {
                showSettingsDialog = false
                showDeactivateDialog = true
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Account",
            message = "Are you sure you want to delete your account? This action cannot be undone.",
            confirmText = "Delete",
            onConfirm = {
                showDeleteDialog = false
                pendingAction = "delete"
                showPasswordDialog = true
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Deactivate Confirmation Dialog
    if (showDeactivateDialog) {
        ConfirmationDialog(
            title = "Deactivate Account",
            message = "Are you sure you want to deactivate your account? You can reactivate it later.",
            confirmText = "Deactivate",
            onConfirm = {
                showDeactivateDialog = false
                pendingAction = "deactivate"
                showPasswordDialog = true
            },
            onDismiss = { showDeactivateDialog = false }
        )
    }

    // Password Confirmation Dialog
    if (showPasswordDialog) {
        PasswordConfirmationDialog(
            action = pendingAction,
            onConfirm = { password ->
                // Here you would validate the password with your backend
                // For now, we'll just check if it's not empty
                if (password.isNotEmpty()) {
                    when (pendingAction) {
                        "delete" -> {
                            Toast.makeText(context, "Account Deleted", Toast.LENGTH_SHORT).show()
                            // Add your actual delete logic here
                        }
                        "deactivate" -> {
                            Toast.makeText(context, "Account Deactivated", Toast.LENGTH_SHORT).show()
                            // Add your actual deactivate logic here
                        }
                    }
                    showPasswordDialog = false
                    pendingAction = ""
                } else {
                    Toast.makeText(context, "Please enter your password", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = {
                showPasswordDialog = false
                pendingAction = ""
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "Logout",
            message = "Are you sure you want to logout?",
            confirmText = "Logout",
            onConfirm = {
                Toast.makeText(context, "Logged Out Successfully", Toast.LENGTH_SHORT).show()
                showLogoutDialog = false
                // Add your logout logic here (clear session, navigate to login, etc.)
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
    onDeactivateClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Settings Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_settings_24),
                        contentDescription = "Settings",
                        modifier = Modifier.size(28.dp),
                        tint = Purple
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Settings",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Purple
                        )
                    )
                }

                Divider(color = Color.Black, thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                // Delete Account Button
                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = "Delete",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Delete Account",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Deactivate Account Button
                Button(
                    onClick = onDeactivateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =Purple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.deactivate),
                        contentDescription = "Deactivate",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Deactivate Account",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Black
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val buttonColor = when (confirmText) {
        "Logout" -> Purple
        "Deactivate" -> Purple
        else -> Color(0xFFFF5252) // Delete
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Text(
                text = message,
                style = TextStyle(fontSize = 16.sp)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Black)
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun PasswordConfirmationDialog(
    action: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val title = when (action) {
        "delete" -> "Confirm Delete"
        "deactivate" -> "Confirm Deactivation"
        else -> "Confirm Action"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Title
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Message
                Text(
                    text = "Please enter your password to confirm this action.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Gray
                    ),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Enter your password") },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (passwordVisible)
                                        R.drawable.baseline_visibility_24
                                    else
                                        R.drawable.baseline_visibility_off_24
                                ),
                                contentDescription = if (passwordVisible)
                                    "Hide password"
                                else
                                    "Show password"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple,
                        focusedLabelColor = Purple,
                        cursorColor = Purple
                    ),
                    singleLine = true
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Black
                            )
                        )
                    }

                    val buttonColor = when (action) {
                        "delete" -> Color(0xFFFF5252)
                        else -> Purple
                    }

                    Button(
                        onClick = { onConfirm(password) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Confirm",
                            style = TextStyle(fontSize = 16.sp)
                        )
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