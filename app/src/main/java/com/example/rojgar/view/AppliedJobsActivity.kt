package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.R
import com.example.rojgar.model.ApplicationModel
import com.example.rojgar.model.JobModel
import com.example.rojgar.repository.ApplicationRepoImpl
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.viewmodel.ApplicationViewModel
import com.example.rojgar.viewmodel.JobViewModel
import java.text.SimpleDateFormat
import java.util.*

class AppliedJobsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val jobSeekerId = intent.getStringExtra("JOB_SEEKER_ID") ?:
        JobSeekerRepoImpl().getCurrentJobSeeker()?.uid ?: ""

        setContent {
            AppliedJobsScreen(
                jobSeekerId = jobSeekerId,
                onBackClick = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppliedJobsScreen(
    jobSeekerId: String,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val applicationViewModel = remember { ApplicationViewModel(ApplicationRepoImpl(context)) }
    val applications by applicationViewModel.applications.observeAsState(emptyList())
    val isLoading by applicationViewModel.loading.observeAsState(false)

    var selectedApplication by remember { mutableStateOf<ApplicationModel?>(null) }
    var selectedJob by remember { mutableStateOf<JobModel?>(null) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    LaunchedEffect(jobSeekerId) {
        if (jobSeekerId.isNotEmpty()) {
            applicationViewModel.getApplicationsByJobSeeker(jobSeekerId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (selectedApplication == null) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A),
                            Color(0xFF3B82F6),
                            Color(0xFFDEEBFF)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8F9FA), Color(0xFFF8F9FA))
                    )
                }
            )
    ) {
        if (selectedApplication == null) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(40.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
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

                            Column {
                                Text(
                                    text = "Applied Jobs",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${applications.size} ${if (applications.size == 1) "Application" else "Applications"}",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Content
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                    applications.isEmpty() -> {
                        EmptyState()
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(
                                items = applications,
                                key = { it.applicationId }
                            ) { application ->
                                AppliedJobCard(
                                    application = application,
                                    onClick = {
                                        selectedApplication = application
                                    }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        } else {
            JobApplicationDetailScreen(
                application = selectedApplication!!,
                onBackClick = {
                    selectedApplication = null
                    selectedJob = null
                },
                onWithdrawClick = {
                    showWithdrawDialog = true
                },
                onJobLoaded = { job ->
                    selectedJob = job
                }
            )
        }

        // Withdraw Dialog
        if (showWithdrawDialog) {
            WithdrawApplicationDialog(
                onDismiss = { showWithdrawDialog = false },
                onConfirm = {
                    selectedApplication?.let { app ->
                        applicationViewModel.deleteApplication(app.applicationId)
                        showWithdrawDialog = false
                        selectedApplication = null
                        selectedJob = null
                    }
                }
            )
        }
    }
}

@Composable
fun JobApplicationDetailScreen(
    application: ApplicationModel,
    onBackClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onJobLoaded: (JobModel) -> Unit
) {
    val context = LocalContext.current
    val jobViewModel = remember { JobViewModel(JobRepoImpl(context)) }
    var jobPost by remember { mutableStateOf<JobModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(application.postId) {
        isLoading = true
        JobRepoImpl(context).getJobPostById(application.postId) { success, _, job ->
            if (success && job != null) {
                jobPost = job
                onJobLoaded(job)
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF00BCD4)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Application Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        },
        floatingActionButton = {
            if (application.status == "Pending" || application.status == "Reviewed") {
                ExtendedFloatingActionButton(
                    onClick = onWithdrawClick,
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Withdraw",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Withdraw Application",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Job not found", fontSize = 16.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClick) {
                        Text("Go Back")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Application Status",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Current Status",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EnhancedStatusBadge(status = application.status)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Applied On",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = formatApplicationDate(application.appliedDate),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF00BCD4)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    JobDescriptionContent(jobPost!!)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
@Composable
fun WithdrawApplicationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val infiniteTransition = rememberInfiniteTransition()
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(tween(300))
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            icon = {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(iconScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFE4E6),
                                    Color(0xFFFECDD3)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(40.dp)
                    )
                }
            },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Withdraw Application?",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color(0xFF0F172A),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(4.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFEF4444),
                                        Color(0xFFF87171)
                                    )
                                ),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Are you sure you want to withdraw this application?",
                        fontSize = 15.sp,
                        color = Color(0xFF475569),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This action cannot be undone.",
                        fontSize = 13.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Yes, Withdraw",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF0EA5E9)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0EA5E9),
                                Color(0xFF38BDF8)
                            )
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        "Cancel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        )
    }
}

@Composable
fun AppliedJobCard(
    application: ApplicationModel,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val companyRepo = remember { CompanyRepoImpl() }
    val jobViewModel = remember { JobViewModel(JobRepoImpl(context)) }

    var companyName by remember { mutableStateOf("") }
    var companyLogo by remember { mutableStateOf("") }
    var jobPost by remember { mutableStateOf<JobModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(application.companyId, application.postId) {
        isLoading = true

        companyRepo.getCompanyById(application.companyId) { success, _, company ->
            if (success && company != null) {
                companyName = company.companyName
                companyLogo = company.companyProfileImage
            }
        }

        JobRepoImpl(context).getJobPostById(application.postId) { success, _, job ->
            if (success && job != null) {
                jobPost = job
            }
            isLoading = false
        }
    }

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(600)) +
                slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) +
                scaleIn(
                    initialScale = 0.95f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp,
                pressedElevation = 6.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF0F9FF),
                                Color(0xFFFFFFFF)
                            )
                        )
                    )
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = Color(0xFF0EA5E9),
                            strokeWidth = 3.dp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left Side - Logo
                            val logoScale by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .size(75.dp)
                                    .scale(logoScale)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF0EA5E9),
                                                Color(0xFF38BDF8),
                                                Color(0xFF7DD3FC)
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                                    .padding(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White, CircleShape)
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (companyLogo.isNotEmpty()) {
                                        Image(
                                            painter = rememberAsyncImagePainter(companyLogo),
                                            contentDescription = "Company Logo",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(id = R.drawable.companynameicon),
                                            contentDescription = "Company",
                                            modifier = Modifier.size(40.dp),
                                            tint = Color(0xFF0EA5E9)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Right Side - Job Info
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                // Company Name
                                Text(
                                    text = companyName.ifEmpty { "Company Name" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0EA5E9),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Job Title
                                Text(
                                    text = jobPost?.title ?: "Job Title",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 24.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Position
                                jobPost?.position?.let { position ->
                                    if (position.isNotEmpty()) {
                                        Text(
                                            text = position,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }

                                // Job Type and Experience Row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    jobPost?.jobType?.let { jobType ->
                                        ModernChip(
                                            text = jobType,
                                            backgroundColor = Color(0xFFE0F2FE),
                                            textColor = Color(0xFF0369A1)
                                        )
                                    }

                                    jobPost?.experience?.let { exp ->
                                        ModernChip(
                                            text = exp,
                                            backgroundColor = Color(0xFFFEF3C7),
                                            textColor = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Gradient Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFFBAE6FD),
                                            Color(0xFF7DD3FC),
                                            Color(0xFFBAE6FD),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Bottom Row - Status and Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status Badge (Left)
                            EnhancedStatusBadge(status = application.status)

                            // Applied Date (Right)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.datetimeicon),
                                    contentDescription = "Date",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF0EA5E9)
                                )
                                Text(
                                    text = formatApplicationDate(application.appliedDate),
                                    fontSize = 13.sp,
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.SemiBold
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
fun ModernChip(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Surface(
        modifier = Modifier.scale(scale),
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.2.sp
        )
    }
}

@Composable
fun EnhancedStatusBadge(status: String) {
    val (backgroundColor, textColor, icon) = when (status) {
        "Pending" -> Triple(
            Color(0xFFFEF3C7),
            Color(0xFFF59E0B),
            Icons.Default.MailOutline
        )
        "Reviewed" -> Triple(
            Color(0xFFDBEAFE),
            Color(0xFF3B82F6),
            Icons.Default.CheckCircle
        )
        "Shortlisted" -> Triple(
            Color(0xFFE9D5FF),
            Color(0xFF9333EA),
            Icons.Default.CheckCircle
        )
        "Accepted" -> Triple(
            Color(0xFFD1FAE5),
            Color(0xFF10B981),
            Icons.Default.CheckCircle
        )
        "Rejected" -> Triple(
            Color(0xFFFFE4E6),
            Color(0xFFEF4444),
            Icons.Default.Close
        )
        else -> Triple(
            Color(0xFFF1F5F9),
            Color(0xFF64748B),
            Icons.Default.MailOutline
        )
    }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .then(
                if (status == "Accepted" || status == "Shortlisted")
                    Modifier.scale(pulseScale)
                else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = status,
                modifier = Modifier.size(16.dp),
                tint = textColor
            )
            Text(
                text = status,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                letterSpacing = 0.3.sp
            )
        }
    }
}


@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.jobpost_filled),
                    contentDescription = "No Applications",
                    modifier = Modifier.size(60.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Applications Yet",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start applying to jobs and track them here",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

fun formatApplicationDate(timestamp: Long): String {
    return try {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else -> {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    } catch (e: Exception) {
        "N/A"
    }
}