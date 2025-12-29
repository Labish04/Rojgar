package com.example.rojgar.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.model.ApplicationModel
import com.example.rojgar.repository.ApplicationRepoImpl
import com.example.rojgar.view.ui.theme.RojgarTheme
import com.example.rojgar.viewmodel.ApplicationViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

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
    val applicationViewModel = remember { ApplicationViewModel(ApplicationRepoImpl()) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) }
    val applications by applicationViewModel.applications.observeAsState(emptyList())
    val isLoading by applicationViewModel.loading.observeAsState(false)

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

    // Load applied jobs when screen opens
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            applicationViewModel.getApplicationsByJobSeeker(currentUser.uid)
        }
    }

    // Filter applications based on selected tab
    val filteredApplications = when (selectedTab) {
        0 -> applications // All
        else -> applications.filter { it.status == tabs[selectedTab] }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header with back arrow
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
                Column {
                    Text(
                        text = "Applied Jobs",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${filteredApplications.size} Applications",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
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
                        color = Color(0xFF00BCD4)
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
                        color = if (selectedTab == index) Color(0xFF00BCD4) else Color.Gray,
                        fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }

        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF00BCD4)
                    )
                }
                currentUser == null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔒",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Please log in to view applied jobs",
                            color = Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                filteredApplications.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📋",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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
                                else -> "No applications"
                            },
                            color = Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start applying to see your applications here",
                            color = Color.Gray.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredApplications) { application ->
                            AppliedJobCard(
                                application = application,
                                onWithdraw = { applicationId ->
                                    applicationViewModel.deleteApplication(applicationId)
                                    Toast.makeText(
                                        context,
                                        "Application withdrawn",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppliedJobCard(
    application: ApplicationModel,
    onWithdraw: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val appliedDateString = dateFormat.format(Date(application.appliedDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Job Title and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.jobTitle.ifEmpty { "Job Title" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = application.jobCompany.ifEmpty { "Company Name" },
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (application.status) {
                        "Pending" -> Color(0xFFFEF3C7)
                        "Viewed" -> Color(0xFFDBEAFE)
                        "Shortlisted" -> Color(0xFFDDD6FE)
                        "Offered" -> Color(0xFFD1FAE5)
                        "Hired" -> Color(0xFFBBF7D0)
                        "Rejected" -> Color(0xFFFFE4E6)
                        "Withdrawn" -> Color(0xFFF3F4F6)
                        else -> Color.LightGray
                    }
                ) {
                    Text(
                        text = application.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (application.status) {
                            "Pending" -> Color(0xFFB45309)
                            "Viewed" -> Color(0xFF1E40AF)
                            "Shortlisted" -> Color(0xFF6D28D9)
                            "Offered" -> Color(0xFF047857)
                            "Hired" -> Color(0xFF15803D)
                            "Rejected" -> Color(0xFFBE123C)
                            "Withdrawn" -> Color(0xFF6B7280)
                            else -> Color.Black
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            // Position & Job Type Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                JobInfoCard(
                    modifier = Modifier.weight(1f),
                    label = "POSITION",
                    value = application.jobPosition,
                    icon = R.drawable.jobpost_filled,
                    backgroundColor = Color(0xFFFFF3E0),
                    iconTint = Color(0xFFFF9800)
                )
                JobInfoCard(
                    modifier = Modifier.weight(1f),
                    label = "JOB TYPE",
                    value = application.jobType,
                    icon = R.drawable.jobtype,
                    backgroundColor = Color(0xFFE8F5E9),
                    iconTint = Color(0xFF4CAF50)
                )
            }

            // Location & Experience Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                JobInfoCard(
                    modifier = Modifier.weight(1f),
                    label = "LOCATION",
                    value = application.jobLocation,
                    icon = R.drawable.locationicon,
                    backgroundColor = Color(0xFFE3F2FD),
                    iconTint = Color(0xFF2196F3)
                )
                JobInfoCard(
                    modifier = Modifier.weight(1f),
                    label = "EXPERIENCE",
                    value = application.jobExperience,
                    icon = R.drawable.experience_filled,
                    backgroundColor = Color(0xFFF3E5F5),
                    iconTint = Color(0xFF9C27B0)
                )
            }

            // Salary Card
            if (application.jobSalary.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SALARY RANGE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = application.jobSalary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "💰", fontSize = 20.sp)
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            // Applied Date and Withdraw Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_calendar_month_24),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF00BCD4)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Applied: $appliedDateString",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Withdraw button for Pending or Viewed status
                if (application.status == "Pending" || application.status == "Viewed") {
                    TextButton(
                        onClick = { onWithdraw(application.applicationId) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFEF4444)
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Withdraw",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JobInfoCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: Int,
    backgroundColor: Color,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = backgroundColor,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value.ifEmpty { "Not specified" },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun AppliedJobsScreenPreview() {
    RojgarTheme {
        AppliedJobsScreen()
    }
}