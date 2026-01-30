package com.example.rojgar.view

import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rojgar.R
import com.example.rojgar.model.ExperienceModel
import com.example.rojgar.repository.ExperienceRepoImpl
import com.example.rojgar.viewmodel.ExperienceViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.util.*

class JobSeekerExperienceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerExperienceBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerExperienceBody() {
    val context = LocalContext.current
    val activity = context as Activity

    val experienceViewModel = remember { ExperienceViewModel(ExperienceRepoImpl()) }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val jobSeekerId = currentUser?.uid ?: ""

    var experiences by remember { mutableStateOf(listOf<ExperienceModel>()) }
    var showSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var currentExperienceId by remember { mutableStateOf("") }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedExperience by remember { mutableStateOf<ExperienceModel?>(null) }

    var companyName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var jobLevel by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var currentlyWorking by remember { mutableStateOf(false) }
    var experienceLetterUrl by remember { mutableStateOf("") }
    var isUploadingExperienceLetter by remember { mutableStateOf(false) }
    var selectedExperienceLetterUri by remember { mutableStateOf<Uri?>(null) }
    var experienceLetterFileName by remember { mutableStateOf("") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedExperienceLetterUri = it
            experienceLetterFileName = it.lastPathSegment ?: "Experience Letter"
        }
    }

    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var expandedLevel by remember { mutableStateOf(false) }
    var showDeleteAlert by remember { mutableStateOf(false) }
    var experienceToDelete by remember { mutableStateOf<String?>(null) }

    var topBarVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    val jobCategories = listOf(
        "Accounting / Finance", "Architecture", "Banking",
        "Construction / Engineering", "Graphics / Designing",
        "IT (Information Technology)", "Computer Engineering", "Others"
    )
    val jobLevels = listOf("Top Level", "Senior Level", "Mid Level", "Entry Level")

    LaunchedEffect(Unit) {
        delay(100)
        topBarVisible = true
        delay(200)
        contentVisible = true

        if (jobSeekerId.isNotEmpty()) {
            experienceViewModel.getExperiencesByJobSeekerId(jobSeekerId) { success, message, expList ->
                if (success) {
                    expList?.let { experiences = it }
                } else {
                    Toast.makeText(context, "Failed to load experiences: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun resetForm() {
        companyName = ""
        jobTitle = ""
        selectedCategory = ""
        jobLevel = ""
        startDate = ""
        endDate = ""
        currentlyWorking = false
        experienceLetterUrl = ""
        selectedExperienceLetterUri = null
        experienceLetterFileName = ""
        currentExperienceId = ""
        isEditing = false
        isUploadingExperienceLetter = false
    }

    fun openAddForm() {
        resetForm()
        showSheet = true
    }

    fun openEditForm(experience: ExperienceModel) {
        companyName = experience.companyName
        jobTitle = experience.title
        selectedCategory = experience.jobCategory
        jobLevel = experience.level
        startDate = experience.startDate
        endDate = experience.endDate
        currentlyWorking = experience.isCurrentlyWorking
        experienceLetterUrl = experience.experienceLetter
        experienceLetterFileName = if (experience.experienceLetter.isNotEmpty()) "Experience Letter Uploaded" else ""
        currentExperienceId = experience.experienceId
        isEditing = true
        showSheet = true
    }
    fun saveExperience() {
        if (companyName.isEmpty() || jobTitle.isEmpty() || startDate.isEmpty()) {
            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        fun saveOrUpdateExperience(experienceModel: ExperienceModel) {
            if (isEditing) {
                experienceViewModel.updateExperience(currentExperienceId, experienceModel) { success, message ->
                    if (success) {
                        Toast.makeText(context, "Experience updated", Toast.LENGTH_SHORT).show()
                        experienceViewModel.getExperiencesByJobSeekerId(jobSeekerId) { _, _, expList ->
                            expList?.let { experiences = it }
                        }
                        showSheet = false
                        resetForm()
                    } else {
                        Toast.makeText(context, "Update failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                experienceViewModel.addExperience(experienceModel) { success, message ->
                    if (success) {
                        Toast.makeText(context, "Experience added", Toast.LENGTH_SHORT).show()
                        experienceViewModel.getExperiencesByJobSeekerId(jobSeekerId) { _, _, expList ->
                            expList?.let { experiences = it }
                        }
                        showSheet = false
                        resetForm()
                    } else {
                        Toast.makeText(context, "Add failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // First upload image if selected
        if (selectedExperienceLetterUri != null) {
            isUploadingExperienceLetter = true
            experienceViewModel.uploadExperienceLetterImage(context, selectedExperienceLetterUri!!) { uploadedUrl ->
                isUploadingExperienceLetter = false
                if (uploadedUrl != null) {
                    // Image uploaded successfully, now save experience with the URL
                    val experienceModel = ExperienceModel(
                        experienceId = if (isEditing) currentExperienceId else "",
                        companyName = companyName,
                        title = jobTitle,
                        jobCategory = selectedCategory,
                        level = jobLevel,
                        startDate = startDate,
                        endDate = if (currentlyWorking) "" else endDate,
                        isCurrentlyWorking = currentlyWorking,
                        experienceLetter = uploadedUrl,
                        jobSeekerId = jobSeekerId
                    )
                    saveOrUpdateExperience(experienceModel)
                } else {
                    Toast.makeText(context, "Failed to upload experience letter", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // No image to upload, use existing URL
            val experienceModel = ExperienceModel(
                experienceId = if (isEditing) currentExperienceId else "",
                companyName = companyName,
                title = jobTitle,
                jobCategory = selectedCategory,
                level = jobLevel,
                startDate = startDate,
                endDate = if (currentlyWorking) "" else endDate,
                isCurrentlyWorking = currentlyWorking,
                experienceLetter = experienceLetterUrl,
                jobSeekerId = jobSeekerId
            )
            saveOrUpdateExperience(experienceModel)
        }
    }



    fun deleteExperience(experienceId: String) {
        experienceToDelete = experienceId
        showDeleteAlert = true
    }

    if (showDeleteAlert) {
        ModernDeleteDialog(
            onDismiss = {
                showDeleteAlert = false
                experienceToDelete = null
            },
            onConfirm = {
                experienceToDelete?.let { expId ->
                    experienceViewModel.deleteExperience(expId) { success, message ->
                        if (success) {
                            Toast.makeText(context, "Experience deleted", Toast.LENGTH_SHORT).show()
                            experienceViewModel.getExperiencesByJobSeekerId(jobSeekerId) { _, _, expList ->
                                expList?.let { experiences = it }
                            }
                            showDetailDialog = false
                        } else {
                            Toast.makeText(context, "Delete failed: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showDeleteAlert = false
                experienceToDelete = null
            }
        )
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                startDate = "$d/${m + 1}/$y"
                showStartDatePicker = false
            },
            year, month, day
        ).show()
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                endDate = "$d/${m + 1}/$y"
                showEndDatePicker = false
            },
            year, month, day
        ).show()
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = topBarVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Card(
                    modifier = Modifier
                        .height(140.dp)
                        .padding(top = 55.dp)
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(5.dp)),
                    shape = RoundedCornerShape(5.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        var backPressed by remember { mutableStateOf(false) }
                        val backScale by animateFloatAsState(
                            targetValue = if (backPressed) 0.85f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )

                        IconButton(
                            onClick = {
                                backPressed = true
                                activity.finish()
                            },
                            modifier = Modifier.graphicsLayer {
                                scaleX = backScale
                                scaleY = backScale
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        var titleVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(300)
                            titleVisible = true
                        }

                        AnimatedVisibility(
                            visible = titleVisible,
                            enter = fadeIn(animationSpec = tween(500)) +
                                    slideInHorizontally(
                                        initialOffsetX = { it / 2 },
                                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                                    ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Experience",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFFBBDEFB),
                            Color(0xFF90CAF9)
                        )
                    )
                )
        ) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(animationSpec = tween(600))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    var headerVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(400)
                        headerVisible = true
                    }

                    AnimatedVisibility(
                        visible = headerVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -it / 2 },
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(500))
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .shadow(8.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF2196F3).copy(alpha = 0.12f)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.experienceicon),
                                        contentDescription = "Experience",
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Work Experience",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF263238)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "What are your most relevant experiences?",
                                        fontSize = 13.sp,
                                        color = Color(0xFF78909C)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (experiences.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            var emptyStateVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(600)
                                emptyStateVisible = true
                            }

                            AnimatedVisibility(
                                visible = emptyStateVisible,
                                enter = scaleIn(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) + fadeIn()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        modifier = Modifier.size(120.dp),
                                        shape = RoundedCornerShape(30.dp),
                                        color = Color.White.copy(alpha = 0.5f)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.noexperience),
                                            contentDescription = "no experience",
                                            tint = Color(0xFF78909C),
                                            modifier = Modifier.padding(30.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        "No Experience Added Yet",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF263238),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        "Start building your professional profile\nby adding your work experience",
                                        textAlign = TextAlign.Center,
                                        fontSize = 14.sp,
                                        color = Color(0xFF78909C),
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        ModernAddButton(
                            onClick = { openAddForm() },
                            text = if (experiences.isEmpty()) "Add Experience" else "Add Another",
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(experiences) { experience ->
                                ModernExperienceCard(
                                    experience = experience,
                                    onClick = {
                                        selectedExperience = experience
                                        showDetailDialog = true
                                    },
                                    onEditClick = { openEditForm(experience) },
                                    onDeleteClick = { deleteExperience(experience.experienceId) }
                                )
                            }
                        }

                        ModernAddButton(
                            onClick = { openAddForm() },
                            text = if (experiences.isEmpty()) "Add Experience" else "Add Another",
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                    }
                }
            }
        }
    }

    if (showDetailDialog && selectedExperience != null) {
        ModernDetailDialog(
            experience = selectedExperience!!,
            onDismiss = { showDetailDialog = false },
            onEdit = {
                showDetailDialog = false
                openEditForm(selectedExperience!!)
            },
            onDelete = {
                experienceToDelete = selectedExperience!!.experienceId
                showDeleteAlert = true
                showDetailDialog = false
            }
        )
    }

    if (showSheet) {
        ModernExperienceFormDialog(
            isEditing = isEditing,
            companyName = companyName,
            onCompanyNameChange = { companyName = it },
            jobTitle = jobTitle,
            onJobTitleChange = { jobTitle = it },
            selectedCategory = selectedCategory,
            expandedCategory = expandedCategory,
            onExpandedCategoryChange = { expandedCategory = it },
            jobCategories = jobCategories,
            onCategorySelect = { selectedCategory = it },
            jobLevel = jobLevel,
            expandedLevel = expandedLevel,
            onExpandedLevelChange = { expandedLevel = it },
            jobLevels = jobLevels,
            onLevelSelect = { jobLevel = it },
            startDate = startDate,
            onStartDateClick = { showStartDatePicker = true },
            endDate = endDate,
            onEndDateClick = { showEndDatePicker = true },
            currentlyWorking = currentlyWorking,
            onCurrentlyWorkingChange = {
                currentlyWorking = it
                if (it) endDate = ""
            },
            selectedImageUri = selectedExperienceLetterUri,
            imageFileName = experienceLetterFileName,
            isUploadingImage = isUploadingExperienceLetter,
            onImageSelect = { launcher.launch("image/*") },
            onDismiss = {
                showSheet = false
                resetForm()
            },
            onSave = { saveExperience() }
        )
    }
}

@Composable
fun ModernExperienceCard(
    experience: ExperienceModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = experience.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF263238)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = experience.companyName,
                        fontSize = 15.sp,
                        color = Color(0xFF78909C)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2196F3).copy(alpha = 0.1f)
                    ) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF44336).copy(alpha = 0.1f)
                    ) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2196F3).copy(alpha = 0.12f)
            ) {
                Text(
                    text = experience.calculateYearsOfExperience(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}


@Composable
fun ModernDetailDialog(
    experience: ExperienceModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Experience Details",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238)
            )
        },
        text = {
            Column {
                DetailItem(title = "Job Title", value = experience.title)
                DetailItem(title = "Company", value = experience.companyName)
                DetailItem(title = "Job Category", value = experience.jobCategory)
                DetailItem(title = "Job Level", value = experience.level)
                DetailItem(title = "Start Date", value = experience.startDate)
                DetailItem(
                    title = "End Date",
                    value = if (experience.isCurrentlyWorking) "Present" else experience.endDate
                )
                DetailItem(
                    title = "Currently Working",
                    value = if (experience.isCurrentlyWorking) "Yes" else "No"
                )
                DetailItem(title = "Duration", value = experience.calculateYearsOfExperience())

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (experience.experienceLetter.isNotEmpty())
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else
                        Color(0xFF9E9E9E).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (experience.experienceLetter.isNotEmpty())
                            "✓ Experience Letter Uploaded"
                        else
                            "No Experience Letter",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (experience.experienceLetter.isNotEmpty())
                            Color(0xFF4CAF50)
                        else
                            Color(0xFF9E9E9E),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun ModernDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF44336).copy(alpha = 0.1f)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        title = {
            Text(
                text = "Delete Experience?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "This action cannot be undone. Are you sure you want to delete this experience?",
                fontSize = 15.sp,
                color = Color(0xFF78909C),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF78909C)
                    )
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun ModernExperienceFormDialog(
    isEditing: Boolean,
    companyName: String,
    onCompanyNameChange: (String) -> Unit,
    jobTitle: String,
    onJobTitleChange: (String) -> Unit,
    selectedCategory: String,
    expandedCategory: Boolean,
    onExpandedCategoryChange: (Boolean) -> Unit,
    jobCategories: List<String>,
    onCategorySelect: (String) -> Unit,
    jobLevel: String,
    expandedLevel: Boolean,
    onExpandedLevelChange: (Boolean) -> Unit,
    jobLevels: List<String>,
    onLevelSelect: (String) -> Unit,
    startDate: String,
    onStartDateClick: () -> Unit,
    endDate: String,
    onEndDateClick: () -> Unit,
    currentlyWorking: Boolean,
    onCurrentlyWorkingChange: (Boolean) -> Unit,
    selectedImageUri: Uri?,
    imageFileName: String,
    isUploadingImage: Boolean,
    onImageSelect: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.80f)
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit Experience" else "Add Experience",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    ModernTextField(
                        value = companyName,
                        onValueChange = onCompanyNameChange,
                        label = "Company Name *",
                        icon = R.drawable.companynameicon
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ModernTextField(
                        value = jobTitle,
                        onValueChange = onJobTitleChange,
                        label = "Job Title *",
                        icon = R.drawable.jobtitleicon
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ModernDropdowns(
                        value = selectedCategory,
                        label = "Job Category",
                        icon = R.drawable.jobcategoryicon,
                        expanded = expandedCategory,
                        onExpandedChange = onExpandedCategoryChange,
                        items = jobCategories,
                        onItemSelect = onCategorySelect
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ModernDropdowns(
                        value = jobLevel,
                        label = "Job Level",
                        icon = R.drawable.joblevelicon,
                        expanded = expandedLevel,
                        onExpandedChange = onExpandedLevelChange,
                        items = jobLevels,
                        onItemSelect = onLevelSelect
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModernDateField(
                            value = startDate,
                            label = "Start Date *",
                            onClick = onStartDateClick,
                            modifier = Modifier.weight(1f)
                        )

                        ModernDateField(
                            value = endDate,
                            label = "End Date",
                            onClick = onEndDateClick,
                            enabled = !currentlyWorking,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCurrentlyWorkingChange(!currentlyWorking) }
                    ) {
                        Switch(
                            checked = currentlyWorking,
                            onCheckedChange = onCurrentlyWorkingChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2196F3),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFBDBDBD)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Currently work here?",
                            fontSize = 15.sp,
                            color = Color(0xFF263238)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    ModernFileUpload(
                        fileName = imageFileName,
                        isUploading = isUploadingImage,
                        onUploadClick = onImageSelect
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.3f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF78909C)
                        ),
                        enabled = !isUploadingImage
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(0.7f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        ),
                        enabled = !isUploadingImage
                    ) {
                        if (isUploadingImage) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = if (isEditing) "Update" else "Save",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: Int
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                painterResource(id = icon),
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(24.dp)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2196F3),
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedLabelColor = Color(0xFF2196F3),
            unfocusedLabelColor = Color(0xFF78909C)
        )
    )
}

@Composable
fun ModernDropdowns(
    value: String,
    label: String,
    icon: Int,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<String>,
    onItemSelect: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_keyboard_arrow_down_24),
                    contentDescription = null,
                    tint = Color(0xFF78909C),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onExpandedChange(true) }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clickable { onExpandedChange(true) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color(0xFFE0E0E0),
                disabledContainerColor = Color.White,
                disabledTextColor = Color(0xFF263238),
                disabledLabelColor = Color(0xFF78909C)
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth(0.85f)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = Color(0xFF263238)) },
                    onClick = {
                        onItemSelect(item)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
fun ModernDateField(
    value: String,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.calendaricon),
                    contentDescription = null,
                    tint = if (enabled) Color(0xFF2196F3) else Color(0xFFBDBDBD),
                    modifier = Modifier.size(24.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor =
                    if (enabled) Color(0xFF2196F3) else Color(0xFFE0E0E0),
                disabledContainerColor = Color.White,
                disabledTextColor = Color(0xFF263238),
                disabledLabelColor = Color(0xFF78909C)
            )
        )
    }
}


@Composable
fun ModernFileUpload(
    fileName: String,
    isUploading: Boolean,
    onUploadClick: () -> Unit
) {
    OutlinedTextField(
        value = if (isUploading) "Uploading..." else fileName.ifEmpty { "No file chosen" },
        onValueChange = {},
        readOnly = true,
        label = { Text("Experience Letter (Optional)") },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.baseline_upload_24),
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(24.dp)
            )
        },
        trailingIcon = {
            if (isUploading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2196F3).copy(alpha = 0.1f)
                ) {
                    IconButton(
                        onClick = onUploadClick,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painterResource(R.drawable.baseline_upload_24),
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(enabled = !isUploading) { onUploadClick() },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = Color(0xFFE0E0E0),
            disabledContainerColor = Color.White,
            disabledTextColor = Color(0xFF263238),
            disabledLabelColor = Color(0xFF78909C)
        )
    )
}

@Composable
fun DetailItem(title: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF78909C)
        )
        Text(
            text = value.ifEmpty { "-" },
            fontSize = 15.sp,
            color = Color(0xFF263238),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview
@Composable
fun JobSeekerExperiencePreview() {
    JobSeekerExperienceBody()
}