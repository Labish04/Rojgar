package com.example.rojgar.view

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.model.ReviewModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.ReviewRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.Gray
import com.example.rojgar.ui.theme.White
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.ReviewViewModel
import com.example.rojgar.view.CalendarActivity
import com.example.rojgar.repository.CalendarRepoImpl
import com.example.rojgar.viewmodel.CalendarViewModel
import com.example.rojgar.model.CalendarEventModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.Color

// Modern Design Theme Colors
object ModernCompanyTheme {
    val PrimaryBlue = ComposeColor(0xFF4A90E2)
    val AccentBlue = ComposeColor(0xFF00BCD4)
    val DeepBlue = ComposeColor(0xFF2C5F8D)
    val SurfaceLight = ComposeColor(0xFFF8FAFC)
    val White = ComposeColor.White
    val CardBackground = ComposeColor.White
    val TextPrimary = ComposeColor(0xFF1E293B)
    val TextSecondary = ComposeColor(0xFF64748B)
    val BorderLight = ComposeColor(0xFFE2E8F0)
    val GradientStart = ComposeColor(0xFFAFCEFC)
    val GradientEnd = ComposeColor(0xFF5594FA)
    val StarGold = ComposeColor(0xFFFFD700)
}

@Composable
fun CompanyHomeScreenBody(){
    val context = LocalContext.current

    val reviewViewModel = remember { ReviewViewModel(ReviewRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val calendarViewModel = remember { CalendarViewModel(CalendarRepoImpl(context)) }

    val companyId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    val reviews by reviewViewModel.reviews.observeAsState(emptyList())
    val averageRating by reviewViewModel.averageRating.observeAsState(0.0)
    val company by companyViewModel.companyDetails.observeAsState(null)
    val events by calendarViewModel.events.observeAsState(emptyList())

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
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ModernCompanyTheme.SurfaceLight),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Modern Search Bar Section
        item {
            ModernCompanySearchBar(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }

        // Welcome Header with Animation
        item {
            WelcomeHeaderSection(
                companyName = company?.companyName ?: "Company",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // Calendar Card - Full Width
        item {
            CalendarCardSection(
                events = events,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        // Reviews Card - Below Calendar
        item {
            EnhancedReviewsCard(
                averageRating = averageRating,
                totalReviews = reviews.size,
                reviews = reviews,
                companyId = companyId,
                companyName = company?.companyName ?: "Company",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun ModernCompanySearchBar(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = ModernCompanyTheme.PrimaryBlue.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ModernCompanyTheme.CardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    context.startActivity(
                        Intent(context, JobSeekerSearchActivity::class.java)
                    )
                }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = ModernCompanyTheme.PrimaryBlue.copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.searchicon),
                        contentDescription = "Search",
                        modifier = Modifier.size(20.dp),
                        tint = ModernCompanyTheme.PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                "Search jobs, candidates...",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = ModernCompanyTheme.TextSecondary,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

@Composable
fun WelcomeHeaderSection(
    companyName: String,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 20.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "header_offset"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "header_alpha"
    )

    Column(
        modifier = modifier
            .offset(y = offsetY)
            .graphicsLayer { this.alpha = alpha }
    ) {
        Text(
            text = "Welcome back,",
            style = TextStyle(
                fontSize = 16.sp,
                color = ModernCompanyTheme.TextSecondary,
                fontWeight = FontWeight.Normal
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = companyName,
            style = TextStyle(
                fontSize = 28.sp,
                color = ModernCompanyTheme.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun EnhancedReviewsCard(
    averageRating: Double,
    totalReviews: Int,
    reviews: List<ReviewModel>,
    companyId: String,
    companyName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )

    val ratingDistribution = remember(reviews) {
        val distribution = mutableMapOf<Int, Int>()
        for (i in 1..5) {
            distribution[i] = reviews.count { it.rating == i }
        }
        distribution
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable {
                val intent = Intent(context, CompanyReviewActivity::class.java)
                intent.putExtra("COMPANY_ID", companyId)
                intent.putExtra("COMPANY_NAME", companyName)
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ModernCompanyTheme.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient Background Decoration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ModernCompanyTheme.GradientStart.copy(alpha = 0.1f),
                                ComposeColor.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reviews",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernCompanyTheme.TextPrimary
                        )
                    )

                    Surface(
                        shape = CircleShape,
                        color = ModernCompanyTheme.PrimaryBlue.copy(alpha = 0.1f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ModernCompanyTheme.StarGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Large animated rating number
                AnimatedRatingNumber(rating = averageRating)

                Spacer(modifier = Modifier.height(8.dp))

                // Star rating display
                EnhancedStarRating(rating = averageRating)

                Spacer(modifier = Modifier.height(6.dp))

                // Total reviews with animation
                AnimatedReviewCount(count = totalReviews)

                Spacer(modifier = Modifier.height(16.dp))

                // Rating distribution bars
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (5 downTo 1).forEach { star ->
                        EnhancedRatingBar(
                            star = star,
                            count = ratingDistribution[star] ?: 0,
                            total = totalReviews
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CalendarCardSection(
    events: List<CalendarEventModel>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val currentWeekDays = remember { getCurrentWeekDays(calendar) }
    val currentWeekEvents = remember(events) {
        getEventsForCurrentWeek(events, currentWeekDays)
    }
    val dateFormat = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }
    val currentMonth = remember { dateFormat.format(calendar.time) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { context.startActivity(Intent(context, CalendarActivity::class.java)) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ModernCompanyTheme.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Calendar Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Calendar",
                        tint = ModernCompanyTheme.PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calendar",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernCompanyTheme.TextPrimary
                        )
                    )
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "Open",
                    tint = ModernCompanyTheme.TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Month/Year Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentMonth,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ModernCompanyTheme.TextPrimary
                    )
                )
                Text(
                    text = "This Week",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ModernCompanyTheme.TextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Week Days Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dayNames = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                dayNames.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ModernCompanyTheme.TextSecondary
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Current Week Days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val today = Calendar.getInstance()
                currentWeekDays.forEach { dayCalendar ->
                    val isToday = isSameDay(dayCalendar, today)
                    val hasEvent = currentWeekEvents.any { event ->
                        isSameDay(getCalendarFromMillis(event.startTimeMillis), dayCalendar)
                    }

                    DayCell(
                        day = dayCalendar.get(Calendar.DAY_OF_MONTH),
                        isToday = isToday,
                        hasEvent = hasEvent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Events for this week
            if (currentWeekEvents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = ModernCompanyTheme.SurfaceLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${currentWeekEvents.size} ${if (currentWeekEvents.size == 1) "Event" else "Events"} This Week",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ModernCompanyTheme.TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show up to 2 events
                currentWeekEvents.take(2).forEach { event ->
                    CompactEventItem(event = event)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (currentWeekEvents.size > 2) {
                    Text(
                        text = "+${currentWeekEvents.size - 2} more",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ModernCompanyTheme.AccentBlue
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No events this week",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ModernCompanyTheme.TextSecondary
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun EmptyCalendarState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = CircleShape,
            color = ModernCompanyTheme.SurfaceLight
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(R.drawable.calendaricon),
                    contentDescription = null,
                    tint = ModernCompanyTheme.TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "No upcoming events",
            style = TextStyle(
                fontSize = 14.sp,
                color = ModernCompanyTheme.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your schedule is clear!",
            style = TextStyle(
                fontSize = 12.sp,
                color = ModernCompanyTheme.TextSecondary.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
fun EnhancedEventItem(
    event: CalendarEventModel,
    index: Int
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 80L)
        isVisible = true
    }

    val offsetX by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 20.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "event_offset"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "event_alpha"
    )

    val startTime = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
        .format(java.util.Date(event.startTimeMillis))
    val eventColor = ComposeColor(android.graphics.Color.parseColor(event.colorHex))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = offsetX)
            .graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(12.dp),
        color = ModernCompanyTheme.SurfaceLight,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator with pulse animation
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(eventColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ModernCompanyTheme.TextPrimary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = ModernCompanyTheme.TextSecondary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = startTime,
                        fontSize = 11.sp,
                        color = ModernCompanyTheme.TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
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
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "rating"
    )

    Text(
        text = String.format("%.1f", animatedRating),
        style = TextStyle(
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ModernCompanyTheme.TextPrimary,
            letterSpacing = (-1).sp
        )
    )
}

@Composable
fun EnhancedStarRating(rating: Double) {
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
                color = ModernCompanyTheme.StarGold,
                index = index
            )

            if (index < 4) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun AnimatedStar(
    fillPercentage: Float,
    color: ComposeColor,
    index: Int
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 80L)
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
            .size(22.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            tint = ModernCompanyTheme.BorderLight
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
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "count"
    )

    Text(
        text = "${String.format("%,d", animatedCount)} reviews",
        style = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = ModernCompanyTheme.TextSecondary
        )
    )
}

@Composable
fun EnhancedRatingBar(
    star: Int,
    count: Int,
    total: Int
) {
    val percentage = if (total > 0) (count.toFloat() / total.toFloat()) else 0f

    var animateBar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay((6 - star) * 100L)
        animateBar = true
    }

    val animatedPercentage by animateFloatAsState(
        targetValue = if (animateBar) percentage else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "bar_percentage"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$star",
            style = TextStyle(
                fontSize = 12.sp,
                color = ModernCompanyTheme.TextPrimary,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.width(12.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .background(
                    color = ModernCompanyTheme.SurfaceLight,
                    shape = RoundedCornerShape(3.dp)
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
                                    ModernCompanyTheme.StarGold,
                                    ModernCompanyTheme.StarGold.copy(alpha = 0.8f)
                                )
                            ),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "$count",
            style = TextStyle(
                fontSize = 11.sp,
                color = ModernCompanyTheme.TextSecondary,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun EmptyStateJobsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = ModernCompanyTheme.SurfaceLight
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(R.drawable.jobpost),
                    contentDescription = null,
                    tint = ModernCompanyTheme.TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}