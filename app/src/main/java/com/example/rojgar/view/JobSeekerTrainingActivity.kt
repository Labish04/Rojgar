package com.example.rojgar.view

import android.app.DatePickerDialog
import android.content.Intent
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
import com.example.rojgar.model.TrainingModel
import com.example.rojgar.repository.TrainingRepoImpl
import com.example.rojgar.viewmodel.TrainingViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.util.*

class JobSeekerTrainingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerTrainingBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerTrainingBody() {
    val context = LocalContext.current

    val trainingViewModel = remember { TrainingViewModel(TrainingRepoImpl()) }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val jobSeekerId = currentUser?.uid ?: ""

    var trainings by remember { mutableStateOf(listOf<TrainingModel>()) }
    var showTrainingSheet by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var selectedTraining by remember { mutableStateOf<TrainingModel?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var topBarVisible by remember { mutableStateOf(false) }

    var trainingName by remember { mutableStateOf("") }
    var instituteName by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var durationType by remember { mutableStateOf("Month") }
    var completionDate by remember { mutableStateOf("") }
    var certificateUri by remember { mutableStateOf<Uri?>(null) }
    var certificateName by remember { mutableStateOf("") }
    var certificateUrl by remember { mutableStateOf("") }
    var isUploadingCertificate by remember { mutableStateOf(false) }

    var currentTrainingId by remember { mutableStateOf("") }
    var showDeleteAlert by remember { mutableStateOf(false) }
    var trainingToDelete by remember { mutableStateOf<String?>(null) }

    val durationTypes = listOf("Month", "Year", "Week", "Day")

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var showDatePicker by remember { mutableStateOf(false) }

    // Gallery launcher for certificate
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            certificateUri = it
            certificateName = "Certificate selected"
            certificateUrl = "" // Clear previous URL when new file is selected
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        topBarVisible = true

        if (jobSeekerId.isNotEmpty()) {
            isLoading = true
            trainingViewModel.getTrainingsByJobSeekerId(jobSeekerId) { success, message, trainingList ->
                isLoading = false
                if (success) {
                    trainingList?.let {
                        trainings = it
                    }
                } else {
                    Toast.makeText(context, "Failed to load trainings: $message", Toast.LENGTH_SHORT).show()
                }
                showContent = true
            }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            showContent = true
        }
    }

    fun resetForm() {
        trainingName = ""
        instituteName = ""
        duration = ""
        durationType = "Month"
        completionDate = ""
        certificateUri = null
        certificateName = ""
        certificateUrl = ""
        currentTrainingId = ""
        isEditing = false
        isUploadingCertificate = false
    }

    fun openAddForm() {
        resetForm()
        showTrainingSheet = true
    }

    fun openEditForm(training: TrainingModel) {
        trainingName = training.trainingName
        instituteName = training.instituteName
        duration = training.duration
        durationType = training.durationType
        completionDate = training.completionDate
        certificateName = if (training.certificate.isNotEmpty()) "Certificate uploaded" else ""
        certificateUrl = training.certificate
        currentTrainingId = training.trainingId
        isEditing = true
        showTrainingSheet = true
    }



    fun saveTraining(certificateImageUrl: String) {
        val trainingModel = TrainingModel(
            trainingId = if (isEditing) currentTrainingId else "",
            trainingName = trainingName,
            instituteName = instituteName,
            duration = duration,
            durationType = durationType,
            completionDate = completionDate,
            certificate = certificateImageUrl,
            jobSeekerId = jobSeekerId
        )

        if (isEditing) {
            trainingViewModel.updateTraining(currentTrainingId, trainingModel) { success, message ->
                if (success) {
                    Toast.makeText(context, "Training updated", Toast.LENGTH_SHORT).show()
                    trainingViewModel.getTrainingsByJobSeekerId(jobSeekerId) { success2, message2, trainingList ->
                        if (success2) {
                            trainingList?.let { trainings = it }
                        }
                    }
                    showTrainingSheet = false
                    resetForm()
                } else {
                    Toast.makeText(context, "Update failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            trainingViewModel.addTraining(trainingModel) { success, message ->
                if (success) {
                    Toast.makeText(context, "Training added", Toast.LENGTH_SHORT).show()
                    trainingViewModel.getTrainingsByJobSeekerId(jobSeekerId) { success2, message2, trainingList ->
                        if (success2) {
                            trainingList?.let { trainings = it }
                        }
                    }
                    showTrainingSheet = false
                    resetForm()
                } else {
                    Toast.makeText(context, "Add failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun uploadCertificateAndSave() {
        if (trainingName.isEmpty() || instituteName.isEmpty() || duration.isEmpty() || completionDate.isEmpty()) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        isUploadingCertificate = true

        if (certificateUri != null) {
            // Upload new certificate
            trainingViewModel.uploadCertificateImage(context, certificateUri!!) { uploadedUrl ->
                isUploadingCertificate = false
                if (uploadedUrl != null) {
                    saveTraining(uploadedUrl)
                } else {
                    Toast.makeText(context, "Failed to upload certificate", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Use existing URL or empty if no certificate
            isUploadingCertificate = false
            saveTraining(certificateUrl)
        }
    }
    fun deleteTraining(trainingId: String) {
        trainingToDelete = trainingId
        showDeleteAlert = true
    }

    if (showDeleteAlert) {
        ModernTrainingDeleteDialog(
            onDismiss = {
                showDeleteAlert = false
                trainingToDelete = null
            },
            onConfirm = {
                trainingToDelete?.let { trainingId ->
                    trainingViewModel.deleteTraining(trainingId) { success, message ->
                        if (success) {
                            Toast.makeText(context, "Training deleted", Toast.LENGTH_SHORT).show()
                            trainingViewModel.getTrainingsByJobSeekerId(jobSeekerId) { success2, message2, trainingList ->
                                if (success2) {
                                    trainingList?.let { trainings = it }
                                }
                            }
                            showDetailDialog = false
                        } else {
                            Toast.makeText(context, "Delete failed: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showDeleteAlert = false
                trainingToDelete = null
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                completionDate = "$d/${m + 1}/$y"
                showDatePicker = false
            },
            year,
            month,
            day
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
                                    text = "Training",
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
                                            painter = painterResource(R.drawable.document),
                                            contentDescription = "Training",
                                            tint = Color(0xFF2196F3),
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Certifications & Training",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF263238)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Showcase your achievements",
                                            fontSize = 13.sp,
                                            color = Color(0xFF78909C)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (trainings.isEmpty()) {
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
                                                painter = painterResource(id = R.drawable.noexperience),
                                                contentDescription = "no training",
                                                tint = Color(0xFF78909C),
                                                modifier = Modifier.size(70.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        "No Training Added Yet",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF263238)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        "Add your certifications to stand out",
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
                                items(trainings) { training ->
                                    ModernTrainingCard(
                                        training = training,
                                        onClick = {
                                            selectedTraining = training
                                            showDetailDialog = true
                                        },
                                        onEditClick = { openEditForm(training) },
                                        onDeleteClick = { deleteTraining(training.trainingId) }
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
                                text = if (trainings.isEmpty()) "Add Training" else "Add Another",
                                onClick = { openAddForm() }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (showDetailDialog && selectedTraining != null) {
        ModernTrainingDetailDialog(
            training = selectedTraining!!,
            onDismiss = {
                showDetailDialog = false
                selectedTraining = null
            },
            onEdit = {
                showDetailDialog = false
                openEditForm(selectedTraining!!)
                selectedTraining = null
            },
            onDelete = {
                showDetailDialog = false
                deleteTraining(selectedTraining!!.trainingId)
                selectedTraining = null
            }
        )
    }

    if (showTrainingSheet) {
        ModernTrainingFormDialog(
            isEditing = isEditing,
            trainingName = trainingName,
            instituteName = instituteName,
            duration = duration,
            durationType = durationType,
            completionDate = completionDate,
            certificateName = certificateName,
            durationTypes = durationTypes,
            isUploadingCertificate = isUploadingCertificate || isUploading,
            onTrainingNameChange = { trainingName = it },
            onInstituteNameChange = { instituteName = it },
            onDurationChange = { duration = it },
            onDurationTypeChange = { durationType = it },
            onCompletionDateClick = { showDatePicker = true },
            onCertificateClick = { galleryLauncher.launch("image/*") },
            onDismiss = {
                showTrainingSheet = false
                resetForm()
            },
            onSave = { uploadCertificateAndSave() }
        )
    }
}

@Composable
fun ModernTrainingCard(
    training: TrainingModel,
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
                            painter = painterResource(R.drawable.document),
                            contentDescription = "Training",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = training.trainingName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF263238)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = training.instituteName,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF78909C)
                        )
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
                horizontalArrangement = Arrangement.SpaceBetween
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
                            text = "${training.duration} ${training.durationType}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Completed",
                        fontSize = 12.sp,
                        color = Color(0xFF78909C),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = training.completionDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238)
                    )
                }
            }

            if (training.certificate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_upload_24),
                            contentDescription = "Certificate",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Certificate Available",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ModernTrainingFormDialog(
    isEditing: Boolean,
    trainingName: String,
    instituteName: String,
    duration: String,
    durationType: String,
    completionDate: String,
    certificateName: String,
    durationTypes: List<String>,
    onTrainingNameChange: (String) -> Unit,
    onInstituteNameChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onDurationTypeChange: (String) -> Unit,
    onCompletionDateClick: () -> Unit,
    onCertificateClick: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    isUploadingCertificate: Boolean
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
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
                    text = if (isEditing) "Edit Training" else "Add Training",
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
                    // Training Name Field
                    ModernTrainingTextField(
                        value = trainingName,
                        onValueChange = onTrainingNameChange,
                        label = "Training Name *",
                        icon = R.drawable.experienceicon
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Institute Name Field
                    ModernTrainingTextField(
                        value = instituteName,
                        onValueChange = onInstituteNameChange,
                        label = "Institute Name *",
                        icon = R.drawable.companynameicon
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Duration and Duration Type Row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Duration Field
                        ModernTrainingTextField(
                            value = duration,
                            onValueChange = onDurationChange,
                            label = "Duration *",
                            icon = R.drawable.joblevelicon,
                            modifier = Modifier.weight(1f)
                        )

                        // Duration Type Dropdown
                        var expandedDurationType by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = durationType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Type *") },
                                leadingIcon = {
                                    Icon(
                                        painterResource(id = R.drawable.jobcategoryicon),
                                        contentDescription = null,
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        painterResource(R.drawable.outline_keyboard_arrow_down_24),
                                        contentDescription = null,
                                        tint = Color(0xFF78909C),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { expandedDurationType = true }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clickable { expandedDurationType = true },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color(0xFFE0E0E0),
                                    disabledContainerColor = Color.White,
                                    disabledTextColor = Color(0xFF263238),
                                    disabledLabelColor = Color(0xFF78909C)
                                )
                            )

                            DropdownMenu(
                                expanded = expandedDurationType,
                                onDismissRequest = { expandedDurationType = false },
                                modifier = Modifier
                                    .background(Color.White)
                                    .fillMaxWidth(0.85f)
                            ) {
                                durationTypes.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item, color = Color(0xFF263238)) },
                                        onClick = {
                                            onDurationTypeChange(item)
                                            expandedDurationType = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Completion Date Field
                    ModernTrainingDateField(
                        value = completionDate,
                        label = "Completion Date *",
                        onClick = onCompletionDateClick,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Certificate Upload Field
                    ModernTrainingFileUpload(
                        fileName = certificateName,
                        isUploading = isUploadingCertificate,
                        onUploadClick = onCertificateClick
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
                        enabled = !isUploadingCertificate
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
                        enabled = !isUploadingCertificate
                    ) {
                        if (isUploadingCertificate) {
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
fun ModernTrainingTextField(
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
fun ModernTrainingDateField(
    value: String,
    label: String,
    onClick: () -> Unit,
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
            label = { Text(label) },
            enabled = false,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.calendaricon),
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier
                        .size(24.dp)

                )
            },
            modifier = modifier
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color(0xFFE0E0E0),
                disabledContainerColor = Color.White,
                disabledTextColor = Color(0xFF263238),
                disabledLabelColor = Color(0xFF78909C)
            )
        )
    }
}

@Composable
fun ModernTrainingFileUpload(
    isUploading: Boolean,
    fileName: String,
    onUploadClick: () -> Unit
) {
    OutlinedTextField(
        value = if (isUploading) "Uploading..." else fileName,
        onValueChange = {},
        readOnly = true,
        label = { Text("Certificate (Optional)") },
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
            }else {
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
fun ModernTrainingDetailDialog(
    training: TrainingModel,
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
                text = "Training Details",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238)
            )
        },
        text = {
            Column {
                DetailItem(title = "Training Name", value = training.trainingName)
                DetailItem(title = "Institute", value = training.instituteName)
                DetailItem(title = "Duration", value = "${training.duration} ${training.durationType}")
                DetailItem(title = "Completion Date", value = training.completionDate)

                if (training.certificate.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "✓ Certificate Available",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
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
fun ModernTrainingDeleteDialog(
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
                text = "Delete Training?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "This action cannot be undone. Are you sure you want to delete this training?",
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


@Preview
@Composable
fun JobSeekerTrainingPreview() {
    JobSeekerTrainingBody()
}