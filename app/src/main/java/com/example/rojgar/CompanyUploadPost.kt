package com.example.rojgar

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.Gray
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.RojgarTheme
import java.util.*

class CompanyUploadPost : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RojgarTheme {
                CompanyUploadPostBody()
            }
        }
    }
}

@Composable
fun CompanyUploadPostBody() {
    val context = LocalContext.current
    val activity = context as Activity

    data class NavItem(
        val label: String,
        val selectedIcon: Int,
        val unselectedIcon: Int
    )

    var selectedIndex by remember { mutableStateOf(2) }


    // State variables for text fields
    var title by remember { mutableStateOf("") }
    var postFor by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var jobType by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var responsibilities by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Calendar instance for date/time picker
    val calendar = Calendar.getInstance()

    // DatePickerDialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // After date is selected, show time picker
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    // Format the selected date and time
                    val formattedDateTime = String.format(
                        "%02d/%02d/%d %02d:%02d",
                        dayOfMonth,
                        month + 1,
                        year,
                        hourOfDay,
                        minute
                    )
                    deadline = formattedDateTime
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(

    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .background(Blue)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Cover Photo Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray)
                        .clickable {
                            launcher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "Upload Photo",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                        )
                    }

                    if (selectedImageUri == null) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_upload_24),
                            contentDescription = "Add Cover Photo",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(60.dp)
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title TextField
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.jobtitleicon),
                        contentDescription = "Title",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = Blue,
                    focusedContainerColor = Blue,
                    unfocusedContainerColor = Blue,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Post For TextField
            OutlinedTextField(
                value = postFor,
                onValueChange = { postFor = it },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.jobpost_filled),
                        contentDescription = "Post for",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Post") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = Blue,
                    focusedContainerColor = Blue,
                    unfocusedContainerColor = Blue,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Category TextField
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.jobcategoryicon),
                        contentDescription = "Category",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = Blue,
                    focusedContainerColor = Blue,
                    unfocusedContainerColor = Blue,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Job Type TextField
            OutlinedTextField(
                value = jobType,
                onValueChange = { jobType = it },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.jobtype),
                        contentDescription = "Job Type",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Job Type") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = Blue,
                    focusedContainerColor = Blue,
                    unfocusedContainerColor = Blue,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Experience TextField
            OutlinedTextField(
                value = experience,
                onValueChange = { experience = it },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.experience_filled),
                        contentDescription = "Experience",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Experience") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = Blue,
                    focusedContainerColor = Blue,
                    unfocusedContainerColor = Blue,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Education TextField
            OutlinedTextField(
                value = education,
                onValueChange = { education = it },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.educationboardicon),
                        contentDescription = "Education",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Education") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = Blue,
                    focusedContainerColor = Blue,
                    unfocusedContainerColor = Blue,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Skills TextField
            OutlinedTextField(
                value = skills,
                onValueChange = { skills = it },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.skills_filledicon),
                        contentDescription = "Skills",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Skills") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = Blue,
                    focusedContainerColor = Blue,
                    unfocusedContainerColor = Blue,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Salary TextField
            OutlinedTextField(
                value = salary,
                onValueChange = { salary = it },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.salaryicon),
                        contentDescription = "Salary",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Salary") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = Blue,
                    focusedContainerColor = Blue,
                    unfocusedContainerColor = Blue,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Deadline TextField with Date Time Picker
            OutlinedTextField(
                value = deadline,
                onValueChange = { deadline = it },
                readOnly = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.deadlineicon),
                        contentDescription = "Deadline",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            datePickerDialog.show()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.datetimeicon),
                            contentDescription = "Select Date and Time",
                            tint = Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = { Text("Deadline") },
                placeholder = { Text("Select date and time") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = Blue,
                    focusedContainerColor = Blue,
                    unfocusedContainerColor = Blue,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Responsibilities TextField
            OutlinedTextField(
                value = responsibilities,
                onValueChange = { responsibilities = it },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.responsibilityicon),
                        contentDescription = "Responsibilities",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Responsibilities") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                shape = RoundedCornerShape(15.dp),
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = Blue,
                    focusedContainerColor = Blue,
                    unfocusedContainerColor = Blue,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Job Description TextField
            OutlinedTextField(
                value = jobDescription,
                onValueChange = { jobDescription = it },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.jobdescriptionicon),
                        contentDescription = "Job Description",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Job Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                shape = RoundedCornerShape(15.dp),
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = Blue,
                    focusedContainerColor = Blue,
                    unfocusedContainerColor = Blue,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row {
                Spacer(modifier = Modifier.width(20.dp))

                Button(
                    onClick = {},
                    shape = RoundedCornerShape(40.dp),
                    modifier = Modifier
                        .width(160.dp)
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue2,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Post",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.width(40.dp))

                Button(
                    onClick = { activity.finish() },
                    shape = RoundedCornerShape(40.dp),
                    modifier = Modifier
                        .width(160.dp)
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gray,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview
@Composable
fun CompanyUploadPostBodyPreview() {
    CompanyUploadPostBody()
}