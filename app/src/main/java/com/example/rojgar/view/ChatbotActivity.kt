package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rojgar.R
import com.example.rojgar.model.ChatbotMessage
import com.example.rojgar.repository.ChatbotRepositoryImpl
import com.example.rojgar.viewmodel.ChatbotViewModel
import kotlinx.coroutines.launch

// Color scheme matching MessageActivity
private val LightBlue50 = Color(0xFFE3F2FD)
private val LightBlue100 = Color(0xFFBBDEFB)
private val LightBlue300 = Color(0xFF64B5F6)
private val LightBlue400 = Color(0xFF42A5F5)
private val LightBlue500 = Color(0xFF2196F3)
private val LightBlue600 = Color(0xFF1E88E5)
private val LightBlue700 = Color(0xFF1976D2)
private val AccentCyan = Color(0xFF00BCD4)
private val SoftWhite = Color(0xFFFAFBFF)
private val UserMessageBlue = Color(0xFF2196F3)
private val AiMessageGray = Color(0xFFF5F5F5)

class ChatbotActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatbotScreen()
        }
    }
}

// ViewModelFactory
class ChatbotViewModelFactory(
    private val apiKey: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatbotViewModel::class.java)) {
            return ChatbotViewModel(ChatbotRepositoryImpl(apiKey)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    viewModel: ChatbotViewModel = viewModel(
        factory = ChatbotViewModelFactory(
            apiKey = "AIzaSyDBAm6-5aEGq0z-AYivV3mQj2Ziq4BRhSk"
        )
    )
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LightBlue50,
                        SoftWhite,
                        LightBlue100.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                ChatInputBar(
                    messageInput = messageInput,
                    onMessageChange = { messageInput = it },
                    onSendClick = {
                        if (messageInput.isNotBlank()) {
                            viewModel.sendMessage(messageInput)
                            messageInput = ""
                        }
                    },
                    isLoading = state.isLoading
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ChatbotTopBar(
                    onBackClick = {
                        (context as? ComponentActivity)?.finish()
                    },
                    onClearClick = { viewModel.clearChat() }
                )
                // Error display
                state.error?.let { error ->
                    ErrorBanner(
                        message = error,
                        onDismiss = { viewModel.clearError() }
                    )
                }

                // Messages list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(
                        items = state.messages,
                        key = { it.id }
                    ) { message ->
                        ChatMessageBubble(message = message)
                    }

                    // Loading indicator
                    if (state.isLoading) {
                        item {
                            TypingIndicators()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatbotTopBar(
    onBackClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(LightBlue400, LightBlue600)
                            )
                        )
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "AI Assistant",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlue700
                        )
                    )
                    Text(
                        text = "Ask me anything...",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = AccentCyan,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Clear chat button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LightBlue50)
                    .clickable { onClearClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_delete),
                    contentDescription = "Clear chat",
                    modifier = Modifier.size(20.dp),
                    tint = LightBlue600
                )
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatbotMessage) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AccentCyan, LightBlue500)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Card(
                modifier = Modifier.widthIn(max = 280.dp),
                shape = RoundedCornerShape(
                    topStart = if (message.isUser) 16.dp else 4.dp,
                    topEnd = if (message.isUser) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isUser) UserMessageBlue else AiMessageGray
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(12.dp),
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = if (message.isUser) Color.White else Color.Black,
                        lineHeight = 20.sp
                    )
                )
            }

            if (message.isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(LightBlue400, LightBlue600)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_myplaces),
                        contentDescription = "User",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun TypingIndicators() {
    Row(
        modifier = Modifier.padding(start = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AiMessageGray),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(3) { index ->
                    val infiniteTransition = rememberInfiniteTransition(label = "dot_$index")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 600,
                                delayMillis = index * 200,
                                easing = FastOutSlowInEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale_$index"
                    )

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(LightBlue400)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    messageInput: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageInput,
                onValueChange = onMessageChange,
                placeholder = {
                    Text(
                        text = "Ask me anything...",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = Color.Gray.copy(alpha = 0.6f)
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = LightBlue700,
                    unfocusedTextColor = Color.Black
                ),
                textStyle = TextStyle(fontSize = 15.sp),
                maxLines = 4,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (messageInput.isNotBlank() && !isLoading)
                            Brush.linearGradient(
                                colors = listOf(LightBlue500, LightBlue700)
                            )
                        else Brush.linearGradient(
                            colors = listOf(
                                Color.Gray.copy(alpha = 0.3f),
                                Color.Gray.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .clickable(
                        enabled = messageInput.isNotBlank() && !isLoading,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSendClick() },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_send),
                        contentDescription = "Send",
                        modifier = Modifier.size(24.dp),
                        tint = if (messageInput.isNotBlank()) Color.White else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_dialog_alert),
                contentDescription = "Error",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFFD32F2F)
                )
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                    contentDescription = "Dismiss",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}