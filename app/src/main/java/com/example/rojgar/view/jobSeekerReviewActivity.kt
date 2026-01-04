package com.example.rojgar.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.example.rojgar.model.ReviewModel
import com.example.rojgar.repository.ReviewRepoImpl
import com.example.rojgar.viewmodel.ReviewViewModel
import com.example.rojgar.viewmodel.ReviewViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class JobSeekerReviewActivity : ComponentActivity() {
    private lateinit var viewModel: ReviewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel
        val repo = ReviewRepoImpl()
        val factory = ReviewViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[ReviewViewModel::class.java]

        // Get company ID from intent (you'll need to pass this when starting the activity)
        val companyId = intent.getStringExtra("COMPANY_ID") ?: ""

        setContent {
            JobSeekerReviewBody(viewModel, companyId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerReviewBody(viewModel: ReviewViewModel, companyId: String) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""

    // Observe LiveData
    val reviews by viewModel.reviews.observeAsState(emptyList())
    val averageRating by viewModel.averageRating.observeAsState(0.0)
    val userReview by viewModel.userReview.observeAsState()
    val loading by viewModel.loading.observeAsState(false)
    val toastMessage by viewModel.toastMessage.observeAsState()

    // Local state for UI
    var showWriteReviewDialog by remember { mutableStateOf(false) }
    var editingReview by remember { mutableStateOf<ReviewModel?>(null) }

    val hasReview = userReview != null

    // Setup real-time updates when component is first composed
    DisposableEffect(companyId, currentUserId) {
        viewModel.setupRealTimeUpdates(companyId, currentUserId)
        viewModel.loadReviews(companyId)
        viewModel.checkUserReview(currentUserId, companyId)

        onDispose {
            // Cleanup is handled by ViewModel.onCleared()
        }
    }

    // Show toast messages
    toastMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.clearToastMessage()
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = Color.White
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    "Company Reviews",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color(0xFF212121)
                                )
                                if (reviews.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFA726),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            String.format("%.1f", averageRating),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF757575)
                                        )
                                        Text(
                                            " • ${reviews.size} reviews",
                                            fontSize = 14.sp,
                                            color = Color(0xFF757575)
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { /* Handle back */ }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFF6366F1)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White
                        )
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !hasReview,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shadowElevation = 12.dp,
                    color = Color.White
                ) {
                    Button(
                        onClick = { showWriteReviewDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp
                        )
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Write Your Review",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8F9FA),
                            Color(0xFFF3F4FF)
                        )
                    )
                )
        ) {
            if (loading && reviews.isEmpty()) {
                // Show loading indicator when first loading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6366F1))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    itemsIndexed(reviews) { index, review ->
                        UltraCoolReviewItem(
                            review = review,
                            viewModel = viewModel,
                            currentUserId = currentUserId,
                            index = index,
                            onEditClick = {
                                editingReview = review
                                showWriteReviewDialog = true
                            },
                            onDeleteClick = {
                                viewModel.deleteReview(review.reviewId, companyId)
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showWriteReviewDialog) {
        UltraCoolWriteReviewDialog(
            existingReview = editingReview,
            loading = loading,
            onDismiss = {
                showWriteReviewDialog = false
                editingReview = null
            },
            onSubmit = { rating, text ->
                if (currentUser == null) {
                    Toast.makeText(context, "Please login to add a review", Toast.LENGTH_SHORT).show()
                    return@UltraCoolWriteReviewDialog
                }

                val review = if (editingReview != null) {
                    // Update existing review
                    editingReview!!.copy(rating = rating, reviewText = text)
                } else {
                    // Create new review
                    ReviewModel(
                        userId = currentUserId,
                        companyId = companyId,
                        userName = currentUser.displayName ?: "Anonymous",
                        userImageUrl = currentUser.photoUrl?.toString() ?: "",
                        rating = rating,
                        reviewText = text
                    )
                }

                if (editingReview != null) {
                    viewModel.updateReview(review)
                } else {
                    viewModel.addReview(review)
                }

                showWriteReviewDialog = false
                editingReview = null
            }
        )
    }
}

@Composable
fun UltraCoolReviewItem(
    review: ReviewModel,
    viewModel: ReviewViewModel,
    currentUserId: String,
    index: Int,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isMyReview = review.userId == currentUserId
    val timeAgo = viewModel.formatTimeAgo(review.timestamp)
    val editedLabel = viewModel.getEditedLabel(review)
    
    // Observe job seeker usernames from ViewModel
    val jobSeekerUsernames by viewModel.jobSeekerUsernames.observeAsState(emptyMap())
    val displayName = jobSeekerUsernames[review.userId] ?: review.userName

    var showMenu by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (showMenu) 90f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isMyReview) 8.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isMyReview) Color(0x406366F1) else Color(0x1A000000)
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        if (isMyReview) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6366F1),
                                Color(0xFF8B5CF6),
                                Color(0xFFA855F7)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isMyReview) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF5F5FF),
                                Color.White
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFFAFAFA)
                            )
                        )
                    }
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    brush = if (isMyReview) {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF6366F1),
                                                Color(0xFF8B5CF6)
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFE0E0E0),
                                                Color(0xFFBDBDBD)
                                            )
                                        )
                                    },
                                    shape = CircleShape
                                )
                                .padding(3.dp)
                        ) {
                            if (review.userImageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = review.userImageUrl,
                                    contentDescription = "User avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Default avatar when no image URL
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFE0E0E0), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = displayName.firstOrNull()?.uppercase() ?: "?",
                                        color = Color(0xFF757575),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = displayName,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = Color(0xFF212121)
                            )
                            if (isMyReview) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF6366F1),
                                                    Color(0xFF8B5CF6)
                                                )
                                            ),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        "YOU",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { index ->
                                Icon(
                                    imageVector = if (index < review.rating) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = "Star",
                                    tint = if (index < review.rating) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                                    modifier = Modifier.size(18.dp)
                                )
                                if (index < 4) Spacer(modifier = Modifier.width(3.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(Color(0xFF9E9E9E), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = timeAgo,
                                    fontSize = 13.sp,
                                    color = Color(0xFF757575),
                                    fontWeight = FontWeight.Medium
                                )
                                if (review.isEdited) {
                                    Text(
                                        text = " (edited ${review.editedTimestamp?.let { viewModel.formatTimeAgo(it) } ?: "just now"})",
                                        fontSize = 12.sp,
                                        color = Color(0xFFFF6B35),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                if (isMyReview) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .rotate(rotationState)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Color(0xFF6366F1),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "Edit",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = Color(0xFF424242)
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onEditClick()
                                }
                            )
                            Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color(0xFFE53935),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "Delete",
                                            color = Color(0xFFE53935),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = review.reviewText,
                fontSize = 15.sp,
                color = Color(0xFF424242),
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun UltraCoolWriteReviewDialog(
    existingReview: ReviewModel?,
    loading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, text: String) -> Unit
) {
    var rating by remember { mutableStateOf(existingReview?.rating ?: 5) }
    var reviewText by remember { mutableStateOf(existingReview?.reviewText ?: "") }
    var hoveredStar by remember { mutableStateOf(-1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFF8F9FA)
                            )
                        )
                    )
            ) {
                // Header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFF8B5CF6),
                                    Color(0xFFA855F7)
                                )
                            )
                        )
                        .padding(24.dp)
                ) { Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (existingReview != null) "Edit Review" else "Write Review",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Share your honest experience",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(top = 6.dp, start = 40.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Your Rating",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(5) { index ->
                            val scale by animateFloatAsState(
                                targetValue = if (index < rating) 1.2f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )

                            Icon(
                                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Star ${index + 1}",
                                tint = if (index < rating) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                                modifier = Modifier
                                    .size(52.dp)
                                    .scale(scale)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { rating = index + 1 }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Your Review",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        placeholder = {
                            Text(
                                "Tell us about your experience working here...",
                                color = Color(0xFFBDBDBD),
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            cursorColor = Color(0xFF6366F1),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFFAFAFA)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 8
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE0E0E0))
                        ) {
                            Text(
                                "Cancel",
                                color = Color(0xFF757575),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Button(
                            onClick = { onSubmit(rating, reviewText) },
                            enabled = reviewText.isNotBlank() && !loading,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1),
                                disabledContainerColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    if (existingReview != null) "Update" else "Submit",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    border: androidx.compose.foundation.BorderStroke,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        border = border,
        color = Color.Transparent
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            content()
        }
    }
}
