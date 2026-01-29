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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.R
import com.example.rojgar.model.*
import com.example.rojgar.repository.*
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.viewmodel.*
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

data class FilterCriteria(
    val profession: String = "",
    val educationDegree: String = "",
    val minExperienceYears: Int = 0,
    val requiredSkills: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationBody(
    jobPostId: String,
    jobTitle: String,
    companyId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    val applicationViewModel = remember { ApplicationViewModel(ApplicationRepoImpl(context)) }
    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val educationViewModel = remember { EducationViewModel(EducationRepoImpl()) }
    val experienceViewModel = remember { ExperienceViewModel(ExperienceRepoImpl()) }
    val skillViewModel = remember { SkillViewModel(SkillRepoImpl()) }

    val applications by applicationViewModel.applications.observeAsState(emptyList())
    val isLoading by applicationViewModel.loading.observeAsState(false)

    var showFilterDialog by remember { mutableStateOf(false) }
    var filterCriteria by remember { mutableStateOf(FilterCriteria()) }
    var activeFiltersCount by remember { mutableStateOf(0) }

    // Job seeker data maps
    var jobSeekerProfiles by remember { mutableStateOf<Map<String, JobSeekerModel>>(emptyMap()) }
    var educationData by remember { mutableStateOf<Map<String, List<EducationModel>>>(emptyMap()) }
    var experienceData by remember { mutableStateOf<Map<String, List<ExperienceModel>>>(emptyMap()) }
    var skillsData by remember { mutableStateOf<Map<String, List<SkillModel>>>(emptyMap()) }

    val filteredApplications = remember(applications, jobPostId) {
        applications.filter { it.postId == jobPostId }
    }

    // Filter applications based on criteria
    val finalFilteredApplications = remember(filteredApplications, filterCriteria, jobSeekerProfiles, educationData, experienceData, skillsData) {
        if (activeFiltersCount == 0) {
            filteredApplications
        } else {
            filteredApplications.filter { application ->
                val jobSeeker = jobSeekerProfiles[application.jobSeekerId]
                val education = educationData[application.jobSeekerId] ?: emptyList()
                val experience = experienceData[application.jobSeekerId] ?: emptyList()
                val skills = skillsData[application.jobSeekerId] ?: emptyList()

                var matches = true

                // Filter by profession
                if (filterCriteria.profession.isNotEmpty()) {
                    matches = matches && jobSeeker?.profession?.contains(filterCriteria.profession, ignoreCase = true) == true
                }

                // Filter by education degree
                if (filterCriteria.educationDegree.isNotEmpty()) {
                    matches = matches && education.any {
                        it.educationDegree.contains(filterCriteria.educationDegree, ignoreCase = true)
                    }
                }

                // Filter by minimum experience
                if (filterCriteria.minExperienceYears > 0) {
                    val totalYears = experience.sumOf { exp ->
                        try {
                            val yearsString = exp.calculateYearsOfExperience()
                            val years = yearsString.split(" ").firstOrNull()?.toIntOrNull() ?: 0
                            years
                        } catch (e: Exception) {
                            0
                        }
                    }
                    matches = matches && totalYears >= filterCriteria.minExperienceYears
                }

                // Filter by required skills
                if (filterCriteria.requiredSkills.isNotEmpty()) {
                    val jobSeekerSkills = skills.map { it.skill.lowercase() }
                    matches = matches && filterCriteria.requiredSkills.all { requiredSkill ->
                        jobSeekerSkills.any { it.contains(requiredSkill.lowercase()) }
                    }
                }

                matches
            }
        }
    }

    LaunchedEffect(Unit) {
        if (companyId.isNotEmpty()) {
            applicationViewModel.getApplicationsByCompany(companyId)
        }
    }

    // Load job seeker data for all applications
    LaunchedEffect(filteredApplications) {
        filteredApplications.forEach { application ->
            // Load job seeker profile
            jobSeekerViewModel.getJobSeekerById(application.jobSeekerId) { success, _, data ->
                if (success && data != null) {
                    jobSeekerProfiles = jobSeekerProfiles + (application.jobSeekerId to data)
                }
            }

            // Load education
            educationViewModel.getEducationsByJobSeekerId(application.jobSeekerId) { success, _, eduList ->
                if (success && eduList != null) {
                    educationData = educationData + (application.jobSeekerId to eduList)
                }
            }

            // Load experience
            experienceViewModel.getExperiencesByJobSeekerId(application.jobSeekerId) { success, _, expList ->
                if (success && expList != null) {
                    experienceData = experienceData + (application.jobSeekerId to expList)
                }
            }

            // Load skills
            skillViewModel.getSkillsByJobSeekerId(application.jobSeekerId) { success, _, skillList ->
                if (success && skillList != null) {
                    skillsData = skillsData + (application.jobSeekerId to skillList)
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            currentFilters = filterCriteria,
            onDismiss = { showFilterDialog = false },
            onApply = { newFilters ->
                filterCriteria = newFilters
                activeFiltersCount =
                    (if (newFilters.profession.isNotEmpty()) 1 else 0) +
                            (if (newFilters.educationDegree.isNotEmpty()) 1 else 0) +
                            (if (newFilters.minExperienceYears > 0) 1 else 0) +
                            (if (newFilters.requiredSkills.isNotEmpty()) 1 else 0)
                showFilterDialog = false
            },
            onClear = {
                filterCriteria = FilterCriteria()
                activeFiltersCount = 0
                showFilterDialog = false
            }
        )
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
                            color = Color.White,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${finalFilteredApplications.size} of ${filteredApplications.size} Applications",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (activeFiltersCount > 0) {
                                Badge(
                                    containerColor = Color(0xFFD32F2F),
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = "$activeFiltersCount",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(
                                painter = painterResource(R.drawable.filter),
                                contentDescription = "Filter",
                                tint = if (activeFiltersCount > 0) DarkBlue2 else Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFFBBDEFB),
                            Color(0xFF90CAF9)
                        )
                    )
                )
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
                finalFilteredApplications.isEmpty() -> {
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
                                text = if (activeFiltersCount > 0) "No Matching Applications" else "No Applications Yet",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (activeFiltersCount > 0)
                                    "Try adjusting your filters"
                                else
                                    "Applications for this job will appear here",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            if (activeFiltersCount > 0) {
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(onClick = {
                                    filterCriteria = FilterCriteria()
                                    activeFiltersCount = 0
                                }) {
                                    Text("Clear Filters", color = DarkBlue2)
                                }
                            }
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
                        items(finalFilteredApplications, key = { it.applicationId }) { application ->
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
fun FilterDialog(
    currentFilters: FilterCriteria,
    onDismiss: () -> Unit,
    onApply: (FilterCriteria) -> Unit,
    onClear: () -> Unit
) {
    var profession by remember { mutableStateOf(currentFilters.profession) }
    var educationDegree by remember { mutableStateOf(currentFilters.educationDegree) }
    var minExperience by remember { mutableStateOf(currentFilters.minExperienceYears.toString()) }
    var skillsText by remember { mutableStateOf(currentFilters.requiredSkills.joinToString(", ")) }

    var professionFocused by remember { mutableStateOf(false) }
    var educationFocused by remember { mutableStateOf(false) }
    var experienceFocused by remember { mutableStateOf(false) }
    var skillsFocused by remember { mutableStateOf(false) }

    val slideIn = remember {
        androidx.compose.animation.core.tween<Float>(
            durationMillis = 300,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF8FBFF),
                                Color.White
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    // Animated Header with Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(
                                                DarkBlue2,
                                                Color(0xFF2962FF)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.filter),
                                    contentDescription = "Filter",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Filter Applications",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A)
                                )
                                Text(
                                    text = "Refine your search",
                                    fontSize = 13.sp,
                                    color = Color(0xFF999999)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Profession Filter with Animation
                    FilterSection(
                        label = "Profession",
                        icon = R.drawable.profileemptypic,
                        isFocused = professionFocused,
                        gradient = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                    ) {
                        OutlinedTextField(
                            value = profession,
                            onValueChange = { profession = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "e.g., Software Engineer, Designer",
                                    fontSize = 13.sp,
                                    color = Color(0xFFAAAAAA)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkBlue2,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            interactionSource = remember { MutableInteractionSource() }
                                .also { interactionSource ->
                                    LaunchedEffect(interactionSource) {
                                        interactionSource.interactions.collect { interaction ->
                                            professionFocused = interaction is FocusInteraction.Focus
                                        }
                                    }
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Education Filter
                    FilterSection(
                        label = "Education Degree",
                        icon = R.drawable.emailicon,
                        isFocused = educationFocused,
                        gradient = listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7))
                    ) {
                        OutlinedTextField(
                            value = educationDegree,
                            onValueChange = { educationDegree = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "e.g., Bachelor's, Master's, PhD",
                                    fontSize = 13.sp,
                                    color = Color(0xFFAAAAAA)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7B1FA2),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            interactionSource = remember { MutableInteractionSource() }
                                .also { interactionSource ->
                                    LaunchedEffect(interactionSource) {
                                        interactionSource.interactions.collect { interaction ->
                                            educationFocused = interaction is FocusInteraction.Focus
                                        }
                                    }
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Experience Filter with Slider Preview
                    FilterSection(
                        label = "Minimum Experience",
                        icon = R.drawable.datetimeicon,
                        isFocused = experienceFocused,
                        gradient = listOf(Color(0xFFFFF9C4), Color(0xFFFFF59D))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = minExperience,
                                onValueChange = {
                                    if (it.isEmpty() || (it.all { char -> char.isDigit() } && it.toIntOrNull() ?: 0 <= 20)) {
                                        minExperience = it
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        "Years",
                                        fontSize = 13.sp,
                                        color = Color(0xFFAAAAAA)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFF57F17),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                interactionSource = remember { MutableInteractionSource() }
                                    .also { interactionSource ->
                                        LaunchedEffect(interactionSource) {
                                            interactionSource.interactions.collect { interaction ->
                                                experienceFocused = interaction is FocusInteraction.Focus
                                            }
                                        }
                                    }
                            )

                            if (minExperience.isNotEmpty() && minExperience.toIntOrNull() != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFF59D))
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "${minExperience}+ years",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF57F17)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Skills Filter with Chips Preview
                    FilterSection(
                        label = "Required Skills",
                        icon = R.drawable.jobpost_filled,
                        isFocused = skillsFocused,
                        gradient = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                    ) {
                        Column {
                            OutlinedTextField(
                                value = skillsText,
                                onValueChange = { skillsText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        "e.g., Java, Kotlin, Android, UI/UX",
                                        fontSize = 13.sp,
                                        color = Color(0xFFAAAAAA)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2E7D32),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                maxLines = 2,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                interactionSource = remember { MutableInteractionSource() }
                                    .also { interactionSource ->
                                        LaunchedEffect(interactionSource) {
                                            interactionSource.interactions.collect { interaction ->
                                                skillsFocused = interaction is FocusInteraction.Focus
                                            }
                                        }
                                    }
                            )

                            // Skills Chips Preview
                            if (skillsText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                androidx.compose.foundation.layout.FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    skillsText.split(",").take(6).forEach { skill ->
                                        if (skill.trim().isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(
                                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                                            colors = listOf(
                                                                Color(0xFF66BB6A),
                                                                Color(0xFF43A047)
                                                            )
                                                        )
                                                    )
                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = skill.trim(),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Modern Action Buttons with Gradient
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClear,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF666666)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                Color(0xFFE0E0E0)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Clear",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val skills = skillsText.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }

                                val newFilters = FilterCriteria(
                                    profession = profession.trim(),
                                    educationDegree = educationDegree.trim(),
                                    minExperienceYears = minExperience.toIntOrNull() ?: 0,
                                    requiredSkills = skills
                                )
                                onApply(newFilters)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(
                                                DarkBlue2,
                                                Color(0xFF2962FF)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.filter),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Apply Filters",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
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

@Composable
fun FilterSection(
    label: String,
    icon: Int,
    isFocused: Boolean,
    gradient: List<Color>,
    content: @Composable () -> Unit
) {
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = gradient
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF666666)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        }
        content()
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