package com.example.rojgar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.RojgarTheme
import com.example.rojgar.ui.theme.White
import kotlinx.coroutines.launch

data class Education(
    val degreeType: String,
    val institutionName: String,
    val board: String,
    val fieldOfStudy: String,
    val startYear: Int,
    val endYear: Int?,
    val gradeType: String,
    val marksSecured: String,
    val currentlyStudying: Boolean,
    val certificates: List<Uri> = emptyList()
)

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

    var educationList by remember { mutableStateOf(listOf<Education>()) }
    var showDegreeSheet by remember { mutableStateOf(false) }
    var showDetailSheet by remember { mutableStateOf(false) }
    var selectedDegree by remember { mutableStateOf("") }

    var institution by remember { mutableStateOf(TextFieldValue("")) }
    var board by remember { mutableStateOf(TextFieldValue("")) }
    var fieldOfStudy by remember { mutableStateOf(TextFieldValue("")) }
    var startYear by remember { mutableStateOf<Int?>(null) }
    var endYear by remember { mutableStateOf<Int?>(null) }
    var gradeTypeIsCGPA by remember { mutableStateOf(true) }
    var marksSecured by remember { mutableStateOf(TextFieldValue("")) }
    var currentlyStudying by remember { mutableStateOf(false) }
    var selectedCertificates by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var showStartYearPicker by remember { mutableStateOf(false) }
    var showEndYearPicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // File picker 
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedCertificates = selectedCertificates + uris
    }

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

    val resetForm = {
        institution = TextFieldValue("")
        board = TextFieldValue("")
        fieldOfStudy = TextFieldValue("")
        startYear = null
        endYear = null
        gradeTypeIsCGPA = true
        marksSecured = TextFieldValue("")
        currentlyStudying = false
        selectedCertificates = emptyList()
    }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "What academic achievements are you most proud of?",
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (educationList.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    educationList.forEach { edu ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = edu.degreeType, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(text = edu.institutionName, color = Color.DarkGray)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${edu.startYear} - ${if (edu.currentlyStudying) "Present" else edu.endYear}",
                                    color = Color.Gray
                                )
                                if (edu.certificates.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "${edu.certificates.size} certificate(s) uploaded",
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
                        painter = painterResource(id = R.drawable.educationicon),
                        contentDescription = "no education",
                        tint = Color.Gray,
                        modifier = Modifier.size(110.dp)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        "Your Education Section is currently empty. Tap the + button to add your education details.",
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
                    onClick = { showDegreeSheet = true },
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
                                    resetForm()
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

    if (showDetailSheet) {
        Dialog(
            onDismissRequest = { showDetailSheet = false },
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
                        .fillMaxHeight(0.85f),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = selectedDegree.ifBlank { "Education Details" },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = institution,
                            onValueChange = { institution = it },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.educationicon),
                                    contentDescription = "institution",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Name of Institution") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(12.dp),
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

                        OutlinedTextField(
                            value = board,
                            onValueChange = { board = it },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.educationboardicon),
                                    contentDescription = "board",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Education Board") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(12.dp),
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

                        OutlinedTextField(
                            value = fieldOfStudy,
                            onValueChange = { fieldOfStudy = it },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.studyfieldicon),
                                    contentDescription = "field",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Enter field of study") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(12.dp),
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

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = startYear?.toString() ?: "",
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.calendaricon),
                                        contentDescription = "startYear",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = { Text("Start Year") },
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    disabledIndicatorColor = Color.Transparent,
                                    disabledContainerColor = White,
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    focusedIndicatorColor = DarkBlue2,
                                    unfocusedIndicatorColor = Color.LightGray
                                ),
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                    .also { interactionSource ->
                                        LaunchedEffect(interactionSource) {
                                            interactionSource.interactions.collect {
                                                if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                                    showStartYearPicker = true
                                                }
                                            }
                                        }
                                    }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedTextField(
                                value = if (currentlyStudying) "Present" else (endYear?.toString() ?: ""),
                                onValueChange = {},
                                readOnly = true,
                                enabled = !currentlyStudying,
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.calendaricon),
                                        contentDescription = "endYear",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = { Text("End Year") },
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    disabledIndicatorColor = Color.Transparent,
                                    disabledContainerColor = White,
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    focusedIndicatorColor = DarkBlue2,
                                    unfocusedIndicatorColor = Color.LightGray
                                ),
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                    .also { interactionSource ->
                                        LaunchedEffect(interactionSource) {
                                            interactionSource.interactions.collect {
                                                if (it is androidx.compose.foundation.interaction.PressInteraction.Release && !currentlyStudying) {
                                                    showEndYearPicker = true
                                                }
                                            }
                                        }
                                    }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(text = "Grade Type", fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Button(
                                onClick = { gradeTypeIsCGPA = true },
                                modifier = Modifier
                                    .height(48.dp)
                                    .width(120.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = if (gradeTypeIsCGPA)
                                    ButtonDefaults.buttonColors(containerColor = DarkBlue2, contentColor = Color.White)
                                else ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                            ) {
                                Text("CGPA")
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = { gradeTypeIsCGPA = false },
                                modifier = Modifier
                                    .height(48.dp)
                                    .width(148.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = if (!gradeTypeIsCGPA)
                                    ButtonDefaults.buttonColors(containerColor = DarkBlue2, contentColor = Color.White)
                                else ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                            ) {
                                Text("Percentage")
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = marksSecured,
                            onValueChange = { marksSecured = it },
                            label = { Text(if (gradeTypeIsCGPA) "Enter CGPA" else "Percentage") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
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

                        Spacer(Modifier.height(16.dp))

                        // Certificate Upload Section
                        Text(
                            text = "Upload Marksheet/Certificates",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { filePickerLauncher.launch("image/*") }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_upload_24),
                                    contentDescription = "Upload",
                                    tint = Black,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (selectedCertificates.isEmpty())
                                        "Tap to upload certificates"
                                    else
                                        "${selectedCertificates.size} file(s) selected",
                                    color = if (selectedCertificates.isEmpty()) Color.Gray else Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        if (selectedCertificates.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            ) {
                                selectedCertificates.forEachIndexed { index, uri ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Certificate ${index + 1}",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                selectedCertificates = selectedCertificates.filterIndexed { i, _ -> i != index }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.outline_arrow_back_ios_24),
                                                contentDescription = "Remove",
                                                tint = Color.Red,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Currently studying ?", fontSize = 16.sp, color = Color.Black)
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = currentlyStudying,
                                onCheckedChange = {
                                    currentlyStudying = it
                                    if (it) {
                                        endYear = null
                                    }
                                }
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showDetailSheet = false
                                    showDegreeSheet = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(0.35f)
                                    .height(52.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBlue2)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_arrow_back_ios_24),
                                    contentDescription = "Back",
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Button(
                                onClick = {
                                    when {
                                        selectedDegree.isBlank() -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Please select a degree type")
                                            }
                                        }
                                        institution.text.isBlank() -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Please enter institution name")
                                            }
                                        }
                                        startYear == null -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Please select start year")
                                            }
                                        }
                                        !currentlyStudying && endYear == null -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Please select end year")
                                            }
                                        }
                                        !currentlyStudying && endYear != null && endYear!! < startYear!! -> {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("End year cannot be before start year")
                                            }
                                        }
                                        else -> {
                                            val newEdu = Education(
                                                degreeType = selectedDegree,
                                                institutionName = institution.text,
                                                board = board.text,
                                                fieldOfStudy = fieldOfStudy.text,
                                                startYear = startYear!!,
                                                endYear = if (currentlyStudying) null else endYear,
                                                gradeType = if (gradeTypeIsCGPA) "CGPA" else "Marks",
                                                marksSecured = marksSecured.text,
                                                currentlyStudying = currentlyStudying,
                                                certificates = selectedCertificates
                                            )
                                            educationList = educationList + newEdu
                                            showDetailSheet = false
                                            selectedDegree = ""
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Education added successfully")
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(0.65f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkBlue2,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(text = "Save", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showStartYearPicker) {
        YearPickerDialog(
            onDismiss = { showStartYearPicker = false },
            onYearSelected = { year ->
                startYear = year
                showStartYearPicker = false
            },
            initialYear = startYear ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        )
    }

    if (showEndYearPicker) {
        YearPickerDialog(
            onDismiss = { showEndYearPicker = false },
            onYearSelected = { year ->
                endYear = year
                showEndYearPicker = false
            },
            initialYear = endYear ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearPickerDialog(
    onDismiss: () -> Unit,
    onYearSelected: (Int) -> Unit,
    initialYear: Int
) {
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
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

@Preview
@Composable
fun JobSeekerEducationPreview() {
        JobSeekerEducationBody()

}