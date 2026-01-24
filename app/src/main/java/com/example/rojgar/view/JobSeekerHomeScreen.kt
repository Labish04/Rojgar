package com.example.rojgar.view

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rojgar.R
import com.example.rojgar.model.EducationModel
import com.example.rojgar.model.ExperienceModel
import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.model.LanguageModel
import com.example.rojgar.model.ObjectiveModel
import com.example.rojgar.model.PortfolioModel
import com.example.rojgar.model.PreferenceModel
import com.example.rojgar.model.ReferenceModel
import com.example.rojgar.model.SkillModel
import com.example.rojgar.model.TrainingModel
import com.example.rojgar.repository.EducationRepo
import com.example.rojgar.repository.EducationRepoImpl
import com.example.rojgar.repository.ExperienceRepo
import com.example.rojgar.repository.ExperienceRepoImpl
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.repository.JobSeekerRepo
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.repository.LanguageRepo
import com.example.rojgar.repository.LanguageRepoImpl
import com.example.rojgar.repository.ObjectiveRepo
import com.example.rojgar.repository.ObjectiveRepoImpl
import com.example.rojgar.repository.PortfolioRepo
import com.example.rojgar.repository.PortfolioRepoImpl
import com.example.rojgar.repository.ReferenceRepo
import com.example.rojgar.repository.ReferenceRepoImpl
import com.example.rojgar.repository.SkillRepo
import com.example.rojgar.repository.SkillRepoImpl
import com.example.rojgar.repository.TrainingRepo
import com.example.rojgar.repository.TrainingRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.Gray
import com.example.rojgar.ui.theme.NormalBlue
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White
import com.example.rojgar.viewmodel.EducationViewModel
import com.example.rojgar.viewmodel.ExperienceViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.example.rojgar.viewmodel.JobViewModel
import com.example.rojgar.viewmodel.LanguageViewModel
import com.example.rojgar.viewmodel.ObjectiveViewModel
import com.example.rojgar.viewmodel.PortfolioViewModel
import com.example.rojgar.viewmodel.PreferenceViewModel
import com.example.rojgar.viewmodel.ReferenceViewModel
import com.example.rojgar.viewmodel.SkillViewModel
import com.example.rojgar.viewmodel.TrainingViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.rojgar.view.CalendarActivity
import com.example.rojgar.repository.CalendarRepoImpl
import com.example.rojgar.viewmodel.CalendarViewModel
import com.example.rojgar.model.CalendarEventModel
import com.example.rojgar.ui.theme.Black

// Filter State Data Class

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerHomeScreenBody() {

    val context = LocalContext.current
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    // Initialize all ViewModels
    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
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
    val calendarViewModel = remember { CalendarViewModel(CalendarRepoImpl()) }

    // Observe LiveData from ViewModels
    val recommendedJobs by jobViewModel.recommendedJobs.observeAsState(emptyList())
    val message by jobViewModel.message.observeAsState("")
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

    var search by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf(JobFilterState()) }

    LaunchedEffect(Unit) {
        // Load recommendations
        jobViewModel.loadRecommendations(PreferenceModel())

        // Get current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val jobSeekerId = currentUser.uid

            // Fetch all profile data
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
        }
    }

    // Fetch calendar events for current month
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            calendarViewModel.observeAllEventsForUser(currentUserId)
        }
    }

    Scaffold (
        topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        titleContentColor = Black,
                        actionIconContentColor = Black,
                        containerColor = Blue,
                        navigationIconContentColor = Black
                    ),
                    title = {
                        Text("")
                    },
                    navigationIcon = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50)), // change color if you want
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = jobSeeker?.profilePhoto,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = "Hi! ${jobSeeker?.fullName}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Let's find your dream job.",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                    },
                    actions = {
                        Row(
                            modifier = Modifier
                                .width(130.dp)
                        ) {
                            IconButton(onClick = {
                                val intent = Intent(context, MessageActivity::class.java)
                                context.startActivity(intent)                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.chat),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            IconButton(onClick = {}) {
                                Icon(
                                    painter = painterResource(R.drawable.notification),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp)
                                )
                            }
                        }
                    }
                )

        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Blue)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Search Bar and Filter Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(
                                Intent(context, JobSeekerSearchActivity::class.java)
                            )
                        }
                ) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false, // Important
                        placeholder = {
                            Text(
                                "Search",
                                fontSize = 20.sp,
                                color = Color.Gray
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.searchicon),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Gray
                            )
                        },
                        shape = RoundedCornerShape(15.dp),
                        colors = TextFieldDefaults.colors(
                            disabledContainerColor = White,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                }


                Spacer(modifier = Modifier.width(16.dp))


            }

            Spacer(modifier = Modifier.height(20.dp))

            // Cards Row - Profile Completion and Calendar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Profile Completion Card
                ProfileCompletionCard(
                    jobSeeker = jobSeeker ?: JobSeekerModel(),
                    objective = objective,
                    languages = languages,
                    education = education,
                    experience = experience,
                    portfolio = portfolio,
                    preference = userPreference,
                    references = references,
                    skills = skills,
                    training = training,
                    modifier = Modifier
                        .height(200.dp)
                        .weight(1f)
                        .clickable {
                            // TODO: Navigate to profile screen
                        }
                )

                // Events Card
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(200.dp)
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MiniEventList(
                            events = events,
                            maxItems = 3
                        )
                    }
                }
            }

            // Recommended Jobs Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp)
                    .padding(vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recommended Jobs", style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "Show All",
                        style = TextStyle(
                            fontSize = 18.sp,
                            color = Purple
                        ),
                        modifier = Modifier.clickable {
                            // TODO: Navigate to all jobs screen
                        }
                    )
                }
            }

            // Jobs List
            Card(
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    if (recommendedJobs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (message.isNotEmpty()) message else "No recommended jobs yet",
                                color = Color.Gray,
                                style = TextStyle(fontSize = 16.sp)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(recommendedJobs.size) { index ->
                                val job = recommendedJobs[index]

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // TODO: Navigate to job details
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 2.dp
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = job.title,
                                            style = TextStyle(
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = job.position,
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                color = Color.DarkGray
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = job.jobType,
                                                style = TextStyle(
                                                    fontSize = 12.sp,
                                                    color = Purple,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                            Text(
                                                text = job.salary,
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF4CAF50),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = job.skills,
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            ),
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCompletionCard(
    jobSeeker: JobSeekerModel,
    objective: ObjectiveModel?,
    languages: List<LanguageModel>?,
    education: List<EducationModel>?,
    experience: List<ExperienceModel>?,
    portfolio: List<PortfolioModel>?,
    preference: PreferenceModel?,
    references: List<ReferenceModel>?,
    skills: List<SkillModel>?,
    training: List<TrainingModel>?,
    modifier: Modifier = Modifier
) {
    val completionPercentage = calculateProfileCompletion(
        jobSeeker = jobSeeker,
        objective = objective,
        languages = languages,
        education = education,
        experience = experience,
        portfolio = portfolio,
        preference = preference,
        references = references,
        skills = skills,
        training = training
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Profile Completed",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
            )
            // Percentage at top
            Text(
                text = "$completionPercentage%",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Circular progress with profile picture
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                // Circular Progress Bar
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2

                    // Background circle
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )

                    // Progress arc
                    drawArc(
                        color = Purple,
                        startAngle = -90f,
                        sweepAngle = (completionPercentage / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        ),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(
                            (size.width - radius * 2) / 2,
                            (size.height - radius * 2) / 2
                        )
                    )
                }


                // Profile Picture
                if (jobSeeker.profilePhoto?.isNotEmpty() == true) {
                    AsyncImage(
                        model = jobSeeker.profilePhoto,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder if no profile photo
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Purple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = jobSeeker.fullName?.firstOrNull()?.toString()?.uppercase() ?: "U",
                            style = TextStyle(
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = Purple
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

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

    // Objective
    totalFields += 1
    if (objective != null && objective.objectiveText?.isNotEmpty() == true) {
        completedFields += 1
    }

    // Education
    totalFields += 1
    if (education?.isNotEmpty() ?: false) {
        completedFields += 1
    }

    // Skills
    totalFields += 1
    if (skills?.isNotEmpty() ?: false) {
        completedFields += 1
    }

    // Experience
    totalFields += 1
    if (experience?.isNotEmpty() ?: false) {
        completedFields += 1
    }

    // Languages
    totalFields += 1
    if (languages?.isNotEmpty() ?: false) {
        completedFields += 1
    }

    // Preference
    totalFields += 1
    if (preference != null && preference.categories?.isNotEmpty() == true) {
        completedFields += 1
    }

    // Portfolio
    totalFields += 1
    if (portfolio?.isNotEmpty() ?: false) {
        completedFields += 1
    }

    // References
    totalFields += 1
    if (references?.isNotEmpty() ?: false) {
        completedFields += 1
    }

    // Training
    totalFields += 1
    if (training?.isNotEmpty() ?: false) {
        completedFields += 1
    }

    val percentage = if (totalFields > 0) {
        ((completedFields.toFloat() / totalFields.toFloat()) * 100).toInt()
    } else {
        0
    }

    return percentage
}
