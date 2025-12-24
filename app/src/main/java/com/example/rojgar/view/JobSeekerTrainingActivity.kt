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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import com.example.rojgar.model.TrainingModel
import com.example.rojgar.repository.TrainingRepoImpl
import com.example.rojgar.ui.theme.*
import com.example.rojgar.viewmodel.TrainingViewModel
import com.google.firebase.auth.FirebaseAuth
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

    var trainingName by remember { mutableStateOf("") }
    var instituteName by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var durationType by remember { mutableStateOf("Month") }
    var completionDate by remember { mutableStateOf("") }
    var certificateUri by remember { mutableStateOf<Uri?>(null) }
    var certificateName by remember { mutableStateOf("") }

    var currentTrainingId by remember { mutableStateOf("") }
    var showDeleteAlert by remember { mutableStateOf(false) }
    var trainingToDelete by remember { mutableStateOf<String?>(null) }

    val durationTypes = listOf("Month", "Year", "Week", "Day")

    // Completion Date (similar to birthdate selection)
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            completionDate = "$d/${m + 1}/$y"
        },
        year,
        month,
        day
    )

    // Gallery launcher for certificate
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            certificateUri = it
            certificateName = "Certificate uploaded"
        }
    }


    // Load trainings
    LaunchedEffect(Unit) {
        if (jobSeekerId.isNotEmpty()) {
            trainingViewModel.getTrainingsByJobSeekerId(jobSeekerId) { success, message, trainingList ->
                if (success) {
                    trainingList?.let {
                        trainings = it
                    }
                } else {
                    Toast.makeText(context, "Failed to load trainings: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to reset form
    fun resetForm() {
        trainingName = ""
        instituteName = ""
        duration = ""
        durationType = "Month"
        completionDate = ""
        certificateUri = null
        certificateName = ""
        currentTrainingId = ""
        isEditing = false
    }

    // Function to adding new training
    fun openAddForm() {
        resetForm()
        showTrainingSheet = true
    }

    // Function to editing existing training
    fun openEditForm(training: TrainingModel) {
        trainingName = training.trainingName
        instituteName = training.instituteName
        duration = training.duration
        durationType = training.durationType
        completionDate = training.completionDate
        certificateName = if (training.certificate.isNotEmpty()) "Certificate uploaded" else ""
        currentTrainingId = training.trainingId
        isEditing = true
        showTrainingSheet = true
    }

    // Function to save training
    fun saveTraining() {
        if (trainingName.isEmpty() || instituteName.isEmpty() || duration.isEmpty() || completionDate.isEmpty()) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val trainingModel = TrainingModel(
            trainingId = if (isEditing) currentTrainingId else "",
            trainingName = trainingName,
            instituteName = instituteName,
            duration = duration,
            durationType = durationType,
            completionDate = completionDate,
            certificate = certificateUri?.toString() ?: "",
            jobSeekerId = jobSeekerId
        )

        if (isEditing) {
            trainingViewModel.updateTraining(currentTrainingId, trainingModel) { success, message ->
                if (success) {
                    Toast.makeText(context, "Training updated", Toast.LENGTH_SHORT).show()
                    // Refresh list
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
                    // Refresh list
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

    fun deleteTraining(trainingId: String) {
        trainingToDelete = trainingId
        showDeleteAlert = true
    }

    // Delete Dialog
    if (showDeleteAlert) {
        AlertDialog(
            onDismissRequest = {
                showDeleteAlert = false
                trainingToDelete = null
            },
            title = {
                Text(
                    text = "Delete Training",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this training? ",
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
                            trainingToDelete = null
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
                        "Training",
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
                "Which certification or training achievement stands out for you?",
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (trainings.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.noexperience),
                        contentDescription = "no training",
                        tint = Color.Gray,
                        modifier = Modifier.size(110.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Your Training Section is currently empty. Tap the + button to add your training details.",
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
                    items(trainings) { training ->
                        TrainingCard(
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

    // Training Detail Dialog
    if (showDetailDialog && selectedTraining != null) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = {
                Text(
                    text = "Training Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = DarkBlue2
                )
            },
            text = {
                Column {
                    selectedTraining?.let { training ->
                        Detail(title = "Training Name", value = training.trainingName)
                        Detail(title = "Institution", value = training.instituteName)
                        Detail(title = "Duration", value = "${training.duration} ${training.durationType}")
                        Detail(title = "Completion Date", value = training.completionDate)
                        if (training.certificate.isNotEmpty()) {
                            Detail(title = "Certificate", value = "Uploaded")
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
                            selectedTraining?.let { openEditForm(it) }
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
                            selectedTraining?.let {
                                trainingToDelete = it.trainingId
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

    // Training Form Dialog
    if (showTrainingSheet) {
        Dialog(
            onDismissRequest = {
                showTrainingSheet = false
                resetForm()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .clickable{showTrainingSheet = false
                            resetForm()},
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.62f),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (isEditing) "Edit Training" else "Add Training",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(16.dp))

                        // Training Name
                        OutlinedTextField(
                            value = trainingName,
                            onValueChange = { trainingName = it },
                            label = { Text("Name of Training *") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.document),
                                    contentDescription = "Training",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Institution Name
                        OutlinedTextField(
                            value = instituteName,
                            onValueChange = { instituteName = it },
                            label = { Text("Institution Name *") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.companynameicon),
                                    contentDescription = "Institution",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Duration and Type
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Duration TextField
                            OutlinedTextField(
                                value = duration,
                                onValueChange = { duration = it },
                                label = { Text("Duration *") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.durationicon),
                                        contentDescription = "Duration",
                                        tint = Color.Black,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                shape = RoundedCornerShape(15.dp),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    focusedIndicatorColor = DarkBlue2,
                                    unfocusedIndicatorColor = Color.LightGray
                                )
                            )

                            // Duration Type Dropdown
                            var expandedDuration by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                            ) {
                                // TextField that acts as the dropdown trigger
                                OutlinedTextField(
                                    value = durationType,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Type") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedDuration = true },
                                    shape = RoundedCornerShape(15.dp),
                                    singleLine = true,
                                    trailingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                                            contentDescription = "Dropdown",
                                            modifier = Modifier.clickable { expandedDuration = true }
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        disabledIndicatorColor = Color.LightGray,
                                        disabledContainerColor = White,
                                        disabledTextColor = Color.Black,
                                        focusedContainerColor = White,
                                        unfocusedContainerColor = White,
                                        focusedIndicatorColor = DarkBlue2,
                                        unfocusedIndicatorColor = Color.LightGray
                                    )
                                )

                                // Dropdown menu anchored to the Box
                                DropdownMenu(
                                    expanded = expandedDuration,
                                    onDismissRequest = { expandedDuration = false },
                                    modifier = Modifier
                                        .background(White)
                                        .width(100.dp)
                                ) {
                                    durationTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    type,
                                                    color = if (type == durationType) DarkBlue2 else Color.Black
                                                )
                                            },
                                            onClick = {
                                                durationType = type
                                                expandedDuration = false
                                            },
                                            modifier = Modifier.background(
                                                if (type == durationType) Color(0xFFE3F2FD) else White
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = completionDate,
                            onValueChange = {},
                            label = { Text("Completion Date *") },
                            readOnly = true,
                            enabled = false,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.calendaricon),
                                    contentDescription = "Calendar",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable { datePickerDialog.show() },
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

                        Spacer(Modifier.height(12.dp))

                        // Upload Certificate
                        OutlinedTextField(
                            value = certificateName,
                            onValueChange = {},
                            label = { Text("Upload Certificate") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable{galleryLauncher.launch("image/*")},
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            trailingIcon = {

                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_upload_24),
                                        contentDescription = "Upload Certificate",
                                        modifier = Modifier.size(22.dp),
                                        tint = Black
                                    )

                            },
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

                        Spacer(Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Back Button
                            OutlinedButton(
                                onClick = {
                                    showTrainingSheet = false
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
                                onClick = { saveTraining() },
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
}

@Composable
fun TrainingCard(
    training: TrainingModel,
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = training.trainingName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(18.dp))
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
                        text = training.instituteName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Duration: ${training.duration} ${training.durationType}",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun Detail(title: String, value: String) {
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
fun JobSeekerTrainingPreview() {
    JobSeekerTrainingBody()
}