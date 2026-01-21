package com.example.rojgar.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.rojgar.model.GroupChat
import com.example.rojgar.model.GroupMessage
import com.example.rojgar.repository.GroupChatRepositoryImpl
import com.example.rojgar.viewmodel.GroupChatRoomViewModel
import java.text.SimpleDateFormat
import java.util.*

class GroupChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val groupId = intent.getStringExtra("groupId") ?: ""
        val groupName = intent.getStringExtra("groupName") ?: "Group"
        val currentUserId = intent.getStringExtra("currentUserId") ?: ""
        val currentUserName = intent.getStringExtra("currentUserName") ?: "Me"

        val viewModel = GroupChatRoomViewModel(GroupChatRepositoryImpl())

        setContent {
            MaterialTheme {
                GroupChatScreen(
                    viewModel = viewModel,
                    groupId = groupId,
                    groupName = groupName,
                    currentUserId = currentUserId,
                    currentUserName = currentUserName,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    viewModel: GroupChatRoomViewModel,
    groupId: String,
    groupName: String,
    currentUserId: String,
    currentUserName: String,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val messages by viewModel.messages.observeAsState(emptyList())
    val groupInfo by viewModel.groupInfo.observeAsState()
    var textState by remember { mutableStateOf("") }
    var showGroupInfoDialog by remember { mutableStateOf(false) }
    var showMembersDialog by remember { mutableStateOf(false) }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Load messages and group info
    LaunchedEffect(groupId) {
        viewModel.listenForMessages(groupId)
        viewModel.loadGroupInfo(groupId)
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            viewModel.sendGroupImage(context, groupId, currentUserId, currentUserName, it)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && selectedImageUri != null) {
            viewModel.sendGroupImage(context, groupId, currentUserId, currentUserName, selectedImageUri!!)
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = viewModel.createImageUri(context)
            selectedImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Scaffold(
        topBar = {
            GroupChatTopBar(
                groupName = groupName,
                groupImage = groupInfo?.groupImage ?: "",
                memberCount = groupInfo?.members?.size ?: 0,
                onBackPressed = onBackPressed,
                onGroupInfoClick = { showGroupInfoDialog = true },
                onMembersClick = { showMembersDialog = true }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = textState,
                onTextChange = { textState = it },
                onSend = {
                    if (textState.isNotBlank()) {
                        viewModel.sendGroupMessage(groupId, currentUserId, currentUserName, textState)
                        textState = ""
                    }
                },
                onAttachmentClick = { showAttachmentOptions = true }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { message ->
                    GroupMessageItem(
                        message = message,
                        isMe = message.senderId == currentUserId,
                        onMessageLongPress = {
                            viewModel.deleteMessage(groupId, message.messageId)
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }

    // Dialogs
    if (showGroupInfoDialog && groupInfo != null) {
        GroupInfoDialog(
            groupInfo = groupInfo!!,
            onDismiss = { showGroupInfoDialog = false }
        )
    }

    if (showMembersDialog && groupInfo != null) {
        GroupMembersDialog(
            groupInfo = groupInfo!!,
            currentUserId = currentUserId,
            onDismiss = { showMembersDialog = false }
        )
    }

    if (showAttachmentOptions) {
        AttachmentOptionsDialog(
            onDismiss = { showAttachmentOptions = false },
            onImageClick = {
                showAttachmentOptions = false
                imagePickerLauncher.launch("image/*")
            },
            onCameraClick = {
                showAttachmentOptions = false
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                        val uri = viewModel.createImageUri(context)
                        selectedImageUri = uri
                        cameraLauncher.launch(uri)
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatTopBar(
    groupName: String,
    groupImage: String,
    memberCount: Int,
    onBackPressed: () -> Unit,
    onGroupInfoClick: () -> Unit,
    onMembersClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGroupInfoClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group Image
                if (groupImage.isNotEmpty()) {
                    AsyncImage(
                        model = groupImage,
                        contentDescription = "Group Image",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1976D2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = groupName.firstOrNull()?.uppercase() ?: "G",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = groupName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$memberCount members",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onMembersClick) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Members",
                    tint = Color.White
                )
            }
            IconButton(onClick = onGroupInfoClick) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF1976D2),
            titleContentColor = Color.White
        )
    )
}

@Composable
fun GroupMessageItem(
    message: GroupMessage,
    isMe: Boolean,
    onMessageLongPress: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // Sender name for others' messages
        if (!isMe) {
            Row(
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.senderName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1976D2)
                )
            }
        }

        // Message bubble
        Surface(
            color = if (isMe) Color(0xFFDCF8C6) else Color.White,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Message content based on type
                when (message.messageType) {
                    "text" -> {
                        Text(
                            text = message.messageText,
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }
                    "image" -> {
                        AsyncImage(
                            model = message.messageText,
                            contentDescription = "Image message",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    "voice" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Voice message",
                                tint = if (isMe) Color(0xFF075E54) else Color(0xFF1976D2),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Voice message", fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp and read status
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatTimestamps(message.timestamp),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.isRead) Icons.Default.Done else Icons.Default.Done,
                            contentDescription = if (message.isRead) "Read" else "Delivered",
                            tint = if (message.isRead) Color(0xFF34B7F1) else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachmentClick: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attachment button
            IconButton(onClick = onAttachmentClick) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Attach",
                    tint = Color(0xFF1976D2)
                )
            }

            // Text field
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...", fontSize = 14.sp) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = Color.LightGray
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send button
            FloatingActionButton(
                onClick = onSend,
                modifier = Modifier.size(48.dp),
                containerColor = Color(0xFF1976D2),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun GroupInfoDialog(groupInfo: GroupChat, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Group Image
                if (groupInfo.groupImage.isNotEmpty()) {
                    AsyncImage(
                        model = groupInfo.groupImage,
                        contentDescription = "Group Image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1976D2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = groupInfo.groupName.firstOrNull()?.uppercase() ?: "G",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = groupInfo.groupName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Created by ${groupInfo.createdByName}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = "${groupInfo.members.size} members",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun GroupMembersDialog(
    groupInfo: GroupChat,
    currentUserId: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Group Members (${groupInfo.members.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn {
                    items(groupInfo.members.indices.toList()) { index ->
                        MemberItem(
                            memberName = groupInfo.memberNames.getOrNull(index) ?: "Unknown",
                            memberPhoto = groupInfo.memberPhotos.getOrNull(index) ?: "",
                            isCreator = groupInfo.members[index] == groupInfo.createdBy,
                            isCurrentUser = groupInfo.members[index] == currentUserId
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun MemberItem(
    memberName: String,
    memberPhoto: String,
    isCreator: Boolean,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Member photo
        if (memberPhoto.isNotEmpty()) {
            AsyncImage(
                model = memberPhoto,
                contentDescription = memberName,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1976D2)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = memberName.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isCurrentUser) "$memberName (You)" else memberName,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            if (isCreator) {
                Text(
                    text = "Group Admin",
                    fontSize = 12.sp,
                    color = Color(0xFF1976D2)
                )
            }
        }
    }
}

@Composable
fun AttachmentOptionsDialog(
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Choose Attachment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                AttachmentOption(
                    icon = Icons.Default.AccountCircle,
                    label = "Gallery",
                    onClick = onImageClick
                )

                AttachmentOption(
                    icon = Icons.Default.Face,
                    label = "Camera",
                    onClick = onCameraClick
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF1976D2),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontSize = 16.sp)
    }
}

fun formatTimestamps(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}