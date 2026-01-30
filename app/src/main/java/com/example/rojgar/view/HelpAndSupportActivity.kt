package com.example.rojgar.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rojgar.R
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.repository.UserRepo
import com.example.rojgar.viewmodel.HelpSupportViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.io.File

class HelpAndSupportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpAndSupportBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpAndSupportBody() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Get ViewModel using viewModel() composable
    val helpSupportViewModel: HelpSupportViewModel = viewModel()
    val userRepo = remember { UserRepo() }
    val companyRepo = remember { CompanyRepoImpl() }
    val jobSeekerRepo = remember { JobSeekerRepoImpl() }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid ?: ""
    val userEmail = currentUser?.email ?: ""

    var userRole by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var isLoadingUserData by remember { mutableStateOf(true) }

    var problemType by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var screenshotUri by remember { mutableStateOf<Uri?>(null) }
    var screenshotUrl by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showImageOptions by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var topBarVisible by remember { mutableStateOf(false) }

    val maxDescriptionLength = 500
    val remainingChars = maxDescriptionLength - description.length

    val problemTypes = listOf(
        "Profile Update Issue",
        "App Crash / Not Responding",
        "Feature Not Working",
        "Slow Performance",
        "Notification Issue",
        "Data Not Syncing",
        "Account Verification Issue",
        "Security / Privacy Concern",
        "Other"
    )

    val priorities = listOf("Low", "Medium", "High", "Urgent")

    // Observing ViewModel states
    val uploadingScreenshot by helpSupportViewModel.uploadingScreenshot.observeAsState()
    val screenshotUrlFromVM by helpSupportViewModel.screenshotUrl.observeAsState()
    val submitSuccess by helpSupportViewModel.submitSuccess.observeAsState()
    val submitMessage by helpSupportViewModel.submitMessage.observeAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            screenshotUri = it
            screenshotUrl = "" // Reset URL when new file is selected
            helpSupportViewModel.clearScreenshot()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Image captured - the URI is already set in screenshotUri
            screenshotUrl = "" // Reset URL when new file is captured
            helpSupportViewModel.clearScreenshot()
        }
    }

    // Observe screenshot URL from ViewModel
    LaunchedEffect(screenshotUrlFromVM) {
        screenshotUrlFromVM?.let { url ->
            screenshotUrl = url
            // If we were waiting for upload to complete, submit the request
            if (isSubmitting && screenshotUrl.isNotEmpty()) {
                helpSupportViewModel.submitHelpRequest(
                    userId = userId,
                    userType = userRole,
                    userEmail = userEmail,
                    userName = userName,
                    problemType = problemType,
                    priority = priority,
                    description = description,
                    screenshotUrl = screenshotUrl
                )
            }
        }
    }

    // Observe submit success
    LaunchedEffect(submitSuccess) {
        submitSuccess?.let { success ->
            if (success) {
                isSubmitting = false
                showSuccessDialog = true
                helpSupportViewModel.resetSubmitState()
            } else {
                isSubmitting = false
                Toast.makeText(context, submitMessage ?: "Submission failed", Toast.LENGTH_SHORT).show()
                helpSupportViewModel.resetSubmitState()
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        topBarVisible = true

        if (userId.isNotEmpty()) {
            userRepo.getCurrentUserData(companyRepo, jobSeekerRepo) { type, data ->
                isLoadingUserData = false
                when (type) {
                    "Company" -> {
                        userRole = "COMPANY"
                        val company = data as? com.example.rojgar.model.CompanyModel
                        userName = company?.companyName ?: "Unknown Company"
                    }
                    "JobSeeker" -> {
                        userRole = "JOB_SEEKER"
                        val jobSeeker = data as? com.example.rojgar.model.JobSeekerModel
                        userName = jobSeeker?.fullName ?: "Unknown User"
                    }
                    else -> {
                        Toast.makeText(context, "Unable to determine user type", Toast.LENGTH_SHORT).show()
                    }
                }
                showContent = true
            }
        } else {
            isLoadingUserData = false
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            showContent = true
        }
    }

    fun resetForm() {
        problemType = ""
        priority = ""
        description = ""
        screenshotUri = null
        screenshotUrl = ""
        helpSupportViewModel.clearScreenshot()
    }

    fun submitHelpRequest() {
        helpSupportViewModel.submitHelpRequest(
            userId = userId,
            userType = userRole,
            userEmail = userEmail,
            userName = userName,
            problemType = problemType,
            priority = priority,
            description = description,
            screenshotUrl = screenshotUrl
        )
    }

    fun validateAndSubmit() {
        when {
            problemType.isEmpty() -> {
                Toast.makeText(context, "Please select a problem type", Toast.LENGTH_SHORT).show()
                return
            }
            priority.isEmpty() -> {
                Toast.makeText(context, "Please select priority", Toast.LENGTH_SHORT).show()
                return
            }
            description.isEmpty() -> {
                Toast.makeText(context, "Please describe your problem", Toast.LENGTH_SHORT).show()
                return
            }
            description.length < 10 -> {
                Toast.makeText(context, "Description too short (min 10 characters)", Toast.LENGTH_SHORT).show()
                return
            }
        }

        isSubmitting = true

        if (screenshotUri != null && screenshotUrl.isEmpty()) {
            // Upload screenshot first, then submit
            helpSupportViewModel.uploadScreenshot(context, screenshotUri!!, userId)
        } else {
            // Submit immediately (with or without existing URL)
            submitHelpRequest()
        }
    }

    fun openGallery() {
        galleryLauncher.launch("image/*")
        showImageOptions = false
    }

    fun openCamera() {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCameraPermission) {
            try {
                val photoFile = File.createTempFile("help_screenshot_", ".jpg", context.cacheDir)
                val photoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    photoFile
                )
                screenshotUri = photoUri
                cameraLauncher.launch(photoUri)
            } catch (e: Exception) {
                Toast.makeText(context, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.CAMERA),
                    100
                )
            }
        }
        showImageOptions = false
    }

    if (showSuccessDialog) {
        SuccessDialog(onDismiss = {
            showSuccessDialog = false
            resetForm()
        })
    }

    if (showImageOptions) {
        ImageOptionsDialog(
            onGalleryClick = { openGallery() },
            onCameraClick = { openCamera() },
            onDismiss = { showImageOptions = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom Top Bar with gradient - Same as FeedbackActivity
            AnimatedVisibility(
                visible = topBarVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1976D2),
                                    Color(0xFF2196F3),
                                    Color(0xFF42A5F5)
                                )
                            )
                        )
                        .padding(top = 40.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { activity?.finish() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Help & Support",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "We're here to help you",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Content
            if (isLoadingUserData) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Color(0xFF2196F3),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600))
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        Spacer(Modifier.height(24.dp))

                        var headerVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { delay(200); headerVisible = true }

                        AnimatedVisibility(
                            visible = headerVisible,
                            enter = slideInVertically(
                                initialOffsetY = { -it / 2 },
                                animationSpec = tween(500, easing = FastOutSlowInEasing)
                            )
                        ) {
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(Color.White)
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        Modifier.size(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFF2196F3).copy(0.12f)
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.document),
                                            "Help",
                                            tint = Color(0xFF2196F3),
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "Need Assistance?",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF263238)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text("We're here to help you", fontSize = 13.sp, color = Color(0xFF78909C))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        var formVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { delay(400); formVisible = true }

                        AnimatedVisibility(
                            visible = formVisible,
                            enter = fadeIn(tween(600)) +
                                    slideInVertically(
                                        initialOffsetY = { -it / 2 },
                                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                                    )
                        ) {
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(Color.White)
                            ) {
                                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                                    Text(
                                        "Describe Your Issue",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF263238)
                                    )

                                    Spacer(Modifier.height(20.dp))

                                    HelpSupportDropdown(
                                        value = problemType,
                                        label = "Problem Type *",
                                        options = problemTypes,
                                        icon = R.drawable.jobcategoryicon,
                                        onValueChange = { problemType = it }
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    HelpSupportDropdown(
                                        value = priority,
                                        label = "Priority *",
                                        options = priorities,
                                        icon = R.drawable.joblevelicon,
                                        onValueChange = { priority = it }
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    Column {
                                        OutlinedTextField(
                                            value = description,
                                            onValueChange = { if (it.length <= maxDescriptionLength) description = it },
                                            label = { Text("Description *") },
                                            placeholder = { Text("Describe your problem in detail...", color = Color(0xFFBDBDBD)) },
                                            leadingIcon = {
                                                Icon(
                                                    painterResource(R.drawable.document),
                                                    null,
                                                    tint = Color(0xFF2196F3),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth().height(150.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            maxLines = 6,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF2196F3),
                                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                focusedLabelColor = Color(0xFF2196F3),
                                                unfocusedLabelColor = Color(0xFF78909C)
                                            )
                                        )

                                        Spacer(Modifier.height(8.dp))

                                        Text(
                                            "$remainingChars characters remaining",
                                            fontSize = 12.sp,
                                            color = if (remainingChars < 50) Color(0xFFF44336) else Color(0xFF78909C),
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }

                                    Spacer(Modifier.height(16.dp))

                                    Column {
                                        Text(
                                            "Screenshot (Optional)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF263238)
                                        )

                                        Spacer(Modifier.height(8.dp))

                                        if (screenshotUri != null) {
                                            Box(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color(0xFFF5F5F5))
                                            ) {
                                                AsyncImage(
                                                    model = screenshotUri,
                                                    contentDescription = "Screenshot",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )

                                                IconButton(
                                                    onClick = {
                                                        screenshotUri = null
                                                        screenshotUrl = ""
                                                        helpSupportViewModel.clearScreenshot()
                                                    },
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(8.dp)
                                                        .size(40.dp)
                                                        .background(Color.White.copy(0.9f), RoundedCornerShape(20.dp))
                                                ) {
                                                    Icon(Icons.Default.Delete, "Remove", tint = Color(0xFFF44336))
                                                }
                                            }
                                        } else {
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(120.dp)
                                                    .clickable { showImageOptions = true },
                                                shape = RoundedCornerShape(16.dp),
                                                color = Color(0xFF2196F3).copy(0.05f),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    2.dp,
                                                    Color(0xFF2196F3).copy(0.3f)
                                                )
                                            ) {
                                                Column(
                                                    Modifier.fillMaxSize(),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    if (uploadingScreenshot == true) {
                                                        CircularProgressIndicator(
                                                            color = Color(0xFF2196F3),
                                                            strokeWidth = 2.dp,
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                        Spacer(Modifier.height(8.dp))
                                                        Text("Uploading...", color = Color(0xFF2196F3), fontSize = 14.sp)
                                                    } else {
                                                        Icon(
                                                            painterResource(R.drawable.baseline_upload_24),
                                                            "Upload",
                                                            tint = Color(0xFF2196F3),
                                                            modifier = Modifier.size(40.dp)
                                                        )
                                                        Spacer(Modifier.height(8.dp))
                                                        Text(
                                                            "Tap to upload screenshot",
                                                            color = Color(0xFF2196F3),
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        var buttonsVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { delay(600); buttonsVisible = true }

                        AnimatedVisibility(
                            visible = buttonsVisible,
                            enter = slideInVertically(
                                initialOffsetY = { -it / 2 },
                                animationSpec = tween(500, easing = FastOutSlowInEasing)
                            )
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { resetForm() },
                                    modifier = Modifier.weight(0.35f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(
                                        0xFFE52750
                                    )
                                    ),
                                    enabled = !isSubmitting && uploadingScreenshot != true
                                ) {
                                    Text("Clear", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { validateAndSubmit() },
                                    modifier = Modifier.weight(0.65f).height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = ButtonDefaults.buttonElevation(4.dp, 8.dp),
                                    enabled = !isSubmitting && uploadingScreenshot != true
                                ) {
                                    if (isSubmitting || uploadingScreenshot == true) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            if (uploadingScreenshot == true) "Uploading..." else "Sending...",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Spacer(Modifier.width(8.dp))
                                        Text("Send Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HelpSupportDropdown(
    value: String,
    label: String,
    options: List<String>,
    icon: Int,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()
        .clickable { expanded = true }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    painterResource(icon),
                    null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                Icon(
                    painterResource(R.drawable.outline_keyboard_arrow_down_24),
                    null,
                    tint = Color(0xFF78909C),
                    modifier = Modifier.size(24.dp).clickable { expanded = true }
                )
            },
            modifier = Modifier.fillMaxWidth().height(60.dp).clickable { expanded = true },
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
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White).fillMaxWidth(0.85f)
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = Color(0xFF263238)) },
                    onClick = { onValueChange(item); expanded = false }
                )
            }
        }
    }
}

@Composable
fun SuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF4CAF50).copy(0.1f)
            ) {
                Icon(
                    painterResource(R.drawable.baseline_upload_24),
                    null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        title = {
            Text(
                "Request Submitted!",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "Your help request has been submitted successfully. Our support team will get back to you soon.",
                fontSize = 15.sp,
                color = Color(0xFF78909C),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ImageOptionsDialog(
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Choose Image Source",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onGalleryClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(painterResource(R.drawable.baseline_upload_24), null, Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Gallery", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCameraClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(painterResource(R.drawable.baseline_upload_24), null, Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Camera", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF78909C))
            }
        }
    )
}

@Preview
@Composable
fun HelpAndSupportPreview() {
    HelpAndSupportBody()
}