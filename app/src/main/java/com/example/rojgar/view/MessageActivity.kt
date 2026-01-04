package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rojgar.viewmodel.ChatViewModel
import com.example.rojgar.viewmodel.ChatViewModelFactory
import com.example.rojgar.repository.ChatRepositoryImpl
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.model.ChatRoom
import com.example.rojgar.ui.theme.Blue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessageBody(
                currentUserId = "1",
                currentUserName = "User"
            )
        }
    }
}

@Composable
fun MessageBody(
    currentUserId: String,
    currentUserName: String,
    chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(ChatRepositoryImpl()))
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val chatRooms by chatViewModel.chatRooms.observeAsState(emptyList())
    val loading by chatViewModel.loading.observeAsState(false)

    // Load chat rooms on start
    LaunchedEffect(currentUserId) {
        chatViewModel.loadChatRooms(currentUserId)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Blue)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                (context as? ComponentActivity)?.finish()
                            }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Messages",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.editmessage),
                    contentDescription = "New Message",
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
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
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search...",
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
                        textStyle = TextStyle(fontSize = 16.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chat List
            if (loading && chatRooms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (chatRooms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No messages yet",
                        color = Color.Gray
                    )
                }
            } else {
                val filteredChatRooms = if (searchQuery.isNotEmpty()) {
                    chatRooms.filter { chatRoom ->
                        val otherParticipantName = if (chatRoom.participant1Id == currentUserId) {
                            chatRoom.participant2Name
                        } else {
                            chatRoom.participant1Name
                        }
                        otherParticipantName.contains(searchQuery, ignoreCase = true) ||
                                chatRoom.lastMessage.contains(searchQuery, ignoreCase = true)
                    }
                } else {
                    chatRooms
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(filteredChatRooms) { chatRoom ->
                        val otherParticipantId = if (chatRoom.participant1Id == currentUserId) {
                            chatRoom.participant2Id
                        } else {
                            chatRoom.participant1Id
                        }

                        val otherParticipantName = if (chatRoom.participant1Id == currentUserId) {
                            chatRoom.participant2Name
                        } else {
                            chatRoom.participant1Name
                        }

                        ChatUserItem(
                            chatRoom = chatRoom,
                            currentUserId = currentUserId,
                            onClick = {
                                val intent = Intent(context, ChatActivity::class.java).apply {
                                    putExtra("receiverId", otherParticipantId)
                                    putExtra("receiverName", otherParticipantName)
                                    putExtra("currentUserId", currentUserId)
                                    putExtra("currentUserName", currentUserName)
                                }
                                context.startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatUserItem(
    chatRoom: ChatRoom,
    currentUserId: String,
    onClick: () -> Unit
) {
    val otherParticipantName = if (chatRoom.participant1Id == currentUserId) {
        chatRoom.participant2Name
    } else {
        chatRoom.participant1Name
    }

    val lastMessageTime = formatRelativeTime(chatRoom.lastMessageTime)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Image(
                painter = painterResource(R.drawable.sampleimage),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // User Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = otherParticipantName,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    )

                    Text(
                        text = lastMessageTime,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chatRoom.lastMessage,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.Gray
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (chatRoom.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.Red),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (chatRoom.unreadCount > 99) "99+"
                                else chatRoom.unreadCount.toString(),
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> {
            val date = Date(timestamp)
            val format = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
            format.format(date)
        }
    }
}

@Preview
@Composable
fun MessagePreview() {
        MessageBody(
            currentUserId = "1",
            currentUserName = "User"
        )
}