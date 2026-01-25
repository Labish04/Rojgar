package com.example.rojgar.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.rojgar.R
import com.example.rojgar.model.GroupChat
import com.example.rojgar.model.GroupMessage
import com.example.rojgar.repository.GroupChatRepositoryImpl
import com.example.rojgar.viewmodel.GroupChatRoomViewModel
import com.example.rojgar.viewmodel.LeaveGroupResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
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
    val error by viewModel.error.observeAsState()
    val isUploading by viewModel.isUploading.observeAsState(false)
    val uploadProgress by viewModel.uploadProgress.observeAsState(0.0)
    val leaveGroupResult by viewModel.leaveGroupResult.observeAsState()

    var textState by remember { mutableStateOf("") }
    var showGroupInfoDialog by remember { mutableStateOf(false) }
    var showMembersDialog by remember { mutableStateOf(false) }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showLeaveGroupDialog by remember { mutableStateOf(false) }
    var showEditMembersDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Show errors
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Load messages and group info on start
    LaunchedEffect(groupId) {
        isLoading = true
        viewModel.loadGroupInfo(groupId)
        viewModel.loadMessages(groupId, limit = 100)
        viewModel.listenForMessages(groupId)
        delay(1000)
        isLoading = false
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Handle leave group result
    LaunchedEffect(leaveGroupResult) {
        when (leaveGroupResult) {
            is LeaveGroupResult.Success -> {
                Toast.makeText(context, "You have left the group", Toast.LENGTH_SHORT).show()
                onBackPressed()
                viewModel.clearLeaveGroupResult()
            }
            is LeaveGroupResult.Error -> {
                Toast.makeText(context, (leaveGroupResult as LeaveGroupResult.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.clearLeaveGroupResult()
            }
            null -> {}
        }
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
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            ModernGroupChatTopBar(
                groupName = groupName,
                groupImage = groupInfo?.groupImage ?: "",
                memberCount = groupInfo?.members?.size ?: 0,
                onBackPressed = onBackPressed,
                onGroupInfoClick = { showGroupInfoDialog = true },
                onMembersClick = { showMembersDialog = true },
                onSettingsClick = { showSettingsMenu = true },
                isCurrentUserCreator = groupInfo?.createdBy == currentUserId
            )
        },
        bottomBar = {
            ModernChatInputBar(
                text = textState,
                onTextChange = { textState = it },
                onSend = {
                    if (textState.isNotBlank()) {
                        viewModel.sendGroupMessage(groupId, currentUserId, currentUserName, textState)
                        textState = ""
                        coroutineScope.launch {
                            delay(100)
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    }
                },
                onAttachmentClick = { showAttachmentOptions = true },
                isUploading = isUploading,
                uploadProgress = uploadProgress
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFFBBDEFB)
                        )
                    )
                )
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color(0xFF1976D2),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading messages...",
                            color = Color(0xFF1976D2),
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF90CAF9)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No messages yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start the conversation!",
                            fontSize = 14.sp,
                            color = Color(0xFF64B5F6)
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(
                        items = messages,
                        key = { it.messageId }
                    ) { message ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { 30 }),
                            modifier = Modifier.animateItem(
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        ) {
                            Column {
                                ModernGroupMessageItem(
                                    message = message,
                                    isMe = message.senderId == currentUserId,
                                    groupInfo = groupInfo,
                                    onMessageLongPress = {
                                        viewModel.deleteMessage(groupId, message.messageId)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Settings menu
    if (showSettingsMenu) {
        GroupSettingsMenu(
            onDismiss = { showSettingsMenu = false },
            onLeaveGroup = {
                showSettingsMenu = false
                showLeaveGroupDialog = true
            },
            onEditMembers = {
                showSettingsMenu = false
                showEditMembersDialog = true
            },
            isCreator = groupInfo?.createdBy == currentUserId
        )
    }

    // Leave group confirmation dialog
    if (showLeaveGroupDialog) {
        LeaveGroupDialog(
            onDismiss = { showLeaveGroupDialog = false },
            onConfirmLeave = {
                showLeaveGroupDialog = false
                viewModel.leaveGroup(groupId, currentUserId)
            },
            isCreator = groupInfo?.createdBy == currentUserId
        )
    }

    // Edit members dialog
    if (showEditMembersDialog && groupInfo != null) {
        EditMembersDialog(
            groupInfo = groupInfo!!,
            currentUserId = currentUserId,
            onDismiss = { showEditMembersDialog = false },
            onRemoveMember = { memberId ->
                // Implement remove member logic
                Toast.makeText(context, "Remove member: $memberId", Toast.LENGTH_SHORT).show()
            },
            onAddMember = {
                // Implement add member logic
                Toast.makeText(context, "Add member clicked", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Other dialogs
    if (showGroupInfoDialog && groupInfo != null) {
        ModernGroupInfoDialog(
            groupInfo = groupInfo!!,
            onDismiss = { showGroupInfoDialog = false }
        )
    }

    if (showMembersDialog && groupInfo != null) {
        ModernGroupMembersDialog(
            groupInfo = groupInfo!!,
            currentUserId = currentUserId,
            onDismiss = { showMembersDialog = false }
        )
    }

    if (showAttachmentOptions) {
        ModernAttachmentPopup(
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
fun ModernGroupChatTopBar(
    groupName: String,
    groupImage: String,
    memberCount: Int,
    onBackPressed: () -> Unit,
    onGroupInfoClick: () -> Unit,
    onMembersClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isCurrentUserCreator: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .shadow(6.dp),
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFE3F2FD), CircleShape)
                    .clickable { onBackPressed() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Group Image
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF1976D2))
                    .clickable { onGroupInfoClick() }
            ) {
                if (groupImage.isNotEmpty()) {
                    AsyncImage(
                        model = groupImage,
                        contentDescription = "Group Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = groupName.firstOrNull()?.uppercase() ?: "G",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Group Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onGroupInfoClick() }
            ) {
                Text(
                    text = groupName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF64B5F6)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$memberCount members",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                    if (isCurrentUserCreator) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFA726), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Admin",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onMembersClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Members",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Settings",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GroupSettingsMenu(
    onDismiss: () -> Unit,
    onLeaveGroup: () -> Unit,
    onEditMembers: () -> Unit,
    isCreator: Boolean
) {
    val density = LocalDensity.current

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(
            x = with(density) { (-16).dp.roundToPx() },
            y = with(density) { 80.dp.roundToPx() }
        ),
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(initialScale = 0.8f) + slideInVertically { -20 },
            exit = fadeOut() + scaleOut() + slideOutVertically { -20 }
        ) {
            Card(
                modifier = Modifier
                    .width(220.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Edit Members (only for creator)
                    if (isCreator) {
                        SettingsMenuItem(
                            icon = Icons.Default.Edit,
                            text = "Edit Members",
                            iconColor = Color(0xFF1976D2),
                            onClick = onEditMembers
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    // Leave Group
                    SettingsMenuItem(
                        icon = Icons.Default.ExitToApp,
                        text = "Leave Group",
                        iconColor = Color.Red,
                        textColor = Color.Red,
                        onClick = onLeaveGroup
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    iconColor: Color,
    textColor: Color = Color(0xFF212121),
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(20.dp),
                tint = iconColor
            )
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            )
        }
    }
}

@Composable
fun LeaveGroupDialog(
    onDismiss: () -> Unit,
    onConfirmLeave: () -> Unit,
    isCreator: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isCreator) "Cannot Leave Group" else "Leave Group?",
                color = if (isCreator) Color.Red else Color(0xFF212121)
            )
        },
        text = {
            Text(
                text = if (isCreator)
                    "You are the group creator. You cannot leave the group. If you want to leave, you must delete the group or transfer ownership first."
                else "Are you sure you want to leave this group? You won't be able to see messages anymore.",
                fontSize = 14.sp
            )
        },
        confirmButton = {
            if (!isCreator) {
                TextButton(
                    onClick = onConfirmLeave,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Leave")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isCreator) "OK" else "Cancel")
            }
        }
    )
}

@Composable
fun EditMembersDialog(
    groupInfo: GroupChat,
    currentUserId: String,
    onDismiss: () -> Unit,
    onRemoveMember: (String) -> Unit,
    onAddMember: () -> Unit
) {
    var showRemoveConfirmation by remember { mutableStateOf(false) }
    var selectedMemberId by remember { mutableStateOf<String?>(null) }
    var selectedMemberName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage Members",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    IconButton(
                        onClick = onAddMember,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Member",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = "${groupInfo.members.size} members",
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(groupInfo.members.indices.toList()) { index ->
                        val memberId = groupInfo.members[index]
                        val memberName = groupInfo.memberNames.getOrNull(index) ?: "Unknown"
                        val memberPhoto = groupInfo.memberPhotos.getOrNull(index) ?: ""
                        val isCreator = memberId == groupInfo.createdBy
                        val isCurrentUser = memberId == currentUserId

                        EditableMemberItem(
                            memberId = memberId,
                            memberName = memberName,
                            memberPhoto = memberPhoto,
                            isCreator = isCreator,
                            isCurrentUser = isCurrentUser,
                            onRemoveClick = {
                                if (!isCreator && !isCurrentUser) {
                                    selectedMemberId = memberId
                                    selectedMemberName = memberName
                                    showRemoveConfirmation = true
                                }
                            }
                        )
                        if (index < groupInfo.members.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFFE0E0E0)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", fontSize = 16.sp)
                }
            }
        }
    }

    // Remove confirmation dialog
    if (showRemoveConfirmation && selectedMemberId != null) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirmation = false },
            title = { Text("Remove Member") },
            text = { Text("Are you sure you want to remove $selectedMemberName from the group?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveMember(selectedMemberId!!)
                        showRemoveConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EditableMemberItem(
    memberId: String,
    memberName: String,
    memberPhoto: String,
    isCreator: Boolean,
    isCurrentUser: Boolean,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Member photo
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF1976D2))
        ) {
            if (memberPhoto.isNotEmpty()) {
                AsyncImage(
                    model = memberPhoto,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = memberName.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCurrentUser) "$memberName (You)" else memberName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color(0xFF212121)
                )
                if (isCurrentUser) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "You",
                            fontSize = 10.sp,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (isCreator) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Group Admin",
                        fontSize = 12.sp,
                        color = Color(0xFFFFA726),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Remove button (not shown for creator or current user)
        if (!isCreator && !isCurrentUser) {
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// The rest of the composables remain the same as before:
// ModernGroupMessageItem, ModernChatInputBar, ModernGroupInfoDialog,
// ModernGroupMembersDialog, ModernAttachmentPopup, ModernAttachmentOption, formatMessageTimes

// Keep all the existing composables from the original file that are not modified

@Composable
fun ModernGroupMessageItem(
    message: GroupMessage,
    isMe: Boolean,
    groupInfo: GroupChat?,
    onMessageLongPress: () -> Unit
) {
    val alignment = if (isMe) Alignment.End else Alignment.Start

    val senderIndex = groupInfo?.members?.indexOf(message.senderId) ?: -1
    val senderName = if (senderIndex >= 0) {
        groupInfo?.memberNames?.getOrNull(senderIndex) ?: message.senderName
    } else message.senderName

    val senderPhoto = if (senderIndex >= 0) {
        groupInfo?.memberPhotos?.getOrNull(senderIndex) ?: ""
    } else ""

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isMe) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF1976D2))
                ) {
                    if (senderPhoto.isNotEmpty()) {
                        AsyncImage(
                            model = senderPhoto,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = senderName.firstOrNull()?.uppercase() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
            ) {
                if (!isMe) {
                    Text(
                        text = message.senderName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                }

                Card(
                    modifier = Modifier
                        .shadow(3.dp, RoundedCornerShape(18.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(onLongPress = { onMessageLongPress() })
                        },
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isMe) 18.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 18.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMe) Color(0xFF42A5F5) else Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        when (message.messageType) {
                            "text" -> {
                                Text(
                                    text = message.messageText,
                                    color = if (isMe) Color.White else Color(0xFF212121),
                                    fontSize = 15.sp,
                                    lineHeight = 20.sp
                                )
                            }
                            "image" -> {
                                AsyncImage(
                                    model = message.messageText,
                                    contentDescription = "Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            "voice" -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Voice",
                                        tint = if (isMe) Color.White else Color(0xFF1976D2),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Voice message",
                                        fontSize = 14.sp,
                                        color = if (isMe) Color.White else Color(0xFF212121)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = formatMessageTimes(message.timestamp),
                                fontSize = 10.sp,
                                color = if (isMe) Color.White.copy(0.8f) else Color.Gray
                            )

                            if (isMe) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = if (message.isRead) "Read" else "Sent",
                                    tint = if (message.isRead) Color(0xFF00E676) else Color.White.copy(0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isMe) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun ModernChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachmentClick: () -> Unit,
    isUploading: Boolean = false,
    uploadProgress: Double = 0.0
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            if (isUploading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val progressValue = (uploadProgress.toFloat() / 100f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF1976D2),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Uploading ${uploadProgress.toInt()}%",
                        fontSize = 14.sp,
                        color = Color(0xFF1976D2)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF42A5F5), Color(0xFF1976D2))
                            ),
                            shape = CircleShape
                        )
                        .clickable(enabled = !isUploading) { onAttachmentClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Attach",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    TextField(
                        value = text,
                        onValueChange = onTextChange,
                        placeholder = {
                            Text(
                                text = "Type a message...",
                                fontSize = 15.sp,
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(fontSize = 15.sp),
                        maxLines = 4,
                        enabled = !isUploading
                    )
                }

                AnimatedContent(
                    targetState = text.isNotBlank(),
                    transitionSpec = {
                        fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                    },
                    label = "sendButton"
                ) { hasText ->
                    if (hasText) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF42A5F5), Color(0xFF1976D2))
                                    ),
                                    shape = CircleShape
                                )
                                .clickable { onSend() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFE0E0E0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernGroupInfoDialog(groupInfo: GroupChat, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF1976D2))
                ) {
                    if (groupInfo.groupImage.isNotEmpty()) {
                        AsyncImage(
                            model = groupInfo.groupImage,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = groupInfo.groupName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${groupInfo.members.size} members",
                        fontSize = 15.sp,
                        color = Color(0xFF757575)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Created by ${groupInfo.createdByName}",
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ModernGroupMembersDialog(
    groupInfo: GroupChat,
    currentUserId: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Group Members",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "${groupInfo.members.size} members",
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn {
                    items(groupInfo.members.indices.toList()) { index ->
                        ModernMemberItem(
                            memberName = groupInfo.memberNames.getOrNull(index) ?: "Unknown",
                            memberPhoto = groupInfo.memberPhotos.getOrNull(index) ?: "",
                            isCreator = groupInfo.members[index] == groupInfo.createdBy,
                            isCurrentUser = groupInfo.members[index] == currentUserId
                        )
                        if (index < groupInfo.members.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFFE0E0E0)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ModernMemberItem(
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
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF1976D2))
        ) {
            if (memberPhoto.isNotEmpty()) {
                AsyncImage(
                    model = memberPhoto,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = memberName.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCurrentUser) "$memberName (You)" else memberName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color(0xFF212121)
                )
                if (isCurrentUser) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "You",
                            fontSize = 10.sp,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (isCreator) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Group Admin",
                        fontSize = 12.sp,
                        color = Color(0xFFFFA726),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ModernAttachmentPopup(
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    val density = LocalDensity.current

    Popup(
        alignment = Alignment.BottomStart,
        offset = IntOffset(
            x = with(density) { 16.dp.roundToPx() },
            y = with(density) { (-100).dp.roundToPx() }
        ),
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(initialScale = 0.8f) + slideInVertically { 50 },
            exit = fadeOut() + scaleOut() + slideOutVertically { 50 }
        ) {
            Card(
                modifier = Modifier
                    .width(220.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Send Attachment",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    ModernAttachmentOption(
                        icon = Icons.Default.AccountCircle,
                        text = "Gallery",
                        gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
                        onClick = onImageClick
                    )

                    ModernAttachmentOption(
                        icon = Icons.Default.Face,
                        text = "Camera",
                        gradientColors = listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)),
                        onClick = onCameraClick
                    )
                }
            }
        }
    }
}

@Composable
fun ModernAttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.linearGradient(colors = gradientColors),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
            )
        }
    }
}

fun formatMessageTimes(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}