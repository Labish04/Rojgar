package com.example.rojgar

import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

data class Experience(
    val company: String,
    val role: String,
    val startDate: String,
    val endDate: String,
    val currentlyWorking: Boolean,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerExperienceBody() {
    // list of experiences (in-memory)
    var experiences by remember { mutableStateOf(listOf<Experience>()) }

    // Bottom sheet control
    var showSheet by remember { mutableStateOf(false) }

    // Form states inside the sheet
    var companyName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var currentlyWorking by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    // Date pickers
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val startPickerState = rememberDatePickerState()
    val endPickerState = rememberDatePickerState()

    // coroutine scope for animations / showing sheet (if needed)
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedImageUri = uri
        }

    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }

    // Dropdown Job Level list
    val jobLevels = listOf("Top Level", "Senior Level", "Mid Level", "Entry Level")
    var expandedLevel by remember { mutableStateOf(false) }
    var selectedLevel by remember { mutableStateOf("") }


    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Blue)
        ) {
            // Header
            Card(
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkBlue2
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                            .clickable(interactionSource = remember {
                                MutableInteractionSource()
                            },
                                indication = null    ){
                                val intent = Intent(context, JobSeekerProfileDetailsActivity ::class.java)
                                context.startActivity(intent)
                            },
                    )
                    Text(
                        "Experience",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.size(30.dp))
                }
            }

            // Content area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "What are your most relevant experiences?",
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(200.dp))

                // If no experiences -> show empty illustration + Add button centered at bottom
                if (experiences.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(520.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(40.dp))

                        // Illustration (use your drawable for empty state)
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
                                    // clear form and show sheet
                                    companyName = ""
                                    jobTitle = ""
                                    startDate = ""
                                    endDate = ""
                                    currentlyWorking = false
                                    description = ""
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
                    // Show list of experiences (simple cards)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        experiences.forEach { exp ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Blue)
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

                        // Add button to add more
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    // clear and show sheet
                                    companyName = ""
                                    jobTitle = ""
                                    startDate = ""
                                    endDate = ""
                                    currentlyWorking = false
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
                                Text(text = "Add")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
    if (showSheet) {


        // Dropdown Job Category list
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

        if (showSheet) {
            Dialog(
                onDismissRequest = { showSheet = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(
                            top = 150.dp
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Blue
                    )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {


                        // Company Name
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Company Name") },
                            leadingIcon = {
                                Icon(
                                    painterResource(id = R.drawable.companynameicon),
                                    contentDescription = null,
                                    tint = Color.Gray,
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
                                focusedContainerColor = Blue,
                                unfocusedContainerColor = Blue
                            )
                        )
                        Spacer(Modifier.height(12.dp))

                        // Job Title
                        OutlinedTextField(
                            value = jobTitle,
                            onValueChange = { jobTitle = it },
                            label = { Text("Job Title") },
                            leadingIcon = {
                                Icon(
                                    painterResource(id = R.drawable.jobtitleicon),
                                    contentDescription = null,
                                    tint = Color.Gray,
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
                                focusedContainerColor = Blue,
                                unfocusedContainerColor = Blue
                            )
                        )

                        Spacer(Modifier.height(12.dp))


                        // Job Category (Dropdown)
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,     // Allows Box to receive click
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.jobcategoryicon),  // Your category icon
                                        contentDescription = "Category Icon",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(27.dp)
                                    )
                                },
                                label = { Text("Select Job Category") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clickable { expandedCategory = true }, // open dropdown
                                shape = RoundedCornerShape(15.dp),
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                                        contentDescription = "Dropdown Arrow",
                                        tint = Color.Black,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { expandedCategory = true }
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    disabledIndicatorColor = Color.Black,
                                    disabledContainerColor = Blue,
                                    focusedContainerColor = Blue,
                                    unfocusedContainerColor = Blue,
                                    focusedIndicatorColor = Purple,
                                    unfocusedIndicatorColor = Black,
                                    disabledTextColor = Black
                                )
                            )

                            // -------- DROPDOWN MENU --------
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


                        // Job Level (Dropdown)

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedLevel,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,     // Needed so click works on Box
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.joblevelicon), // your icon
                                        contentDescription = "Level Icon",
                                        tint = Color.Gray,
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
                                        tint = Color.Black,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { expandedLevel = true }
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    disabledIndicatorColor = Color.Black,
                                    disabledContainerColor = Blue,
                                    disabledTextColor = Color.Black,
                                    focusedContainerColor = Blue,
                                    unfocusedContainerColor = Blue,
                                    focusedIndicatorColor = DarkBlue2,
                                    unfocusedIndicatorColor = Color.Black
                                )
                            )

                            // ----------- DROPDOWN MENU -----------
                            DropdownMenu(
                                expanded = expandedLevel,
                                onDismissRequest = { expandedLevel = false },
                                modifier = Modifier
                                    .background(Blue)
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

                        //-------------------------
                        // Start & End Date
                        //-------------------------
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                            OutlinedTextField(
                                value = startDate,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clickable { showStartPicker = true },
                                label = { Text("Start Date") },
                                leadingIcon = {
                                    Icon(
                                        painterResource(id = R.drawable.calendaricon),
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                shape = RoundedCornerShape(15.dp),
                                colors = TextFieldDefaults.colors(
                                    disabledIndicatorColor = Color.Black,
                                    disabledContainerColor = Blue,
                                    disabledTextColor = Color.Black,
                                    focusedContainerColor = Blue,
                                    unfocusedContainerColor = Blue,
                                    focusedIndicatorColor = DarkBlue2,
                                    unfocusedIndicatorColor = Color.Black
                                )
                            )

                            OutlinedTextField(
                                value = if (currentlyWorking) "" else endDate,
                                onValueChange = {},
                                enabled = !currentlyWorking,
                                readOnly = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clickable(enabled = !currentlyWorking) {
                                        showEndPicker = true
                                    },
                                label = { Text("End Date") },
                                leadingIcon = {
                                    Icon(
                                        painterResource(id = R.drawable.calendaricon),
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                shape = RoundedCornerShape(15.dp),
                                colors = TextFieldDefaults.colors(
                                    disabledIndicatorColor = Color.Black,
                                    disabledContainerColor = Blue,
                                    disabledTextColor = Color.Black,
                                    focusedContainerColor = Blue,
                                    unfocusedContainerColor = Blue,
                                    focusedIndicatorColor = DarkBlue2,
                                    unfocusedIndicatorColor = Color.Black
                                )
                            )
                        }

                        Spacer(Modifier.height(12.dp))


                        // Currently working switch

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = currentlyWorking,
                                onCheckedChange = { currentlyWorking = it }
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Currently work here?")
                        }

                        Spacer(Modifier.height(12.dp))

                        //-------------------------
                        // Upload Experience Letter
                        //-------------------------
                        OutlinedTextField(
                            value = selectedImageUri?.lastPathSegment ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Upload Experience Letter") },
                            trailingIcon = {
                                Icon(
                                    painterResource(id = R.drawable.baseline_upload_24),
                                    contentDescription = null,
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
                                disabledContainerColor = Blue,
                                disabledTextColor = Color.Black,
                                focusedContainerColor = Blue,
                                unfocusedContainerColor = Blue,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.Black
                            )
                        )

                        Spacer(Modifier.height(20.dp))

                        //-------------------------
                        // SAVE BUTTON
                        //-------------------------
                        Button(
                            onClick = {
                                val exp = Experience(
                                    company = companyName,
                                    role = jobTitle,
                                    startDate = startDate,
                                    endDate = endDate,
                                    currentlyWorking = currentlyWorking,
                                    description = selectedCategory + " | " + selectedLevel
                                )
                                experiences = experiences + exp
                                showSheet = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkBlue2,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Save", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(16.dp))
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
