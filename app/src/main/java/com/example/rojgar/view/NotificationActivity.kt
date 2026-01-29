package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rojgar.R
import com.example.rojgar.model.NotificationModel
import com.example.rojgar.model.NotificationType
import com.example.rojgar.model.UserType
import com.example.rojgar.viewmodel.NotificationViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userTypeString = intent.getStringExtra("USER_TYPE") ?: "JOBSEEKER"
        val userType = try {
            UserType.valueOf(userTypeString)
        } catch (e: IllegalArgumentException) {
            UserType.JOBSEEKER
        }

        setContent {
            NotificationScreen(
                userType = userType,
                onNotificationClick = { notification ->
                    handleNotificationClick(notification, userType)
                }
            )
        }
    }

    /**
     * Handle notification click and navigate to appropriate screen
     */
    private fun handleNotificationClick(notification: NotificationModel, userType: UserType) {
        when (notification.type) {
            NotificationType.JOB_ALERT -> {
                // Navigate to job details or job list
                navigateToJobs()
            }
            NotificationType.PROFILE_UPDATE -> {
                // Navigate to profile (follower's profile if possible)
                navigateToProfile(userType)
            }
            NotificationType.MESSAGE -> {
                // Navigate to messages
                navigateToMessages()
            }
            NotificationType.EVENTS -> {
                // Navigate to events/calendar
                navigateToEvents()
            }
            NotificationType.APPLICATION_STATUS -> {
                // Navigate to applications
                navigateToJobs()
            }
            NotificationType.CANDIDATE_ALERT -> {
                // Navigate to candidates list
                navigateToProfile(userType)
            }
            NotificationType.SYSTEM -> {
                // Navigate to settings or verification
                navigateToProfile(userType)
            }
            else -> {
                // Default: go back to dashboard
                navigateBack(userType)
            }
        }
        finish()
    }

    /**
     * Navigate back to appropriate dashboard based on user type
     */
    private fun navigateBack(userType: UserType) {
        val intent = when (userType) {
            UserType.COMPANY -> Intent(this, CompanyDashboardActivity::class.java)
            UserType.JOBSEEKER -> Intent(this, JobSeekerDashboardActivity::class.java)
            else -> Intent(this, JobSeekerDashboardActivity::class.java)
        }

        // Clear all activities on top and bring dashboard to front
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun navigateToJobs() {
        // Navigate to Job Post screen in dashboard
        val intent = Intent(this, JobSeekerDashboardActivity::class.java).apply {
            putExtra("OPEN_TAB", 1) // Index for Jobs/Post tab
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    private fun navigateToProfile(userType: UserType) {
        val intent = when (userType) {
            UserType.COMPANY -> Intent(this, CompanyDashboardActivity::class.java).apply {
                putExtra("OPEN_TAB", 3) // Index for Profile tab
            }
            UserType.JOBSEEKER -> Intent(this, JobSeekerDashboardActivity::class.java).apply {
                putExtra("OPEN_TAB", 3) // Index for Profile tab
            }
            else -> Intent(this, JobSeekerDashboardActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    private fun navigateToMessages() {
        val intent = Intent(this, MessageActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToEvents() {
        // TODO: Create EventsActivity or navigate to events section
        // For now, navigate to dashboard with events tab if you have one
        val intent = Intent(this, JobSeekerDashboardActivity::class.java).apply {
            putExtra("OPEN_TAB", 2) // Update this to your Events tab index
            putExtra("OPEN_EVENTS", true)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }
}

@Composable
fun NotificationScreen(
    userType: UserType = UserType.JOBSEEKER,
    onNotificationClick: (NotificationModel) -> Unit = {},
    viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(userType)
    )
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val loadingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFBBDEFB),
                        Color(0xFF90CAF9)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar - Same as AppliedJobsActivity
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1976D2),
                                Color(0xFF2196F3),
                                Color(0xFF42A5F5)
                            )
                        )
                    )
                    .padding(top = 40.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { (context as? ComponentActivity)?.finish() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notifications",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        AnimatedVisibility(visible = !isLoading) {
                            Text(
                                text = if (notifications.isNotEmpty()) {
                                    "$unreadCount new • ${notifications.size} total"
                                } else {
                                    "No notifications"
                                },
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // Menu Button
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            enabled = notifications.isNotEmpty(),
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(12.dp))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.markasread),
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            "Mark all as read",
                                            color = Color(0xFF1E3A5F),
                                            fontSize = 14.sp
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.markAllAsRead()
                                    showMenu = false
                                }
                            )
                            Divider(color = Color(0xFFE1F5FE))
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color(0xFFEF5350),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            "Clear all",
                                            color = Color(0xFFEF5350),
                                            fontSize = 14.sp
                                        )
                                    }
                                },
                                onClick = {
                                    showDeleteDialog = true
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Content
            when {
                isLoading -> {
                    LoadingState(alpha = loadingAlpha)
                }

                notifications.isEmpty() -> {
                    EmptyNotificationState(userType)
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = notifications,
                            key = { _, item -> item.id }
                        ) { index, notification ->
                            AnimatedNotificationItem(
                                notification = notification,
                                index = index,
                                onClick = {
                                    // Mark as read when clicked
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification.id)
                                    }
                                    // Navigate to appropriate screen
                                    onNotificationClick(notification)
                                },
                                onMarkAsRead = { viewModel.markAsRead(it) },
                                onMarkAsUnread = { viewModel.markAsUnread(it) },
                                onDelete = { viewModel.deleteNotification(it) }
                            )
                        }

                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        // Delete Dialog
        if (showDeleteDialog) {
            ModernDeletionDialog(
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    viewModel.clearAllNotifications()
                    showDeleteDialog = false
                }
            )
        }
    }
}

@Composable
fun AnimatedNotificationItem(
    notification: NotificationModel,
    index: Int,
    onClick: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    onMarkAsUnread: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    // Entry animation
    LaunchedEffect(Unit) {
        delay(index * 50L)
        isVisible = true
    }

    // Scale animation on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press_scale"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(300))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .shadow(
                    elevation = if (notification.isRead) 2.dp else 6.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color(0xFF4FC3F7).copy(alpha = 0.3f)
                )
                .clickable { onClick() }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onLongPress = {
                            showMenu = true
                        }
                    )
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (notification.isRead)
                    Color.White
                else
                    Color(0xFFE1F5FE).copy(alpha = 0.6f)
            )
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Animated Icon
                    ModernNotificationIcon(
                        type = notification.type,
                        isRead = notification.isRead
                    )

                    // Content
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Title with unread indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = notification.title,
                                fontSize = 16.sp,
                                fontWeight = if (notification.isRead)
                                    FontWeight.Medium
                                else
                                    FontWeight.Bold,
                                color = Color(0xFF0D47A1),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            if (!notification.isRead) {
                                PulsingDot()
                            }
                        }

                        // Message
                        Text(
                            text = notification.message,
                            fontSize = 14.sp,
                            color = Color(0xFF546E7A),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp
                        )

                        // Timestamp & Type Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.datetimeicon),
                                    contentDescription = null,
                                    tint = Color(0xFF90CAF9),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = formatTimestamp(notification.timestamp),
                                    fontSize = 12.sp,
                                    color = Color(0xFF90CAF9),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = getNotificationColor(notification.type).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = notification.type.name
                                        .replace("_", " ")
                                        .lowercase()
                                        .split(" ")
                                        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    color = getNotificationColor(notification.type),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Menu Icon
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF90CAF9),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Dropdown Menu
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (notification.isRead)
                                        Icons.Default.Clear
                                    else
                                        Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF4FC3F7),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    if (notification.isRead) "Mark as unread" else "Mark as read",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E3A5F)
                                )
                            }
                        },
                        onClick = {
                            if (notification.isRead) {
                                onMarkAsUnread(notification.id)
                            } else {
                                onMarkAsRead(notification.id)
                            }
                            showMenu = false
                        }
                    )
                    Divider(color = Color(0xFFE1F5FE))
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFEF5350),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Delete",
                                    fontSize = 14.sp,
                                    color = Color(0xFFEF5350)
                                )
                            }
                        },
                        onClick = {
                            onDelete(notification.id)
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernNotificationIcon(
    type: NotificationType,
    isRead: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "icon_pulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (!isRead) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )

    val (icon, color) = getModernNotificationIconAndColor(type)

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(if (!isRead) iconScale else 1f)
            .shadow(
                elevation = if (!isRead) 8.dp else 4.dp,
                shape = CircleShape,
                spotColor = color.copy(alpha = 0.4f)
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.3f),
                        color.copy(alpha = 0.15f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "dot_pulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(dotScale)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                spotColor = Color(0xFF4FC3F7)
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4FC3F7),
                        Color(0xFF29B6F6)
                    )
                ),
                shape = CircleShape
            )
    )
}

@Composable
fun LoadingState(alpha: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(5) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE1F5FE).copy(alpha = alpha * 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFB3E5FC).copy(alpha = alpha))
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFB3E5FC).copy(alpha = alpha))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFB3E5FC).copy(alpha = alpha * 0.7f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFB3E5FC).copy(alpha = alpha * 0.5f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationState(userType: UserType) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "empty_float"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(y = offsetY.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFF4FC3F7).copy(alpha = 0.3f)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE1F5FE),
                            Color(0xFFB3E5FC)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(70.dp),
                tint = Color(0xFF4FC3F7)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No notifications yet",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D47A1),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        val emptyMessage = when (userType) {
            UserType.JOBSEEKER -> "When companies post new jobs or follow you, notifications will appear here"
            UserType.COMPANY -> "When job seekers apply or follow your company, notifications will appear here"
            UserType.ALL -> "Your notifications will appear here"
        }

        Text(
            text = emptyMessage,
            fontSize = 15.sp,
            color = Color(0xFF546E7A),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun ModernDeletionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Color(0xFFFFEBEE),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Clear All Notifications?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D47A1),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "This will permanently delete all your notifications. This action cannot be undone.",
                fontSize = 14.sp,
                color = Color(0xFF546E7A),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF5350)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    "Clear All",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = null,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF4FC3F7)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    "Cancel",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        tonalElevation = 0.dp
    )
}

// Helper Functions
fun getModernNotificationIconAndColor(type: NotificationType): Pair<ImageVector, Color> {
    return when (type) {
        NotificationType.JOB_ALERT -> Pair(Icons.Default.MailOutline, Color(0xFF66BB6A))
        NotificationType.MESSAGE -> Pair(Icons.Default.Send, Color(0xFF42A5F5))
        NotificationType.SYSTEM -> Pair(Icons.Default.Settings, Color(0xFFFFA726))
        NotificationType.GENERAL -> Pair(Icons.Default.Notifications, Color(0xFFAB47BC))
        NotificationType.APPLICATION_STATUS -> Pair(Icons.Default.Create, Color(0xFF26C6DA))
        NotificationType.CANDIDATE_ALERT -> Pair(Icons.Default.Person, Color(0xFFEC407A))
        NotificationType.EVENTS -> Pair(Icons.Default.DateRange, Color(0xFF7E57C2))
        NotificationType.PROFILE_UPDATE -> Pair(Icons.Default.AccountCircle, Color(0xFF8D6E63))
        NotificationType.JOB_APPLICATION -> Pair(Icons.Default.AccountCircle, Color(0xFFFF5722))
    }
}

fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.JOB_ALERT -> Color(0xFF66BB6A)
        NotificationType.MESSAGE -> Color(0xFF42A5F5)
        NotificationType.SYSTEM -> Color(0xFFFFA726)
        NotificationType.GENERAL -> Color(0xFFAB47BC)
        NotificationType.APPLICATION_STATUS -> Color(0xFF26C6DA)
        NotificationType.CANDIDATE_ALERT -> Color(0xFFEC407A)
        NotificationType.EVENTS -> Color(0xFF7E57C2)
        NotificationType.PROFILE_UPDATE -> Color(0xFF8D6E63)
        NotificationType.JOB_APPLICATION -> Color(0xFFFF5722)
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

// ViewModelFactory
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