package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.rojgar.model.JobModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class ActiveJob : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ActiveJobScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveJobScreen() {
    val context = LocalContext.current
    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }

    val currentUser = companyViewModel.getCurrentCompany()
    val companyId = currentUser?.uid ?: ""

    val jobPosts by jobViewModel.company.observeAsState(emptyList())
    val isLoading by jobViewModel.loading.observeAsState(false)

    // Filter active jobs based on deadline
    val activeJobs = remember(jobPosts) {
        jobPosts.filter { job ->
            isJobActive(job.deadline)
        }
    }

    LaunchedEffect(Unit) {
        if (companyId.isNotEmpty()) {
            jobViewModel.getJobPostsByCompanyId(companyId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Active Jobs",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Animated Header Section
                var headerVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(100)
                    headerVisible = true
                }

                AnimatedVisibility(
                    visible = headerVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -40 },
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(500))
                ) {
                    ActiveJobsHeader(activeJobCount = activeJobs.size)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Content Section
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = DarkBlue2,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading active jobs...",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    activeJobs.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyActiveJobsView()
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(
                                items = activeJobs,
                                key = { _, job -> job.postId }
                            ) { index, job ->
                                var cardVisible by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) {
                                    delay(index * 50L)
                                    cardVisible = true
                                }

                                AnimatedVisibility(
                                    visible = cardVisible,
                                    enter = slideInHorizontally(
                                        initialOffsetX = { 100 },
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    ) + fadeIn()
                                ) {
                                    ActiveJobCard(
                                        jobPost = job,
                                        daysRemaining = getDaysRemaining(job.deadline)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ActiveJobsHeader(activeJobCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Currently Active",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$activeJobCount",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (activeJobCount == 1) "Job" else "Jobs",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color(0xFF2196F3).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.jobpost_filled),
                    contentDescription = "Active Jobs",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun ActiveJobCard(
    jobPost: JobModel,
    daysRemaining: Int
) {
    val context = LocalContext.current
    val displayImageUrl = jobPost.hiringBanner.ifEmpty { jobPost.imageUrl }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, ApplicationActivity::class.java).apply {
                    putExtra("JOB_POST_ID", jobPost.postId)
                    putExtra("JOB_TITLE", jobPost.title)
                    putExtra("COMPANY_ID", jobPost.companyId)
                }
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Image Section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (displayImageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(displayImageUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.jobpost_filled),
                        contentDescription = "No Image",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content Section
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = jobPost.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = jobPost.position,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (jobPost.jobType.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = jobPost.jobType,
                                fontSize = 12.sp,
                                color = DarkBlue2,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Deadline Indicator
                Surface(
                    color = when {
                        daysRemaining <= 3 -> Color(0xFFFFEBEE)
                        daysRemaining <= 7 -> Color(0xFFFFF3E0)
                        else -> Color(0xFFE8F5E9)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.deadlineicon),
                            contentDescription = "Deadline",
                            tint = when {
                                daysRemaining <= 3 -> Color(0xFFD32F2F)
                                daysRemaining <= 7 -> Color(0xFFF57C00)
                                else -> Color(0xFF388E3C)
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                daysRemaining == 0 -> "Expires today"
                                daysRemaining == 1 -> "1 day left"
                                else -> "$daysRemaining days left"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                daysRemaining <= 3 -> Color(0xFFD32F2F)
                                daysRemaining <= 7 -> Color(0xFFF57C00)
                                else -> Color(0xFF388E3C)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyActiveJobsView() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = Color(0xFF2196F3).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(60.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.jobpost_filled),
                    contentDescription = "No Active Jobs",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Active Jobs",
                fontSize = 24.sp,
                color = Color(0xFF424242),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "All your job posts have expired\nor you haven't created any yet",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Helper Functions
fun isJobActive(deadline: String): Boolean {
    if (deadline.isEmpty()) return true // If no deadline, consider it active

    return try {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val deadlineDate = dateFormat.parse(deadline) ?: return false
        val currentDate = Date()
        currentDate.time <= deadlineDate.time
    } catch (e: Exception) {
        true // If parsing fails, consider it active
    }
}

fun getDaysRemaining(deadline: String): Int {
    if (deadline.isEmpty()) return Int.MAX_VALUE

    return try {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val deadlineDate = dateFormat.parse(deadline) ?: return Int.MAX_VALUE
        val currentDate = Date()
        val diffInMillis = deadlineDate.time - currentDate.time
        val daysRemaining = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        maxOf(0, daysRemaining)
    } catch (e: Exception) {
        Int.MAX_VALUE
    }
}