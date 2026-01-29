package com.example.rojgar.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.model.ApplicationModel
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobModel
import com.example.rojgar.repository.ApplicationRepoImpl
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.viewmodel.ApplicationViewModel
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobViewModel
import java.text.SimpleDateFormat
import java.util.*

class FeedbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val jobSeekerId = intent.getStringExtra("JOB_SEEKER_ID") ?: ""
        setContent {
            FeedbackBody(
                jobSeekerId = jobSeekerId,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackBody(
    jobSeekerId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val applicationViewModel = remember { ApplicationViewModel(ApplicationRepoImpl(context)) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val jobViewModel = remember { JobViewModel(JobRepoImpl(context)) }

    val applications by applicationViewModel.applications.observeAsState(emptyList())
    val isLoading by applicationViewModel.loading.observeAsState(false)

    // Debug logging
    LaunchedEffect(applications) {
        Log.d("FeedbackActivity", "Total applications: ${applications.size}")
        applications.forEach { app ->
            Log.d("FeedbackActivity", "App ID: ${app.applicationId}, Status: ${app.status}, Feedback: ${app.rejectionFeedback}")
        }
    }

    val rejectedApplications = remember(applications) {
        applications.filter {
            Log.d("FeedbackActivity", "Filtering - Status: ${it.status}, Feedback: ${it.rejectionFeedback}")
            it.status == "Rejected" && !it.rejectionFeedback.isNullOrEmpty()
        }.sortedByDescending { it.rejectionDate ?: it.appliedDate }
    }

    LaunchedEffect(jobSeekerId) {
        Log.d("FeedbackActivity", "Fetching applications for jobSeekerId: $jobSeekerId")
        if (jobSeekerId.isNotEmpty()) {
            applicationViewModel.getApplicationsByJobSeeker(jobSeekerId)
        }
    }

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
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom Top Bar with gradient - Same as SavedJobsActivity
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
                        onClick = onBack,
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
                            text = "Application Feedback",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        AnimatedVisibility(visible = !isLoading && rejectedApplications.isNotEmpty()) {
                            Text(
                                text = "${rejectedApplications.size} ${if (rejectedApplications.size == 1) "feedback" else "feedbacks"}",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Content
            when {
                isLoading -> {
                    LoadingAnimation()
                }
                rejectedApplications.isEmpty() -> {
                    EmptyFeedbackState(totalApplications = applications.size)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {

                        items(
                            items = rejectedApplications,
                            key = { it.applicationId }
                        ) { application ->
                            FeedbackCard(
                                application = application,
                                companyViewModel = companyViewModel,
                                jobViewModel = jobViewModel
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun FeedbackCard(
    application: ApplicationModel,
    companyViewModel: CompanyViewModel,
    jobViewModel: JobViewModel
) {
    var company by remember { mutableStateOf<CompanyModel?>(null) }
    var jobPost by remember { mutableStateOf<JobModel?>(null) }
    var isLoadingData by remember { mutableStateOf(true) }
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "scale"
    )

    LaunchedEffect(application.companyId, application.postId) {
        visible = true
        isLoadingData = true

        Log.d("FeedbackCard", "Loading data for application: ${application.applicationId}")
        Log.d("FeedbackCard", "CompanyId: ${application.companyId}, PostId: ${application.postId}")

        // Fetch company details
        companyViewModel.getCompanyById(application.companyId) { success, msg, data ->
            Log.d("FeedbackCard", "Company fetch: success=$success, data=${data?.companyName}")
            if (success && data != null) {
                company = data
            }
        }

        // Fetch job post details
        jobViewModel.getJobPostById(application.postId)

        // Wait for LiveData to update
        kotlinx.coroutines.delay(800)
        jobPost = jobViewModel.job.value
        Log.d("FeedbackCard", "Job post: ${jobPost?.title}")
        isLoadingData = false
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        if (isLoadingData) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = DarkBlue2,
                    strokeWidth = 3.dp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Company Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Company Logo
                    Box(
                        modifier = Modifier.size(70.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(66.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Blue.copy(alpha = 0.1f),
                                            DarkBlue2.copy(alpha = 0.1f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!company?.companyProfileImage.isNullOrEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(company?.companyProfileImage),
                                    contentDescription = "Company Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountBox,
                                    contentDescription = "Company",
                                    tint = DarkBlue2.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = company?.companyName ?: "Company",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = company?.companyEmail ?: "",
                            fontSize = 13.sp,
                            color = Color(0xFF666666),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Job Details Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8F9FA)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        DetailRow(
                            icon = Icons.Default.AddCircle,
                            label = "Position",
                            value = jobPost?.title ?: jobPost?.position ?: "N/A",
                            iconTint = Color(0xFF1976D2),
                            iconBackground = Color(0xFFE3F2FD)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        DetailRow(
                            icon = Icons.Default.DateRange,
                            label = "Applied Date",
                            value = formatDate(application.appliedDate),
                            iconTint = Color(0xFFF57F17),
                            iconBackground = Color(0xFFFFF8E1)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        DetailRow(
                            icon = Icons.Default.DateRange,
                            label = "Rejection Date",
                            value = formatTime(application.rejectionDate ?: application.appliedDate),
                            iconTint = Color(0xFFD32F2F),
                            iconBackground = Color(0xFFFFEBEE)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Badge
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD32F2F))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "APPLICATION REJECTED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Feedback Section
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFF3E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Feedback",
                                tint = Color(0xFFF57C00),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Company Feedback",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFFBF5)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFE0B2),
                                    Color(0xFFFFCC80)
                                )
                            )
                        )
                    ) {
                        Text(
                            text = application.rejectionFeedback ?: "No feedback provided",
                            fontSize = 14.sp,
                            color = Color(0xFF424242),
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    iconBackground: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF888888),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LoadingAnimation() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = DarkBlue2,
                strokeWidth = 4.dp,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading feedback...",
                fontSize = 15.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyFeedbackState(totalApplications: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800), label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer(alpha = alpha)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Blue.copy(alpha = 0.2f),
                                Blue.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "No Feedback",
                    modifier = Modifier.size(60.dp),
                    tint = DarkBlue2.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Feedback Yet",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "You haven't received any rejection feedback from companies.",
                fontSize = 15.sp,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            if (totalApplications > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "You have $totalApplications total application(s)",
                    fontSize = 13.sp,
                    color = Color(0xFF999999),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Keep applying to opportunities!",
                fontSize = 14.sp,
                color = DarkBlue2,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

//fun formatDate(timestamp: Long): String {
//    return try {
//        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
//        sdf.format(Date(timestamp))
//    } catch (e: Exception) {
//        "N/A"
//    }
//}

fun formatTime(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "N/A"
    }
}