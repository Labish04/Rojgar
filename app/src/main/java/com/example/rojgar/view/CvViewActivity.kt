package com.example.rojgar.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.repository.EducationRepoImpl
import com.example.rojgar.repository.ExperienceRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.repository.LanguageRepoImpl
import com.example.rojgar.repository.ObjectiveRepoImpl
import com.example.rojgar.repository.PortfolioRepoImpl
import com.example.rojgar.repository.ReferenceRepoImpl
import com.example.rojgar.repository.SkillRepoImpl
import com.example.rojgar.repository.TrainingRepoImpl
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.White
import com.example.rojgar.viewmodel.EducationViewModel
import com.example.rojgar.viewmodel.ExperienceViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.example.rojgar.viewmodel.LanguageViewModel
import com.example.rojgar.viewmodel.ObjectiveViewModel
import com.example.rojgar.viewmodel.PortfolioViewModel
import com.example.rojgar.viewmodel.ReferenceViewModel
import com.example.rojgar.viewmodel.SkillViewModel
import com.example.rojgar.viewmodel.TrainingViewModel

class CvViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CvViewBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CvViewBody() {

    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val objectiveViewModel = remember { ObjectiveViewModel(ObjectiveRepoImpl()) }
    val educationViewModel = remember { EducationViewModel(EducationRepoImpl()) }
    val experienceViewModel = remember { ExperienceViewModel(ExperienceRepoImpl()) }
    val skillViewModel = remember { SkillViewModel(SkillRepoImpl()) }
    val trainingViewModel = remember { TrainingViewModel(TrainingRepoImpl()) }
    val languageViewModel = remember { LanguageViewModel(LanguageRepoImpl()) }
    val portfolioViewModel = remember { PortfolioViewModel(PortfolioRepoImpl()) }
    val referenceViewModel = remember { ReferenceViewModel(ReferenceRepoImpl()) }

    val jobSeeker = jobSeekerViewModel.jobSeeker.observeAsState(initial = null)
    val loading = jobSeekerViewModel.loading.observeAsState(initial = false)
    val objective = objectiveViewModel.objective.observeAsState(initial = null)
    val allEducations = educationViewModel.allEducations.observeAsState(initial = emptyList())
    val allExperiences = experienceViewModel.allExperiences.observeAsState(initial = emptyList())
    val allSkills = skillViewModel.allSkills.observeAsState(initial = emptyList())
    val allTrainings = trainingViewModel.allTrainings.observeAsState(initial = emptyList())
    val allLanguages = languageViewModel.allLanguages.observeAsState(initial = emptyList())
    val allPortfolios = portfolioViewModel.allPortfolios.observeAsState(initial = emptyList())
    val allReferences = referenceViewModel.allReferences.observeAsState(initial = emptyList())

    val currentUser = jobSeekerViewModel.getCurrentJobSeeker()

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E88E5),
            Color(0xFF1565C0),
            Color(0xFFF5F5F5)
        ),
        startY = 0f,
        endY = 1200f
    )

    LaunchedEffect(Unit) {
        val userId = currentUser?.uid
        if (userId != null) {
            Log.d("CvViewBody", "Fetching data for user: $userId")
            jobSeekerViewModel.fetchCurrentJobSeeker()
            objectiveViewModel.fetchObjectiveByJobSeekerId(userId)
            educationViewModel.fetchEducationsByJobSeekerId(userId)
            experienceViewModel.fetchExperiencesByJobSeekerId(userId)
            skillViewModel.fetchSkillsByJobSeekerId(userId)
            trainingViewModel.fetchTrainingsByJobSeekerId(userId)
            languageViewModel.fetchLanguagesByJobSeekerId(userId)
            portfolioViewModel.fetchPortfoliosByJobSeekerId(userId)
            referenceViewModel.fetchReferencesByJobSeekerId(userId)
        } else {
            Log.e("CvViewBody", "Current user is null!")
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = White,
                    actionIconContentColor = White,
                    containerColor = Color.Transparent,
                    navigationIconContentColor = White
                ),
                title = {
                    Text(
                        "Curriculum Vitae",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null
                        )
                    }
                },
                actions = {}
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                if (loading.value){
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                        CircularProgressIndicator(
                            modifier = Modifier.padding(32.dp),
                            color = Color(0xFF1E88E5)
                        )
                    }
                }
                // Modern Profile Header
                ModernProfileHeader(
                    name = jobSeeker.value?.fullName,
                    profession = jobSeeker.value?.profession,
                )

                // Two Column Layout for Personal Info
                PersonalInfoGrid(
                    phoneNumber = jobSeeker.value?.phoneNumber,
                    email = jobSeeker.value?.email,
                    dob = jobSeeker.value?.dob,
                    gender = jobSeeker.value?.gender,
                    religion = jobSeeker.value?.religion,
                    nationality = jobSeeker.value?.nationality,
                    maritalStatus = jobSeeker.value?.maritalStatus,
                    address = jobSeeker.value?.currentAddress,
                )

                // Objective with Icon
                StylishSectionCard(
                    title = "Career Objective",
                    icon = "🎯"
                ) {
                    Text(
                        text =  objective.value?.objectiveText?:"Loading...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF424242),
                        lineHeight = 22.sp,
                        letterSpacing = 0.3.sp
                    )
                }

                // Education Timeline
                StylishSectionCard(
                    title = "Education",
                    icon = "🎓"
                ) {
                    allEducations.value!!.forEachIndexed { index, education ->
                        TimelineEducationItem(
                            degree = education.educationDegree ?: "Unknown Degree",
                            institution = education.instituteName ?: "Unknown Institution",
                            year = "${education.startYear ?: ""} - ${education.endYear ?: ""}",
                            grade = education.score ?: "N/A",
                            isLast = index == allEducations.value!!.lastIndex
                        )
                    }
                }

                // Experience with Modern Design
                StylishSectionCard(
                    title = "Professional Experience",
                    icon = "💼"
                ) {
                    allExperiences.value!!.forEachIndexed { index, experience ->
                        ModernExperienceItem(
                            position = experience.title ?: "Unknown Position",
                            company = experience.companyName ?: "Unknown Company",
                            duration = "${experience.startDate ?: ""} - ${experience.endDate ?: "Present"}",
                            description = experience.experienceLetter ?: "No letter available",
                            isActive = experience.isCurrentlyWorking ?: false
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Skills with Progress Style
                StylishSectionCard(
                    title = "Technical Skills",
                    icon = "⚡"
                ) {
                    if (allSkills.value!!.isEmpty()) {
                        Text(
                            text = "No skills added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        ModernSkillChipGroup(
                            skills = allSkills.value!!.map { skillModel ->
                                val color = when (skillModel.level?.lowercase()) {
                                    "expert" -> Color(0xFF4CAF50)
                                    "advanced" -> Color(0xFF2196F3)
                                    "intermediate" -> Color(0xFFFF9800)
                                    "beginner" -> Color(0xFF9C27B0)
                                    else -> Color(0xFF607D8B)
                                }
                                (skillModel.skill ?: "Unknown") to color
                            }
                        )
                    }
                }

                // Training Cards
                StylishSectionCard(
                    title = "Training & Certifications",
                    icon = "📜"
                ) {
                    if (allTrainings.value!!.isEmpty()) {
                        Text(
                            text = "No trainings added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        allTrainings.value!!.forEachIndexed { index, training ->
                            ModernTrainingCard(
                                title = training.trainingName ?: "Unknown Training",
                                organization = training.instituteName ?: "Unknown Organization",
                                year = training.completionDate ?: "N/A",
                                duration = "${training.duration ?: ""} ${training.durationType ?: ""}"
                            )
                            if (index != allTrainings.value!!.lastIndex) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                // function to convert level string to float value:
                fun getLevelValue(level: String): Float {
                    return when (level.lowercase()) {
                        "expert", "fluent", "native" -> 1.0f
                        "advanced", "proficient" -> 0.75f
                        "intermediate" -> 0.5f
                        "beginner", "basic" -> 0.25f
                        else -> 0.5f
                    }
                }

                // Languages with Proficiency Bars
                StylishSectionCard(
                    title = "Languages",
                    icon = "🌐"
                ) {
                    if (allLanguages.value!!.isEmpty()) {
                        Text(
                            text = "No languages added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        allLanguages.value!!.forEachIndexed { index, language ->
                            // Calculate average proficiency
                            val readingProf = getLevelValue(language.readingLevel ?: "Beginner")
                            val speakingProf = getLevelValue(language.speakingLevel ?: "Beginner")
                            val writingProf = getLevelValue(language.writingLevel ?: "Beginner")
                            val listeningProf = getLevelValue(language.listeningLevel ?: "Beginner")
                            val averageProficiency = (readingProf + speakingProf + writingProf + listeningProf) / 4f

                            LanguageWithBar(
                                language = language.language ?: "Unknown",
                                proficiency = averageProficiency
                            )
                            if (index != allLanguages.value!!.lastIndex) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                // Portfolio with Social Links
                StylishSectionCard(
                    title = "Portfolio & Connect",
                    icon = "🔗"
                ) {
                    if (allPortfolios.value!!.isEmpty()) {
                        Text(
                            text = "No portfolio links added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        allPortfolios.value!!.forEachIndexed { index, portfolio ->
                            val color = getPortfolioColor(portfolio.accountName ?: "")
                            SocialLinkItem(
                                platform = portfolio.accountName ?: "Unknown",
                                handle = portfolio.accountLink ?: "",
                                color = color
                            )
                            if (index != allPortfolios.value!!.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // References
                StylishSectionCard(
                    title = "References",
                    icon = "👥"
                ) {
                    if (allReferences.value!!.isEmpty()) {
                        Text(
                            text = "No references added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        allReferences.value!!.forEachIndexed { index, reference ->
                            ModernReferenceCard(
                                name = reference.name ?: "Unknown",
                                designation = reference.jobTitle ?: "Unknown Position",
                                organization = reference.companyName ?: "Unknown Company",
                                contact = reference.contactNumber ?: reference.email ?: "N/A"
                            )
                            if (index != allReferences.value!!.lastIndex) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ModernProfileHeader(
    name: String? = null,
    profession: String? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color.Black.copy(alpha = 0.25f)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Box {
                // Decorative background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E88E5),
                                    Color(0xFF1565C0)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    // Profile Photo with Ring
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(136.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(128.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE3F2FD)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = name?:"Loading...",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color(0xFF212121)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = profession?:"Loading...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PersonalInfoGrid(
    phoneNumber: String? = null,
    email: String? = null,
    dob: String? = null,
    gender: String? = null,
    religion: String? = null,
    nationality: String? = null,
    maritalStatus: String? = null,
    address: String? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
                Text(text = "📋", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    CompactInfoItem(icon = "📱", label = "Phone", value = phoneNumber?:"loading...")
                    Spacer(modifier = Modifier.height(12.dp))
                    CompactInfoItem(icon = "📧", label = "Email", value = email?:"loading...")
                    Spacer(modifier = Modifier.height(12.dp))
                    CompactInfoItem(icon = "🎂", label = "Date of Birth", value = dob?:"loading...")
                    Spacer(modifier = Modifier.height(12.dp))
                    CompactInfoItem(icon = "⚧", label = "Gender", value = gender?:"loading...")
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    CompactInfoItem(icon = "🕉", label = "Religion", value = religion?:"loading...")
                    Spacer(modifier = Modifier.height(12.dp))
                    CompactInfoItem(icon = "🇳🇵", label = "Nationality", value = nationality?:"loading...")
                    Spacer(modifier = Modifier.height(12.dp))
                    CompactInfoItem(icon = "💑", label = "Marital Status", value = maritalStatus?:"loading...")
                    Spacer(modifier = Modifier.height(12.dp))
                    CompactInfoItem(icon = "📍", label = "Address", value = address?:"loading...")
                }
            }
        }
    }
}

@Composable
fun CompactInfoItem(icon: String, label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 11.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun StylishSectionCard(
    title: String,
    icon: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    fontSize = 20.sp
                )
            }
            content()
        }
    }
}

@Composable
fun TimelineEducationItem(degree: String, institution: String, year: String, grade: String, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E88E5))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(Color(0xFFBBDEFB))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = degree,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = institution,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Text(
                        text = year,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Text(
                        text = grade,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (!isLast) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ModernExperienceItem(position: String, company: String, duration: String, description: String, isActive: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = position,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = company,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (isActive) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text(
                            text = "Current",
                            color = White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = duration,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF424242),
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
fun ModernSkillChipGroup(skills: List<Pair<String, Color>>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        skills.chunked(2).forEach { rowSkills ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowSkills.forEach { (skill, color) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = color.copy(alpha = 0.15f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = skill,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = color,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (rowSkills.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ModernTrainingCard(title: String, organization: String, year: String, duration: String = "") {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = organization,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (duration.isNotBlank()) {
                    Text(
                        text = "Duration: $duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 2.dp),
                        fontSize = 11.sp
                    )
                }
            }
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF6F00))
            ) {
                Text(
                    text = year,
                    color = White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun LanguageWithBar(language: String, proficiency: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = language,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121)
            )
            Text(
                text = "${(proficiency * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1565C0),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE0E0E0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(proficiency)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1E88E5),
                                Color(0xFF1565C0)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun SocialLinkItem(platform: String, handle: String, color: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = platform.first().toString(),
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = platform,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Text(
                    text = handle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            }
        }
    }
}

@Composable
fun ModernReferenceCard(name: String, designation: String, organization: String, contact: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9C27B0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.split(" ").map { it.first() }.take(2).joinToString(""),
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = designation,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9C27B0),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = organization,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = contact,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1565C0),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
fun getPortfolioColor(accountName: String): Color {
    return when (accountName.lowercase()) {
        "github" -> Color(0xFF333333)
        "linkedin" -> Color(0xFF0077B5)
        "twitter", "x" -> Color(0xFF1DA1F2)
        "facebook" -> Color(0xFF1877F2)
        "instagram" -> Color(0xFFE4405F)
        "youtube" -> Color(0xFFFF0000)
        "portfolio", "website" -> Color(0xFF1E88E5)
        "behance" -> Color(0xFF1769FF)
        "dribbble" -> Color(0xFFEA4C89)
        "medium" -> Color(0xFF000000)
        "stackoverflow" -> Color(0xFFF48024)
        else -> Color(0xFF607D8B)
    }
}

@Preview
@Composable
fun GreetingPreview3() {
    CvViewBody()
}