package com.example.rojgar.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerSearchScreenBody(onBack: () -> Unit = {}) {
    // Filter states
    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedJobTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedExperience by remember { mutableStateOf("") }
    var selectedEducation by remember { mutableStateOf<List<String>>(emptyList()) }
    var minSalary by remember { mutableStateOf("") }
    var maxSalary by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // Predefined options
    val jobCategories = listOf(
        "IT & Software",
        "Marketing",
        "Sales",
        "Design",
        "Finance",
        "Healthcare",
        "Education",
        "Engineering",
        "Customer Service",
        "Human Resources"
    )

    val jobTypes = listOf(
        "Full Time",
        "Part Time",
        "Contract",
        "Remote",
        "Freelance",
        "Internship"
    )

    val experienceLevels = listOf(
        "Entry Level",
        "1-2 Years",
        "3-5 Years",
        "5-10 Years",
        "10+ Years"
    )

    val educationLevels = listOf(
        "High School",
        "Bachelor's Degree",
        "Master's Degree",
        "PhD",
        "Diploma",
        "Certificate"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Filter Jobs",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Blue)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // Job Categories
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Job Categories",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    jobCategories.forEach { category ->
                        FilterChip(
                            selected = category in selectedCategories,
                            onClick = {
                                selectedCategories = if (category in selectedCategories) {
                                    selectedCategories - category
                                } else {
                                    selectedCategories + category
                                }
                            },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple,
                                selectedLabelColor = Color.White,
                                containerColor = White
                            )
                        )
                    }
                }
            }

            // Job Type
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Job Type",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    jobTypes.forEach { type ->
                        FilterChip(
                            selected = type in selectedJobTypes,
                            onClick = {
                                selectedJobTypes = if (type in selectedJobTypes) {
                                    selectedJobTypes - type
                                } else {
                                    selectedJobTypes + type
                                }
                            },
                            label = { Text(type) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple,
                                selectedLabelColor = Color.White,
                                containerColor = White
                            )
                        )
                    }
                }
            }

            // Experience Level
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Experience Level",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    experienceLevels.forEach { level ->
                        FilterChip(
                            selected = selectedExperience == level,
                            onClick = {
                                selectedExperience = if (selectedExperience == level) "" else level
                            },
                            label = { Text(level) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple,
                                selectedLabelColor = Color.White,
                                containerColor = White
                            )
                        )
                    }
                }
            }

            // Education
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Education",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    educationLevels.forEach { education ->
                        FilterChip(
                            selected = education in selectedEducation,
                            onClick = {
                                selectedEducation = if (education in selectedEducation) {
                                    selectedEducation - education
                                } else {
                                    selectedEducation + education
                                }
                            },
                            label = { Text(education) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple,
                                selectedLabelColor = Color.White,
                                containerColor = White
                            )
                        )
                    }
                }
            }

            // Salary Range
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Salary Range",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = minSalary,
                        onValueChange = { minSalary = it },
                        label = { Text("Min Salary") },
                        placeholder = { Text("e.g., 30000") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White
                        )
                    )
                    OutlinedTextField(
                        value = maxSalary,
                        onValueChange = { maxSalary = it },
                        label = { Text("Max Salary") },
                        placeholder = { Text("e.g., 80000") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White
                        )
                    )
                }
            }

            // Location
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Location",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("Enter location (e.g., Kathmandu)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    )
                )
            }

            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedCategories = emptyList()
                            selectedJobTypes = emptyList()
                            selectedExperience = ""
                            selectedEducation = emptyList()
                            minSalary = ""
                            maxSalary = ""
                            location = ""
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = White
                        )
                    ) {
                        Text("Reset", style = TextStyle(fontSize = 16.sp))
                    }

                    Button(
                        onClick = {
                            // TODO: Apply filters and fetch filtered jobs
                            println("Selected Categories: $selectedCategories")
                            println("Selected Job Types: $selectedJobTypes")
                            println("Selected Experience: $selectedExperience")
                            println("Selected Education: $selectedEducation")
                            println("Salary Range: $minSalary - $maxSalary")
                            println("Location: $location")

                            // After applying filters, you can navigate back
                            // onBack()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple
                        )
                    ) {
                        Text("Apply Filters", style = TextStyle(fontSize = 16.sp))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}