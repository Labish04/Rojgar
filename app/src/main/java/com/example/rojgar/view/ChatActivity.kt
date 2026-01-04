package com.example.rojgar.view

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rojgar.R
import com.example.rojgar.model.ChatMessage
import com.example.rojgar.repository.ChatRepositoryImpl
import com.example.rojgar.ui.theme.DarkBlue3
import com.example.rojgar.ui.theme.SkyBlue
import com.example.rojgar.viewmodel.ChatViewModel
import com.example.rojgar.viewmodel.ChatViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatActivity : ComponentActivity() {
    private val chatViewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(ChatRepositoryImpl())
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
                chatViewModel = chatViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBody(
    receiverName: String,
    receiverId: String,
    currentUserId: String,
    chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(ChatRepositoryImpl()))
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val keyboardController = LocalSoftwareKeyboardController.current

    var messageText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val chatRoom by chatViewModel.currentChatRoom.observeAsState()
    val messages by chatViewModel.messages.observeAsState(emptyList())
    val typingStatus by chatViewModel.isTyping.observeAsState()
    val loading by chatViewModel.loading.observeAsState(false)
    val error by chatViewModel.error.observeAsState()

    val listState = rememberLazyListState()

    var typingJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(error) {
        error?.let {
            val errorMessage = it
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            Log.e("ChatActivity", "Error: $errorMessage")
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
                                senderName = "", // You might want to pass the sender's name
                                receiverName = receiverName,
                                messageText = messageText
                            )
                            messageText = ""
                            keyboardController?.hide()
                        }
                    }
                },
                onAddClick = { /* TODO */ },
                onVoiceClick = { /* TODO */ }
            )
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

                            MessageBubble(
                                message = message,
                                isSentByMe = message.senderId == currentUserId,
                                onLongPress = {
                                    // Show message options (delete, copy, etc.)
                                    showMessageOptions(context, message, chatViewModel)
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            // Floating action button to scroll to bottom
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
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
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
        colors = CardDefaults.cardColors(
            containerColor = DarkBlue3
        )
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
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
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
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        singleLine = false,
                        maxLines = 3
                    )
                }
            }

            // Send Button
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable(enabled = messageText.isNotBlank()) {
                        onSendClick()
                    }
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
                    detectTapGestures(
                        onLongPress = { onLongPress() }
                    )
                },
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isSentByMe) 16.dp else 4.dp,
                bottomEnd = if (isSentByMe) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = bubbleColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
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
                            imageVector = if (message.isRead) Icons.Default.Done else Icons.Default.Done,
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
                text = formatDate(timestamp),
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

fun shouldShowDateHeader(messages: List<ChatMessage>, index: Int): Boolean {
    if (index == 0) return true

    val currentMessage = messages[index]
    val previousMessage = messages[index - 1]

    val currentDate = Calendar.getInstance().apply {
        time = Date(currentMessage.timestamp)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val previousDate = Calendar.getInstance().apply {
        time = Date(previousMessage.timestamp)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    return currentDate != previousDate
}

fun showMessageOptions(context: Context, message: ChatMessage, viewModel: ChatViewModel) {
    val options = arrayOf("Copy", "Delete", "Cancel")

    AlertDialog.Builder(context)
        .setTitle("Message Options")
        .setItems(options) { _, which ->
            when (which) {
                0 -> { // Copy
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("message", message.messageText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                }
                1 -> { // Delete
                    AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton("Delete") { _, _ ->
                            viewModel.deleteMessage(message.messageId, message.chatId)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
        .show()
}
