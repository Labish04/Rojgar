package com.example.rojgar.view

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.rojgar.model.*
import com.example.rojgar.repository.*
import com.example.rojgar.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Import activities for navigation (add these to your actual project)
// import com.example.rojgar.MessageActivity
// import com.example.rojgar.NotificationActivity
// import com.example.rojgar.JobSeekerSearchActivity
// import com.example.rojgar.JobDetailActivity
// import com.example.rojgar.ProfileActivity

// Modern Theme Colors

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
    val notificationViewModel = remember { NotificationViewModel(userType = UserType.JOBSEEKER) }
    val chatViewModel = remember { ChatViewModel(ChatRepositoryImpl(context), GroupChatRepositoryImpl()) }

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

    // Notification and Message counts
    val unreadNotificationCount by notificationViewModel.unreadCount.collectAsState()
    val chatRooms by chatViewModel.chatRooms.observeAsState(emptyList())
    val unreadMessageCount = chatRooms.count { it.unreadCount > 0 }

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

    Scaffold(
        topBar = {
            ModernTopAppBar(
                jobSeeker = jobSeeker,
                unreadNotificationCount = unreadNotificationCount,
                unreadMessageCount = unreadMessageCount,
                onNotificationClick = {
                    val intent = Intent(context, NotificationActivity::class.java).apply {
                        putExtra("USER_TYPE", "JOBSEEKER")
                    }
                    context.startActivity(intent)
                },
                onMessageClick = {
                    val intent = Intent(context, MessageActivity::class.java)
                    context.startActivity(intent)
                }
            )
        },
        containerColor = ModernLoginTheme.SurfaceLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Animated Header Section
            item {
                AnimatedHeaderSection(
                    jobSeeker = jobSeeker,
                    completionPercentage = calculateProfileCompletion(
                        jobSeeker ?: JobSeekerModel(),
                        objective, languages, education, experience,
                        portfolio, userPreference, references, skills, training
                    )
                )
            }

            // Search Bar
            item {
                ModernSearchBar(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            // Calendar Card Section
            item {
                CalendarCardSection(
                    events = events,
                    onCalendarClick = {
                        val intent = Intent(context, CalendarActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }

            // Recommended Jobs Section
            item {
                SectionHeader(
                    title = "Recommended For You",
                    subtitle = "${recommendedJobs.size} jobs match your profile",
                    icon = Icons.Filled.Email
                )
            }

            // Job Cards
            items(recommendedJobs.take(5)) { job ->
                ModernJobCard(
                    job = job,
                    onClick = { }
                )
            }

            // Profile Completion Tips
            item {
                ProfileCompletionTips(
                    jobSeeker = jobSeeker,
                    skills = skills,
                    experience = experience
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopAppBar(
    jobSeeker: JobSeekerModel?,
    unreadNotificationCount: Int,
    unreadMessageCount: Int,
    onNotificationClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            scale = 1.1f
            delay(1000)
            scale = 1f
            delay(1000)
        }
    }

    TopAppBar(
        title = {
            Column {
                Text(
                    "Let's find your dream job!",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ModernLoginTheme.TextSecondary,
                        fontWeight = FontWeight.Normal
                    )
                )
                Text(
                    jobSeeker?.fullName ?: "User",
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = ModernLoginTheme.TextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(48.dp)
            ) {
                if (jobSeeker?.profilePhoto?.isNotEmpty() == true) {
                    AsyncImage(
                        model = jobSeeker.profilePhoto,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ModernLoginTheme.White
                            )
                        )
                    }
                }
            }
        },
        actions = {
            // Messages Icon with Badge
            Box(
                modifier = Modifier.padding(end = 8.dp)
            ) {
                IconButton(onClick = onMessageClick) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Messages",
                        tint = ModernLoginTheme.TextPrimary
                    )
                }
                if (unreadMessageCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .scale(scale),
                        containerColor = Color(0xFFEF4444),
                        contentColor = ModernLoginTheme.White
                    ) {
                        Text(
                            text = if (unreadMessageCount > 9) "9+" else unreadMessageCount.toString(),
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Notifications Icon with Badge
            Box(
                modifier = Modifier.padding(end = 16.dp)
            ) {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = ModernLoginTheme.TextPrimary
                    )
                }
                if (unreadNotificationCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .scale(scale),
                        containerColor = Color(0xFFEF4444),
                        contentColor = ModernLoginTheme.White
                    ) {
                        Text(
                            text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ModernLoginTheme.White,
            titleContentColor = ModernLoginTheme.TextPrimary
        )
    )
}

@Composable
fun AnimatedHeaderSection(
    jobSeeker: JobSeekerModel?,
    completionPercentage: Int
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
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
fun ModernJobCard(
    job: JobModel,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { 100 })
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = ModernLoginTheme.White
            ),
            elevation = CardDefaults.cardElevation(4.dp),
            onClick = {
                val intent = Intent(context, JobApplyActivity::class.java).apply {
                }
                context.startActivity(intent)
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Company Logo
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ModernLoginTheme.IceBlue),
                    contentAlignment = Alignment.Center
                ) {
                    if (job.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = job.imageUrl,
                            contentDescription = "Company Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = "Company",
                            tint = ModernLoginTheme.PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Job Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernLoginTheme.TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = job.title,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = ModernLoginTheme.TextSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Location",
                            tint = ModernLoginTheme.AccentBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = job.deadline,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ModernLoginTheme.TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Salary",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = job.salary,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ModernLoginTheme.TextSecondary
                            )
                        )
                    }
                }

                // Bookmark Icon
                IconButton(onClick = { /* Bookmark */ }) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save",
                        tint = ModernLoginTheme.PrimaryBlue
                    )
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