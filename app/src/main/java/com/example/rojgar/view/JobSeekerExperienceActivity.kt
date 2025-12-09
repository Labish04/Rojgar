package com.example.rojgar.view

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.rojgar.ui.theme.*
import java.util.*

data class Experience(
    val company: String,
    val role: String,
    val startDate: String,
    val endDate: String,
    val currentlyWorking: Boolean,
    val description: String
)

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

    var experiences by remember { mutableStateOf(listOf<Experience>()) }
    var showSheet by remember { mutableStateOf(false) }

    var companyName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var currentlyWorking by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

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

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }

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
    var expandedLevel by remember { mutableStateOf(false) }
    var selectedLevel by remember { mutableStateOf("") }

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
                        val intent = Intent(context, JobSeekerProfileDetailsActivity::class.java)
                        context.startActivity(intent)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "What are your most relevant experiences?",
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(200.dp))

            if (experiences.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(520.dp),
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
                        "You haven't added any experience yet.\nTap the + button to add your work experience.",
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
                            onClick = {
                                companyName = ""
                                jobTitle = ""
                                startDate = ""
                                endDate = ""
                                currentlyWorking = false
                                description = ""
                                selectedCategory = ""
                                selectedLevel = ""
                                selectedImageUri = null
                                showSheet = true
                            },
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
                            Text(
                                text = "Add",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    experiences.forEach { exp ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = exp.role,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = exp.company, color = Color.Gray)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (exp.currentlyWorking) "${exp.startDate} - Present" else "${exp.startDate} - ${exp.endDate}",
                                    color = Color.DarkGray
                                )
                                if (exp.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = exp.description)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(300.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                companyName = ""
                                jobTitle = ""
                                startDate = ""
                                endDate = ""
                                currentlyWorking = false
                                description = ""
                                selectedCategory = ""
                                selectedLevel = ""
                                selectedImageUri = null
                                showSheet = true
                            },
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier
                                .width(150.dp)
                                .height(45.dp),
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Add Another")
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        Dialog(
            onDismissRequest = {
                showSheet = false
                companyName = ""
                jobTitle = ""
                startDate = ""
                endDate = ""
                currentlyWorking = false
                description = ""
                selectedCategory = ""
                selectedLevel = ""
                selectedImageUri = null
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
                        .fillMaxHeight(0.75f),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Company Name") },
                            leadingIcon = {
                                Icon(
                                    painterResource(id = R.drawable.companynameicon),
                                    contentDescription = null,
                                    tint = Color.Blue,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                disabledIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.Black,
                                focusedContainerColor =White,
                                unfocusedContainerColor = White
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = jobTitle,
                            onValueChange = { jobTitle = it },
                            label = { Text("Job Title") },
                            leadingIcon = {
                                Icon(
                                    painterResource(id = R.drawable.jobtitleicon),
                                    contentDescription = null,
                                    tint = Color.Blue,
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
                                        tint = Color.Blue,
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
                                        tint = Color.Blue,
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
                                    .background(Blue)
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

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedLevel,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.joblevelicon),
                                        contentDescription = "Level Icon",
                                        tint = Color.Blue,
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
                                        tint = Color.Blue,
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
                                            selectedLevel = level
                                            expandedLevel = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("dd/mm/yyyy") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.calendaricon),
                                        contentDescription = "Open Calendar",
                                        tint = Color.Blue,
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

                            OutlinedTextField(
                                value = endDate,
                                onValueChange = {},
                                enabled = !currentlyWorking,
                                readOnly = true,
                                placeholder = { Text("dd/mm/yyyy") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.calendaricon),
                                        contentDescription = "Open Calendar",
                                        tint = Color.Blue,
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

                        OutlinedTextField(
                            value = selectedImageUri?.lastPathSegment ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Upload Experience Letter") },
                            trailingIcon = {
                                Icon(
                                    painterResource(id = R.drawable.baseline_upload_24),
                                    contentDescription = null,
                                    tint = Color.Blue,
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showSheet = false
                                    companyName = ""
                                    jobTitle = ""
                                    startDate = ""
                                    endDate = ""
                                    currentlyWorking = false
                                    description = ""
                                    selectedCategory = ""
                                    selectedLevel = ""
                                    selectedImageUri = null
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

                            Button(
                                onClick = {
                                    if (companyName.isNotBlank() && jobTitle.isNotBlank()) {
                                        val newExperience = Experience(
                                            company = companyName,
                                            role = jobTitle,
                                            startDate = startDate,
                                            endDate = if (currentlyWorking) "Present" else endDate,
                                            currentlyWorking = currentlyWorking,
                                            description = description
                                        )
                                        experiences = experiences + newExperience
                                        showSheet = false
                                        companyName = ""
                                        jobTitle = ""
                                        startDate = ""
                                        endDate = ""
                                        currentlyWorking = false
                                        description = ""
                                        selectedCategory = ""
                                        selectedLevel = ""
                                        selectedImageUri = null
                                    }
                                },
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
                                    text = "Save Changes",
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

@Preview
@Composable
fun JobSeekerExperiencePreview() {
    JobSeekerExperienceBody()
}