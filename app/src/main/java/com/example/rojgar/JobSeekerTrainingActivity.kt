package com.example.rojgar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White
import java.util.Calendar

data class Training(
    val trainingName: String,
    val institutionName: String,
    val duration: String,
    val durationType: String,
    val completionDate: String,
    val certificateUri: Uri? = null
)

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

    val snackbarHostState = remember { SnackbarHostState() }
    var showBottomSheet by remember { mutableStateOf(false) }
    val trainingList = remember { mutableStateListOf<Training>() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Which certification or training achievement stands out for you?",
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (trainingList.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    trainingList.forEach { training ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = training.trainingName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(text = training.institutionName, color = Color.DarkGray)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Duration: ${training.duration} ${training.durationType}",
                                    color = Color.Gray
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Completed: ${training.completionDate}",
                                    color = Color.Gray
                                )
                                if (training.certificateUri != null) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Certificate uploaded",
                                        color = DarkBlue2,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(200.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.noexperience),
                        contentDescription = "no education",
                        tint = Color.Gray,
                        modifier = Modifier.size(110.dp)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        "You haven't added any training details. Tap + to get started.",
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { showBottomSheet = true },
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

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = White
            ) {
                AddTrainingSheet(
                    onDismiss = { showBottomSheet = false },
                    onSave = { training ->
                        trainingList.add(training)
                        showBottomSheet = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTrainingSheet(
    onDismiss: () -> Unit,
    onSave: (Training) -> Unit
) {
    var trainingName by remember { mutableStateOf("") }
    var institutionName by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var selectedDurationType by remember { mutableStateOf("Month") }
    var completionDate by remember { mutableStateOf("") }
    var expandedDuration by remember { mutableStateOf(false) }
    var certificateUri by remember { mutableStateOf<Uri?>(null) }
    var certificateName by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val durationTypes = listOf("Month", "Year", "Week", "Day")

    // Date picker state
    val datePickerState = rememberDatePickerState()
    val confirmEnabled = remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            certificateUri = it
            certificateName = "Certificate uploaded"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Name of Training
        OutlinedTextField(
            value = trainingName,
            onValueChange = { trainingName = it },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.document),
                    contentDescription = "Training",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Name of Training") },
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
                focusedIndicatorColor = Purple,
                unfocusedIndicatorColor = Color.Black
            )
        )


        Spacer(modifier = Modifier.height(16.dp))

        // Institution Name
        OutlinedTextField(
            value = institutionName,
            onValueChange = { institutionName = it },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.companynameicon),
                    contentDescription = "Institution",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Institution Name") },
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
                focusedIndicatorColor = Purple,
                unfocusedIndicatorColor = Color.Black
            )
        )


        Spacer(modifier = Modifier.height(16.dp))

        // Duration and Month (Dropdown)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Duration TextField
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.durationicon),
                        contentDescription = "Duration",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Duration") },
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = White,
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )


            // Duration Type Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedDuration,
                onExpandedChange = { expandedDuration = !expandedDuration },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedDurationType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Month") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDuration)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = White,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = DarkBlue2,
                        unfocusedIndicatorColor = Color.LightGray
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedDuration,
                    onDismissRequest = { expandedDuration = false },
                    modifier = Modifier.background(White)
                ) {
                    durationTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type, color = Color.DarkGray) },
                            onClick = {
                                selectedDurationType = type
                                expandedDuration = false
                            },
                            colors = androidx.compose.material3.MenuDefaults.itemColors(
                                textColor = Color.DarkGray
                            ),
                            modifier = Modifier.background(
                                if (type == selectedDurationType) Color(0xFFE3F2FD) else White
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Completion Date
        OutlinedTextField(
            value = completionDate,
            onValueChange = {},
            label = { Text("Completion Date") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    showDatePicker = true
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.calendaricon),
                        contentDescription = "Select Date",
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                disabledIndicatorColor = Color.Transparent,
                disabledContainerColor = White,
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedIndicatorColor = DarkBlue2,
                unfocusedIndicatorColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Upload Certificate
        OutlinedTextField(
            value = certificateName,
            onValueChange = {},
            label = { Text("Upload Certificate") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    galleryLauncher.launch("image/*")
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_upload_24),
                        contentDescription = "Upload Certificate",
                        modifier = Modifier.size(22.dp),
                        tint = Black
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                disabledIndicatorColor = Color.Transparent,
                disabledContainerColor = White,
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedIndicatorColor = DarkBlue2,
                unfocusedIndicatorColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.Black
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
                onClick = {
                    if (trainingName.isNotEmpty() && institutionName.isNotEmpty() &&
                        duration.isNotEmpty() && completionDate.isNotEmpty()
                    ) {
                        onSave(
                            Training(
                                trainingName = trainingName,
                                institutionName = institutionName,
                                duration = duration,
                                durationType = selectedDurationType,
                                completionDate = completionDate,
                                certificateUri = certificateUri
                            )
                        )
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
                    text = "Save",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = millis
                            }
                            completionDate = "${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
                        }
                        showDatePicker = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK", color = DarkBlue2)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = DarkBlue2)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = White
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = White,
                    titleContentColor = DarkBlue2,
                    headlineContentColor = DarkBlue2,
                    weekdayContentColor = DarkBlue2,
                    subheadContentColor = DarkBlue2,
                    yearContentColor = Color.DarkGray,
                    currentYearContentColor = DarkBlue2,
                    selectedYearContentColor = White,
                    selectedYearContainerColor = DarkBlue2,
                    dayContentColor = Color.DarkGray,
                    selectedDayContentColor = White,
                    selectedDayContainerColor = DarkBlue2,
                    todayContentColor = DarkBlue2,
                    todayDateBorderColor = DarkBlue2
                )
            )
        }
    }
}

@Preview
@Composable
fun JobSeekerTrainingPreview() {
    JobSeekerTrainingBody()
}