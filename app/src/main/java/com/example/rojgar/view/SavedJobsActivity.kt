package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.R
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.repository.SavedJobRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobViewModel
import com.example.rojgar.viewmodel.JobViewModelFactory
import com.example.rojgar.viewmodel.SavedJobViewModel
import com.example.rojgar.viewmodel.SavedJobViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class SavedJobsActivity : ComponentActivity() {

    private val savedJobViewModel by viewModels<SavedJobViewModel> {
        SavedJobViewModelFactory(SavedJobRepoImpl())
    }

    private val jobViewModel by viewModels<JobViewModel> {
        JobViewModelFactory(JobRepoImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SavedJobsBody(
                savedJobViewModel = savedJobViewModel,
                jobViewModel = jobViewModel,
                onBackPressed = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedJobsBody(
    savedJobViewModel: SavedJobViewModel,
    jobViewModel: JobViewModel,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }

    val savedJobs by savedJobViewModel.savedJobs.observeAsState(emptyList())
    val savedJobIds by savedJobViewModel.savedJobIds.observeAsState(emptySet())
    val loading by savedJobViewModel.loading.observeAsState(false)

    var jobDetailsMap by remember { mutableStateOf<Map<String, JobModel>>(emptyMap()) }
    var companies by remember { mutableStateOf<Map<String, CompanyModel>>(emptyMap()) }
    var isLoadingData by remember { mutableStateOf(true) }

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )

    LaunchedEffect(Unit) {
        savedJobViewModel.loadSavedJobs()

        jobViewModel.getAllJobPosts { success, message, posts ->
            if (success && posts != null) {
                jobDetailsMap = posts.associateBy { it.postId }

                // Load companies
                companyViewModel.getAllCompany { companySuccess, _, companyList ->
                    if (companySuccess && companyList != null) {
                        companies = companyList.associateBy { it.companyId }
                    }
                    isLoadingData = false
                }
            } else {
                isLoadingData = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFBBDEFB),
                        Blue
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom Top Bar with gradient
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
                        onClick = onBackPressed,
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
                            text = "Saved Jobs",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        AnimatedVisibility(visible = !loading && !isLoadingData) {
                            Text(
                                text = "${savedJobs.size} ${if (savedJobs.size == 1) "job" else "jobs"} saved",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // Animated bookmark icon
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "bookmark"
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.save_filled),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .scale(scale)
                    )
                }
            }

            // Content
            if (loading || isLoadingData) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF1976D2),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading saved jobs...",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (savedJobs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.save_outline),
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "No Saved Jobs Yet",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start saving jobs to view them here",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(
                        items = savedJobs,
                        key = { _, savedJob -> savedJob.jobId }
                    ) { index, savedJob ->
                        val jobDetails = jobDetailsMap[savedJob.jobId]
                        val company = jobDetails?.let { companies[it.companyId] }

                        // Animate items appearing
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(index * 50L)
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(animationSpec = tween(300)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 2 },
                                        animationSpec = tween(300)
                                    )
                        ) {
                            if (jobDetails != null) {
                                Column {
                                    SavedJobCard(
                                        job = jobDetails,
                                        company = company,
                                        savedAt = savedJob.savedAt,
                                        isSaved = savedJobIds.contains(savedJob.jobId),
                                        onClick = {
                                            val intent = Intent(context, JobApplyActivity::class.java).apply {
                                                putExtra("POST_ID", jobDetails.postId)
                                                putExtra("COMPANY_ID", jobDetails.companyId)
                                            }
                                            context.startActivity(intent)
                                        },
                                        onUnsaveClick = {
                                            savedJobViewModel.unsaveJob(savedJob.jobId) { success, message ->
                                                if (success) {
                                                    Toast.makeText(
                                                        context,
                                                        "Job unsaved successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed: $message",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        },
                                        onShareClick = {
                                            val shareIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "Check out this job: ${jobDetails.title}\n" +
                                                            "Company: ${company?.companyName ?: ""}\n" +
                                                            "Position: ${jobDetails.position}\n" +
                                                            "Type: ${jobDetails.jobType}\n" +
                                                            "Deadline: ${jobDetails.deadline}"
                                                )
                                                type = "text/plain"
                                            }
                                            context.startActivity(
                                                Intent.createChooser(shareIntent, "Share Job")
                                            )
                                        }
                                    )

                                    // Saved date below card
                                    Text(
                                        text = "Saved on ${formatSavedTimestamp(savedJob.savedAt)}",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SavedJobCard(
    job: JobModel,
    company: CompanyModel?,
    savedAt: Long,
    isSaved: Boolean,
    onClick: () -> Unit,
    onUnsaveClick: () -> Unit,
    onShareClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Company Profile Image
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                            .border(1.dp, Color(0xFFBDBDBD), CircleShape)
                    ) {
                        if (company?.companyProfileImage?.isNotEmpty() == true) {
                            Image(
                                painter = rememberAsyncImagePainter(company.companyProfileImage),
                                contentDescription = "Company Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.jobpost_filled),
                                contentDescription = "Company",
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.Center),
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = company?.companyName ?: "Unknown Company",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    painter = painterResource(id = R.drawable.save_filled),
                    contentDescription = "Unsave Job",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onUnsaveClick() },
                    tint = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Job Image
                Card(
                    modifier = Modifier
                        .width(150.dp)
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (job.imageUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(job.imageUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF00BCD4),
                                                Color(0xFF00ACC1)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.jobpost_filled),
                                        contentDescription = "Job",
                                        modifier = Modifier.size(40.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "we're\nhiring.",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Job Details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = job.title,
                            fontSize = 18.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = job.position,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF00BCD4).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = job.jobType,
                                fontSize = 12.sp,
                                color = Color(0xFF00BCD4),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Icon(
                            painter = painterResource(id = R.drawable.shareicon),
                            contentDescription = "Share Job",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onShareClick() },
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

fun formatSavedTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}