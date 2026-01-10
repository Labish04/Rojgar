package com.example.rojgar.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.rojgar.R
import com.example.rojgar.model.ReviewModel
import com.example.rojgar.repository.ReviewRepoImpl
import com.example.rojgar.viewmodel.ReviewViewModel
import com.example.rojgar.viewmodel.ReviewViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class CompanyReviewActivity : ComponentActivity() {
    private lateinit var viewModel: ReviewViewModel
    private lateinit var companyId: String
    private lateinit var companyName: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get company details from intent
        companyId = intent.getStringExtra("COMPANY_ID") ?: ""
        companyName = intent.getStringExtra("COMPANY_NAME") ?: "Company"
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Initialize ViewModel
        val repository = ReviewRepoImpl()
        val factory = ReviewViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ReviewViewModel::class.java]

        setContent {
            CompanyReviewBody(
                viewModel = viewModel,
                companyId = companyId,
                companyName = companyName,
                userId = userId,
                onBack = { finish() }
            )
        }

        // Setup real-time updates and check user review
        viewModel.setupRealTimeUpdates(companyId, userId)
        viewModel.checkUserReview(userId, companyId)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyReviewBody(
    viewModel: ReviewViewModel,
    companyId: String,
    companyName: String,
    userId: String,
    onBack: () -> Unit
) {
    val reviews by viewModel.reviews.observeAsState(emptyList())
    val averageRating by viewModel.averageRating.observeAsState(0.0)
    val userReview by viewModel.userReview.observeAsState()
    val loading by viewModel.loading.observeAsState(false)
    val toastMessage by viewModel.toastMessage.observeAsState()
    val jobSeekerUsernames by viewModel.jobSeekerUsernames.observeAsState(emptyMap())

    var showReviewDialog by remember { mutableStateOf(false) }
    var editingReview by remember { mutableStateOf<ReviewModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Get context for toast messages
    val context = androidx.compose.ui.platform.LocalContext.current

    // Show toast messages
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    // Colors
    val primaryBlue = Color(0xFF4A90E2)
    val lightBlue = Color(0xFFE3F2FD)
    val accentBlue = Color(0xFF2196F3)
    val darkBlue = Color(0xFF1976D2)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(companyName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(lightBlue)
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rating Summary Card
                item {
                    RatingSummaryCard(
                        averageRating = averageRating,
                        totalReviews = reviews.size,
                        primaryBlue = primaryBlue,
                        darkBlue = darkBlue
                    )
                }

                // User's Review (if exists)
                userReview?.let { review ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFBBDEFB)
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = primaryBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Your Review",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = darkBlue
                                        )
                                    }
                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingReview = review
                                                showReviewDialog = true
                                            }
                                        ) {
                                            Icon(Icons.Default.Edit, "Edit", tint = primaryBlue)
                                        }
                                        IconButton(onClick = { showDeleteDialog = true }) {
                                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFE57373))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                RatingStars(review.rating, primaryBlue)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    review.reviewText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    viewModel.formatTimeAgo(review.timestamp) + viewModel.getEditedLabel(review),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Reviews Header
                if (reviews.isNotEmpty()) {
                    item {
                        Text(
                            "All Reviews (${reviews.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = darkBlue,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // Reviews List
                items(reviews) { review ->
                    ReviewCard(
                        review = review,
                        username = jobSeekerUsernames[review.userId] ?: "Loading...",
                        viewModel = viewModel,
                        primaryBlue = primaryBlue,
                        isOwnReview = review.userId == userId
                    )
                }

                // Empty State
                if (reviews.isEmpty() && !loading) {
                    item {
                        EmptyReviewsState(primaryBlue)
                    }
                }
            }

            // Loading Indicator
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryBlue
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Review") },
            text = { Text("Are you sure you want to delete your review?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userReview?.let {
                            viewModel.deleteReview(it.reviewId, companyId)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color(0xFFE57373))
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

@Composable
fun RatingSummaryCard(
    averageRating: Double,
    totalReviews: Int,
    primaryBlue: Color,
    darkBlue: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(primaryBlue, darkBlue)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "%.1f".format(averageRating),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                RatingStars(averageRating.toInt().coerceIn(0, 5), Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Based on $totalReviews review${if (totalReviews != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun ReviewCard(
    review: ReviewModel,
    username: String,
    viewModel: ReviewViewModel,
    primaryBlue: Color,
    isOwnReview: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOwnReview) Color(0xFFE3F2FD) else Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(primaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        username.firstOrNull()?.uppercase() ?: "U",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        viewModel.formatTimeAgo(review.timestamp) + viewModel.getEditedLabel(review),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            RatingStars(review.rating, primaryBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                review.reviewText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun RatingStars(rating: Int, color: Color) {
    Row {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Default.Star else Icons.Default.Star,
                contentDescription = null,
                tint = if (index < rating) color else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun EmptyReviewsState(primaryBlue: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.outline_rate_review_24),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = primaryBlue.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No reviews yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = primaryBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Be the first to share your experience!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}