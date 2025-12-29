package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.DarkBlue3
import com.example.rojgar.ui.theme.SkyBlue
import com.example.rojgar.ui.theme.RojgarTheme

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RojgarTheme {
                ChatBody()
            }
        }
    }
}

data class Message(
    val id: String,
    val text: String,
    val isSentByMe: Boolean,
    val timestamp: String = ""
)

@Composable
fun ChatBody() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var messageText by remember { mutableStateOf("") }

    // Sample messages - replace with your actual data
    val messages = remember {
        mutableStateListOf(
            Message(
                id = "1",
                text = "Are you hiring ??",
                isSentByMe = true
            ),
            Message(
                id = "2",
                text = "Yess",
                isSentByMe = false
            )
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFE8F4F8))
        ) {
            // Top Bar
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = "Back",
                            modifier = Modifier
                                .size(30.dp)
                                .clickable { activity?.finish() },
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

                        Text(
                            text = "David",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
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
                                .clickable { /* TODO: Implement call */ },
                            tint = Color.Black
                        )

                        Icon(
                            painter = painterResource(R.drawable.videocall),
                            contentDescription = "Video Call",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { /* TODO: Implement video call */ },
                            tint = Color.Black
                        )

                        Icon(
                            painter = painterResource(R.drawable.infoicon),
                            contentDescription = "Info",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { /* TODO: Show info */ }
                                .padding(4.dp)

                        )
                    }
                }
            }

            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    MessageBubble(message = message)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Message Input Bar
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
                            .clickable { /* TODO: Add attachment */ }
                            .padding(8.dp)

                    )

                    Icon(
                        painter = painterResource(R.drawable.voiceicon),
                        contentDescription = "Voice",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable { /* TODO: Voice message */ }
                            .padding(8.dp)

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
                                onValueChange = { messageText = it },
                                placeholder = {
                                    Text(
                                        text = "Message",
                                        style = TextStyle(
                                            fontSize = 18.sp,
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
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isSentByMe) {
            Image(
                painter = painterResource(R.drawable.picture),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isSentByMe) SkyBlue else Color(0xFFB3E5FC)
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                style = TextStyle(
                    fontSize = 18.sp,
                    color = Color.Black
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        if (message.isSentByMe) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Preview
@Composable
fun ChatPreview() {
    RojgarTheme {
        ChatBody()
    }
}