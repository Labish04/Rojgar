package com.example.rojgar.view

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.rojgar.util.CalendarDateUtils
import com.example.rojgar.model.CalendarEventModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CompanyHomeScreenBody(){
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }

    val reviewViewModel = remember { ReviewViewModel(ReviewRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val calendarViewModel = remember { CalendarViewModel(CalendarRepoImpl()) }

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

    // Fetch calendar events for current month
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            calendarViewModel.observeAllEventsForUser(currentUserId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        context.startActivity(
                            Intent(context, JobSeekerSearchActivity::class.java)
                        )
                    }
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    placeholder = {
                        Text(
                            "Search",
                            fontSize = 20.sp,
                            color = Color.Gray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.searchicon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Gray
                        )
                    },
                    shape = RoundedCornerShape(15.dp),
                    colors = TextFieldDefaults.colors(
                        disabledContainerColor = White,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            ReviewsRatingsCard(
                averageRating = averageRating,
                totalReviews = reviews.size,
                reviews = reviews,
                companyId = companyId,
                companyName = company?.companyName ?: "Company",
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
            )

            Spacer(modifier = Modifier.width(20.dp))

            Card(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            context.startActivity(
                                Intent(context, CalendarActivity::class.java)
                            )
                        }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                MiniCalendar(events = events)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp)
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Posted Jobs",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "Show All",
                    style = TextStyle(
                        fontSize = 18.sp
                    )
                )
            }
        }

        Card(
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(395.dp)
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {}
    }
}

@Composable
fun MiniCalendar(modifier: Modifier = Modifier, events: List<CalendarEventModel> = emptyList()) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

    val monthNames = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Get days of week (Sun, Mon, Tue, etc.)
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    // Generate calendar grid similar to CalendarActivity
    val calendarGrid = generateCalendarDays(currentYear, currentMonth)
    val todayCalendar = Calendar.getInstance()
    val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)
    val todayMonth = todayCalendar.get(Calendar.MONTH)
    val todayYear = todayCalendar.get(Calendar.YEAR)

    // Helper function to get event colors for a specific day
    fun getEventColorsForDay(day: Int): List<Color> {
        val (dayStart, dayEnd) = CalendarDateUtils.dayRangeMillis(currentYear, currentMonth, day)
        return events
            .filter { event -> event.startTimeMillis < dayEnd && event.endTimeMillis > dayStart }
            .map { event -> Color(android.graphics.Color.parseColor(event.colorHex)) }
            .distinct()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Month and Year
        Text(
            text = "${monthNames[currentMonth]} $currentYear",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Days of week header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(textAlign = TextAlign.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid (showing only 4 weeks for mini view)
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Show weeks 2-5 (skipping first and last week if incomplete)
            for (week in 1..4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (dayIndex in 0 until 7) {
                        val dayNumber = calendarGrid[week * 7 + dayIndex]
                        val isToday = dayNumber == todayDay && currentMonth == todayMonth && currentYear == todayYear && dayNumber > 0
                        val eventColors = if (dayNumber > 0) getEventColorsForDay(dayNumber) else emptyList()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    if (isToday) Color(0xFF3B82F6) else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNumber > 0) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        fontSize = 10.sp,
                                        color = if (isToday) Color.White else Color.Black,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    )

                                    if (eventColors.isNotEmpty() && !isToday) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val displayColors = eventColors.take(2) // Show max 2 dots for mini calendar
                                            displayColors.forEachIndexed { index, color ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(3.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                )
                                                if (index < displayColors.size - 1) {
                                                    Spacer(modifier = Modifier.width(1.dp))
                                                }
                                            }
                                            if (eventColors.size > 2) {
                                                Spacer(modifier = Modifier.width(1.dp))
                                                Text(
                                                    text = "+",
                                                    fontSize = 6.sp,
                                                    color = Color(0xFF64748B),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Current date display
        val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.time)
        Text(
            text = currentDate,
            style = TextStyle(
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

private fun generateCalendarDays(year: Int, month: Int): List<Int> {
    val calendar = Calendar.getInstance().apply {
        set(year, month, 1)
    }

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday, 1 = Monday, etc.
    val totalCells = 42

    return List(totalCells) { index ->
        if (index >= startDayOfWeek && index < startDayOfWeek + daysInMonth) {
            index - startDayOfWeek + 1
        } else {
            0
        }
    }
}

@Composable
fun ReviewsRatingsCard(
    averageRating: Double,
    totalReviews: Int,
    reviews: List<ReviewModel>,
    companyId: String,
    companyName: String,
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
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                val intent = Intent(context, CompanyReviewActivity::class.java)
                intent.putExtra("COMPANY_ID", companyId)
                intent.putExtra("COMPANY_NAME", companyName)
                context.startActivity(intent)
            }
        ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large animated rating number
            AnimatedRatingNumber(rating = averageRating)

            Spacer(modifier = Modifier.height(4.dp))

            // Star rating display
            StarRatingDisplay(rating = averageRating)

            Spacer(modifier = Modifier.height(4.dp))

            // Total reviews with animation
            AnimatedReviewCount(count = totalReviews)

            Spacer(modifier = Modifier.height(12.dp))

            // Rating distribution bars
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
fun StarRatingDisplay(rating: Double) {
    val starColor = Color(0xFFFFC107) // Blue color from reference

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
                index = index
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
    index: Int
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
            .size(20.dp)
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
        text = String.format("%,d", animatedCount),
        style = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray,
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
    val barColor = Color(0xFFE3C808) // Blue color from reference

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
            .height(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$star",
            style = TextStyle(
                fontSize = 11.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(8.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .background(
                    color = Color(0xFFE8E8E8),
                    shape = RoundedCornerShape(3.dp)
                )
        ) {
            // Show bar only if there's actual percentage
            if (animatedPercentage > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedPercentage.coerceIn(0f, 1f))
                        .background(
                            color = barColor,
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    }
}