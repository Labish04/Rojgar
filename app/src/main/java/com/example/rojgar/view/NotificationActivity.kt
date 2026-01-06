package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.rojgar.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rojgar.model.NotificationModel
import com.example.rojgar.model.NotificationType
import com.example.rojgar.model.UserType
import com.example.rojgar.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get user type from intent extras
        val userTypeString = intent.getStringExtra("USER_TYPE") ?: "JOBSEEKER"
        val userType = try {
            UserType.valueOf(userTypeString)
        } catch (e: IllegalArgumentException) {
            UserType.JOBSEEKER
        }

        setContent {
            NotificationTheme {
                NotificationBody(userType = userType)
            }
        }
    }
}

@Composable
fun NotificationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF4A90E2),
            primaryContainer = Color(0xFFE3F2FD),
            secondary = Color(0xFF64B5F6),
            secondaryContainer = Color(0xFFBBDEFB),
            background = Color(0xFFF5F9FF),
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF1A1A1A),
            onSurface = Color(0xFF1A1A1A),
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBody(
    userType: UserType = UserType.JOBSEEKER,
    viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(userType)
    )
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Notifications",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (unreadCount > 0) {
                                Text(
                                    "$unreadCount unread",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "•",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                            Text(
                                text = when (userType) {
                                    UserType.JOBSEEKER -> "Jobseeker"
                                    UserType.COMPANY -> "Company"
                                    UserType.ALL -> "All"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mark all as read") },
                                onClick = {
                                    viewModel.markAllAsRead()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Star, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear all") },
                                onClick = {
                                    showDeleteDialog = true
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                notifications.isEmpty() -> {
                    EmptyNotificationState(userType)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = notifications,
                            key = { it.id }
                        ) { notification ->
                            NotificationItem(
                                notification = notification,
                                onMarkAsRead = { viewModel.markAsRead(it) },
                                onMarkAsUnread = { viewModel.markAsUnread(it) },
                                onDelete = { viewModel.deleteNotification(it) }
                            )
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Clear All Notifications?") },
                text = { Text("This will permanently delete all your notifications. This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearAllNotifications()
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Clear ", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationItem(
    notification: NotificationModel,
    onMarkAsRead: (String) -> Unit,
    onMarkAsUnread: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(notification.id)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Black
                )
            }
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (notification.isRead)
                    Color.White
                else
                    Color(0xFFE8F4F8)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (notification.isRead) 2.dp else 4.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon based on type
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(getNotificationColor(notification.type).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = getNotificationIcon(notification.type),
                        contentDescription = null,
                        tint = getNotificationColor(notification.type),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (!notification.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTimestamp(notification.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )

                        Text(
                            text = notification.type.name.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = getNotificationColor(notification.type)
                        )
                    }
                }

                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(if (notification.isRead) "Mark as unread" else "Mark as read")
                        },
                        onClick = {
                            if (notification.isRead) {
                                onMarkAsUnread(notification.id)
                            } else {
                                onMarkAsRead(notification.id)
                            }
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                if (notification.isRead) Icons.Default.Face
                                else Icons.Default.DateRange,
                                null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete(notification.id)
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationState(userType: UserType) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No notifications yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        val emptyMessage = when (userType) {
            UserType.JOBSEEKER -> "When you receive job alerts, application updates, or messages, they'll appear here"
            UserType.COMPANY -> "When you receive candidate applications, messages, or system updates, they'll appear here"
            UserType.ALL -> "When you get notifications, they'll show up here"
        }

        Text(
            text = emptyMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun getNotificationIcon(type: NotificationType): Painter {
    return when (type) {
        NotificationType.JOB_ALERT ->
            painterResource(R.drawable.outline_work_24)
        NotificationType.MESSAGE ->
            painterResource(R.drawable.outline_business_messages_24)
        NotificationType.SYSTEM ->
            painterResource(R.drawable.settings)
        NotificationType.GENERAL ->
            painterResource(R.drawable.outline_notifications_24)
        NotificationType.APPLICATION_UPDATE ->
            painterResource(R.drawable.outline_work_24)
        NotificationType.CANDIDATE_ALERT ->
            painterResource(R.drawable.outline_work_24)
        NotificationType.INTERVIEW_SCHEDULED ->
            painterResource(R.drawable.outline_notifications_24)
        NotificationType.PROFILE_UPDATE ->
            painterResource(R.drawable.outline_notifications_24)
    }
}

fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.JOB_ALERT -> Color(0xFF4CAF50)
        NotificationType.MESSAGE -> Color(0xFF2196F3)
        NotificationType.SYSTEM -> Color(0xFFFF9800)
        NotificationType.GENERAL -> Color(0xFF9C27B0)
        NotificationType.APPLICATION_UPDATE -> Color(0xFF00BCD4)
        NotificationType.CANDIDATE_ALERT -> Color(0xFFE91E63)
        NotificationType.INTERVIEW_SCHEDULED -> Color(0xFF673AB7)
        NotificationType.PROFILE_UPDATE -> Color(0xFF795548)
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

// ViewModelFactory to pass userType to ViewModel
class NotificationViewModelFactory(
    private val userType: UserType
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(userType = userType) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}