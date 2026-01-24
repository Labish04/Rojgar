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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rojgar.R
import com.example.rojgar.model.ReferenceModel
import com.example.rojgar.repository.ReferenceRepoImpl
import com.example.rojgar.ui.theme.*
import com.example.rojgar.viewmodel.ReferenceViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class JobSeekerReferenceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerReferenceBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun JobSeekerReferenceBody() {
    val context = LocalContext.current
    val referenceViewModel = remember { ReferenceViewModel(ReferenceRepoImpl()) }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val jobSeekerId = currentUser?.uid ?: ""

    var references by remember { mutableStateOf(listOf<ReferenceModel>()) }
    var showReferenceSheet by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var selectedReference by remember { mutableStateOf<ReferenceModel?>(null) }

    var refereeName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var contactType by remember { mutableStateOf("Mobile") }
    var currentReferenceId by remember { mutableStateOf("") }
    var showDeleteAlert by remember { mutableStateOf(false) }
    var referenceToDelete by remember { mutableStateOf<String?>(null) }

    var topBarVisible by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val contactTypes = listOf("Mobile", "Work", "Home")

    // Load references with animation delay
    LaunchedEffect(Unit) {
        delay(100)
        topBarVisible = true
        if (jobSeekerId.isNotEmpty()) {
            referenceViewModel.getReferencesByJobSeekerId(jobSeekerId) { success, message, referenceList ->
                if (success) {
                    referenceList?.let {
                        references = it
                    }
                } else {
                    Toast.makeText(context, "Failed to load references: $message", Toast.LENGTH_SHORT).show()
                }
            }
            delay(500)
        }
        isLoading = false
        showContent = true
    }

    // Function to reset form
    fun resetForm() {
        refereeName = ""
        jobTitle = ""
        companyName = ""
        email = ""
        contactNumber = ""
        contactType = "Mobile"
        currentReferenceId = ""
        isEditing = false
    }

    // Function to add new reference
    fun openAddForm() {
        resetForm()
        showReferenceSheet = true
    }

    // Function to edit existing reference
    fun openEditForm(reference: ReferenceModel) {
        refereeName = reference.name
        jobTitle = reference.jobTitle
        companyName = reference.companyName
        email = reference.email
        contactNumber = reference.contactNumber
        contactType = reference.contactType
        currentReferenceId = reference.referenceId
        isEditing = true
        showReferenceSheet = true
    }

    // Function to save reference
    fun saveReference() {
        if (refereeName.isEmpty() || jobTitle.isEmpty() || companyName.isEmpty() ||
            email.isEmpty() || contactNumber.isEmpty()) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val referenceModel = ReferenceModel(
            referenceId = if (isEditing) currentReferenceId else "",
            name = refereeName,
            jobTitle = jobTitle,
            companyName = companyName,
            email = email,
            contactType = contactType,
            contactNumber = contactNumber,
            jobSeekerId = jobSeekerId
        )

        if (isEditing) {
            referenceViewModel.updateReference(currentReferenceId, referenceModel) { success, message ->
                if (success) {
                    Toast.makeText(context, "Reference updated", Toast.LENGTH_SHORT).show()
                    referenceViewModel.getReferencesByJobSeekerId(jobSeekerId) { success2, message2, referenceList ->
                        if (success2) {
                            referenceList?.let { references = it }
                        }
                    }
                    showReferenceSheet = false
                    resetForm()
                } else {
                    Toast.makeText(context, "Update failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            referenceViewModel.addReference(referenceModel) { success, message ->
                if (success) {
                    Toast.makeText(context, "Reference added", Toast.LENGTH_SHORT).show()
                    referenceViewModel.getReferencesByJobSeekerId(jobSeekerId) { success2, message2, referenceList ->
                        if (success2) {
                            referenceList?.let { references = it }
                        }
                    }
                    showReferenceSheet = false
                    resetForm()
                } else {
                    Toast.makeText(context, "Add failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun deleteReference(referenceId: String) {
        referenceToDelete = referenceId
        showDeleteAlert = true
    }

    // Loading indicator
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF2196F3),
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
        }
    }

    // Delete Dialog
    if (showDeleteAlert) {
        ModernReferenceDeleteDialog(
            onDismiss = {
                showDeleteAlert = false
                referenceToDelete = null
            },
            onConfirm = {
                referenceToDelete?.let { referenceId ->
                    referenceViewModel.deleteReference(referenceId) { success, message ->
                        if (success) {
                            Toast.makeText(context, "Reference deleted", Toast.LENGTH_SHORT).show()
                            referenceViewModel.getReferencesByJobSeekerId(jobSeekerId) { success2, message2, referenceList ->
                                if (success2) {
                                    referenceList?.let { references = it }
                                }
                            }
                            showDetailDialog = false
                        } else {
                            Toast.makeText(context, "Delete failed: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showDeleteAlert = false
                referenceToDelete = null
            },
            referenceName = references.find { it.referenceId == referenceToDelete }?.name ?: ""
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
                                val intent = Intent(context, JobSeekerProfileDetailsActivity::class.java)
                                context.startActivity(intent)
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
                                    text = "References",
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
                                        painter = painterResource(R.drawable.companynameicon),
                                        contentDescription = "Reference",
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Professional References",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF263238)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Add your professional references",
                                        fontSize = 13.sp,
                                        color = Color(0xFF78909C)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (references.isEmpty()) {
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
                                            contentDescription = "no references",
                                            tint = Color(0xFF78909C),
                                            modifier = Modifier.size(70.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    "No References Added Yet",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF263238)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Add professional references to strengthen your profile",
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
                            items(references) { reference ->
                                ModernReferenceCard(
                                    reference = reference,
                                    onClick = {
                                        selectedReference = reference
                                        showDetailDialog = true
                                    },
                                    onEditClick = { openEditForm(reference) },
                                    onDeleteClick = { deleteReference(reference.referenceId) }
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
                        ModernAddButtons(
                            text = if (references.isEmpty()) "Add Reference" else "Add Another",
                            onClick = { openAddForm() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Reference Detail Dialog
    if (showDetailDialog && selectedReference != null) {
        ModernReferenceDetailDialog(
            reference = selectedReference!!,
            onDismiss = {
                showDetailDialog = false
                selectedReference = null
            },
            onEdit = {
                showDetailDialog = false
                openEditForm(selectedReference!!)
                selectedReference = null
            },
            onDelete = {
                showDetailDialog = false
                deleteReference(selectedReference!!.referenceId)
                selectedReference = null
            }
        )
    }

    // Reference Form Dialog
    if (showReferenceSheet) {
        ModernReferenceFormDialog(
            isEditing = isEditing,
            refereeName = refereeName,
            jobTitle = jobTitle,
            companyName = companyName,
            email = email,
            contactNumber = contactNumber,
            contactType = contactType,
            contactTypes = contactTypes,
            onRefereeNameChange = { refereeName = it },
            onJobTitleChange = { jobTitle = it },
            onCompanyNameChange = { companyName = it },
            onEmailChange = { email = it },
            onContactNumberChange = { contactNumber = it },
            onContactTypeChange = { contactType = it },
            onDismiss = {
                showReferenceSheet = false
                resetForm()
            },
            onSave = { saveReference() }
        )
    }
}

@Composable
fun ModernReferenceCard(
    reference: ReferenceModel,
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
                            painter = painterResource(R.drawable.companynameicon),
                            contentDescription = "Reference",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = reference.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF263238)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reference.jobTitle,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF78909C),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reference.companyName,
                        fontSize = 14.sp,
                        color = Color(0xFF263238),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reference.email,
                        fontSize = 13.sp,
                        color = Color(0xFF2196F3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ModernReferenceFormDialog(
    isEditing: Boolean,
    refereeName: String,
    jobTitle: String,
    companyName: String,
    email: String,
    contactNumber: String,
    contactType: String,
    contactTypes: List<String>,
    onRefereeNameChange: (String) -> Unit,
    onJobTitleChange: (String) -> Unit,
    onCompanyNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onContactNumberChange: (String) -> Unit,
    onContactTypeChange: (String) -> Unit,
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
                .fillMaxHeight(0.63f)
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
                    text = if (isEditing) "Edit Reference" else "Add Reference",
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
                    // Referee's Name
                    ModernReferenceTextField(
                        value = refereeName,
                        onValueChange = onRefereeNameChange,
                        label = "Referrer's Name *",
                        icon = R.drawable.companynameicon,
                        placeholder = "e.g., John Doe"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Job Title
                    ModernReferenceTextField(
                        value = jobTitle,
                        onValueChange = onJobTitleChange,
                        label = "Job Title *",
                        icon = R.drawable.jobtitleicon,
                        placeholder = "e.g., Senior Manager"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Company Name
                    ModernReferenceTextField(
                        value = companyName,
                        onValueChange = onCompanyNameChange,
                        label = "Company Name *",
                        icon = R.drawable.companynameicon,
                        placeholder = "e.g., ABC Corporation"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    ModernReferenceTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = "Email *",
                        icon = R.drawable.emailicon,
                        placeholder = "example@company.com"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact Type and Number Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Contact Type Dropdown
                        var expandedContact by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                        ) {
                            OutlinedTextField(
                                value = contactType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Type") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedContact = true },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                                        contentDescription = "Dropdown",
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.clickable { expandedContact = true }
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2196F3),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedLabelColor = Color(0xFF2196F3),
                                    unfocusedLabelColor = Color(0xFF78909C),
                                    disabledTextColor = Color(0xFF263238),
                                    disabledBorderColor = Color(0xFFE0E0E0),
                                    disabledContainerColor = Color.White,
                                    disabledLabelColor = Color(0xFF78909C)
                                ),
                                enabled = false
                            )

                            DropdownMenu(
                                expanded = expandedContact,
                                onDismissRequest = { expandedContact = false },
                                modifier = Modifier
                                    .background(Color.White)
                                    .width(120.dp)
                            ) {
                                contactTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                type,
                                                color = if (type == contactType) Color(0xFF2196F3) else Color(0xFF263238),
                                                fontWeight = if (type == contactType) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            onContactTypeChange(type)
                                            expandedContact = false
                                        },
                                        modifier = Modifier.background(
                                            if (type == contactType) Color(0xFFE3F2FD) else Color.White
                                        )
                                    )
                                }
                            }
                        }

                        // Contact Number
                        ModernReferenceTextField(
                            value = contactNumber,
                            onValueChange = onContactNumberChange,
                            label = "Contact Number *",
                            icon = R.drawable.emailicon,
                            placeholder = "123-456-7890",
                            modifier = Modifier.weight(2f)
                        )
                    }
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
fun ModernReferenceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: Int,
    placeholder: String = "",
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
        placeholder = { Text(placeholder, color = Color(0xFFBDBDBD)) },
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
fun ModernAddButtons(
    text: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2196F3)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.addexperience),
            contentDescription = "Add",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

@Composable
fun ModernReferenceDetailDialog(
    reference: ReferenceModel,
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
                text = "Reference Details",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238)
            )
        },
        text = {
            Column {
                DetailItem(title = "Referrer Name", value = reference.name)
                Spacer(modifier = Modifier.height(8.dp))
                DetailItem(title = "Job Title", value = reference.jobTitle)
                Spacer(modifier = Modifier.height(8.dp))
                DetailItem(title = "Company", value = reference.companyName)
                Spacer(modifier = Modifier.height(8.dp))
                DetailItem(title = "Email", value = reference.email)
                Spacer(modifier = Modifier.height(8.dp))
                DetailItem(title = "Contact Type", value = reference.contactType)
                Spacer(modifier = Modifier.height(8.dp))
                DetailItem(title = "Contact Number", value = reference.contactNumber)
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
fun ModernReferenceDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    referenceName: String
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
                text = "Delete Reference?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$referenceName\"? This action cannot be undone.",
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
fun JobSeekerReferencePreview() {
    JobSeekerReferenceBody()
}