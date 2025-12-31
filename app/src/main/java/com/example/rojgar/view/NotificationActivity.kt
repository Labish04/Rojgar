package com.example.rojgar.view
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.view.ui.theme.RojgarTheme
import java.util.UUID

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RojgarTheme {
                NotificationHomeScreen()
            }
        }
    }
}

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val time: String,
    var isRead: Boolean = false
)

object NotificationStore {
    val notifications = mutableStateListOf(
        AppNotification(
            title = "New Job Alert",
            message = "A new job matches your profile",
            time = "Just now"
        ),
        AppNotification(
            title = "Profile Updated",
            message = "Your profile was successfully updated",
            time = "1 hour ago"
        ),
        AppNotification(
            title = "Application Sent",
            message = "Your application has been submitted",
            time = "Yesterday"
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHomeScreen() {
    var showNotifications by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                actions = {
                    NotificationBell {
                        showNotifications = !showNotifications
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showNotifications) {
            NotificationList(
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Tap the bell icon to view notifications")
            }
        }
    }
}

@Composable
fun NotificationBell(onClick: () -> Unit) {
    val unreadCount =
        NotificationStore.notifications.count { !it.isRead }

    Box {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notification Bell"
            )
        }

        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Red, CircleShape)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unreadCount.toString(),
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun NotificationList(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(NotificationStore.notifications, key = { it.id }) { notification ->
            NotificationItem(notification)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun NotificationItem(notification: AppNotification) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                if (notification.isRead) Color(0xFFF3F3F3)
                else Color(0xFFE3F2FD)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = notification.title,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = notification.message,
                fontSize = 14.sp
            )

            Text(
                text = notification.time,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                if (!notification.isRead) {
                    TextButton(onClick = {
                        notification.isRead = true
                    }) {
                        Text("Mark as Read")
                    }
                }

                TextButton(onClick = {
                    NotificationStore.notifications.remove(notification)
                }) {
                    Text("Delete", color = Color.Red)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationPreview() {
    RojgarTheme {
        NotificationHomeScreen()
    }
}
