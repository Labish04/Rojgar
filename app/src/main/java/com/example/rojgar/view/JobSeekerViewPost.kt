package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.R
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobViewModel
import java.text.SimpleDateFormat
import java.util.*

class JobSeekerViewPost : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerViewPostBody()
        }
    }
}

@Composable
fun JobSeekerViewPostBody() {
    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var jobs by remember { mutableStateOf<List<JobModel>>(emptyList()) }
    var savedJobIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var companies by remember { mutableStateOf<Map<String, CompanyModel>>(emptyMap()) }


    // Fetch jobs first
    LaunchedEffect(Unit) {
        jobViewModel.getAllJobPosts { success, message, posts ->
            if (!success || posts == null) {
                isLoading = false
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                return@getAllJobPosts
            }

            val activePosts = posts.filter {
                it.postId.isNotEmpty() && !isDeadlineExpired(it.deadline)
            }
            jobs = activePosts

            // Now fetch all companies
            companyViewModel.getAllCompany { success, message, companyList ->
                if (success && companyList != null) {
                    companies = companyList.associateBy { it.companyId }
                } else {
                    Toast.makeText(context, "Failed to load company details: $message", Toast.LENGTH_SHORT).show()
                }
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Available Jobs",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "${jobs.size} jobs available",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (jobs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No jobs found",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = jobs,
                    key = { job -> job.postId }
                ) { job ->
                    val company = companies[job.companyId]

                    JobSeekerPostCard(
                        job = job,
                        companyName = company?.companyName ?: "Unknown Company",
                        companyProfile = company?.companyProfileImage ?: "",
                        isLoadingCompany = company == null && job.companyId.isNotEmpty(),
                        onClick = {
                            if (job.postId.isNotEmpty()) {
                                val intent = Intent(context, JobApplyActivity::class.java).apply {
                                    putExtra("POST_ID", job.postId)
                                    putExtra("COMPANY_ID", job.companyId)
                                }
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Job ID is empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        isSaved = savedJobIds.contains(job.postId),
                        onSaveClick = { postId ->
                            if (postId.isNotEmpty()) {
                                savedJobIds = if (savedJobIds.contains(postId)) {
                                    savedJobIds - postId
                                } else {
                                    savedJobIds + postId
                                }
                                val message = if (savedJobIds.contains(postId)) {
                                    "Job saved"
                                } else {
                                    "Job unsaved"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onShareClick = { jobToShare ->
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out this job: ${jobToShare.title}\n" +
                                            "Company: ${company?.companyName ?: ""}\n" +
                                            "Position: ${jobToShare.position}\n" +
                                            "Type: ${jobToShare.jobType}\n" +
                                            "Deadline: ${jobToShare.deadline}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(
                                Intent.createChooser(shareIntent, "Share Job")
                            )
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun JobSeekerPostCard(
    job: JobModel,
    companyName: String,
    companyProfile: String,
    isLoadingCompany: Boolean,
    onClick: () -> Unit,
    onSaveClick: (String) -> Unit = {},
    onShareClick: (JobModel) -> Unit = {},
    isSaved: Boolean = false
) {
    val daysUntilDeadline = calculateDaysUntilDeadline(job.deadline)
    val isUrgent = daysUntilDeadline in 1..3

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        when {
                            isLoadingCompany -> {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.Center),
                                    strokeWidth = 2.dp
                                )
                            }
                            companyProfile.isNotEmpty() -> {
                                Image(
                                    painter = rememberAsyncImagePainter(companyProfile),
                                    contentDescription = "Company Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
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
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (isLoadingCompany) "Loading..." else companyName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    painter = painterResource(
                        id = if (isSaved) R.drawable.save_filled
                        else R.drawable.save_outline
                    ),
                    contentDescription = "Save Job",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (job.postId.isNotEmpty()) {
                                onSaveClick(job.postId)
                            }
                        },
                    tint = if (isSaved) Color.Black else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .width(150.dp)
                            .height(120.dp),
                        shape = RoundedCornerShape(8.dp)
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
                                        .background(Color(0xFF00BCD4)),
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

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp),
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

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = job.position,
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(12.dp))

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
                                        .clickable { onShareClick(job) },
                                    tint = Color.Gray
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
fun JobSeekerPostCard(
    jobPostWithCompany: JobPostWithCompany,
    onClick: () -> Unit,
    onSaveClick: (String) -> Unit = {},
    onShareClick: (JobModel) -> Unit = {},
    isSaved: Boolean = false
) {
    JobSeekerPostCard(
        job = jobPostWithCompany.jobPost,
        companyName = jobPostWithCompany.companyName,
        companyProfile = jobPostWithCompany.companyProfile,
        isLoadingCompany = jobPostWithCompany.isLoading,
        onClick = onClick,
        onSaveClick = onSaveClick,
        onShareClick = onShareClick,
        isSaved = isSaved
    )
}

fun isDeadlineExpired(deadline: String): Boolean {
    if (deadline.isEmpty()) return false

    return try {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val deadlineDate = dateFormat.parse(deadline)
        val currentDate = Date()
        deadlineDate?.before(currentDate) ?: false
    } catch (e: Exception) {
        false
    }
}

fun calculateDaysUntilDeadline(deadline: String): Int {
    if (deadline.isEmpty()) return -1

    return try {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val deadlineDate = dateFormat.parse(deadline)
        val currentDate = Date()

        if (deadlineDate != null) {
            val diffInMillis = deadlineDate.time - currentDate.time
            val days = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
            if (days < 0) 0 else days
        } else {
            -1
        }
    } catch (e: Exception) {
        -1
    }
}

@Preview
@Composable
fun JobSeekerViewPostPreview() {
    JobSeekerViewPostBody()
}