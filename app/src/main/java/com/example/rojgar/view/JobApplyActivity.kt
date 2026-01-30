package com.example.rojgar.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.R
import com.example.rojgar.model.ApplicationModel
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobModel
import com.example.rojgar.model.ReviewModel
import com.example.rojgar.repository.ApplicationRepoImpl
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.repository.ReviewRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.viewmodel.ApplicationViewModel
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobViewModel
import com.example.rojgar.viewmodel.ReviewViewModel
import com.example.rojgar.viewmodel.ReviewViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp

class JobApplyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val postId = intent.getStringExtra("POST_ID") ?: ""
        val companyId = intent.getStringExtra("COMPANY_ID") ?: ""

        setContent {
            JobApplyBody(postId = postId, companyId = companyId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobApplyBody(postId: String, companyId: String) {
    val context = LocalContext.current

    val jobViewModel = remember { JobViewModel(JobRepoImpl(context)) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val applicationViewModel = remember { ApplicationViewModel(ApplicationRepoImpl(context)) }

    val reviewViewModel: ReviewViewModel = viewModel(
        factory = ReviewViewModelFactory(ReviewRepoImpl())
    )


    var selectedTab by remember { mutableStateOf("Job Description") }
    var jobPost by remember { mutableStateOf<JobModel?>(null) }
    var company by remember { mutableStateOf<CompanyModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // User profile data
    var userName by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var userProfile by remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""

    // Observe hasApplied from ViewModel
    val hasApplied by applicationViewModel.hasApplied.observeAsState(false)
    val isCheckingApplication by applicationViewModel.loading.observeAsState(false)

    // Top bar scroll state for animations
    var topBarElevation by remember { mutableStateOf(0.dp) }
    val animatedElevation by animateDpAsState(
        targetValue = topBarElevation,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "elevation"
    )

    // Check if user has already applied when screen loads
    LaunchedEffect(currentUserId, postId) {
        if (currentUserId.isNotEmpty() && postId.isNotEmpty()) {
            applicationViewModel.checkIfApplied(currentUserId, postId)
        }
    }

    // Load reviews when companyId is available
    LaunchedEffect(companyId) {
        if (companyId.isNotEmpty()) {
            reviewViewModel.loadReviews(companyId)
            reviewViewModel.checkUserReview(currentUserId, companyId)
        }
    }

    // Fetch user profile from Firebase Database
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            val database = FirebaseDatabase.getInstance()

            val jobSeekerRef = database.getReference("JobSeekers").child(user.uid)
            jobSeekerRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    userName = snapshot.child("fullName").getValue(String::class.java) ?: "Unknown User"
                    userPhone = snapshot.child("phoneNumber").getValue(String::class.java) ?: ""
                    userProfile = snapshot.child("profilePhoto").getValue(String::class.java) ?: ""
                } else {
                    val companyRef = database.getReference("Companys").child(user.uid)
                    companyRef.get().addOnSuccessListener { companySnapshot ->
                        if (companySnapshot.exists()) {
                            userName = companySnapshot.child("companyName").getValue(String::class.java) ?: "Unknown User"
                            userPhone = companySnapshot.child("companyContactNumber").getValue(String::class.java) ?: ""
                            userProfile = companySnapshot.child("companyProfileImage").getValue(String::class.java) ?: ""
                        } else {
                            userName = user.displayName ?: "Unknown User"
                            userPhone = user.phoneNumber ?: ""
                            userProfile = user.photoUrl?.toString() ?: ""
                        }
                    }
                }
            }.addOnFailureListener {
                userName = user.displayName ?: "Unknown User"
                userPhone = user.phoneNumber ?: ""
                userProfile = user.photoUrl?.toString() ?: ""
            }
        }
    }

    LaunchedEffect(postId, companyId) {
        if (postId.isNotEmpty()) {
            jobViewModel.getJobPostById(postId)
        }
        if (companyId.isNotEmpty()) {
            companyViewModel.getCompanyDetails(companyId)
        }
    }

    val jobData by jobViewModel.job.observeAsState()
    val companyData by companyViewModel.companyDetails.observeAsState()
    val loading by jobViewModel.loading.observeAsState(false)

    LaunchedEffect(jobData, companyData, loading) {
        jobPost = jobData
        company = companyData
        isLoading = loading
    }

    Scaffold(
        topBar = {
            StylishTopBar(
                jobTitle = jobPost?.title ?: "Job Details",
                onBackClick = { (context as? ComponentActivity)?.finish() },
                onShareClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, jobPost?.title ?: "Job Opportunity")
                        putExtra(Intent.EXTRA_TEXT,
                            """
                            🎯 Job Opportunity: ${jobPost?.title}
                            
                            📍 Position: ${jobPost?.position}
                            🏢 Company: ${company?.companyName}
                            💼 Type: ${jobPost?.jobType}
                            📚 Experience: ${jobPost?.experience}
                            💰 Salary: ${jobPost?.salary?.ifEmpty { "Negotiable" }}
                            
                            Apply now through Rojgar app!
                            """.trimIndent()
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Job"))
                },
                elevation = animatedElevation
            )
        },
        bottomBar = {
            if (!isLoading && jobPost != null) {
                AnimatedApplyButton(
                    hasApplied = hasApplied,
                    isLoading = isCheckingApplication,
                    onApplyClick = {
                        if (currentUser == null) {
                            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                            return@AnimatedApplyButton
                        }

                        if (hasApplied) {
                            Toast.makeText(context, "You have already applied for this job", Toast.LENGTH_SHORT).show()
                            return@AnimatedApplyButton
                        }

                        val application = ApplicationModel(
                            applicationId = "",
                            postId = postId,
                            companyId = companyId,
                            jobSeekerId = currentUser.uid,
                            jobSeekerName = userName,
                            jobSeekerEmail = currentUser.email ?: "",
                            jobSeekerPhone = userPhone,
                            jobSeekerProfile = userProfile,
                            appliedDate = System.currentTimeMillis(),
                            status = "Pending",
                            coverLetter = "",
                            resumeUrl = ""
                        )

                        applicationViewModel.applyForJob(application)
                        applicationViewModel.checkIfApplied(currentUserId, postId)
                        Toast.makeText(context, "Application submitted successfully!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00BCD4))
            }
        } else if (jobPost == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Job not found", fontSize = 16.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(padding)
            ) {
                item {
                    TabSection(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }

                item {
                    when (selectedTab) {
                        "Job Description" -> JobDescriptionContent(jobPost!!)
                        "About Company" -> AboutCompanyContent(company)
                        "Reviews" -> ReviewsContent(
                            viewModel = reviewViewModel,
                            companyId = companyId,
                            currentUserId = currentUserId
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylishTopBar(
    jobTitle: String,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    elevation: Dp = 0.dp
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = elevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2196F3),
                            Color(0xFF1976D2)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button with ripple effect
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(scale),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                    onClick = onBackClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Job Title with animation
                Text(
                    text = jobTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )

                // Share Button with ripple effect
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(scale),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                    onClick = onShareClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Bottom accent line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

// Animated Apply Button
@Composable
fun AnimatedApplyButton(
    hasApplied: Boolean,
    isLoading: Boolean,
    onApplyClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isLoading -> Color(0xFF9E9E9E)
            hasApplied -> Color(0xFF4CAF50)
            else -> Color(0xFF00BCD4)
        },
        animationSpec = tween(300),
        label = "backgroundColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (hasApplied) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = onApplyClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp)
                .scale(scale),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                disabledContainerColor = Color(0xFF9E9E9E)
            ),
            enabled = !hasApplied && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasApplied) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = when {
                            hasApplied -> "Applied ✓"
                            else -> "Apply Now"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun TabSection(selectedTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("Job Description", "About Company", "Reviews")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            TabButton(
                text = tab,
                isSelected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) Color(0xFF00BCD4) else Color(0xFFF5F5F5)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun JobDescriptionContent(jobPost: JobModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Job Title Card - Gradient Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00BCD4),
                                Color(0xFF00ACC1)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "JOB TITLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = jobPost.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 30.sp
                    )
                }
            }
        }

        // Position & Job Type Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                modifier = Modifier.weight(1f),
                label = "POSITION",
                value = jobPost.position,
                icon = R.drawable.jobpost_filled,
                backgroundColor = Color(0xFFFFF3E0),
                iconTint = Color(0xFFFF9800)
            )
            InfoCard(
                modifier = Modifier.weight(1f),
                label = "JOB TYPE",
                value = jobPost.jobType,
                icon = R.drawable.jobtype,
                backgroundColor = Color(0xFFE8F5E9),
                iconTint = Color(0xFF4CAF50)
            )
        }

        // Categories Card
        if (jobPost.categories.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.jobcategoryicon),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF00BCD4)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "JOB CATEGORIES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00BCD4),
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        jobPost.categories.forEach { category ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF00BCD4).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF00BCD4),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Experience & Education Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                modifier = Modifier.weight(1f),
                label = "EXPERIENCE",
                value = jobPost.experience,
                icon = R.drawable.experience_filled,
                backgroundColor = Color(0xFFF3E5F5),
                iconTint = Color(0xFF9C27B0)
            )
            InfoCard(
                modifier = Modifier.weight(1f),
                label = "EDUCATION",
                value = jobPost.education,
                icon = R.drawable.educationboardicon,
                backgroundColor = Color(0xFFE3F2FD),
                iconTint = Color(0xFF2196F3)
            )
        }

        // Skills Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.skills_filledicon),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF00BCD4)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REQUIRED SKILLS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00BCD4),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = jobPost.skills.ifEmpty { "Not specified" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    lineHeight = 22.sp
                )
            }
        }

        // Salary Card - Premium Style
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SALARY RANGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = jobPost.salary.ifEmpty { "Negotiable" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "💰",
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }

        // Deadline Card - Urgent Style
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF7566))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "APPLICATION DEADLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = jobPost.deadline.ifEmpty { "Not specified" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "⏰",
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }

        // Job Description Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF00BCD4).copy(alpha = 0.1f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.jobdescriptionicon),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF00BCD4)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Job Description",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = jobPost.jobDescription.ifEmpty { "No description available" },
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
            }
        }

        // Responsibilities Section
        if (jobPost.responsibilities.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF9800).copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.responsibilityicon),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFFFF9800)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Responsibilities",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = jobPost.responsibilities,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: Int,
    backgroundColor: Color,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = backgroundColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value.ifEmpty { "Not specified" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun AboutCompanyContent(company: CompanyModel?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (company == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🏢", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Company Not Found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Company information is not available",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Company Header Card with Logo and Cover
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Cover Photo Background
                    if (company.companyCoverPhoto.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(company.companyCoverPhoto),
                            contentDescription = "Company Cover",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF00BCD4),
                                            Color(0xFF00ACC1)
                                        )
                                    )
                                )
                        )
                    }

                    // Company Logo and Name
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 70.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Logo with border
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 8.dp
                        ) {
                            if (company.companyProfileImage.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(company.companyProfileImage),
                                    contentDescription = "Company Logo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = company.companyName.firstOrNull()?.toString() ?: "C",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00BCD4)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Company Name
                        Text(
                            text = company.companyName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        // Company Tagline
                        if (company.companyTagline.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = company.companyTagline,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            // Company Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00BCD4).copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "ℹ️", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "About Company",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = company.companyInformation.ifEmpty { "No information available" },
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )
                }
            }

            // Company Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (company.companyIndustry.isNotEmpty()) {
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        label = "INDUSTRY",
                        value = company.companyIndustry,
                        icon = R.drawable.jobcategoryicon,
                        backgroundColor = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF2196F3)
                    )
                }
                if (company.companyEstablishedDate.isNotEmpty()) {
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        label = "ESTABLISHED",
                        value = company.companyEstablishedDate,
                        icon = R.drawable.experience_filled,
                        backgroundColor = Color(0xFFFFF3E0),
                        iconTint = Color(0xFFFF9800)
                    )
                }
            }

            // Location Card
            if (company.companyLocation.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "📍", fontSize = 24.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "LOCATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = company.companyLocation,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Contact Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF9C27B0).copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "📞", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Contact Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    if (company.companyEmail.isNotEmpty()) {
                        ContactInfoRow(
                            icon = "✉️",
                            label = "Email",
                            value = company.companyEmail
                        )
                    }

                    // Phone
                    if (company.companyContactNumber.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ContactInfoRow(
                            icon = "📱",
                            label = "Phone",
                            value = company.companyContactNumber
                        )
                    }

                    // Website
                    if (company.companyWebsite.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ContactInfoRow(
                            icon = "🌐",
                            label = "Website",
                            value = company.companyWebsite
                        )
                    }
                }
            }

            // Specialties Card
            if (company.companySpecialties.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFF9800).copy(alpha = 0.1f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.skills_filledicon),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color(0xFFFF9800)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Specialties",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            company.companySpecialties.forEach { specialty ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFF9800).copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "•", fontSize = 16.sp, color = Color(0xFFFF9800))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = specialty,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
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

@Composable
fun ContactInfoRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ReviewsContent(
    viewModel: ReviewViewModel,
    companyId: String,
    currentUserId: String
) {
    val reviews by viewModel.reviews.observeAsState(emptyList())
    val averageRating by viewModel.averageRating.observeAsState(0.0)
    val loading by viewModel.loading.observeAsState(false)
    val jobSeekerUsernames by viewModel.jobSeekerUsernames.observeAsState(emptyMap())

    // Setup real-time updates when composable is first created
    LaunchedEffect(companyId) {
        if (companyId.isNotEmpty()) {
            viewModel.setupRealTimeUpdates(companyId, currentUserId)
        }
    }

    // Cleanup listener when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            // Listener will be cleaned up in ViewModel.onCleared()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Average Rating Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00BCD4),
                                Color(0xFF00ACC1)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "OVERALL RATING",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("%.1f", averageRating),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "/ 5",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { index ->
                                val scale by animateFloatAsState(
                                    targetValue = if (index < averageRating.toInt()) 1f else 0.9f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ), label = ""
                                )
                                Icon(
                                    imageVector = if (index < averageRating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(end = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Based on ${reviews.size} review${if (reviews.size != 1) "s" else ""}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = reviews.size.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Reviews",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reviews List
        if (loading && reviews.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00BCD4))
                }
            }
        } else if (reviews.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📝", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Reviews Yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Be the first to share your experience!",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Add a header for the reviews list
            Text(
                text = "All Reviews (${reviews.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            reviews.forEach { review ->
                SimpleReviewCard(
                    review = review,
                    username = jobSeekerUsernames[review.userId] ?: review.userName,
                    viewModel = viewModel,
                    isOwnReview = review.userId == currentUserId
                )
            }
        }
    }
}

@Composable
fun SimpleReviewCard(
    review: ReviewModel,
    username: String,
    viewModel: ReviewViewModel,
    isOwnReview: Boolean
) {
    val primaryBlue = Color(0xFF00BCD4)

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isOwnReview) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "You",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        viewModel.formatTimeAgo(review.timestamp) + viewModel.getEditedLabel(review),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Rating stars
            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < review.rating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (index < review.rating) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                review.reviewText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ApplyNowButton(hasApplied: Boolean, onApplyClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = onApplyClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasApplied) Color.Gray else Color(0xFF00BCD4),
                disabledContainerColor = Color.Gray
            ),
            enabled = !hasApplied
        ) {
            Text(
                text = if (hasApplied) "Applied" else "Apply Now",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview
@Composable
fun JobApplyPreview() {
    JobApplyBody(postId = "", companyId = "")
}