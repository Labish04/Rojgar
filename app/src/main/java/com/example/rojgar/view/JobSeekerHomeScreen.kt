package com.example.rojgar.view

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.R
import com.example.rojgar.model.*
import com.example.rojgar.repository.*
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.White
import com.example.rojgar.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerHomeScreenBody() {
    val context = LocalContext.current
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    // Initialize ViewModels
    val jobViewModel = remember { JobViewModel(JobRepoImpl(context)) }
    val jobSeekerRepo = JobSeekerRepoImpl()
    val jobSeekerViewModel = remember { JobSeekerViewModel(jobSeekerRepo) }
    val objectiveViewModel = remember { ObjectiveViewModel(ObjectiveRepoImpl()) }
    val educationViewModel = remember { EducationViewModel(EducationRepoImpl()) }
    val experienceViewModel = remember { ExperienceViewModel(ExperienceRepoImpl()) }
    val portfolioViewModel = remember { PortfolioViewModel(PortfolioRepoImpl()) }
    val preferenceViewModel = remember { PreferenceViewModel() }
    val referenceViewModel = remember { ReferenceViewModel(ReferenceRepoImpl()) }
    val skillViewModel = remember { SkillViewModel(SkillRepoImpl()) }
    val trainingViewModel = remember { TrainingViewModel(TrainingRepoImpl()) }
    val languageViewModel = remember { LanguageViewModel(LanguageRepoImpl()) }
    val calendarViewModel = remember { CalendarViewModel(CalendarRepoImpl(context)) }
    val notificationViewModel = remember { NotificationViewModel(NotificationRepoImpl(), UserType.JOBSEEKER) }
    val chatViewModel = remember { ChatViewModel(ChatRepositoryImpl(context), GroupChatRepositoryImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }

    // Observe LiveData
    val recommendedJobs by jobViewModel.recommendedJobs.observeAsState(emptyList())
    val jobSeeker by jobSeekerViewModel.jobSeeker.observeAsState()
    val objective by objectiveViewModel.objective.observeAsState()
    val languages by languageViewModel.allLanguages.observeAsState(emptyList())
    val education by educationViewModel.allEducations.observeAsState(emptyList())
    val experience by experienceViewModel.allExperiences.observeAsState(emptyList())
    val portfolio by portfolioViewModel.allPortfolios.observeAsState(emptyList())
    val userPreference by preferenceViewModel.preferenceData.observeAsState()
    val references by referenceViewModel.allReferences.observeAsState(emptyList())
    val skills by skillViewModel.allSkills.observeAsState(emptyList())
    val training by trainingViewModel.allTrainings.observeAsState(emptyList())
    val events by calendarViewModel.events.observeAsState(emptyList())

    // Company data for job cards
    var companies by remember { mutableStateOf<Map<String, CompanyModel>>(emptyMap()) }
    var isLoadingCompanies by remember { mutableStateOf(true) }

    // Notification and Message counts
    val unreadNotificationCount by notificationViewModel.unreadCount.collectAsState(initial = 0)
    val chatRooms by chatViewModel.chatRooms.observeAsState(emptyList())
    val unreadMessageCount = remember(chatRooms) { chatRooms.sumOf { it.unreadCount } }

    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        jobViewModel.loadRecommendations(PreferenceModel())
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val jobSeekerId = currentUser.uid
            jobSeekerViewModel.fetchCurrentJobSeeker()
            objectiveViewModel.fetchObjectiveByJobSeekerId(jobSeekerId)
            educationViewModel.fetchEducationsByJobSeekerId(jobSeekerId)
            experienceViewModel.fetchExperiencesByJobSeekerId(jobSeekerId)
            languageViewModel.fetchLanguagesByJobSeekerId(jobSeekerId)
            portfolioViewModel.fetchPortfoliosByJobSeekerId(jobSeekerId)
            preferenceViewModel.getPreference(jobSeekerId)
            referenceViewModel.fetchReferencesByJobSeekerId(jobSeekerId)
            skillViewModel.fetchSkillsByJobSeekerId(jobSeekerId)
            trainingViewModel.fetchTrainingsByJobSeekerId(jobSeekerId)
            calendarViewModel.observeAllEventsForUser(jobSeekerId)
            chatViewModel.loadChatRooms(jobSeekerId)
        }
    }

    LaunchedEffect(userPreference) {
        if (userPreference != null) {
            jobViewModel.loadRecommendations(userPreference!!)
        } else {
            // Load with empty preference if no preference is set
            jobViewModel.loadRecommendations(PreferenceModel())
        }
    }

    LaunchedEffect(recommendedJobs) {
        if (recommendedJobs.isNotEmpty()) {
            isLoadingCompanies = true
            companyViewModel.getAllCompany { success, _, companyList ->
                if (success && companyList != null) {
                    companies = companyList.associateBy { it.companyId }
                }
                isLoadingCompanies = false
            }
        }
    }

    // ADD THIS AT THE END OF THE IMPORTS SECTION:
    // Add this import if not already present
    // import androidx.compose.animation.core.*
    // import androidx.compose.foundation.border

    Scaffold(
        topBar = {
            EnhancedJobSeekerTopBar(
                jobSeekerName = jobSeeker?.fullName ?: "Job Seeker",
                jobSeekerProfileImage = jobSeeker?.profilePhoto ?: "",
                unreadMessageCount = unreadMessageCount,
                unreadNotificationCount = unreadNotificationCount,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1976D2),
                                Color(0xFF2196F3),
                                Color(0xFF42A5F5)
                            )
                        )
                    )
                    .padding(top = 45.dp)
            )
        },
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    ModernLoginTheme.SurfaceLight,
                    ModernLoginTheme.IceBlue
                )
            )
        )
    ) { padding ->
        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Search bar - Use CompanySearchBar style
            JobSeekerSearchBar(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Header Section
            AnimatedHeaderSection(
                jobSeeker = jobSeeker,
                completionPercentage = calculateProfileCompletion(
                    jobSeeker ?: JobSeekerModel(),
                    objective, languages, education, experience,
                    portfolio, userPreference, references, skills, training
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar Card Section
            CalendarCardSection(
                events = events,
                onCalendarClick = {
                    val intent = Intent(context, CalendarActivity::class.java)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recommended Jobs Section
            if (recommendedJobs.isNotEmpty()) {
                Text(
                    text = "Recommended For You",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernLoginTheme.TextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${recommendedJobs.size} jobs match your profile",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = ModernLoginTheme.TextSecondary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Job Cards
                if (isLoadingCompanies) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    recommendedJobs.take(5).forEach { job ->
                        val company = companies[job.companyId]

                        RecommendedJobCard(
                            job = job,
                            companyName = company?.companyName ?: "Unknown Company",
                            companyProfile = company?.companyProfileImage ?: "",
                            onClick = {
                                val intent = Intent(context, JobApplyActivity::class.java).apply {
                                    putExtra("POST_ID", job.postId)
                                    putExtra("COMPANY_ID", job.companyId)
                                }
                                context.startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // View All Jobs Button
                if (recommendedJobs.size > 5) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = {
                                val intent = Intent(context, JobSeekerViewPost::class.java)
                                context.startActivity(intent)
                            }
                        ) {
                            Text(
                                text = "View All ${recommendedJobs.size} Jobs",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ModernLoginTheme.PrimaryBlue
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "View All",
                                tint = ModernLoginTheme.PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Completion Tips
            ProfileCompletionTips(
                jobSeeker = jobSeeker,
                skills = skills,
                experience = experience
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun EnhancedJobSeekerTopBar(
    jobSeekerName: String,
    jobSeekerProfileImage: String,
    unreadMessageCount: Int = 0,
    unreadNotificationCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier,
        color = Color.Transparent,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile and Name Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (jobSeekerProfileImage.isNotEmpty()) {
                        AsyncImage(
                            model = jobSeekerProfileImage,
                            contentDescription = "Job Seeker Profile",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = jobSeekerName.firstOrNull()?.uppercase() ?: "J",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Job Seeker Name and Subtitle
                Column(horizontalAlignment = Alignment.Start) {
                    val greeting = remember {
                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        when (hour) {
                            in 0..11 -> "Good Morning"
                            in 12..16 -> "Good Afternoon"
                            else -> "Good Evening"
                        }
                    }

                    Text(
                        text = "$greeting, $jobSeekerName!",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Let's find your dream job today",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
            }

            // Action Icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chat Icon with Badge
                Box {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, MessageActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.chat_filled),
                            contentDescription = "Messages",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Badge
                    NotificationBadge(
                        count = unreadMessageCount,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }

                // Notification Icon with Badge
                Box {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, NotificationActivity::class.java).apply {
                                putExtra("USER_TYPE", "JOBSEEKER")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.notification_filled),
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Badge
                    NotificationBadge(
                        count = unreadNotificationCount,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

// Add this function for Job Seeker Search Bar (similar to CompanySearchBar)
@Composable
fun JobSeekerSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .height(56.dp)
            .clickable {
                context.startActivity(
                    Intent(context, JobSeekerSearchActivity::class.java)
                )
            }
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = ModernLoginTheme.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = true,
                enabled = false,
                placeholder = {
                    Text(
                        "Search jobs, companies...",
                        fontSize = 14.sp,
                        color = ModernLoginTheme.TextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.searchicon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = ModernLoginTheme.PrimaryBlue
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

// Update the AnimatedHeaderSection to accept a modifier parameter
@Composable
fun AnimatedHeaderSection(
    jobSeeker: JobSeekerModel?,
    completionPercentage: Int,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Card(
        modifier = modifier
            .scale(scale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = ModernLoginTheme.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ModernLoginTheme.IceBlue,
                                ModernLoginTheme.SkyBlue.copy(alpha = 0.3f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Profile Completion",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ModernLoginTheme.TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Animated Circular Progress
                AnimatedCircularProgress(
                    completionPercentage = completionPercentage,
                    jobSeeker = jobSeeker
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Motivational Text
                AnimatedVisibility(
                    visible = completionPercentage < 100,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Text(
                        text = when {
                            completionPercentage < 30 -> "Let's get started! 🚀"
                            completionPercentage < 60 -> "You're making progress! 💪"
                            completionPercentage < 90 -> "Almost there! Keep going! 🎯"
                            else -> "Just a little more! 🌟"
                        },
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ModernLoginTheme.PrimaryBlue,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                if (completionPercentage == 100) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Complete",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Profile Complete! 🎉",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedCircularProgress(
    completionPercentage: Int,
    jobSeeker: JobSeekerModel?
) {
    var animatedProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(completionPercentage) {
        animatedProgress = 0f
        animate(
            initialValue = 0f,
            targetValue = completionPercentage.toFloat(),
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        ) { value, _ ->
            animatedProgress = value
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(140.dp)
    ) {
        // Circular Progress
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            // Background circle
            drawCircle(
                color = ModernLoginTheme.IceBlue,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Gradient Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        ModernLoginTheme.AccentBlue,
                        ModernLoginTheme.PrimaryBlue,
                        ModernLoginTheme.LightBlue
                    )
                ),
                startAngle = -90f,
                sweepAngle = (animatedProgress / 100f) * 360f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }

        // Profile Picture or Initial
        if (jobSeeker?.profilePhoto?.isNotEmpty() == true) {
            AsyncImage(
                model = jobSeeker.profilePhoto,
                contentDescription = "Profile Photo",
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ModernLoginTheme.LightBlue,
                                ModernLoginTheme.PrimaryBlue
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = jobSeeker?.fullName?.firstOrNull()?.toString()?.uppercase() ?: "U",
                    style = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernLoginTheme.White
                    )
                )
            }
        }

        // Percentage Badge
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 8.dp, y = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = ModernLoginTheme.PrimaryBlue
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Text(
                text = "${animatedProgress.toInt()}%",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ModernLoginTheme.White
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ModernSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var focused by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.clickable {
            context.startActivity(Intent(context, JobSeekerSearchActivity::class.java))
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ModernLoginTheme.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (focused) 8.dp else 2.dp
        )
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = false,
            placeholder = {
                Text(
                    "Search jobs, companies...",
                    style = TextStyle(
                        color = ModernLoginTheme.TextSecondary,
                        fontSize = 14.sp
                    )
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = ModernLoginTheme.PrimaryBlue
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = ModernLoginTheme.TextSecondary
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ModernLoginTheme.White,
                unfocusedContainerColor = ModernLoginTheme.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledContainerColor = White,
                cursorColor = ModernLoginTheme.PrimaryBlue
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = ModernLoginTheme.TextPrimary
            )
        )
    }
}

@Composable
fun CalendarCardSection(
    events: List<CalendarEventModel>,
    onCalendarClick: () -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    val currentWeekDays = remember { getCurrentWeekDays(calendar) }
    val currentWeekEvents = remember(events) {
        getEventsForCurrentWeek(events, currentWeekDays)
    }
    val dateFormat = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }
    val currentMonth = remember { dateFormat.format(calendar.time) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onCalendarClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ModernLoginTheme.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Calendar Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Calendar",
                        tint = ModernLoginTheme.PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calendar",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernLoginTheme.TextPrimary
                        )
                    )
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "Open",
                    tint = ModernLoginTheme.TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Month/Year Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentMonth,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ModernLoginTheme.TextPrimary
                    )
                )
                Text(
                    text = "This Week",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ModernLoginTheme.TextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Week Days Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dayNames = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                dayNames.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ModernLoginTheme.TextSecondary
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Current Week Days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val today = Calendar.getInstance()
                currentWeekDays.forEach { dayCalendar ->
                    val isToday = isSameDay(dayCalendar, today)
                    val hasEvent = currentWeekEvents.any { event ->
                        isSameDay(getCalendarFromMillis(event.startTimeMillis), dayCalendar)
                    }

                    DayCell(
                        day = dayCalendar.get(Calendar.DAY_OF_MONTH),
                        isToday = isToday,
                        hasEvent = hasEvent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Events for this week
            if (currentWeekEvents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = ModernLoginTheme.SurfaceLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${currentWeekEvents.size} ${if (currentWeekEvents.size == 1) "Event" else "Events"} This Week",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ModernLoginTheme.TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show up to 2 events
                currentWeekEvents.take(2).forEach { event ->
                    CompactEventItem(event = event)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (currentWeekEvents.size > 2) {
                    Text(
                        text = "+${currentWeekEvents.size - 2} more",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ModernLoginTheme.AccentBlue
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No events this week",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ModernLoginTheme.TextSecondary
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isToday: Boolean,
    hasEvent: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isToday) ModernLoginTheme.PrimaryBlue
                        else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        color = if (isToday) ModernLoginTheme.White
                        else ModernLoginTheme.TextPrimary
                    )
                )
            }

            if (hasEvent && !isToday) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(ModernLoginTheme.AccentBlue)
                )
            }
        }
    }
}

@Composable
fun CompactEventItem(event: CalendarEventModel) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val eventTime = timeFormat.format(Date(event.startTimeMillis))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(android.graphics.Color.parseColor(event.colorHex)).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(event.colorHex)))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ModernLoginTheme.TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = eventTime,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = ModernLoginTheme.TextSecondary
                )
            )
        }
    }
}

// Helper functions
fun getCurrentWeekDays(calendar: Calendar): List<Calendar> {
    val days = mutableListOf<Calendar>()
    val currentCalendar = calendar.clone() as Calendar

    // Get to Sunday of current week
    currentCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

    // Add all 7 days
    for (i in 0..6) {
        days.add(currentCalendar.clone() as Calendar)
        currentCalendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    return days
}

fun getEventsForCurrentWeek(
    events: List<CalendarEventModel>,
    currentWeekDays: List<Calendar>
): List<CalendarEventModel> {
    val startOfWeek = currentWeekDays.first().timeInMillis
    val endOfWeek = currentWeekDays.last().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }.timeInMillis

    return events.filter { event ->
        event.startTimeMillis >= startOfWeek && event.startTimeMillis <= endOfWeek
    }.sortedBy { it.startTimeMillis }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun getCalendarFromMillis(millis: Long): Calendar {
    return Calendar.getInstance().apply {
        timeInMillis = millis
    }
}


@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            ModernLoginTheme.LightBlue,
                            ModernLoginTheme.PrimaryBlue
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = ModernLoginTheme.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ModernLoginTheme.TextPrimary
                )
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = ModernLoginTheme.TextSecondary
                )
            )
        }
        TextButton(onClick = {
            // Navigate to all jobs screen
            context.startActivity(Intent(context, JobSeekerSearchActivity::class.java))
        }) {
            Text(
                text = "View All",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ModernLoginTheme.PrimaryBlue
                )
            )
        }
    }
}

@Composable
fun RecommendedJobCard(
    job: JobModel,
    companyName: String,
    companyProfile: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Company Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Company Profile Image
                    if (companyProfile.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(companyProfile),
                            contentDescription = "Company Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00BCD4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = companyName.take(1).uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = companyName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Hiring Now",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    painter = painterResource(id = R.drawable.save_outline),
                    contentDescription = "Save Job",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Job Image and Details
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Job Image
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

                    // Job Details
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
                                    modifier = Modifier.size(24.dp),
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
fun ProfileCompletionTips(
    jobSeeker: JobSeekerModel?,
    skills: List<SkillModel>?,
    experience: List<ExperienceModel>?
) {
    val tips = buildList {
        if (jobSeeker?.bio.isNullOrEmpty()) {
            add(TipItem(Icons.Filled.Person, "Add a professional bio", ModernLoginTheme.PrimaryBlue))
        }
        if (skills.isNullOrEmpty()) {
            add(TipItem(Icons.Filled.Star, "List your top skills", ModernLoginTheme.AccentBlue))
        }
        if (experience.isNullOrEmpty()) {
            add(TipItem(Icons.Filled.Email, "Add work experience", ModernLoginTheme.DeepBlue))
        }
    }

    if (tips.isNotEmpty()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Complete Your Profile",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ModernLoginTheme.TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            tips.forEach { tip ->
                TipCard(tip = tip)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

data class TipItem(
    val icon: ImageVector,
    val text: String,
    val color: Color
)

@Composable
fun TipCard(tip: TipItem) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            val intent = Intent(context, JobSeekerProfileActivity::class.java)
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = tip.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tip.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tip.icon,
                    contentDescription = tip.text,
                    tint = ModernLoginTheme.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = tip.text,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ModernLoginTheme.TextPrimary
                ),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Go",
                tint = ModernLoginTheme.TextSecondary
            )
        }
    }
}

fun calculateProfileCompletion(
    jobSeeker: JobSeekerModel,
    objective: ObjectiveModel?,
    languages: List<LanguageModel>?,
    education: List<EducationModel>?,
    experience: List<ExperienceModel>?,
    portfolio: List<PortfolioModel>?,
    preference: PreferenceModel?,
    references: List<ReferenceModel>?,
    skills: List<SkillModel>?,
    training: List<TrainingModel>?
): Int {
    var totalFields = 0
    var completedFields = 0

    // Basic Information
    val basicInfoFields = listOf(
        jobSeeker.fullName ?: "",
        jobSeeker.email ?: "",
        jobSeeker.phoneNumber ?: "",
        jobSeeker.gender ?: "",
        jobSeeker.dob ?: "",
        jobSeeker.currentAddress ?: "",
        jobSeeker.profession ?: "",
        jobSeeker.profilePhoto ?: "",
        jobSeeker.bio ?: ""
    )
    totalFields += basicInfoFields.size
    completedFields += basicInfoFields.count { it.isNotEmpty() }

    // Other sections
    totalFields += 7
    if (objective?.objectiveText?.isNotEmpty() == true) completedFields++
    if (!education.isNullOrEmpty()) completedFields++
    if (!skills.isNullOrEmpty()) completedFields++
    if (!experience.isNullOrEmpty()) completedFields++
    if (!languages.isNullOrEmpty()) completedFields++
    if (preference?.categories?.isNotEmpty() == true) completedFields++
    if (!portfolio.isNullOrEmpty()) completedFields++

    return if (totalFields > 0) {
        ((completedFields.toFloat() / totalFields.toFloat()) * 100).toInt()
    } else {
        0
    }
}