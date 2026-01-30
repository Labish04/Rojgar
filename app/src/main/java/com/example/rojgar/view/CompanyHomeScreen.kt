package com.example.rojgar.view

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rojgar.R
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.ReviewModel
import com.example.rojgar.model.ApplicationModel
import com.example.rojgar.repository.CalendarRepoImpl
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.ReviewRepoImpl
import com.example.rojgar.repository.ApplicationRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.Gray
import com.example.rojgar.ui.theme.White
import com.example.rojgar.viewmodel.CalendarViewModel
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.ReviewViewModel
import com.example.rojgar.viewmodel.ApplicationViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import com.example.rojgar.viewmodel.NotificationViewModel
import com.example.rojgar.viewmodel.ChatViewModel
import com.example.rojgar.repository.NotificationRepoImpl
import com.example.rojgar.repository.ChatRepositoryImpl
import com.example.rojgar.repository.GroupChatRepositoryImpl
import com.example.rojgar.model.UserType
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.border


@Composable
fun CompanyHomeScreenBody(company: CompanyModel? = null){
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }

    val reviewViewModel = remember { ReviewViewModel(ReviewRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val calendarViewModel = remember { CalendarViewModel(CalendarRepoImpl(context)) }
    val applicationViewModel = remember { ApplicationViewModel(ApplicationRepoImpl(context)) }
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl(), UserType.COMPANY) }
    val chatViewModel = remember { ChatViewModel(ChatRepositoryImpl(context), GroupChatRepositoryImpl()) }

    val companyId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    val reviews by reviewViewModel.reviews.observeAsState(emptyList())
    val averageRating by reviewViewModel.averageRating.observeAsState(0.0)
    val companyDetails by companyViewModel.companyDetails.observeAsState(company)
    val events by calendarViewModel.events.observeAsState(emptyList())
    val applications by applicationViewModel.applications.observeAsState(emptyList())

    val unreadNotificationCount by notificationViewModel.unreadCount.collectAsState(initial = 0)
    val chatRooms by chatViewModel.chatRooms.observeAsState(emptyList())
    val unreadMessageCount = remember(chatRooms) { chatRooms.sumOf { it.unreadCount } }

    // For demo purposes - you can connect these to actual data later
    val activeJobs = remember { mutableStateOf(0) }
    val totalApplications = remember { mutableStateOf(0) }

    LaunchedEffect(companyId) {
        if (companyId.isNotEmpty()) {
            companyViewModel.fetchCurrentCompany()
        }
    }

    LaunchedEffect(companyId, currentUserId) {
        if (companyId.isNotEmpty() && currentUserId.isNotEmpty()) {
            reviewViewModel.setupRealTimeUpdates(companyId, currentUserId)
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            calendarViewModel.observeAllEventsForUser(currentUserId)
            chatViewModel.loadChatRooms(currentUserId)
        }
    }

    LaunchedEffect(companyId) {
        if (companyId.isNotEmpty()) {
            applicationViewModel.getApplicationsByCompany(companyId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ModernLoginTheme.SurfaceLight,
                        ModernLoginTheme.IceBlue
                    )
                )
            )
    ) {
        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Add space for the floating top bar
            Spacer(modifier = Modifier.height(72.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // Search bar
            CompanySearchBar(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Calendar Card
            CalendarCardSection(
                events = events,
                onCalendarClick = {
                    val intent = Intent(context, CalendarActivity::class.java)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Applications Card
            if (applications.isNotEmpty()) {
                RecentApplicationsCard(
                    applications = applications.sortedByDescending { it.appliedDate }.take(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    onApplicationClick = { application ->
                        val intent = Intent(context, ApplicationActivity::class.java).apply {
                            putExtra("JOB_POST_ID", application.postId)
                            putExtra("COMPANY_ID", application.companyId)
                        }
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Reviews & Ratings Card
            EnhancedReviewsRatingsCard(
                averageRating = averageRating,
                totalReviews = reviews.size,
                reviews = reviews,
                companyId = companyId,
                companyName = companyDetails?.companyName ?: "Company",
                reviewViewModel = reviewViewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Floating Top Bar - stays fixed at the top
        EnhancedCompanyTopBar(
            companyName = companyDetails?.companyName ?: "Company",
            companyProfileImage = companyDetails?.companyProfileImage ?: "",
            unreadMessageCount = unreadMessageCount,
            unreadNotificationCount = unreadNotificationCount,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )
    }
}

@Composable
fun NotificationBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = count > 0,
        enter = scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
        exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF5252), Color(0xFFB71C1C))
                    ),
                    shape = CircleShape
                )
                .border(1.5.dp, Color.White, CircleShape)
                .size(if (count > 99) 22.dp else 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EnhancedCompanyTopBar(
    companyName: String,
    companyProfileImage: String,
    unreadMessageCount: Int = 0,
    unreadNotificationCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier,
        color = Color(0xFF1976D2), // Brighter, more vibrant blue
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile and Name Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (companyProfileImage.isNotEmpty()) {
                        AsyncImage(
                            model = companyProfileImage,
                            contentDescription = "Company Profile",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = companyName.firstOrNull()?.uppercase() ?: "C",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Company Name and Subtitle
                Column(horizontalAlignment = Alignment.Start) {
                    val greeting = remember {
                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        when (hour) {
                            in 0..11 -> "Good Morning"
                            in 12..16 -> "Good Afternoon"
                            else -> "Good Evening"
                        }
                    }

                    Text(
                        text = "$greeting, $companyName !",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Let's find top talent today",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
            }

            // Action Icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chat Icon with Badge
                Box {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, MessageActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.chat),
                            contentDescription = "Messages",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Badge
                    NotificationBadge(
                        count = unreadMessageCount,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }

                // Notification Icon with Badge
                Box {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, NotificationActivity::class.java).apply {
                                putExtra("USER_TYPE", "COMPANY")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.notification),
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Badge
                    NotificationBadge(
                        count = unreadNotificationCount,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}
@Composable
fun RecentApplicationsCard(
    applications: List<ApplicationModel>,
    modifier: Modifier = Modifier,
    onApplicationClick: (ApplicationModel) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isExpanded = true
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn(animationSpec = tween(400)) + expandVertically(
            animationSpec = tween(400),
            expandFrom = Alignment.Top
        )
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = ModernLoginTheme.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = ModernLoginTheme.PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Recent Applications",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ModernLoginTheme.TextPrimary
                            )
                        )
                    }

                    Text(
                        text = "${applications.size}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ModernLoginTheme.PrimaryBlue
                        ),
                        modifier = Modifier
                            .background(
                                color = ModernLoginTheme.PrimaryBlue.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Applications List
                applications.forEachIndexed { index, application ->
                    RecentApplicationItem(
                        application = application,
                        delay = index * 100L,
                        onClick = { onApplicationClick(application) }
                    )

                    if (index < applications.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(
                            color = ModernLoginTheme.TextSecondary.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecentApplicationItem(
    application: ApplicationModel,
    delay: Long,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            animationSpec = tween(300),
            initialOffsetY = { 20 }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            verticalAlignment = Alignment.Top
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ModernLoginTheme.PrimaryBlue,
                                ModernLoginTheme.PrimaryBlue.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = application.jobSeekerName.firstOrNull()?.uppercase() ?: "A",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Application Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = application.jobSeekerName.ifEmpty { "Applicant" },
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ModernLoginTheme.TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatTimeAgo(application.appliedDate),
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = ModernLoginTheme.TextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Email or phone info
                if (application.jobSeekerEmail.isNotEmpty()) {
                    Text(
                        text = application.jobSeekerEmail,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = ModernLoginTheme.TextSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = getApplicationStatusColor(application.status),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = application.status,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = getApplicationStatusTextColor(application.status)
                            )
                        )
                    }

                    if (application.status == "Pending") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF57F17))
                        )
                    }
                }
            }
        }
    }
}

fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

fun getApplicationStatusColor(status: String): Color {
    return when (status) {
        "Pending" -> Color(0xFFFFF9C4)
        "Reviewed" -> Color(0xFFE1F5FE)
        "Shortlisted" -> Color(0xFFE8F5E9)
        "Accepted" -> Color(0xFFC8E6C9)
        "Rejected" -> Color(0xFFFFCDD2)
        else -> Color.LightGray
    }
}

fun getApplicationStatusTextColor(status: String): Color {
    return when (status) {
        "Pending" -> Color(0xFFF57F17)
        "Reviewed" -> Color(0xFF01579B)
        "Shortlisted" -> Color(0xFF2E7D32)
        "Accepted" -> Color(0xFF1B5E20)
        "Rejected" -> Color(0xFFC62828)
        else -> Color.DarkGray
    }
}

@Composable
fun AnimatedWelcomeHeader(
    companyName: String,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
            animationSpec = tween(600),
            initialOffsetY = { -40 }
        ),
        modifier = modifier
    ) {
        Column {
            Text(
                text = getGreeting(),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = ModernLoginTheme.TextSecondary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = companyName,
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ModernLoginTheme.TextPrimary,
                    letterSpacing = (-0.5).sp
                )
            )
        }
    }
}

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

@Composable
fun CompanySearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .height(56.dp)
            .clickable {
                context.startActivity(
                    Intent(context, CompanySearchActivity::class.java)
                )
            }
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = ModernLoginTheme.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = true,
                enabled = false,
                placeholder = {
                    Text(
                        "Search jobs, candidates...",
                        fontSize = 14.sp,
                        color = ModernLoginTheme.TextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.searchicon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = ModernLoginTheme.PrimaryBlue
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun QuickStatsRow(
    activeJobs: Int,
    totalApplications: Int,
    averageRating: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            title = "Active Jobs",
            value = activeJobs.toString(),
            icon = Icons.Filled.Work,
            color = ModernLoginTheme.PrimaryBlue,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = "Applications",
            value = totalApplications.toString(),
            icon = Icons.Filled.Description,
            color = ModernLoginTheme.AccentBlue,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = "Rating",
            value = String.format("%.1f", averageRating),
            icon = Icons.Filled.Star,
            color = Color(0xFFFFD700),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ModernLoginTheme.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedCounterText(targetValue = value, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = ModernLoginTheme.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AnimatedCounterText(targetValue: String, color: Color) {
    val numericValue = targetValue.toIntOrNull() ?: 0
    var count by remember { mutableStateOf(0) }

    LaunchedEffect(numericValue) {
        if (numericValue > 0) {
            val step = if (numericValue > 100) numericValue / 20 else 1
            while (count < numericValue) {
                count = (count + step).coerceAtMost(numericValue)
                delay(30)
            }
        } else {
            count = numericValue
        }
    }

    Text(
        text = if (targetValue.contains(".")) targetValue else count.toString(),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
fun ActionCardsGrid(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ModernLoginTheme.TextPrimary
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                title = "Post Job",
                icon = Icons.Filled.Add,
                gradient = Brush.linearGradient(
                    colors = listOf(
                        ModernLoginTheme.PrimaryBlue,
                        ModernLoginTheme.LightBlue
                    )
                ),
                onClick = {
                    try {
                        val intent = Intent(context, Class.forName("com.example.rojgar.view.CompanyUploadPostActivity"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Activity not found - handle gracefully
                    }
                },
                modifier = Modifier.weight(1f)
            )
            ActionCard(
                title = "Analytics",
                icon = Icons.Filled.TrendingUp,
                gradient = Brush.linearGradient(
                    colors = listOf(
                        ModernLoginTheme.AccentBlue,
                        ModernLoginTheme.SkyBlue
                    )
                ),
                onClick = {
                    try {
                        val intent = Intent(context, Class.forName("com.example.rojgar.view.AnalyticsActivity"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Activity not found - handle gracefully
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                title = "Messages",
                icon = Icons.Filled.Message,
                gradient = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF10B981),
                        Color(0xFF34D399)
                    )
                ),
                onClick = {
                    try {
                        val intent = Intent(context, Class.forName("com.example.rojgar.view.MessageActivity"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Activity not found - handle gracefully
                    }
                },
                modifier = Modifier.weight(1f)
            )
            ActionCard(
                title = "Applicants",
                icon = Icons.Filled.People,
                gradient = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF59E0B),
                        Color(0xFFFBBF24)
                    )
                ),
                onClick = {
                    try {
                        val intent = Intent(context, Class.forName("com.example.rojgar.view.ApplicantListActivity"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Activity not found - handle gracefully
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Card(
        modifier = modifier
            .height(100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = ModernLoginTheme.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ModernLoginTheme.White
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
fun EnhancedReviewsRatingsCard(
    averageRating: Double,
    totalReviews: Int,
    reviews: List<ReviewModel>,
    companyId: String,
    companyName: String,
    reviewViewModel: ReviewViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val ratingDistribution = remember(reviews) {
        val distribution = mutableMapOf<Int, Int>()
        for (i in 1..5) {
            distribution[i] = reviews.count { it.rating == i }
        }
        distribution
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .clickable {
                val intent = Intent(context, CompanyReviewActivity::class.java)
                intent.putExtra("COMPANY_ID", companyId)
                intent.putExtra("COMPANY_NAME", companyName)
                context.startActivity(intent)
            },
        colors = CardDefaults.cardColors(containerColor = ModernLoginTheme.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Reviews",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reviews & Ratings",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernLoginTheme.TextPrimary
                        )
                    )
                }

                if (totalReviews > 0) {
                    Badge(
                        containerColor = ModernLoginTheme.IceBlue,
                        contentColor = ModernLoginTheme.PrimaryBlue
                    ) {
                        Text(
                            text = "$totalReviews",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Big Rating Number with circular progress
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(120.dp)
                    ) {
                        CircularRatingIndicator(
                            rating = averageRating,
                            maxRating = 5.0
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AnimatedRatingNumber(rating = averageRating)
                            StarRatingDisplay(rating = averageRating, size = 16.dp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedReviewCount(count = totalReviews)
                }

                // Right side - Distribution bars
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (5 downTo 1).forEach { star ->
                        AnimatedRatingBar(
                            star = star,
                            count = ratingDistribution[star] ?: 0,
                            total = totalReviews
                        )
                    }
                }
            }

            // Light gray divider line
            Spacer(modifier = Modifier.height(20.dp))
            Divider(
                color = Color.LightGray.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Recent Reviews Section
            val jobSeekerUsernames by reviewViewModel.jobSeekerUsernames.observeAsState(emptyMap())

            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        tint = ModernLoginTheme.PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Reviews",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernLoginTheme.TextPrimary
                        )
                    )
                }

                Text(
                    text = "${reviews.size}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ModernLoginTheme.PrimaryBlue
                    ),
                    modifier = Modifier
                        .background(
                            color = ModernLoginTheme.PrimaryBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reviews List (showing only first 3)
            reviews.take(3).forEachIndexed { index, review ->
                RecentReviewItem(
                    review = review,
                    username = jobSeekerUsernames[review.userId] ?: "Job Seeker",
                    reviewViewModel = reviewViewModel,
                    delay = index * 100L
                )

                if (index < reviews.take(3).lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(
                        color = ModernLoginTheme.TextSecondary.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun CircularRatingIndicator(
    rating: Double,
    maxRating: Double
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val percentage = (rating / maxRating).toFloat()

    val animatedPercentage by animateFloatAsState(
        targetValue = if (animationPlayed) percentage else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "rating_circle"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Canvas(modifier = Modifier.size(120.dp)) {
        val strokeWidth = 10.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2

        // Background circle
        drawCircle(
            color = Color(0xFFE8E8E8),
            radius = radius,
            style = Stroke(width = strokeWidth)
        )

        // Progress arc
        val sweepAngle = animatedPercentage * 360f
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFFFFD700),
                    Color(0xFFFFA500),
                    Color(0xFFFFD700)
                )
            ),
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        )
    }
}

@Composable
fun AnimatedRatingNumber(rating: Double) {
    var targetRating by remember { mutableStateOf(0.0) }

    LaunchedEffect(rating) {
        targetRating = rating
    }

    val animatedRating by animateFloatAsState(
        targetValue = targetRating.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "rating"
    )

    Text(
        text = String.format("%.1f", animatedRating),
        style = TextStyle(
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            letterSpacing = (-1).sp
        )
    )
}

@Composable
fun StarRatingDisplay(rating: Double, size: androidx.compose.ui.unit.Dp = 20.dp) {
    val starColor = Color(0xFFFFD700)

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            val starValue = index + 1
            val fillPercentage = when {
                starValue <= rating.toInt() -> 1f
                starValue == rating.toInt() + 1 -> {
                    val decimal = rating - rating.toInt()
                    if (decimal >= 0.5) 0.5f else 0f
                }
                else -> 0f
            }

            AnimatedStar(
                fillPercentage = fillPercentage,
                color = starColor,
                index = index,
                size = size
            )

            if (index < 4) {
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}

@Composable
fun AnimatedStar(
    fillPercentage: Float,
    color: Color,
    index: Int,
    size: androidx.compose.ui.unit.Dp = 20.dp
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 50L)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "star_scale"
    )

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            tint = Color(0xFFE8E8E8)
        )

        if (fillPercentage > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillPercentage)
                    .fillMaxHeight()
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = color
                )
            }
        }
    }
}

@Composable
fun AnimatedReviewCount(count: Int) {
    var targetCount by remember { mutableStateOf(0) }

    LaunchedEffect(count) {
        targetCount = count
    }

    val animatedCount by animateIntAsState(
        targetValue = targetCount,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "count"
    )

    Text(
        text = if (animatedCount == 1) "$animatedCount review" else "$animatedCount reviews",
        style = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ModernLoginTheme.TextSecondary,
            letterSpacing = 0.sp
        )
    )
}

@Composable
fun AnimatedRatingBar(
    star: Int,
    count: Int,
    total: Int
) {
    val percentage = if (total > 0) (count.toFloat() / total.toFloat()) else 0f
    val barColor = Color(0xFFFFD700)

    var animateBar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay((6 - star) * 80L)
        animateBar = true
    }

    val animatedPercentage by animateFloatAsState(
        targetValue = if (animateBar) percentage else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "bar_percentage"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$star",
            style = TextStyle(
                fontSize = 13.sp,
                color = ModernLoginTheme.TextPrimary,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(
                    color = Color(0xFFE8E8E8),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            if (animatedPercentage > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedPercentage.coerceIn(0f, 1f))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    barColor,
                                    barColor.copy(alpha = 0.8f)
                                )
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = count.toString(),
            style = TextStyle(
                fontSize = 12.sp,
                color = ModernLoginTheme.TextSecondary,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun RecentReviewsSection(
    reviews: List<ReviewModel>,
    reviewViewModel: ReviewViewModel,
    modifier: Modifier = Modifier
) {
    val jobSeekerUsernames by reviewViewModel.jobSeekerUsernames.observeAsState(emptyMap())
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isExpanded = true
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn(animationSpec = tween(400)) + expandVertically(
            animationSpec = tween(400),
            expandFrom = Alignment.Top
        )
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = ModernLoginTheme.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = ModernLoginTheme.PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Recent Reviews",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ModernLoginTheme.TextPrimary
                            )
                        )
                    }

                    Text(
                        text = "${reviews.size}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ModernLoginTheme.PrimaryBlue
                        ),
                        modifier = Modifier
                            .background(
                                color = ModernLoginTheme.PrimaryBlue.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reviews List
                reviews.forEachIndexed { index, review ->
                    RecentReviewItem(
                        review = review,
                        username = jobSeekerUsernames[review.userId] ?: "Job Seeker",
                        reviewViewModel = reviewViewModel,
                        delay = index * 100L
                    )

                    if (index < reviews.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(
                            color = ModernLoginTheme.TextSecondary.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecentReviewItem(
    review: ReviewModel,
    username: String,
    reviewViewModel: ReviewViewModel,
    delay: Long
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            animationSpec = tween(300),
            initialOffsetY = { 20 }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ModernLoginTheme.PrimaryBlue,
                                ModernLoginTheme.PrimaryBlue.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.firstOrNull()?.uppercase() ?: "U",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Review Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = username,
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ModernLoginTheme.TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = reviewViewModel.formatTimeAgo(review.timestamp),
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = ModernLoginTheme.TextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Star Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < review.rating) Color(0xFFFFD700) else Color(0xFFE8E8E8),
                            modifier = Modifier.size(16.dp)
                        )
                        if (index < 4) Spacer(modifier = Modifier.width(2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Review Text
                Text(
                    text = review.reviewText,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = ModernLoginTheme.TextPrimary.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}