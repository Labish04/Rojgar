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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.rojgar.ui.theme.White
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

// Data class for Category
//data class JobCategory(val name: String, var isSelected: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyUploadPostBody() {
    val context = LocalContext.current
    val activity = context as Activity

    // State variables for text fields
    var title by remember { mutableStateOf("") }
    var postFor by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(listOf<String>()) }
    var jobType by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var responsibilities by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Bottom sheet state
    var showCategoryBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
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

    Scaffold {padding ->
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

            // Title
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
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

            // Post
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
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

            // Category - With Bottom Sheet (NEW)
            Column {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryBottomSheet = true },
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(containerColor = Blue),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.jobcategoryicon),
                                contentDescription = "Category",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            if (selectedCategories.isEmpty()) {
                                Text(
                                    text = "Select Category",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            } else {
                                Text(
                                    text = selectedCategories.joinToString(", "),
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Icon(
                            painter = painterResource(R.drawable.outline_keyboard_arrow_down_24),
                            contentDescription = "Dropdown",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Job Type
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
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

            // Deadline TextField
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

        // Category Bottom Sheet
        if (showCategoryBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategoryBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
            ) {
                CategoryBottomSheet(
                    onDismiss = { showCategoryBottomSheet = false },
                    onSave = { categories ->
                        selectedCategories = categories
                        showCategoryBottomSheet = false
                    },
                    initialCategories = selectedCategories
                )
            }
        }
    }
}

@Composable
fun CategoryBottomSheet(
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
    initialCategories: List<String>
) {
    // Categories list
    val categoryList = remember {
        mutableStateListOf(
            JobCategory("Creative / Graphics / Designing", initialCategories.contains("Creative / Graphics / Designing")),
            JobCategory("IT & Telecommunication", initialCategories.contains("IT & Telecommunication")),
            JobCategory("NGO / INGO / Social work", initialCategories.contains("NGO / INGO / Social work")),
            JobCategory("Sales / Public Relations", initialCategories.contains("Sales / Public Relations")),
            JobCategory("Accounting / Finance", initialCategories.contains("Accounting / Finance")),
            JobCategory("Architecture / Interior Designing", initialCategories.contains("Architecture / Interior Designing")),
            JobCategory("Banking / Insurance / Financial Services", initialCategories.contains("Banking / Insurance / Financial Services")),
            JobCategory("Commercial / Logistics / Supply Chain", initialCategories.contains("Commercial / Logistics / Supply Chain")),
            JobCategory("Construction / Engineering / Architects", initialCategories.contains("Construction / Engineering / Architects")),
            JobCategory("Fashion / Textile Designing", initialCategories.contains("Fashion / Textile Designing")),
            JobCategory("General Management", initialCategories.contains("General Management"))
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .padding(horizontal = 20.dp)
    ) {
        // Header
        Column {
            Text(
                "Select Job Category",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                "You can add upto 5 categories.",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                "${categoryList.count { it.isSelected }}/5",
                fontSize = 14.sp,
                color = if (categoryList.count { it.isSelected } >= 5) Color.Red else DarkBlue2,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search job categories", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedBorderColor = DarkBlue2,
                unfocusedBorderColor = Color.LightGray
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // List of categories
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(
                items = categoryList.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                },
                key = { it.name }
            ) { category ->
                val index = categoryList.indexOf(category)
                SelectableCategoryItem(
                    name = category.name,
                    isSelected = category.isSelected,
                    onToggle = {
                        val selectedCount = categoryList.count { it.isSelected }
                        if (!category.isSelected && selectedCount >= 5) return@SelectableCategoryItem
                        categoryList[index] = category.copy(isSelected = !category.isSelected)
                    }
                )
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Cancel", fontSize = 16.sp)
            }

            Button(
                onClick = {
                    onSave(categoryList.filter { it.isSelected }.map { it.name })
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue2)
            ) {
                Text("Done", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun SelectableCategoryItem(name: String, isSelected: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                name,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.outline_keyboard_arrow_down_24),
                    contentDescription = "Selected",
                    tint = DarkBlue2,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
        }
    }
}

@Preview
@Composable
fun CompanyUploadPostBodyPreview() {
    CompanyUploadPostBody()
}