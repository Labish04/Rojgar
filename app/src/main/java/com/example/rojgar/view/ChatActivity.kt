package com.example.rojgar.view

import android.Manifest
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rojgar.R
import com.example.rojgar.model.ChatMessage
import com.example.rojgar.repository.ChatRepositoryImpl
import com.example.rojgar.ui.theme.DarkBlue3
import com.example.rojgar.ui.theme.SkyBlue
import com.example.rojgar.utils.VoicePlayer
import com.example.rojgar.utils.VoiceRecorder
import com.example.rojgar.viewmodel.ChatViewModel
import com.example.rojgar.viewmodel.ChatViewModelFactory
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : ComponentActivity() {
    private val chatViewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(ChatRepositoryImpl())
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Microphone permission is required for voice messages", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val receiverId = intent.getStringExtra("receiverId") ?: ""
        val receiverName = intent.getStringExtra("receiverName") ?: "David"
        val currentUserId = intent.getStringExtra("currentUserId") ?: ""
        val currentUserName = intent.getStringExtra("currentUserName") ?: ""

        chatViewModel.getOrCreateChatRoom(
            participant1Id = currentUserId,
            participant2Id = receiverId,
            participant1Name = currentUserName,
            participant2Name = receiverName
        )

        setContent {
            ChatBody(
                receiverName = receiverName,
                receiverId = receiverId,
                currentUserId = currentUserId,
                currentUserName = currentUserName,
                chatViewModel = chatViewModel,
                onRequestPermission = { checkAndRequestPermission() }
            )
        }
    }

    private fun checkAndRequestPermission(): Boolean {
        return when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> true
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBody(
    receiverName: String,
    receiverId: String,
    currentUserId: String,
    currentUserName: String,
    chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(ChatRepositoryImpl())),
    onRequestPermission: () -> Boolean
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val keyboardController = LocalSoftwareKeyboardController.current

    var messageText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0L) }
    val voiceRecorder = remember { VoiceRecorder(context) }
    val voicePlayer = remember { VoicePlayer(context) }

    val coroutineScope = rememberCoroutineScope()

    val chatRoom by chatViewModel.currentChatRoom.observeAsState()
    val messages by chatViewModel.messages.observeAsState(emptyList())
    val typingStatus by chatViewModel.isTyping.observeAsState()
    val loading by chatViewModel.loading.observeAsState(false)
    val error by chatViewModel.error.observeAsState()

    val listState = rememberLazyListState()
    var typingJob by remember { mutableStateOf<Job?>(null) }
    var recordingJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            voiceRecorder.release()
            voicePlayer.release()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            Log.e("ChatActivity", "Error: $it")
            chatViewModel.clearError()
        }
    }

    LaunchedEffect(chatRoom) {
        chatRoom?.chatId?.let { chatId ->
            chatViewModel.loadMessages(chatId)
            chatViewModel.listenForNewMessages(chatId)
            chatViewModel.listenForTypingStatus(chatId)
            chatViewModel.markMessagesAsRead(chatId, currentUserId)
        }
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val isReceiverTyping = remember(typingStatus, receiverId) {
        typingStatus?.let { (userId, typing) ->
            userId == receiverId && typing
        } ?: false
    }

    LaunchedEffect(messageText) {
        typingJob?.cancel()
        if (messageText.isNotEmpty()) {
            typingJob = coroutineScope.launch {
                chatRoom?.chatId?.let {
                    chatViewModel.setTypingStatus(it, currentUserId, true)
                    delay(2000)
                    chatViewModel.setTypingStatus(it, currentUserId, false)
                }
            }
        } else {
            chatRoom?.chatId?.let { chatViewModel.setTypingStatus(it, currentUserId, false) }
        }
    }

    fun uploadVoiceMessage(audioFile: File) {
        val storageRef = FirebaseStorage.getInstance().reference
        val audioRef = storageRef.child("voice_messages/${UUID.randomUUID()}.m4a")
        val uri = Uri.fromFile(audioFile)

        audioRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                audioRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    chatRoom?.let { cr ->
                        val duration = getDurationFromFile(audioFile)
                        chatViewModel.sendMessage(
                            chatId = cr.chatId,
                            senderId = currentUserId,
                            receiverId = receiverId,
                            senderName = currentUserName,
                            receiverName = receiverName,
                            messageText = "Voice message (${formatDuration(duration)})",
                            messageType = "voice",
                            mediaUrl = downloadUri.toString()
                        )
                    }
                    audioFile.delete()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to upload voice message: ${exception.message}", Toast.LENGTH_SHORT).show()
                audioFile.delete()
            }
    }

    Scaffold(
        topBar = {
            ChatTopAppBar(
                receiverName = receiverName,
                isReceiverTyping = isReceiverTyping,
                onBackClick = { activity?.finish() },
                onCallClick = { /* TODO */ },
                onVideoCallClick = { /* TODO */ },
                onInfoClick = { /* TODO */ }
            )
        },
        bottomBar = {
            if (isRecording) {
                RecordingBar(
                    duration = recordingDuration,
                    onCancelClick = {
                        recordingJob?.cancel()
                        voiceRecorder.cancelRecording()
                        isRecording = false
                        recordingDuration = 0L
                    },
                    onSendClick = {
                        recordingJob?.cancel()
                        val audioFile = voiceRecorder.stopRecording()
                        isRecording = false
                        recordingDuration = 0L

                        audioFile?.let { uploadVoiceMessage(it) }
                            ?: Toast.makeText(context, "Failed to save recording", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                MessageInputBar(
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSendClick = {
                        if (messageText.isNotBlank()) {
                            chatRoom?.let { cr ->
                                chatViewModel.sendMessage(
                                    chatId = cr.chatId,
                                    senderId = currentUserId,
                                    receiverId = receiverId,
                                    senderName = currentUserName,
                                    receiverName = receiverName,
                                    messageText = messageText
                                )
                                messageText = ""
                                keyboardController?.hide()
                            }
                        }
                    },
                    onAddClick = { /* TODO */ },
                    onVoiceClick = {
                        if (onRequestPermission()) {
                            val audioFile = voiceRecorder.startRecording { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }

                            if (audioFile != null) {
                                isRecording = true
                                recordingJob = coroutineScope.launch {
                                    while (isRecording) {
                                        delay(1000)
                                        recordingDuration += 1000
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFE8F4F8))
        ) {
            if (loading && messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    state = listState,
                    reverseLayout = false,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    itemsIndexed(messages, key = { _, message -> message.messageId }) { index, message ->
                        Column {
                            if (shouldShowDateHeader(messages, index)) {
                                DateHeader(message.timestamp)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (message.messageType == "voice") {
                                VoiceMessageBubble(
                                    message = message,
                                    isSentByMe = message.senderId == currentUserId,
                                    voicePlayer = voicePlayer,
                                    onLongPress = {
                                        showMessageOptions(context, message, chatViewModel)
                                    }
                                )
                            } else {
                                MessageBubble(
                                    message = message,
                                    isSentByMe = message.senderId == currentUserId,
                                    onLongPress = {
                                        showMessageOptions(context, message, chatViewModel)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            if (messages.isNotEmpty()) {
                val isAtBottom by remember {
                    derivedStateOf {
                        listState.firstVisibleItemIndex >= messages.size - 5
                    }
                }

                if (!isAtBottom) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = SkyBlue
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_keyboard_arrow_down_24),
                            contentDescription = "Scroll to bottom",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecordingBar(
    duration: Long,
    onCancelClick: () -> Unit,
    onSendClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkBlue3
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Cancel",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable { onCancelClick() }
                    .padding(8.dp),
                tint = Color.Red
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RecordingAnimation()

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = formatDuration(duration),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable { onSendClick() }
                    .padding(8.dp),
                tint = Color.Black
            )
        }
    }
}

@Composable
fun RecordingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(16.dp)
            .background(Color.Red.copy(alpha = alpha), CircleShape)
    )
}

@Composable
fun VoiceMessageBubble(
    message: ChatMessage,
    isSentByMe: Boolean,
    voicePlayer: VoicePlayer,
    onLongPress: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val alignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isSentByMe) SkyBlue else Color.White

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongPress() })
                },
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isSentByMe) 16.dp else 4.dp,
                bottomEnd = if (isSentByMe) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                voicePlayer.stopAudio()
                                isPlaying = false
                            } else {
                                voicePlayer.playAudio(
                                    audioUrl = message.mediaUrl,
                                    onCompletion = {
                                        isPlaying = false
                                        currentPosition = 0
                                    },
                                    onError = { isPlaying = false }
                                )
                                isPlaying = true

                                coroutineScope.launch {
                                    duration = voicePlayer.getDuration()
                                    while (isPlaying) {
                                        currentPosition = voicePlayer.getCurrentPosition()
                                        delay(100)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        LinearProgressIndicator(
                            progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (isSentByMe) Color.Black else SkyBlue
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isPlaying) formatDuration(currentPosition.toLong()) else message.messageText,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )

                    if (isSentByMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Read status",
                            modifier = Modifier.size(12.dp),
                            tint = if (message.isRead) Color.Blue else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatTopAppBar(
    receiverName: String,
    isReceiverTyping: Boolean,
    onBackClick: () -> Unit,
    onCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBlue3)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { onBackClick() },
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(12.dp))
                Image(
                    painter = painterResource(R.drawable.sampleimage),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = receiverName,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                    if (isReceiverTyping) {
                        Text(
                            text = "typing...",
                            style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.call),
                    contentDescription = "Call",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onCallClick() },
                    tint = Color.Black
                )
                Icon(
                    painter = painterResource(R.drawable.videocall),
                    contentDescription = "Video Call",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onVideoCallClick() },
                    tint = Color.Black
                )
                Icon(
                    painter = painterResource(R.drawable.infoicon),
                    contentDescription = "Info",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onInfoClick() }
                        .padding(4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAddClick: () -> Unit,
    onVoiceClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBlue3)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.addicon),
                contentDescription = "Add",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable { onAddClick() }
                    .padding(8.dp),
                tint = Color.Black
            )
            Icon(
                painter = painterResource(R.drawable.voiceicon),
                contentDescription = "Voice",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable { onVoiceClick() }
                    .padding(8.dp),
                tint = Color.Black
            )
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = onMessageChange,
                        placeholder = {
                            Text(
                                text = "Type a message...",
                                style = TextStyle(fontSize = 16.sp, color = Color.Gray)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                        singleLine = false,
                        maxLines = 3
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable(enabled = messageText.isNotBlank()) { onSendClick() }
                    .padding(8.dp),
                tint = if (messageText.isNotBlank()) Color.Black else Color.Gray
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isSentByMe: Boolean,
    onLongPress: () -> Unit
) {
    val alignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isSentByMe) SkyBlue else Color.White
    val textColor = if (isSentByMe) Color.Black else Color.Black

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clickable { }
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongPress() })
                },
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isSentByMe) 16.dp else 4.dp,
                bottomEnd = if (isSentByMe) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.messageText,
                    color = textColor,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    if (isSentByMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Read status",
                            modifier = Modifier.size(12.dp),
                            tint = if (message.isRead) Color.Blue else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateHeader(timestamp: Long) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.LightGray.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = formatDates(timestamp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}

fun formatMessageTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return format.format(date)
}

fun formatDates(timestamp: Long): String {
    val date = Date(timestamp)
    val today = Calendar.getInstance()
    val messageDate = Calendar.getInstance().apply { time = date }

    return when {
        today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR) -> "Today"

        today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) - 1 == messageDate.get(Calendar.DAY_OF_YEAR) -> "Yesterday"

        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    }
}

// Continuation of ChatActivity.kt - Add these functions at the end

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun shouldShowDateHeader(messages: List<ChatMessage>, index: Int): Boolean {
    if (index == 0) return true

    val currentMessage = messages[index]
    val previousMessage = messages[index - 1]

    val currentDate = Calendar.getInstance().apply {
        timeInMillis = currentMessage.timestamp
    }
    val previousDate = Calendar.getInstance().apply {
        timeInMillis = previousMessage.timestamp
    }

    return currentDate.get(Calendar.DAY_OF_YEAR) != previousDate.get(Calendar.DAY_OF_YEAR) ||
            currentDate.get(Calendar.YEAR) != previousDate.get(Calendar.YEAR)
}

fun getDurationFromFile(file: File): Long {
    return try {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(file.absolutePath)
        mediaPlayer.prepare()
        val duration = mediaPlayer.duration.toLong()
        mediaPlayer.release()
        duration
    } catch (e: Exception) {
        0L
    }
}

fun showMessageOptions(context: Context, message: ChatMessage, chatViewModel: ChatViewModel) {
    val options = if (message.messageType == "voice") {
        arrayOf("Delete Message")
    } else {
        arrayOf("Copy Text", "Delete Message")
    }

    AlertDialog.Builder(context)
        .setTitle("Message Options")
        .setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    if (message.messageType == "voice") {
                        // Delete voice message
                        chatViewModel.deleteMessage(message.chatId, message.messageId)
                    } else {
                        // Copy text to clipboard
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("message", message.messageText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                    }
                }
                1 -> {
                    // Delete message (only appears for text messages)
                    chatViewModel.deleteMessage(message.chatId, message.messageId)
                }
            }
            dialog.dismiss()
        }
        .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        .show()
}