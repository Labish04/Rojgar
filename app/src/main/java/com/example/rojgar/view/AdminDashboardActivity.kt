package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = CompanyRepoImpl()
            val JobSeekerRepository = JobSeekerRepoImpl()
            val viewModel = CompanyViewModel(repository)
            val jobSeekerViewModel = JobSeekerViewModel(JobSeekerRepository)
            AdminDashboardBody(viewModel, jobSeekerViewModel)
        }
    }
}

@Composable
fun AdminDashboardBody(companyViewModel: CompanyViewModel, jobSeekerViewModel: JobSeekerViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Companies", "Job Seekers")

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1565C0),
            Color(0xFF0D47A1)
        )
    )

    Scaffold(
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradient)
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Admin Dashboard",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Welcome back, Admin",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tab Row
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color.White
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> AdminHomeBody() // Overview tab shows home screen
                1 -> CompaniesContent(companyViewModel)
                2 -> JobSeekersContent(jobSeekerViewModel)
            }
        }
    }
}

@Composable
fun CompaniesContent(viewModel: CompanyViewModel) {
    AdminCompaniesScreen(viewModel = viewModel)
}

@Composable
fun JobSeekersContent(viewModel: JobSeekerViewModel) {
    AdminJobSeekerScreen(viewModel = viewModel)
}