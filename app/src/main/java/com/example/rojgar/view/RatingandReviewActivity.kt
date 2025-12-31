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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.view.ui.theme.RojgarTheme

// Data class for Review
data class Review(
    val userName: String,
    val userImage: Int? = null, // Resource ID for user image
    val rating: Float,
    val comment: String
)

class RatingandReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RojgarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ReviewAndRatingScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewAndRatingScreen(
    modifier: Modifier = Modifier,
    companyName: String = "Tech Company"
) {
    // Sample reviews data
    val reviews = listOf(
        Review("David", null, 4f, "It is comfortable to work in this company."),
        Review("Sarah", null, 5f, "Great work environment and excellent benefits."),
        Review("Michael", null, 3f, "Good company but work-life balance could be better."),
        Review("Emma", null, 4.5f, "Professional team and good career growth opportunities.")
    )

    val averageRating = reviews.map { it.rating }.average().toFloat()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = "Review And Rating",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Reviews
        items(reviews) { review ->
            ReviewCard(review = review)
        }

        // Average Rating
        item {
            AverageRatingCard(averageRating = averageRating)
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB3D9FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User Image
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                // If you have user images, use:
                // Image(painter = painterResource(id = review.userImage), ...)
                Text(
                    text = review.userName.first().toString(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Review Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = review.userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    StarRating(rating = review.rating)
                }

                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    color = Color(0xFF555555),
                    lineHeight = 20.sp,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            }
        }
    }
}

@Composable
fun AverageRatingCard(averageRating: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Average",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StarRating(rating = averageRating, size = 40.dp)

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = String.format("%.1f", averageRating),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
fun StarRating(
    rating: Float,
    maxStars: Int = 5,
    size: androidx.compose.ui.unit.Dp = 24.dp
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(maxStars) { index ->
            Icon(
                imageVector = if (index < rating.toInt()) {
                    Icons.Filled.Star
                } else if (index < rating && rating % 1 >= 0.5) {
                    Icons.Filled.Star // For half stars, you'd need a custom icon
                } else {
                    Icons.Outlined.Star
                },
                contentDescription = "Star ${index + 1}",
                tint = if (index < rating) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                modifier = Modifier.size(size)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewReviewAndRatingScreen() {
    RojgarTheme {
        ReviewAndRatingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewReviewCard() {
    RojgarTheme {
        ReviewCard(
            review = Review(
                "David",
                null,
                4f,
                "It is comfortable to work in this company."
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAverageRatingCard() {
    RojgarTheme {
        AverageRatingCard(averageRating = 4.2f)
    }
}