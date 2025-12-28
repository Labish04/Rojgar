package com.example.rojgar.view

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.R
import com.example.rojgar.model.ApplicationModel
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobModel
import com.example.rojgar.repository.ApplicationRepoImpl
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.viewmodel.ApplicationViewModel
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class JobApplyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val postId = intent.getStringExtra("POST_ID") ?: ""
        val companyId = intent.getStringExtra("COMPANY_ID") ?: ""

        setContent {
            JobApplyBody(postId = postId, companyId = companyId)
        }
    }
}

@Composable
fun JobApplyBody(postId: String, companyId: String) {
    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val applicationViewModel = remember { ApplicationViewModel(ApplicationRepoImpl()) }
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf("Job Description") }
    var jobPost by remember { mutableStateOf<JobModel?>(null) }
    var company by remember { mutableStateOf<CompanyModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // User profile data
    var userName by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var userProfile by remember { mutableStateOf("") }

    // Get current user from Firebase Auth
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Fetch user profile from Firebase Database
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            val database = FirebaseDatabase.getInstance()

            // Try JobSeekers node first
            val jobSeekerRef = database.getReference("JobSeekers").child(user.uid)

            jobSeekerRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Found in JobSeekers
                    userName = snapshot.child("fullName").getValue(String::class.java) ?: "Unknown User"
                    userPhone = snapshot.child("phoneNumber").getValue(String::class.java) ?: ""
                    userProfile = snapshot.child("profilePhoto").getValue(String::class.java) ?: ""
                } else {
                    // Try Companys node as fallback
                    val companyRef = database.getReference("Companys").child(user.uid)
                    companyRef.get().addOnSuccessListener { companySnapshot ->
                        if (companySnapshot.exists()) {
                            userName = companySnapshot.child("companyName").getValue(String::class.java) ?: "Unknown User"
                            userPhone = companySnapshot.child("companyContactNumber").getValue(String::class.java) ?: ""
                            userProfile = companySnapshot.child("companyProfileImage").getValue(String::class.java) ?: ""
                        } else {
                            // Fallback to Firebase Auth
                            userName = user.displayName ?: "Unknown User"
                            userPhone = user.phoneNumber ?: ""
                            userProfile = user.photoUrl?.toString() ?: ""
                        }
                    }
                }
            }.addOnFailureListener {
                // Fallback to Firebase Auth data
                userName = user.displayName ?: "Unknown User"
                userPhone = user.phoneNumber ?: ""
                userProfile = user.photoUrl?.toString() ?: ""
            }
        }
    }

    LaunchedEffect(postId, companyId) {
        if (postId.isNotEmpty()) {
            jobViewModel.getJobPostById(postId)
        }
        if (companyId.isNotEmpty()) {
            companyViewModel.getCompanyDetails(companyId)
        }
    }

    val jobData by jobViewModel.job.observeAsState()
    val companyData by companyViewModel.companyDetails.observeAsState()
    val loading by jobViewModel.loading.observeAsState(false)

    LaunchedEffect(jobData, companyData, loading) {
        jobPost = jobData
        company = companyData
        isLoading = loading
    }

    Scaffold(
        bottomBar = {
            if (!isLoading && jobPost != null) {
                ApplyNowButton(
                    onApplyClick = {
                        // Check if user is logged in
                        if (currentUser == null) {
                            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                            return@ApplyNowButton
                        }

                        // Create complete application model
                        val application = ApplicationModel(
                            applicationId = "", // Will be generated in repository
                            postId = postId,
                            companyId = companyId,
                            jobSeekerId = currentUser.uid,
                            jobSeekerName = userName,
                            jobSeekerEmail = currentUser.email ?: "",
                            jobSeekerPhone = userPhone,
                            jobSeekerProfile = userProfile,
                            appliedDate = System.currentTimeMillis(),
                            status = "Pending",
                            coverLetter = "", // You can add a text field for this
                            resumeUrl = "" // You can add file upload for this
                        )

                        applicationViewModel.applyForJob(application)
                        Toast.makeText(context, "Application submitted!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
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
                Text("Job not found", fontSize = 16.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(padding)
            ) {
                item {
                    TabSection(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }

                item {
                    when (selectedTab) {
                        "Job Description" -> JobDescriptionContent(jobPost!!)
                        "About Company" -> AboutCompanyContent(company)
                        "Reviews" -> ReviewsContent()
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun TabSection(selectedTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("Job Description", "About Company", "Reviews")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            TabButton(
                text = tab,
                isSelected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) Color(0xFF00BCD4) else Color(0xFFF5F5F5)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun JobDescriptionContent(jobPost: JobModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Job Title Card - Gradient Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
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
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "JOB TITLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = jobPost.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 30.sp
                    )
                }
            }
        }

        // Position & Job Type Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                modifier = Modifier.weight(1f),
                label = "POSITION",
                value = jobPost.position,
                icon = R.drawable.jobpost_filled,
                backgroundColor = Color(0xFFFFF3E0),
                iconTint = Color(0xFFFF9800)
            )
            InfoCard(
                modifier = Modifier.weight(1f),
                label = "JOB TYPE",
                value = jobPost.jobType,
                icon = R.drawable.jobtype,
                backgroundColor = Color(0xFFE8F5E9),
                iconTint = Color(0xFF4CAF50)
            )
        }

        // Categories Card
        if (jobPost.categories.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.jobcategoryicon),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF00BCD4)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "JOB CATEGORIES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00BCD4),
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        jobPost.categories.take(3).forEach { category ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF00BCD4).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF00BCD4),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Experience & Education Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                modifier = Modifier.weight(1f),
                label = "EXPERIENCE",
                value = jobPost.experience,
                icon = R.drawable.experience_filled,
                backgroundColor = Color(0xFFF3E5F5),
                iconTint = Color(0xFF9C27B0)
            )
            InfoCard(
                modifier = Modifier.weight(1f),
                label = "EDUCATION",
                value = jobPost.education,
                icon = R.drawable.educationboardicon,
                backgroundColor = Color(0xFFE3F2FD),
                iconTint = Color(0xFF2196F3)
            )
        }

        // Skills Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.skills_filledicon),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF00BCD4)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REQUIRED SKILLS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00BCD4),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = jobPost.skills.ifEmpty { "Not specified" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    lineHeight = 22.sp
                )
            }
        }

        // Salary Card - Premium Style
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SALARY RANGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = jobPost.salary.ifEmpty { "Negotiable" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "💰",
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }

        // Deadline Card - Urgent Style
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF7566))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "APPLICATION DEADLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = jobPost.deadline.ifEmpty { "Not specified" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "⏰",
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }

        // Job Description Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF00BCD4).copy(alpha = 0.1f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.jobdescriptionicon),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF00BCD4)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Job Description",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = jobPost.jobDescription.ifEmpty { "No description available" },
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )
            }
        }

        // Responsibilities Section
        if (jobPost.responsibilities.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF9800).copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.responsibilityicon),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFFFF9800)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Responsibilities",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = jobPost.responsibilities,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: Int,
    backgroundColor: Color,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = backgroundColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value.ifEmpty { "Not specified" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun AboutCompanyContent(company: CompanyModel?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (company == null) {
                    Text("Company information not available", color = Color.Gray)
                } else {
                    Text(
                        text = "About ${company.companyName}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Company details will be displayed here",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewsContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📝",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Reviews Yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Be the first to share your experience!",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ApplyNowButton(onApplyClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = onApplyClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00BCD4)
            )
        ) {
            Text(
                text = "Apply Now",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview
@Composable
fun JobApplyPreview() {
    JobApplyBody(postId = "", companyId = "")
}