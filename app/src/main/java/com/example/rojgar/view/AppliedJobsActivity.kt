package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.model.AppliedJobModel
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.view.ui.theme.RojgarTheme

class AppliedJobsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RojgarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppliedJobsScreen(
                        modifier = Modifier.padding(innerPadding),
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun AppliedJobsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val repository = remember { JobSeekerRepoImpl() }
    val currentUser = repository.getCurrentJobSeeker()

    var selectedTab by remember { mutableStateOf(0) }
    var appliedJobs by remember { mutableStateOf<List<AppliedJobModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val tabs = listOf(
        "All",
        "Pending",
        "Viewed",
        "Shortlisted",
        "Offered",
        "Hired",
        "Rejected",
        "Withdrawn"
    )

    // Load applied jobs based on selected tab
    LaunchedEffect(selectedTab, currentUser) {
        if (currentUser != null) {
            isLoading = true
            errorMessage = null

            when (selectedTab) {
                0 -> {
                    // Load all applied jobs
                    repository.getAppliedJobsByJobSeeker(currentUser.uid) { success, message, jobs ->
                        isLoading = false
                        if (success && jobs != null) {
                            appliedJobs = jobs
                        } else {
                            errorMessage = message
                            appliedJobs = emptyList()
                        }
                    }
                }
                else -> {
                    // Load filtered jobs by status
                    val status = tabs[selectedTab]
                    repository.getAppliedJobsByStatus(currentUser.uid, status) { success, message, jobs ->
                        isLoading = false
                        if (success && jobs != null) {
                            appliedJobs = jobs
                        } else {
                            errorMessage = message
                            appliedJobs = emptyList()
                        }
                    }
                }
            }
        } else {
            isLoading = false
            errorMessage = "Please log in to view applied jobs"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header with back arrow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF7C3AED))
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_arrow_back_ios_24),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Applied Jobs",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color.Black,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF7C3AED)
                    )
                }
            },
            divider = {
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        color = if (selectedTab == index) Color(0xFF7C3AED) else Color.Gray,
                        fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }

        // Column Headers with horizontal scroll
        val scrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9FAFB))
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderCell(
                text = "Title",
                minWidth = 140.dp
            )
            HeaderCell(
                text = "Company",
                minWidth = 140.dp
            )
            HeaderCell(
                text = "Status",
                minWidth = 100.dp
            )
            HeaderCell(
                text = "Location",
                minWidth = 120.dp
            )
            HeaderCell(
                text = "Openings",
                minWidth = 100.dp
            )
            HeaderCell(
                text = "Applied Date",
                minWidth = 120.dp
            )
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF7C3AED)
                    )
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "An error occurred",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        color = Color.Red,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
                appliedJobs.isEmpty() -> {
                    Text(
                        text = when (selectedTab) {
                            0 -> "No applied jobs yet"
                            1 -> "No pending applications"
                            2 -> "No viewed applications"
                            3 -> "No shortlisted applications"
                            4 -> "No offered applications"
                            5 -> "No hired applications"
                            6 -> "No rejected applications"
                            7 -> "No withdrawn applications"
                            else -> ""
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(appliedJobs) { job ->
                            AppliedJobRow(
                                appliedJob = job,
                                onWithdraw = { applicationId ->
                                    if (currentUser != null) {
                                        repository.withdrawApplication(
                                            applicationId,
                                            currentUser.uid
                                        ) { success, message ->
                                            // Handle withdrawal result
                                            // You can show a toast or snackbar here
                                        }
                                    }
                                }
                            )
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppliedJobRow(
    appliedJob: AppliedJobModel,
    onWithdraw: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title
        Box(
            modifier = Modifier.widthIn(min = 140.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = appliedJob.jobTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Company
        Box(
            modifier = Modifier.widthIn(min = 140.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = appliedJob.companyName,
                fontSize = 14.sp,
                color = Color(0xFF374151),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Status
        Box(
            modifier = Modifier.widthIn(min = 100.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = when (appliedJob.status) {
                            "Pending" -> Color(0xFFFEF3C7)
                            "Viewed" -> Color(0xFFDBEAFE)
                            "Shortlisted" -> Color(0xFFDDD6FE)
                            "Offered" -> Color(0xFFD1FAE5)
                            "Hired" -> Color(0xFFBBF7D0)
                            "Rejected" -> Color(0xFFFFE4E6)
                            "Withdrawn" -> Color(0xFFF3F4F6)
                            else -> Color.LightGray
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = appliedJob.status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (appliedJob.status) {
                        "Pending" -> Color(0xFFB45309)
                        "Viewed" -> Color(0xFF1E40AF)
                        "Shortlisted" -> Color(0xFF6D28D9)
                        "Offered" -> Color(0xFF047857)
                        "Hired" -> Color(0xFF15803D)
                        "Rejected" -> Color(0xFFBE123C)
                        "Withdrawn" -> Color(0xFF6B7280)
                        else -> Color.Black
                    }
                )
            }
        }

        // Location
        Box(
            modifier = Modifier.widthIn(min = 120.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = appliedJob.location,
                fontSize = 14.sp,
                color = Color(0xFF374151),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Openings
        Box(
            modifier = Modifier.widthIn(min = 100.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = appliedJob.openings.toString(),
                fontSize = 14.sp,
                color = Color(0xFF374151)
            )
        }

        // Applied Date
        Box(
            modifier = Modifier.widthIn(min = 120.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = appliedJob.appliedDate,
                fontSize = 14.sp,
                color = Color(0xFF374151)
            )
        }

        // Actions (optional - withdraw button)
        if (appliedJob.status == "Pending" || appliedJob.status == "Viewed") {
            TextButton(
                onClick = { onWithdraw(appliedJob.applicationId) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFEF4444)
                )
            ) {
                Text(
                    text = "Withdraw",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun HeaderCell(
    text: String,
    minWidth: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .widthIn(min = minWidth),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6B7280),
            maxLines = 1
        )
    }
}

@Preview(showBackground = true, widthDp = 900, heightDp = 600)
@Composable
fun AppliedJobsScreenPreview() {
    RojgarTheme {
        AppliedJobsScreen()
    }
}
