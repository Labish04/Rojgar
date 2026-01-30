package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import com.example.rojgar.model.EducationModel
import com.example.rojgar.repository.EducationRepoImpl
import com.example.rojgar.ui.theme.*
import com.example.rojgar.viewmodel.EducationViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.util.*

class JobSeekerEducationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerEducationBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerEducationBody() {
    val context = LocalContext.current
    val educationViewModel = remember { EducationViewModel(EducationRepoImpl()) }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val jobSeekerId = currentUser?.uid ?: ""

    var educations by remember { mutableStateOf(listOf<EducationModel>()) }
    var showDegreeSheet by remember { mutableStateOf(false) }
    var showDetailSheet by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var selectedEducation by remember { mutableStateOf<EducationModel?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var topBarVisible by remember { mutableStateOf(false) }

    var selectedDegree by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var board by remember { mutableStateOf("") }
    var fieldOfStudy by remember { mutableStateOf("") }
    var startYear by remember { mutableStateOf("") }
    var endYear by remember { mutableStateOf("") }
    var gradeType by remember { mutableStateOf("CGPA") }
    var score by remember { mutableStateOf("") }
    var currentlyStudying by remember { mutableStateOf(false) }

    var currentEducationId by remember { mutableStateOf("") }
    var showDeleteAlert by remember { mutableStateOf(false) }
    var educationToDelete by remember { mutableStateOf<String?>(null) }

    var showStartYearPicker by remember { mutableStateOf(false) }
    var showEndYearPicker by remember { mutableStateOf(false) }

    val degreeOptions = listOf(
        "Doctorate (Ph. D)",
        "Graduate (Masters)",
        "Professional Certification",
        "Under Graduate (Bachelor)",
        "Higher Secondary (+2/A Levels/IB)",
        "Diploma Certificate",
        "School (SLC/ SEE)",
        "Other"
    )

    // Load educations
    LaunchedEffect(Unit) {
        delay(100)
        topBarVisible = true

        if (jobSeekerId.isNotEmpty()) {
            isLoading = true
            educationViewModel.getEducationsByJobSeekerId(jobSeekerId) { success, message, educationList ->
                isLoading = false
                if (success) {
                    educationList?.let {
                        educations = it
                    }
                } else {
                    Toast.makeText(context, "Failed to load educations: $message", Toast.LENGTH_SHORT).show()
                }
                showContent = true
            }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            showContent = true
        }
    }

    // Function to reset form
    fun resetForm() {
        selectedDegree = ""
        institution = ""
        board = ""
        fieldOfStudy = ""
        startYear = ""
        endYear = ""
        gradeType = "CGPA"
        score = ""
        currentlyStudying = false
        currentEducationId = ""
        isEditing = false
    }

    // Function to adding new education
    fun openAddForm() {
        resetForm()
        showDegreeSheet = true
    }

    // Function to editing existing education
    fun openEditForm(education: EducationModel) {
        selectedDegree = education.educationDegree
        institution = education.instituteName
        board = education.board
        fieldOfStudy = education.field
        startYear = education.startYear
        endYear = education.endYear
        gradeType = education.gradeType
        score = education.score
        currentlyStudying = education.currentlyStudying
        currentEducationId = education.educationId
        isEditing = true
        showDetailSheet = true
    }

    // Function to save education
    fun saveEducation() {
        if (selectedDegree.isEmpty() || institution.isEmpty() || startYear.isEmpty()) {
            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!currentlyStudying && endYear.isEmpty()) {
            Toast.makeText(context, "Please select end year or mark as currently studying", Toast.LENGTH_SHORT).show()
            return
        }

        val educationModel = EducationModel(
            educationId = if (isEditing) currentEducationId else "",
            educationDegree = selectedDegree,
            instituteName = institution,
            board = board,
            field = fieldOfStudy,
            startYear = startYear,
            endYear = if (currentlyStudying) "Present" else endYear,
            gradeType = gradeType,
            score = score,
            currentlyStudying = currentlyStudying,
            jobSeekerId = jobSeekerId
        )

        if (isEditing) {
            educationViewModel.updateEducation(currentEducationId, educationModel) { success, message ->
                if (success) {
                    Toast.makeText(context, "Education updated", Toast.LENGTH_SHORT).show()
                    educationViewModel.getEducationsByJobSeekerId(jobSeekerId) { success2, message2, educationList ->
                        if (success2) {
                            educationList?.let { educations = it }
                        }
                    }
                    showDetailSheet = false
                    resetForm()
                } else {
                    Toast.makeText(context, "Update failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            educationViewModel.addEducation(educationModel) { success, message ->
                if (success) {
                    Toast.makeText(context, "Education added", Toast.LENGTH_SHORT).show()
                    educationViewModel.getEducationsByJobSeekerId(jobSeekerId) { success2, message2, educationList ->
                        if (success2) {
                            educationList?.let { educations = it }
                        }
                    }
                    showDetailSheet = false
                    resetForm()
                } else {
                    Toast.makeText(context, "Add failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun deleteEducation(educationId: String) {
        educationToDelete = educationId
        showDeleteAlert = true
    }

    // Delete Dialog
    if (showDeleteAlert) {
        ModernEducationDeleteDialog(
            onDismiss = {
                showDeleteAlert = false
                educationToDelete = null
            },
            onConfirm = {
                educationToDelete?.let { eduId ->
                    educationViewModel.deleteEducation(eduId) { success, message ->
                        if (success) {
                            Toast.makeText(context, "Education deleted", Toast.LENGTH_SHORT).show()
                            educationViewModel.getEducationsByJobSeekerId(jobSeekerId) { success2, message2, educationList ->
                                if (success2) {
                                    educationList?.let { educations = it }
                                }
                            }
                            showDetailDialog = false
                        } else {
                            Toast.makeText(context, "Delete failed: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showDeleteAlert = false
                educationToDelete = null
            }
        )
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
                                (context as? ComponentActivity)?.finish()
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
                                    text = "Education",
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
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF2196F3),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(600))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        var headerVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(200)
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
                                            painter = painterResource(R.drawable.educationicon),
                                            contentDescription = "Education",
                                            tint = Color(0xFF2196F3),
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Academic Achievements",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF263238)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Showcase your educational journey",
                                            fontSize = 13.sp,
                                            color = Color(0xFF78909C)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (educations.isEmpty()) {
                            var emptyStateVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(400)
                                emptyStateVisible = true
                            }

                            AnimatedVisibility(
                                visible = emptyStateVisible,
                                enter = fadeIn(animationSpec = tween(600)) +
                                        scaleIn(
                                            initialScale = 0.8f,
                                            animationSpec = tween(600, easing = FastOutSlowInEasing)
                                        )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        modifier = Modifier.size(120.dp),
                                        shape = RoundedCornerShape(60.dp),
                                        color = Color.White.copy(alpha = 0.5f)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.educationicon),
                                                contentDescription = "no education",
                                                tint = Color(0xFF78909C),
                                                modifier = Modifier.size(70.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        "No Education Added Yet",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF263238)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        "Add your academic qualifications to stand out",
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF78909C),
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(educations) { education ->
                                    ModernEducationCard(
                                        education = education,
                                        onClick = {
                                            selectedEducation = education
                                            showDetailDialog = true
                                        },
                                        onEditClick = { openEditForm(education) },
                                        onDeleteClick = { deleteEducation(education.educationId) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        var buttonVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(600)
                            buttonVisible = true
                        }

                        AnimatedVisibility(
                            visible = buttonVisible,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(500, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(500))
                        ) {
                            ModernAddButton(
                                text = if (educations.isEmpty()) "Add Education" else "Add Another",
                                onClick = { openAddForm() }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Education Detail Dialog
    if (showDetailDialog && selectedEducation != null) {
        ModernEducationDetailDialog(
            education = selectedEducation!!,
            onDismiss = {
                showDetailDialog = false
                selectedEducation = null
            },
            onEdit = {
                showDetailDialog = false
                openEditForm(selectedEducation!!)
                selectedEducation = null
            },
            onDelete = {
                showDetailDialog = false
                deleteEducation(selectedEducation!!.educationId)
                selectedEducation = null
            }
        )
    }

    // Degree Selection Dialog
    if (showDegreeSheet) {
        ModernDegreeSelectionDialog(
            degreeOptions = degreeOptions,
            selectedDegree = selectedDegree,
            onDegreeSelected = { degree ->
                selectedDegree = degree
                showDegreeSheet = false
                showDetailSheet = true
            },
            onDismiss = { showDegreeSheet = false }
        )
    }

    // Education Detail Form Dialog
    if (showDetailSheet) {
        ModernEducationFormDialog(
            isEditing = isEditing,
            selectedDegree = selectedDegree,
            institution = institution,
            board = board,
            fieldOfStudy = fieldOfStudy,
            startYear = startYear,
            endYear = endYear,
            gradeType = gradeType,
            score = score,
            currentlyStudying = currentlyStudying,
            onSelectedDegreeChange = { selectedDegree = it },
            onInstitutionChange = { institution = it },
            onBoardChange = { board = it },
            onFieldOfStudyChange = { fieldOfStudy = it },
            onStartYearClick = { showStartYearPicker = true },
            onEndYearClick = { if (!currentlyStudying) showEndYearPicker = true },
            onGradeTypeChange = { gradeType = it },
            onScoreChange = { score = it },
            onCurrentlyStudyingChange = { currentlyStudying = it },
            onDismiss = {
                showDetailSheet = false
                resetForm()
            },
            onBackToDegree = {
                showDetailSheet = false
                showDegreeSheet = true
            },
            onSave = { saveEducation() }
        )
    }

    // Year Pickers
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    if (showStartYearPicker) {
        ModernYearPickerDialog(
            onDismiss = { showStartYearPicker = false },
            onYearSelected = { year ->
                startYear = year.toString()
                showStartYearPicker = false
            },
            initialYear = startYear.toIntOrNull() ?: currentYear
        )
    }

    if (showEndYearPicker) {
        ModernYearPickerDialog(
            onDismiss = { showEndYearPicker = false },
            onYearSelected = { year ->
                endYear = year.toString()
                showEndYearPicker = false
            },
            initialYear = endYear.toIntOrNull() ?: currentYear
        )
    }
}

@Composable
fun ModernEducationCard(
    education: EducationModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2196F3).copy(alpha = 0.12f)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.educationicon),
                            contentDescription = "Education",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = education.educationDegree,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF263238)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = education.instituteName,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF78909C)
                        )
                        if (education.field.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = education.field,
                                fontSize = 13.sp,
                                color = Color(0xFF546E7A)
                            )
                        }
                    }
                }

                Row {
                    var editPressed by remember { mutableStateOf(false) }
                    val editScale by animateFloatAsState(
                        targetValue = if (editPressed) 0.85f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    IconButton(
                        onClick = {
                            editPressed = true
                            onEditClick()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = editScale
                                scaleY = editScale
                            }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2196F3).copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    LaunchedEffect(editPressed) {
                        if (editPressed) {
                            delay(150)
                            editPressed = false
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    var deletePressed by remember { mutableStateOf(false) }
                    val deleteScale by animateFloatAsState(
                        targetValue = if (deletePressed) 0.85f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    IconButton(
                        onClick = {
                            deletePressed = true
                            onDeleteClick()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = deleteScale
                                scaleY = deleteScale
                            }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF44336).copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    LaunchedEffect(deletePressed) {
                        if (deletePressed) {
                            delay(150)
                            deletePressed = false
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Duration",
                        fontSize = 12.sp,
                        color = Color(0xFF78909C),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2196F3).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${education.startYear} - ${education.endYear}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (education.score.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Grade",
                            fontSize = 12.sp,
                            color = Color(0xFF78909C),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${education.score} ${education.gradeType}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                if (education.currentlyStudying) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Currently Studying",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernDegreeSelectionDialog(
    degreeOptions: List<String>,
    selectedDegree: String,
    onDegreeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Degree",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_close_24),
                            contentDescription = "Close",
                            tint = Color(0xFF78909C)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(degreeOptions) { degree ->
                        var isPressed by remember { mutableStateOf(false) }
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.98f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                .clickable {
                                    isPressed = true
                                    onDegreeSelected(degree)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (degree == selectedDegree)
                                    Color(0xFF2196F3).copy(alpha = 0.1f)
                                else Color.White
                            ),
                            border = if (degree == selectedDegree)
                                BorderStroke(2.dp, Color(0xFF2196F3))
                            else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (degree == selectedDegree)
                                        Color(0xFF2196F3)
                                    else Color(0xFFE0E0E0)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.educationicon),
                                        contentDescription = "Degree",
                                        tint = if (degree == selectedDegree)
                                            Color.White
                                        else Color(0xFF78909C),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = degree,
                                    fontSize = 16.sp,
                                    color = if (degree == selectedDegree)
                                        Color(0xFF2196F3)
                                    else Color(0xFF263238),
                                    fontWeight = if (degree == selectedDegree)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ModernEducationFormDialog(
    isEditing: Boolean,
    selectedDegree: String,
    institution: String,
    board: String,
    fieldOfStudy: String,
    startYear: String,
    endYear: String,
    gradeType: String,
    score: String,
    currentlyStudying: Boolean,
    onSelectedDegreeChange: (String) -> Unit,
    onInstitutionChange: (String) -> Unit,
    onBoardChange: (String) -> Unit,
    onFieldOfStudyChange: (String) -> Unit,
    onStartYearClick: () -> Unit,
    onEndYearClick: () -> Unit,
    onGradeTypeChange: (String) -> Unit,
    onScoreChange: (String) -> Unit,
    onCurrentlyStudyingChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onBackToDegree: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.89f)
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
                    text = if (isEditing) "Edit Education" else "Add Education",
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
                    // Selected Degree
                    OutlinedTextField(
                        value = selectedDegree,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Education Degree *") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.educationicon),
                                contentDescription = "Degree",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable { onBackToDegree() },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Color(0xFFE0E0E0),
                            disabledContainerColor = Color.White,
                            disabledTextColor = Color(0xFF263238),
                            disabledLabelColor = Color(0xFF78909C)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Institution Name
                    ModernEducationTextField(
                        value = institution,
                        onValueChange = onInstitutionChange,
                        label = "Institution Name *",
                        icon = R.drawable.companynameicon
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field of Study
                    ModernEducationTextField(
                        value = fieldOfStudy,
                        onValueChange = onFieldOfStudyChange,
                        label = "Field of Study",
                        icon = R.drawable.studyfieldicon
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Board
                    ModernEducationTextField(
                        value = board,
                        onValueChange = onBoardChange,
                        label = "Education Board",
                        icon = R.drawable.educationboardicon
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start Year Field
                        ModernEducationDateField(
                            value = startYear,
                            label = "Start Year *",
                            onClick = onStartYearClick,
                            modifier = Modifier.weight(1f)
                        )

                        // End Year Field
                        ModernEducationDateField(
                            value = if (currentlyStudying) "Present" else endYear,
                            label = "End Year",
                            onClick = onEndYearClick,
                            modifier = Modifier.weight(1f),
                            enabled = !currentlyStudying
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Currently Studying Switch
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2196F3).copy(alpha = 0.05f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Switch(
                                checked = currentlyStudying,
                                onCheckedChange = onCurrentlyStudyingChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2196F3),
                                    uncheckedThumbColor = Color(0xFFB0BEC5),
                                    uncheckedTrackColor = Color(0xFFE0E0E0)
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Currently studying?",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color(0xFF263238)
                                )
                                Text(
                                    "If checked, end year will be 'Present'",
                                    fontSize = 12.sp,
                                    color = Color(0xFF78909C)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grade Type Selection
                    Text(
                        text = "Grade Type",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color(0xFF263238)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { onGradeTypeChange("CGPA") },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (gradeType == "CGPA")
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3),
                                    contentColor = Color.White
                                )
                            else
                                ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF78909C)),
                            border = if (gradeType != "CGPA")
                                BorderStroke(1.dp, Color(0xFFE0E0E0))
                            else null
                        ) {
                            Text("CGPA", fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = { onGradeTypeChange("Marks") },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (gradeType == "Marks")
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3),
                                    contentColor = Color.White
                                )
                            else
                                ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF78909C)),
                            border = if (gradeType != "Marks")
                                BorderStroke(1.dp, Color(0xFFE0E0E0))
                            else null
                        ) {
                            Text("Percentage", fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Score Input
                    ModernEducationTextField(
                        value = score,
                        onValueChange = onScoreChange,
                        label = if (gradeType == "CGPA") "CGPA" else "Percentage",
                        icon = R.drawable.appliedjob
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackToDegree,
                        modifier = Modifier
                            .weight(0.3f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF78909C)
                        )
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
                        )
                    ) {
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

@Composable
fun ModernEducationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: Int,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(24.dp)
            )
        },
        modifier = modifier
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
fun ModernEducationDateField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
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
fun ModernEducationDetailDialog(
    education: EducationModel,
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
                text = "Education Details",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238)
            )
        },
        text = {
            Column {
                ModernDetailItem(title = "Degree", value = education.educationDegree)
                ModernDetailItem(title = "Institution", value = education.instituteName)
                ModernDetailItem(title = "Field of Study", value = education.field)
                ModernDetailItem(title = "Board", value = education.board)
                ModernDetailItem(title = "Start Year", value = education.startYear)
                ModernDetailItem(title = "End Year", value = education.endYear)
                ModernDetailItem(title = "Grade Type", value = education.gradeType)
                ModernDetailItem(title = "Score", value = education.score)
                ModernDetailItem(title = "Currently Studying", value = if (education.currentlyStudying) "Yes" else "No")
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
fun ModernEducationDeleteDialog(
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
                text = "Delete Education?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "This action cannot be undone. Are you sure you want to delete this education?",
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
fun ModernYearPickerDialog(
    onDismiss: () -> Unit,
    onYearSelected: (Int) -> Unit,
    initialYear: Int
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (1950..currentYear + 10).toList().reversed()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.6f),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Select Year",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(years) { year ->
                        var isPressed by remember { mutableStateOf(false) }
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.98f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )

                        Card(
                            onClick = {
                                isPressed = true
                                onYearSelected(year)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .graphicsLayer { scaleX = scale; scaleY = scale },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (year == initialYear)
                                    Color(0xFF2196F3).copy(alpha = 0.1f)
                                else Color.White
                            ),
                            border = if (year == initialYear)
                                BorderStroke(2.dp, Color(0xFF2196F3))
                            else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = year.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = if (year == initialYear) FontWeight.Bold else FontWeight.Normal,
                                    color = if (year == initialYear)
                                        Color(0xFF2196F3)
                                    else Color(0xFF263238)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF78909C))
                ) {
                    Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ModernDetailItem(title: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF78909C)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color(0xFF263238),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview
@Composable
fun JobSeekerEducationPreview() {
    JobSeekerEducationBody()
}