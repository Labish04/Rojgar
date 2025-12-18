package com.example.rojgar.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.example.rojgar.viewmodel.JobViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class JobSeekerViewPost : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerViewPostBody()
        }
    }
}

data class JobPostWithCompany(
    val jobPost: JobModel,
    val companyName: String = "",
    val companyProfile: String = "",
    val isLoading: Boolean = true
)

@Composable
fun JobSeekerViewPostBody() {
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var jobPostsWithCompany by remember { mutableStateOf<List<JobPostWithCompany>>(emptyList()) }
    var savedJobIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        jobViewModel.getAllJobPosts { success, message, posts ->
            if (success && posts != null) {
                val activePosts = posts.filter { !isDeadlineExpired(it.deadline) }

                jobPostsWithCompany = activePosts.map {
                    JobPostWithCompany(jobPost = it, isLoading = true)
                }

                isLoading = false

                activePosts.forEachIndexed { index, job ->
                    companyViewModel.getCompanyDetails(job.companyId) { companySuccess, _, company ->
                        jobPostsWithCompany = jobPostsWithCompany.toMutableList().apply {
                            if (index < size) {
                                this[index] = JobPostWithCompany(
                                    jobPost = job,
                                    companyName = company?.companyName ?: "Unknown Company",
                                    companyProfile = company?.companyProfileImage ?: "",
                                    isLoading = false
                                )
                            }
                        }
                    }
                }

            } else {
                isLoading = false
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
            text = "${jobPostsWithCompany.size} jobs available",
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
        } else if (jobPostsWithCompany.isEmpty()) {
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
                items(jobPostsWithCompany, key = { it.jobPost.postId }) { jobWithCompany ->
                    JobSeekerPostCard(
                        jobPostWithCompany = jobWithCompany,
                        onClick = { },
                        isSaved = savedJobIds.contains(jobWithCompany.jobPost.postId),
                        onSaveClick = { postId ->
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
                        },
                        onShareClick = { job ->
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    "Check out this job: ${job.title}\n" +
                                            "Company: ${jobWithCompany.companyName}\n" +
                                            "Position: ${job.position}\n" +
                                            "Type: ${job.jobType}\n" +
                                            "Deadline: ${job.deadline}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(
                                android.content.Intent.createChooser(shareIntent, "Share Job")
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
    jobPostWithCompany: JobPostWithCompany,
    onClick: () -> Unit,
    onSaveClick: (String) -> Unit = {},
    onShareClick: (JobModel) -> Unit = {},
    isSaved: Boolean = false
) {
    val jobPost = jobPostWithCompany.jobPost
    val daysUntilDeadline = calculateDaysUntilDeadline(jobPost.deadline)
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
                    // Company Profile Image with loading state
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                            .border(1.dp, Color(0xFFBDBDBD), CircleShape)
                    ) {
                        when {
                            jobPostWithCompany.isLoading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.Center),
                                    strokeWidth = 2.dp
                                )
                            }
                            jobPostWithCompany.companyProfile.isNotEmpty() -> {
                                Image(
                                    painter = rememberAsyncImagePainter(jobPostWithCompany.companyProfile),
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
                        text = if (jobPostWithCompany.isLoading) {
                            "Loading..."
                        } else {
                            jobPostWithCompany.companyName
                        },
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
                        .clickable { onSaveClick(jobPost.postId) },
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
                            if (jobPost.imageUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(jobPost.imageUrl),
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
                                text = jobPost.title,
                                fontSize = 18.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = jobPost.position,
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFF00BCD4).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = jobPost.jobType,
                                    fontSize = 12.sp,
                                    color = Color(0xFF00BCD4),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.shareicon),
                                    contentDescription = "Share Job",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { onShareClick(jobPost) },
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }

                if (isUrgent) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Red
                    ) {
                        Text(
                            text = "⚠ URGENT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
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