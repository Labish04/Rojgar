package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.R
import com.example.rojgar.model.ApplicationModel
import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.repository.ApplicationRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.viewmodel.ApplicationViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import java.text.SimpleDateFormat
import java.util.*

class ApplicationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val jobPostId = intent.getStringExtra("JOB_POST_ID") ?: ""
        val jobTitle = intent.getStringExtra("JOB_TITLE") ?: "Job Applications"
        val companyId = intent.getStringExtra("COMPANY_ID") ?: ""
        setContent {
            ApplicationBody(
                jobPostId = jobPostId,
                jobTitle = jobTitle,
                companyId = companyId,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationBody(
    jobPostId: String,
    jobTitle: String,
    companyId: String,
    onBack: () -> Unit
) {
    val applicationViewModel = remember { ApplicationViewModel(ApplicationRepoImpl()) }
    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val context = LocalContext.current
    val applications by applicationViewModel.applications.observeAsState(emptyList())
    val isLoading by applicationViewModel.loading.observeAsState(false)
    val filteredApplications = remember(applications, jobPostId) {
        applications.filter { it.postId == jobPostId }
    }

    LaunchedEffect(Unit) {
        if (companyId.isNotEmpty()) {
            applicationViewModel.getApplicationsByCompany(companyId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = jobTitle,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${filteredApplications.size} ${if (filteredApplications.size == 1) "Application" else "Applications"}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Blue)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DarkBlue2)
                    }
                }
                filteredApplications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.jobpost_filled),
                                contentDescription = "No Applications",
                                modifier = Modifier.size(80.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Applications Yet",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Applications for this job will appear here",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredApplications, key = { it.applicationId }) { application ->
                            ApplicationCard(
                                application = application,
                                jobSeekerViewModel = jobSeekerViewModel,
                                onStatusChanged = { applicationId, newStatus, feedback ->
                                    applicationViewModel.updateApplicationStatus(
                                        applicationId,
                                        newStatus,
                                        feedback
                                    )
                                    Toast.makeText(
                                        context,
                                        "Status updated to $newStatus",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    applicationViewModel.getApplicationsByCompany(companyId)
                                },
                                onCardClick = { jobSeekerId ->
                                    val intent = Intent(context, CvViewActivity::class.java).apply {
                                        putExtra("JOB_SEEKER_ID", jobSeekerId)
                                    }
                                    context.startActivity(intent)
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
    }
}


@Composable
fun ApplicationCard(
    application: ApplicationModel,
    jobSeekerViewModel: JobSeekerViewModel,
    onStatusChanged: (String, String, String?) -> Unit,
    onCardClick: (String) -> Unit
) {
    var jobSeeker by remember { mutableStateOf<JobSeekerModel?>(null) }
    var isLoadingJobSeeker by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var showRejectionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(application.jobSeekerId) {
        isLoadingJobSeeker = true
        jobSeekerViewModel.getJobSeekerById(application.jobSeekerId) { success, message, data ->
            isLoadingJobSeeker = false
            if (success && data != null) {
                jobSeeker = data
            }
        }
    }

    if (showRejectionDialog) {
        RejectionFeedbackDialog(
            applicantName = jobSeeker?.fullName ?: "Applicant",
            onDismiss = { showRejectionDialog = false },
            onConfirm = { feedback ->
                onStatusChanged(application.applicationId, "Rejected", feedback)
                showRejectionDialog = false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick(application.jobSeekerId) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (isLoadingJobSeeker) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = DarkBlue2,
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier.size(95.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF5F5F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!jobSeeker?.profilePhoto.isNullOrEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(jobSeeker?.profilePhoto),
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.profileemptypic),
                                        contentDescription = "No Profile",
                                        tint = Color(0xFF9E9E9E),
                                        modifier = Modifier.size(45.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = jobSeeker?.fullName ?: "Unknown Applicant",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            letterSpacing = 0.2.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF0F4FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.emailicon),
                                    contentDescription = "Email",
                                    modifier = Modifier.size(14.dp),
                                    tint = DarkBlue2
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = jobSeeker?.email ?: application.jobSeekerEmail,
                                fontSize = 13.sp,
                                color = Color(0xFF666666),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFF8E1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.datetimeicon),
                                    contentDescription = "Date",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFF57F17)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Applied ${formatDate(application.appliedDate)}",
                                fontSize = 13.sp,
                                color = Color(0xFF666666),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFEEEEEE))
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = getStatusColor(application.status),
                        onClick = { expanded = true },
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(getStatusTextColor(application.status))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = application.status.uppercase(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = getStatusTextColor(application.status),
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Change Status",
                                tint = getStatusTextColor(application.status),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Color.White)
                            .width(250.dp)
                    ) {
                        listOf("Pending", "Reviewed", "Shortlisted", "Accepted", "Rejected").forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(getStatusColor(status)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(getStatusTextColor(status))
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = status,
                                            color = if (status == application.status) getStatusTextColor(status) else Color(0xFF333333),
                                            fontWeight = if (status == application.status) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    if (status == "Rejected") {
                                        showRejectionDialog = true
                                    } else {
                                        onStatusChanged(application.applicationId, status, null)
                                    }
                                },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RejectionFeedbackDialog(
    applicantName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var feedbackText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rejection Feedback",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF666666)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Applicant info
                Text(
                    text = "Providing feedback to $applicantName",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Icon and message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF3F3))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFE5E5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.emailicon),
                                contentDescription = "Info",
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFFD32F2F)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Please provide constructive feedback to help the applicant improve.",
                            fontSize = 13.sp,
                            color = Color(0xFF666666),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Feedback input label
                Text(
                    text = "Rejection Reason *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Feedback TextField
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = {
                        feedbackText = it
                        showError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = {
                        Text(
                            text = "e.g., We appreciate your interest, but we found candidates with more relevant experience for this position...",
                            fontSize = 13.sp,
                            color = Color(0xFFAAAAAA)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (showError) Color(0xFFD32F2F) else DarkBlue2,
                        unfocusedBorderColor = if (showError) Color(0xFFD32F2F) else Color(0xFFDDDDDD),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    isError = showError,
                    maxLines = 5
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Please provide feedback before rejecting",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${feedbackText.length}/500 characters",
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.8f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF666666)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.5.dp
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Confirm button
                    Button(
                        onClick = {
                            if (feedbackText.trim().isEmpty()) {
                                showError = true
                            } else {
                                onConfirm(feedbackText.trim())
                            }
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = "Confirm Rejection",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "N/A"
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Pending" -> Color(0xFFFFF9C4)
        "Reviewed" -> Color(0xFFE1F5FE)
        "Shortlisted" -> Color(0xFFE8F5E9)
        "Accepted" -> Color(0xFFC8E6C9)
        "Rejected" -> Color(0xFFFFCDD2)
        else -> Color.LightGray
    }
}

fun getStatusTextColor(status: String): Color {
    return when (status) {
        "Pending" -> Color(0xFFF57F17)
        "Reviewed" -> Color(0xFF01579B)
        "Shortlisted" -> Color(0xFF2E7D32)
        "Accepted" -> Color(0xFF1B5E20)
        "Rejected" -> Color(0xFFC62828)
        else -> Color.DarkGray
    }
}