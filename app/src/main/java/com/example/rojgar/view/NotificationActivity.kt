package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.ui.theme.RojgarTheme
import java.text.SimpleDateFormat
import java.util.*

// Notification Data Model
data class Notification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.INFO
)

enum class NotificationType {
    INFO, JOB_ALERT, MESSAGE, IMPORTANT
}

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RojgarTheme {
                NotificationScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {
    val context = LocalContext.current

    // Sample notifications - Replace with actual data from your backend
    var notifications by remember {
        mutableStateOf(
            listOf(
                Notification(
                    title = "New Job Match",
                    message = "A new job matching your profile has been posted: Software Developer at Tech Corp",
                    type = NotificationType.JOB_ALERT
                ),
                Notification(
                    title = "Application Update",
                    message = "Your application for Data Analyst position has been viewed by the employer",
                    type = NotificationType.IMPORTANT
                ),
                Notification(
                    title = "New Message",
                    message = "You have received a message from ABC Company regarding your application",
                    type = NotificationType.MESSAGE,
                    isRead = true
                ),
                Notification(
                    title = "Profile Views",
                    message = "Your profile has been viewed 15 times this week",
                    type = NotificationType.INFO,
                    isRead = true
                ),
                Notification(
                    title = "Interview Scheduled",
                    message = "Interview scheduled for Marketing Manager position on Dec 25, 2025 at 10:00 AM",
                    type = NotificationType.IMPORTANT
                )
            )
        )
    }

    var showNotificationScreen by remember { mutableStateOf(false) }

    data class NavItem(
        val label: String,
        val selectedIcon: Int,
        val unselectedIcon: Int
    )

    var selectedIndex by remember { mutableStateOf(0) }

    val listItem = listOf(
        NavItem(
            label = "Home",
            selectedIcon = R.drawable.home_filled,
            unselectedIcon = R.drawable.home
        ),
        NavItem(
            label = "Message",
            selectedIcon = R.drawable.chat_filled,
            unselectedIcon = R.drawable.chat
        ),
        NavItem(
            label = "Post",
            selectedIcon = R.drawable.jobpost_filled,
            unselectedIcon = R.drawable.jobpost
        ),
        NavItem(
            label = "Map",
            selectedIcon = R.drawable.map_filled,
            unselectedIcon = R.drawable.map
        )
    )

    val unreadCount = notifications.count { !it.isRead }

    if (showNotificationScreen) {
        NotificationScreen(
            notifications = notifications,
            onBack = { showNotificationScreen = false },
            onMarkAsRead = { notification ->
                notifications = notifications.map {
                    if (it.id == notification.id) it.copy(isRead = true) else it
                }
            },
            onDelete = { notification ->
                notifications = notifications.filter { it.id != notification.id }
            },
            onMarkAllAsRead = {
                notifications = notifications.map { it.copy(isRead = true) }
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Rojgar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    actions = {
                        // Notification Bell with Badge
                        Box {
                            IconButton(
                                onClick = {
                                    showNotificationScreen = true
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_notifications_24),
                                    contentDescription = "Notifications",
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Notification Badge
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(top = 8.dp, end = 8.dp)
                                        .background(Color.Red, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 10.dp
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent
                    ) {
                        listItem.forEachIndexed { index, item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(
                                            if (selectedIndex == index) item.selectedIcon else item.unselectedIcon
                                        ),
                                        contentDescription = item.label,
                                        modifier = Modifier.size(25.dp)
                                    )
                                },
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (selectedIndex) {
                    0 -> Text("Home Screen", modifier = Modifier.padding(16.dp))
                    1 -> Text("Message Screen", modifier = Modifier.padding(16.dp))
                    2 -> Text("Post Screen", modifier = Modifier.padding(16.dp))
                    3 -> Text("Map Screen", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    notifications: List<Notification>,
    onBack: () -> Unit,
    onMarkAsRead: (Notification) -> Unit,
    onDelete: (Notification) -> Unit,
    onMarkAllAsRead: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = onMarkAllAsRead) {
                            Text("Mark all read")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No notifications",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You're all caught up!",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationItem(
                        notification = notification,
                        onMarkAsRead = { onMarkAsRead(notification) },
                        onDelete = { onDelete(notification) }
                    )
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = if (notification.isRead) {
        Color.White
    } else {
        Color(0xFFF0F8FF) // Light blue for unread
    }

    var showDeleteButton by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!notification.isRead) {
                    onMarkAsRead()
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Notification type indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.Top)
                    .background(
                        color = when (notification.type) {
                            NotificationType.IMPORTANT -> Color.Red
                            NotificationType.JOB_ALERT -> Color(0xFF4CAF50)
                            NotificationType.MESSAGE -> Color(0xFF2196F3)
                            NotificationType.INFO -> Color.Gray
                        },
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Notification content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = formatTimestamp(notification.timestamp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!notification.isRead) {
                        OutlinedButton(
                            onClick = onMarkAsRead,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text("Mark as read", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

@Preview(showSystemUi = true)
@Composable
fun NotificationPreview() {
    RojgarTheme {
        NotificationScreen()
    }
}
