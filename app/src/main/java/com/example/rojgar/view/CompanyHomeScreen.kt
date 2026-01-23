package com.example.rojgar.view

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
                    .clickable {
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
                    .height(220.dp)
                    .width(200.dp)
            )

            Spacer(modifier = Modifier.width(20.dp))

            Card(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(220.dp)
                    .width(200.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                MiniEventList(
                    events = events,
                    maxItems = 3,
                    showAllEvents = true
                )
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
        modifier = modifier.clickable {
            val intent = Intent(context, CompanyReviewActivity::class.java)
            intent.putExtra("COMPANY_ID", companyId)
            intent.putExtra("COMPANY_NAME", companyName)
            context.startActivity(intent)
        },
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
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            letterSpacing = (-1).sp
        )
    )
}

@Composable
fun StarRatingDisplay(rating: Double) {
    val starColor = Color(0xFFFFD700) // Gold color

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
            fontSize = 16.sp,
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
    val barColor = Color(0xFFFFD700) // Gold color

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
            .height(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$star",
            style = TextStyle(
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(10.dp)
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