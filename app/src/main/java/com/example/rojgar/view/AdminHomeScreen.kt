package com.example.rojgar.view

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.repository.AdminRepoImpl
import com.example.rojgar.repository.ApplicationRepoImpl
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.viewmodel.AdminViewModel
import com.example.rojgar.viewmodel.ApplicationViewModel
import com.example.rojgar.viewmodel.JobViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import java.text.SimpleDateFormat
import java.util.*

data class StatCard(
    val title: String,
    val value: String,
    val change: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun AdminHomeBody() {
    val context = LocalContext.current

    val adminViewModel = remember { AdminViewModel(AdminRepoImpl()) }
    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val jobViewModel = remember { JobViewModel(JobRepoImpl(context)) }
    val applicationViewModel = remember { ApplicationViewModel(ApplicationRepoImpl(context)) }


    // Observe data from ViewModels
    val verificationStats by adminViewModel.verificationStats.observeAsState(emptyMap())
    val pendingVerifications by adminViewModel.pendingVerificationRequests.observeAsState(emptyList())
    val loading by adminViewModel.loading.observeAsState(false)

    // State for job seekers count
    var jobSeekersCount by remember { mutableStateOf(0) }
    var activeJobsCount by remember { mutableStateOf(0) }
    var applicationsCount by remember { mutableStateOf(0) }

    // Fetch data on first composition
    LaunchedEffect(Unit) {
        adminViewModel.fetchVerificationStats()
        adminViewModel.fetchPendingVerificationRequests()

        // Fetch job seekers count
        jobSeekerViewModel.getAllJobSeeker { success, _, list ->
            if (success && list != null) {
                jobSeekersCount = list.size
            }
        }

        // Fetch active jobs count
        jobViewModel.getAllJobPosts { success, _, jobs ->
            if (success && jobs != null) {
                activeJobsCount = jobs.size
            }
        }

        // Fetch applications count (you'll need to add this to ApplicationViewModel)
        // For now, we'll use a placeholder or fetch from company applications
    }

    // Create stats from real data
    val stats = listOf(
        StatCard(
            "Companies",
            "${verificationStats["total"] ?: 0}",
            "+${calculatePercentageChange(verificationStats["total"] ?: 0)}%",
            Icons.Default.Email,
            Color(0xFF2196F3)
        ),
        StatCard(
            "Job Seekers",
            "$jobSeekersCount",
            "+${calculatePercentageChange(jobSeekersCount)}%",
            Icons.Default.Person,
            Color(0xFF00BCD4)
        ),
        StatCard(
            "Active Jobs",
            "$activeJobsCount",
            "+${calculatePercentageChange(activeJobsCount)}%",
            Icons.Default.Build,
            Color(0xFF3F51B5)
        ),
        StatCard(
            "Applications",
            "$applicationsCount",
            "+${calculatePercentageChange(applicationsCount)}%",
            Icons.Default.Create,
            Color(0xFF1976D2)
        )
    )

    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Statistics Cards
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stats) { stat ->
                        AnimatedStatCard(stat)
                    }
                }
            }

            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.AccountBox,
                        title = "Requests",
                        onClick = {
                            val intent = Intent(context, AdminHelpAndSupportActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Default.Info,
                        title = "Reports",
                        onClick = {
                            val intent = Intent(context, AdminReportsActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Verification Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verification",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF9800).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${verificationStats["pending"] ?: 0} Pending",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFF9800),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // View All Button
                        TextButton(
                            onClick = {
                                val intent = Intent(context, AdminVerificationActivity::class.java)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF1565C0)
                            )
                        ) {
                            Text(
                                text = "View All",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "View All",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Show message if no pending verifications
            if (pendingVerifications.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "No pending",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All Caught Up!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                text = "No pending verification requests",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                // Real pending verification requests
                items(pendingVerifications.take(6)) { company ->
                    CompanyVerificationCard(company)
                }
            }
        }
    }
}

@Composable
fun CompanyVerificationCard(company: CompanyModel) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, AdminVerificationActivity::class.java)
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Company Profile Image or Initial
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF2196F3), Color(0xFF1565C0))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (company.companyProfileImage.isNotEmpty()) {
                    AsyncImage(
                        model = company.companyProfileImage,
                        contentDescription = "Company Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = company.companyName.firstOrNull()?.toString()?.uppercase() ?: "C",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Company Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = company.companyName.ifEmpty { "Company Name" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1565C0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = company.companyEmail.ifEmpty { "email@company.com" },
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Company • ${formatDate(company.verificationRequestDate)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (company.verificationStatus.lowercase()) {
                    "approved" -> Color(0xFF4CAF50)
                    "rejected" -> Color(0xFFF44336)
                    else -> Color(0xFFFFA726)
                }
            ) {
                Text(
                    text = company.verificationStatus.ifEmpty { "Pending" }.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedStatCard(stat: StatCard) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    Card(
        modifier = Modifier
            .width(160.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(stat.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = stat.title,
                    tint = stat.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stat.value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )

            Text(
                text = stat.title,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stat.change,
                fontSize = 12.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1565C0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

// Helper function to calculate percentage change (simplified)
private fun calculatePercentageChange(current: Int): Int {
    // Simple calculation - in real app, you'd compare with previous period
    return when {
        current > 100 -> 18
        current > 50 -> 12
        current > 20 -> 8
        current > 0 -> 5
        else -> 0
    }
}

// Helper function to format date
private fun formatDate(dateString: String): String {
    if (dateString.isEmpty()) return "Recently"

    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)

        if (date != null) {
            val now = Date()
            val diff = now.time - date.time
            val days = diff / (1000 * 60 * 60 * 24)

            return when {
                days == 0L -> "Today"
                days == 1L -> "Yesterday"
                days < 7 -> "$days days ago"
                days < 14 -> "1 week ago"
                days < 30 -> "${days / 7} weeks ago"
                else -> outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return "Recently"
}