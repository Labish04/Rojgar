package com.example.rojgar.view

import android.app.Activity
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
import androidx.compose.ui.graphics.Color
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
import com.example.rojgar.ui.theme.*
import com.example.rojgar.viewmodel.ExperienceViewModel
import com.google.firebase.auth.FirebaseAuth
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

    // Initialize ViewModel
    val experienceViewModel = remember { ExperienceViewModel(ExperienceRepoImpl()) }

    // Get current job seeker ID
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val jobSeekerId = currentUser?.uid ?: ""

    var experiences by remember { mutableStateOf(listOf<ExperienceModel>()) }
    var showSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var currentExperienceId by remember { mutableStateOf("") }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedExperience by remember { mutableStateOf<ExperienceModel?>(null) }

    // Form fields
    var companyName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var jobLevel by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var currentlyWorking by remember { mutableStateOf(false) }
    var experienceLetterUrl by remember { mutableStateOf("") }

    // Date pickers
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Image picker
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
        experienceLetterUrl = uri?.toString() ?: ""
    }

    // Dropdown states
    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var expandedLevel by remember { mutableStateOf(false) }

    var showDeleteAlert by remember { mutableStateOf(false) }
    var experienceToDelete by remember { mutableStateOf<String?>(null) }

    val jobCategories = listOf(
        "Accounting / Finance",
        "Architecture",
        "Banking",
        "Construction / Engineering",
        "Graphics / Designing",
        "IT (Information Technology)",
        "Computer Engineering",
        "Others"
    )

    val jobLevels = listOf("Top Level", "Senior Level", "Mid Level", "Entry Level")

    // Load experiences when activity starts
    LaunchedEffect(Unit) {
        if (jobSeekerId.isNotEmpty()) {
            experienceViewModel.getExperiencesByJobSeekerId(jobSeekerId) { success, message, expList ->
                if (success) {
                    expList?.let {
                        experiences = it
                    }
                } else {
                    Toast.makeText(context, "Failed to load experiences: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to reset form
    fun resetForm() {
        companyName = ""
        jobTitle = ""
        selectedCategory = ""
        jobLevel = ""
        startDate = ""
        endDate = ""
        currentlyWorking = false
        experienceLetterUrl = ""
        selectedImageUri = null
        currentExperienceId = ""
        isEditing = false
    }

    // Function to open form for adding new experience
    fun openAddForm() {
        resetForm()
        showSheet = true
    }

    // Function to open form for editing existing experience
    // Function to open form for editing existing experience
    fun openEditForm(experience: ExperienceModel) {
        companyName = experience.companyName
        jobTitle = experience.title
        selectedCategory = experience.jobCategory  // Add this line
        jobLevel = experience.level
        startDate = experience.startDate
        endDate = experience.endDate
        currentlyWorking = experience.currentlyWorkingStatus == "Yes"
        experienceLetterUrl = experience.experienceLetter
        currentExperienceId = experience.experienceId
        isEditing = true
        showSheet = true
    }

    // Function to save experience
    fun saveExperience() {
        if (companyName.isEmpty() || jobTitle.isEmpty() || startDate.isEmpty()) {
            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val experienceModel = ExperienceModel(
            experienceId = if (isEditing) currentExperienceId else "",
            companyName = companyName,
            title = jobTitle,
            jobCategory = selectedCategory,
            level = jobLevel,
            startDate = startDate,
            endDate = if (currentlyWorking) "" else endDate,
            currentlyWorkingStatus = if (currentlyWorking) "Yes" else "No",
            experienceLetter = experienceLetterUrl,
            jobSeekerId = jobSeekerId
        )

        if (isEditing) {
            experienceViewModel.updateExperience(currentExperienceId, experienceModel) { success, message ->
                if (success) {
                    Toast.makeText(context, "Experience updated", Toast.LENGTH_SHORT).show()
                    // Refresh list
                    experienceViewModel.getExperiencesByJobSeekerId(jobSeekerId) { success2, message2, expList ->
                        if (success2) {
                            expList?.let { experiences = it }
                        }
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
                    // Refresh list
                    experienceViewModel.getExperiencesByJobSeekerId(jobSeekerId) { success2, message2, expList ->
                        if (success2) {
                            expList?.let { experiences = it }
                        }
                    }
                    showSheet = false
                    resetForm()
                } else {
                    Toast.makeText(context, "Add failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun deleteExperience(experienceId: String) {
        // Set the experience to delete and show alert
        experienceToDelete = experienceId
        showDeleteAlert = true
    }

// Add this AlertDialog after the existing dialogs (after the detail dialog)
    if (showDeleteAlert) {
        AlertDialog(
            onDismissRequest = {
                showDeleteAlert = false
                experienceToDelete = null
            },
            title = {
                Text(
                    text = "Delete Experience",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this experience? This action cannot be undone.",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Button
                    TextButton(
                        onClick = {
                            showDeleteAlert = false
                            experienceToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Delete Button
                    Button(
                        onClick = {
                            experienceToDelete?.let { expId ->
                                // Perform actual deletion
                                experienceViewModel.deleteExperience(expId) { success, message ->
                                    if (success) {
                                        Toast.makeText(context, "Experience deleted", Toast.LENGTH_SHORT).show()
                                        // Refresh list
                                        experienceViewModel.getExperiencesByJobSeekerId(jobSeekerId) { success2, message2, expList ->
                                            if (success2) {
                                                expList?.let { experiences = it }
                                            }
                                        }
                                        // Close detail dialog if open
                                        showDetailDialog = false
                                    } else {
                                        Toast.makeText(context, "Delete failed: $message", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            showDeleteAlert = false
                            experienceToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Delete", color = Color.White)
                    }
                }
            }
        )
    }

    // Date pickers
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
            Card(
                modifier = Modifier
                    .height(140.dp)
                    .padding(top = 55.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(5.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBlue2),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = {
                        activity.finish()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(90.dp))

                    Text(
                        "Experience",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Blue)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "What are your most relevant experiences?",
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (experiences.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.noexperience),
                        contentDescription = "no experience",
                        tint = Color.Gray,
                        modifier = Modifier.size(110.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "You haven't added any experience yet.\nTap the Add button to add your work experience.",
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { openAddForm() },
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier
                                .width(170.dp)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkBlue2,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.addexperience),
                                contentDescription = "Add",
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Add", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(experiences) { experience ->
                        ExperienceCard(
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

                // Bottom Center Add Another Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { openAddForm() },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .width(170.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkBlue2,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.addexperience),
                            contentDescription = "Add",
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Add", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // Experience Detail Dialog
    // Experience Detail Dialog
    if (showDetailDialog && selectedExperience != null) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = {
                Text(
                    text = "Experience Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = DarkBlue2
                )
            },
            text = {
                Column {
                    selectedExperience?.let { exp ->
                        DetailItem(title = "Job Title", value = exp.title)
                        DetailItem(title = "Company", value = exp.companyName)
                        DetailItem(title = "Job Category", value = exp.jobCategory)
                        DetailItem(title = "Job Level", value = exp.level)
                        DetailItem(title = "Start Date", value = exp.startDate)
                        DetailItem(title = "End Date",
                            value = if (exp.currentlyWorkingStatus == "Yes") "Present" else exp.endDate)
                        DetailItem(title = "Currently Working", value = exp.currentlyWorkingStatus)
                        DetailItem(title = "Duration", value = exp.calculateYearsOfExperience())

                        if (exp.experienceLetter.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Experience Letter: Uploaded",
                                fontWeight = FontWeight.Medium,
                                color = Color.Green
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Experience Letter: Not Uploaded",
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            showDetailDialog = false
                            selectedExperience?.let { openEditForm(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue2),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            selectedExperience?.let {
                                // Show delete confirmation and close detail dialog
                                experienceToDelete = it.experienceId
                                showDeleteAlert = true
                                showDetailDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        )
    }
    // Add/Edit Experience Dialog
    if (showSheet) {
        Dialog(
            onDismissRequest = {
                showSheet = false
                resetForm()
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.72f),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Company Name
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Company Name *") },
                            leadingIcon = {
                                Icon(
                                    painterResource(id = R.drawable.companynameicon),
                                    contentDescription = null,
                                    tint = Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.Black,
                                focusedContainerColor = White,
                                unfocusedContainerColor = White
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Job Title
                        OutlinedTextField(
                            value = jobTitle,
                            onValueChange = { jobTitle = it },
                            label = { Text("Job Title *") },
                            leadingIcon = {
                                Icon(
                                    painterResource(id = R.drawable.jobtitleicon),
                                    contentDescription = null,
                                    tint = Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.Black,
                                focusedContainerColor = White,
                                unfocusedContainerColor = White
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Job Category
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.jobcategoryicon),
                                        contentDescription = "Category Icon",
                                        tint = Black,
                                        modifier = Modifier.size(27.dp)
                                    )
                                },
                                label = { Text("Select Job Category") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clickable { expandedCategory = true },
                                shape = RoundedCornerShape(15.dp),
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                                        contentDescription = "Dropdown Arrow",
                                        tint = Black,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { expandedCategory = true }
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    disabledIndicatorColor = Color.Black,
                                    disabledContainerColor = White,
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    focusedIndicatorColor = Purple,
                                    unfocusedIndicatorColor = Color.Black,
                                    disabledTextColor = Color.Black
                                )
                            )
                            DropdownMenu(
                                expanded = expandedCategory,
                                onDismissRequest = { expandedCategory = false },
                                modifier = Modifier
                                    .background(White)
                                    .fillMaxWidth()
                            ) {
                                jobCategories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category) },
                                        onClick = {
                                            selectedCategory = category
                                            expandedCategory = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Job Level
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = jobLevel,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.joblevelicon),
                                        contentDescription = "Level Icon",
                                        tint = Black,
                                        modifier = Modifier.size(27.dp)
                                    )
                                },
                                label = { Text("Job Level") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clickable { expandedLevel = true },
                                shape = RoundedCornerShape(15.dp),
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                                        contentDescription = "Dropdown",
                                        tint = Black,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { expandedLevel = true }
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    disabledIndicatorColor = Color.Black,
                                    disabledContainerColor = White,
                                    disabledTextColor = Color.Black,
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    focusedIndicatorColor = DarkBlue2,
                                    unfocusedIndicatorColor = Color.Black
                                )
                            )

                            DropdownMenu(
                                expanded = expandedLevel,
                                onDismissRequest = { expandedLevel = false },
                                modifier = Modifier
                                    .background(White)
                                    .fillMaxWidth()
                            ) {
                                jobLevels.forEach { level ->
                                    DropdownMenuItem(
                                        text = { Text(level) },
                                        onClick = {
                                            jobLevel = level
                                            expandedLevel = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Date Range
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Start Date
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Start Date *") },
                                label = { Text("Start Date *") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.calendaricon),
                                        contentDescription = "Open Calendar",
                                        tint = Black,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { showStartDatePicker = true }
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clickable { showStartDatePicker = true },
                                shape = RoundedCornerShape(15.dp),
                                colors = TextFieldDefaults.colors(
                                    disabledIndicatorColor = Color.Black,
                                    disabledContainerColor = White,
                                    disabledTextColor = Color.Black,
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    focusedIndicatorColor = DarkBlue2,
                                    unfocusedIndicatorColor = Color.Black
                                )
                            )

                            // End Date
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = {},
                                enabled = !currentlyWorking,
                                readOnly = true,
                                placeholder = { Text("End Date") },
                                label = { Text("End Date") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.calendaricon),
                                        contentDescription = "Open Calendar",
                                        tint = Black,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                if (!currentlyWorking) showEndDatePicker = true
                                            }
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clickable {
                                        if (!currentlyWorking) showEndDatePicker = true
                                    },
                                shape = RoundedCornerShape(15.dp),
                                colors = TextFieldDefaults.colors(
                                    disabledIndicatorColor = Color.Black,
                                    disabledContainerColor = White,
                                    disabledTextColor = Color.Black,
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    focusedIndicatorColor = DarkBlue2,
                                    unfocusedIndicatorColor = Color.Black
                                )
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Currently Working Switch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = currentlyWorking,
                                onCheckedChange = {
                                    currentlyWorking = it
                                    if (it) endDate = ""
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Blue,
                                    checkedTrackColor = DarkBlue2,
                                    uncheckedThumbColor = Color.DarkGray,
                                    uncheckedTrackColor = Color.DarkGray.copy(alpha = 0.4f)
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Currently work here?")
                        }

                        Spacer(Modifier.height(12.dp))

                        // Experience Letter Upload
                        OutlinedTextField(
                            value = selectedImageUri?.lastPathSegment ?: "No file chosen",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Upload Experience Letter") },
                            trailingIcon = {
                                Icon(
                                    painterResource(id = R.drawable.baseline_upload_24),
                                    contentDescription = null,
                                    tint = Black,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { launcher.launch("image/*") }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = TextFieldDefaults.colors(
                                disabledIndicatorColor = Color.Black,
                                disabledContainerColor = White,
                                disabledTextColor = Color.Black,
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.Black
                            )
                        )

                        Spacer(Modifier.height(32.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Cancel Button
                            OutlinedButton(
                                onClick = {
                                    showSheet = false
                                    resetForm()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(0.3f)
                                    .height(50.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = DarkBlue2
                                )
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_arrow_back_ios_24),
                                    contentDescription = "Back",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))

                            // Save Button
                            Button(
                                onClick = { saveExperience() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(0.7f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkBlue2,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = if (isEditing) "Update Experience" else "Save Experience",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
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
fun ExperienceCard(
    experience: ExperienceModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically

            ) {
                // Job Title
                Text(
                    text = experience.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkBlue2
                )

                // Edit and Delete buttons on the right side
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ){
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Delete Icon
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(6.dp))

            // Company Name
            Text(
                text = experience.companyName,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Duration (instead of date range)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Duration badge
                Box(
                    modifier = Modifier
                        .background(DarkBlue2, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = experience.calculateYearsOfExperience(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }


            }
        }
    }
}

@Composable
fun DetailItem(title: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview
@Composable
fun JobSeekerExperiencePreview() {
    JobSeekerExperienceBody()
}