package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.rojgar.model.EducationModel
import com.example.rojgar.repository.EducationRepoImpl
import com.example.rojgar.ui.theme.*
import com.example.rojgar.viewmodel.EducationViewModel
import com.google.firebase.auth.FirebaseAuth
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

    var selectedDegree by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var board by remember { mutableStateOf("") }
    var fieldOfStudy by remember { mutableStateOf("") }
    var startYear by remember { mutableStateOf("") }
    var endYear by remember { mutableStateOf("") }
    var gradeType by remember { mutableStateOf("CGPA") } // CGPA or Marks
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
        if (jobSeekerId.isNotEmpty()) {
            educationViewModel.getEducationsByJobSeekerId(jobSeekerId) { success, message, educationList ->
                if (success) {
                    educationList?.let {
                        educations = it
                    }
                } else {
                    Toast.makeText(context, "Failed to load educations: $message", Toast.LENGTH_SHORT).show()
                }
            }
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

    // Function to  editing existing education
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
                    // Refresh list
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
                    // Refresh list
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
        AlertDialog(
            onDismissRequest = {
                showDeleteAlert = false
                educationToDelete = null
            },
            title = {
                Text(
                    text = "Delete Education",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this education? ",
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
                            educationToDelete = null
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
                    Spacer(modifier = Modifier.width(110.dp))
                    Text(
                        "Education",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
                "What academic achievements are you most proud of?",
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (educations.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.educationicon),
                        contentDescription = "no education",
                        tint = Color.Gray,
                        modifier = Modifier.size(110.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Your Education Section is currently empty. Tap the + button to add your education details.",
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
                    items(educations) { education ->
                        EducationCard(
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

    // Education Detail Dialog
    if (showDetailDialog && selectedEducation != null) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = {
                Text(
                    text = "Education Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = DarkBlue2
                )
            },
            text = {
                Column {
                    selectedEducation?.let { edu ->
                        DetailsItem(title = "Degree", value = edu.educationDegree)
                        DetailsItem(title = "Institution", value = edu.instituteName)
                        DetailsItem(title = "Field of Study", value = edu.field)
                        DetailsItem(title = "Board", value = edu.board)
                        DetailsItem(title = "Start Year", value = edu.startYear)
                        DetailsItem(title = "End Year", value = edu.endYear)
                        DetailsItem(title = "Grade Type", value = edu.gradeType)
                        DetailsItem(title = "Score", value = edu.score)
                        DetailsItem(title = "Currently Studying", value = if (edu.currentlyStudying) "Yes" else "No")
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
                            selectedEducation?.let { openEditForm(it) }
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
                            selectedEducation?.let {
                                educationToDelete = it.educationId
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

    // Degree Selection Dialog
    if (showDegreeSheet) {
        Dialog(
            onDismissRequest = { showDegreeSheet = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.55f),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Education Degree",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(16.dp))

                        degreeOptions.forEach { degree ->
                            OutlinedButton(
                                onClick = {
                                    selectedDegree = degree
                                    showDegreeSheet = false
                                    showDetailSheet = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                            ) {
                                Text(text = degree, textAlign = TextAlign.Start)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            OutlinedButton(
                                onClick = { showDegreeSheet = false },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(44.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBlue2)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_arrow_back_ios_24),
                                    contentDescription = "Back",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Education Detail Form Dialog
    if (showDetailSheet) {
        Dialog(
            onDismissRequest = {
                showDetailSheet = false
                resetForm()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
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
                        Text(
                            text = if (isEditing) "Edit Education" else "Add Education",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(16.dp))

                        // Selected Degree
                        OutlinedTextField(
                            value = selectedDegree,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Education Degree") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.educationicon),
                                    contentDescription = "Degree",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                disabledIndicatorColor = Color.Transparent,
                                disabledContainerColor = White,
                                disabledTextColor = Color.Black,
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Institution Name
                        OutlinedTextField(
                            value = institution,
                            onValueChange = { institution = it },
                            label = { Text("Name of Institution *") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.educationicon),
                                    contentDescription = "Start Year",
                                    modifier = Modifier.size(20.dp)

                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                disabledIndicatorColor = Color.Transparent,
                                disabledContainerColor = White,
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Board
                        OutlinedTextField(
                            value = board,
                            onValueChange = { board = it },
                            label = { Text("Education Board") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.educationboardicon),
                                    contentDescription = "Start Year",
                                    modifier = Modifier.size(20.dp)

                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                disabledIndicatorColor = Color.Transparent,
                                disabledContainerColor = White,
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Field of Study
                        OutlinedTextField(
                            value = fieldOfStudy,
                            onValueChange = { fieldOfStudy = it },
                            label = { Text("Field of Study") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.studyfieldicon),
                                    contentDescription = "Start Year",
                                    modifier = Modifier.size(20.dp)

                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                disabledIndicatorColor = Color.Transparent,
                                disabledContainerColor = White,
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(12.dp))


                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Start Year Field
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = startYear,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.calendaricon),
                                            contentDescription = "Start Year",
                                            modifier = Modifier.size(20.dp)

                                        )
                                    },
                                    label = { Text("Start Year *") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .clickable { showStartYearPicker = true },
                                    shape = RoundedCornerShape(15.dp),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        disabledIndicatorColor = Color.LightGray,
                                        disabledContainerColor = White,
                                        disabledLeadingIconColor = Black,
                                        disabledTextColor = Color.Black,
                                        disabledLabelColor = Black,
                                        focusedContainerColor = White,
                                        unfocusedContainerColor = White,
                                        focusedIndicatorColor = DarkBlue2,
                                        unfocusedIndicatorColor = Color.LightGray
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // End Year Field
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = if (currentlyStudying) "Present" else endYear,
                                    onValueChange = {},
                                    enabled = false,
                                    readOnly = true,
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.calendaricon),
                                            contentDescription = "End Year",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = { Text("End Year") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .clickable {
                                            if (!currentlyStudying) showEndYearPicker = true
                                        },
                                    shape = RoundedCornerShape(15.dp),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        disabledIndicatorColor = Color.LightGray,
                                        disabledContainerColor = White,
                                        disabledLabelColor = Black,
                                        disabledLeadingIconColor = Black,
                                        disabledTextColor = if (currentlyStudying) Color.Gray else Color.Black,
                                        focusedContainerColor = White,
                                        unfocusedContainerColor = White,
                                        focusedIndicatorColor = DarkBlue2,
                                        unfocusedIndicatorColor = Color.LightGray
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Currently Studying Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Switch(
                                checked = currentlyStudying,
                                onCheckedChange = {
                                    currentlyStudying = it
                                    if (it) {
                                        endYear = ""
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Blue,
                                    checkedTrackColor = DarkBlue2
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Currently studying?")
                        }

                        Spacer(Modifier.height(12.dp))

                        // Grade Type Selection
                        Text(
                            text = "Grade Type",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { gradeType = "CGPA" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = if (gradeType == "CGPA")
                                    ButtonDefaults.buttonColors(
                                        containerColor = DarkBlue2,
                                        contentColor = Color.White
                                    )
                                else
                                    ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                            ) {
                                Text("CGPA")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedButton(
                                onClick = { gradeType = "Marks" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = if (gradeType == "Marks")
                                    ButtonDefaults.buttonColors(
                                        containerColor = DarkBlue2,
                                        contentColor = Color.White
                                    )
                                else
                                    ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                            ) {
                                Text("Percentage")
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Score Input
                        OutlinedTextField(
                            value = score,
                            onValueChange = { score = it },
                            label = { Text(if (gradeType == "CGPA") "CGPA" else "Percentage") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                disabledIndicatorColor = Color.Transparent,
                                disabledContainerColor = White,
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Back Button
                            OutlinedButton(
                                onClick = {
                                    showDetailSheet = false
                                    resetForm()
                                },
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier
                                    .weight(0.4f)
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

                            Spacer(modifier = Modifier.width(12.dp))

                            // Save Button
                            Button(
                                onClick = { saveEducation() },
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier
                                    .weight(0.6f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkBlue2,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = if (isEditing) "Update" else "Save",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Year Pickers
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    if (showStartYearPicker) {
        YearPickerDialog(
            onDismiss = { showStartYearPicker = false },
            onYearSelected = { year ->
                startYear = year.toString()
                showStartYearPicker = false
            },
            initialYear = startYear.toIntOrNull() ?: currentYear
        )
    }

    if (showEndYearPicker) {
        YearPickerDialog(
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
fun EducationCard(
    education: EducationModel,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = education.educationDegree,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(78.dp))
                        Row {
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color.Black,
                                    modifier = Modifier.size(26.dp)
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
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Institution Name
                    Text(
                        text = education.instituteName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = education.field,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }


            }
        }
    }
}

@Composable
fun YearPickerDialog(
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.6f),
                colors = CardDefaults.cardColors(containerColor = White)
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
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            years.forEach { year ->
                                OutlinedButton(
                                    onClick = { onYearSelected(year) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = if (year == initialYear)
                                        ButtonDefaults.buttonColors(
                                            containerColor = DarkBlue2,
                                            contentColor = Color.White
                                        )
                                    else
                                        ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                                ) {
                                    Text(text = year.toString(), fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBlue2)
                    ) {
                        Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsItem(title: String, value: String) {
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
fun JobSeekerEducationPreview() {
    JobSeekerEducationBody()
}