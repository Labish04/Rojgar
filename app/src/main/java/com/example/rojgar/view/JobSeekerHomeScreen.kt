package com.example.rojgar.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.Gray
import com.example.rojgar.ui.theme.NormalBlue
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import com.example.rojgar.model.PreferenceModel
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.viewmodel.JobViewModel

// Filter State Data Class
data class JobFilterState(
    val selectedCategories: List<String> = emptyList(),
    val selectedJobTypes: List<String> = emptyList(),
    val selectedExperience: String = "",
    val selectedEducation: List<String> = emptyList(),
    val minSalary: String = "",
    val maxSalary: String = "",
    val location: String = ""
)

@Composable
fun JobSeekerHomeScreenBody(){

    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
    val preference = remember { PreferenceModel() }
    val recommendedJobs by jobViewModel.recommendedJobs.observeAsState(emptyList())
    val message by jobViewModel.message.observeAsState("")


    var search by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf(JobFilterState()) }

    LaunchedEffect (Unit) {
        jobViewModel.loadRecommendations(preference)
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Blue)
    ){

        Spacer(modifier = Modifier.height(20.dp))

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search jobs", style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Gray
                )) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.searchicon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp),
                        tint = Gray,
                    )
                },
                shape = RoundedCornerShape(15.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = NormalBlue,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .height(50.dp)
                    .width(300.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { showFilterSheet = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .height(50.dp)
                    .width(56.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.filter),
                    contentDescription = "Filter",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ){
            Card (
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ){
                Text("Profile Completed", style = TextStyle(
                    fontSize = 18.sp,
                    color = Color.DarkGray
                ),
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Card (
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ){
                Text("Calendar", style = TextStyle(
                    fontSize = 18.sp,
                    color = Color.DarkGray
                ),
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp)
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                "Recommended Jobs", style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            )
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp),
                horizontalArrangement = Arrangement.End
            ){
                Text(
                    "Show All", style = TextStyle(
                        fontSize = 18.sp
                    )
                )
            }
        }

        Card (
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(395.dp)
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {

                if (recommendedJobs.isEmpty()) {
                    Text(
                        text = if (message.isNotEmpty()) message else "No recommended jobs yet",
                        color = Color.Gray
                    )
                } else {
                    LazyColumn {
                        items(recommendedJobs.size) { index ->
                            val job = recommendedJobs[index]

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )

                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = job.title, color = Color.Black)
                                    Text(text = job.position, color = Color.DarkGray)
                                    Text(text = job.jobType, color = Color.Blue)
                                    Text(text = job.skills, color = Color.Gray)
                                    Text(text = job.salary, color = Color.Green)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet
    JobFilterBottomSheet(
        showFilter = showFilterSheet,
        onDismiss = { showFilterSheet = false },
        onApplyFilter = { filterState ->
            currentFilter = filterState
            // TODO: Apply filter to your job list
            // Filter jobs based on filterState criteria
            println("Applied filters: $filterState")
        },
        initialFilterState = currentFilter
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobFilterBottomSheet(
    showFilter: Boolean,
    onDismiss: () -> Unit,
    onApplyFilter: (JobFilterState) -> Unit,
    initialFilterState: JobFilterState = JobFilterState()
) {
    var filterState by remember { mutableStateOf(initialFilterState) }

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

    if (showFilter) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = White,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Filter Jobs",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                }

                // Job Categories
                item {
                    Text(
                        "Job Categories",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(vertical = 12.dp)
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
                                selected = category in filterState.selectedCategories,
                                onClick = {
                                    filterState = if (category in filterState.selectedCategories) {
                                        filterState.copy(
                                            selectedCategories = filterState.selectedCategories - category
                                        )
                                    } else {
                                        filterState.copy(
                                            selectedCategories = filterState.selectedCategories + category
                                        )
                                    }
                                },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Purple,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Job Type
                item {
                    Text(
                        "Job Type",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
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
                                selected = type in filterState.selectedJobTypes,
                                onClick = {
                                    filterState = if (type in filterState.selectedJobTypes) {
                                        filterState.copy(
                                            selectedJobTypes = filterState.selectedJobTypes - type
                                        )
                                    } else {
                                        filterState.copy(
                                            selectedJobTypes = filterState.selectedJobTypes + type
                                        )
                                    }
                                },
                                label = { Text(type) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Purple,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Experience Level
                item {
                    Text(
                        "Experience Level",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
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
                                selected = filterState.selectedExperience == level,
                                onClick = {
                                    filterState = filterState.copy(
                                        selectedExperience = if (filterState.selectedExperience == level) "" else level
                                    )
                                },
                                label = { Text(level) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Purple,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Education
                item {
                    Text(
                        "Education",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
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
                                selected = education in filterState.selectedEducation,
                                onClick = {
                                    filterState = if (education in filterState.selectedEducation) {
                                        filterState.copy(
                                            selectedEducation = filterState.selectedEducation - education
                                        )
                                    } else {
                                        filterState.copy(
                                            selectedEducation = filterState.selectedEducation + education
                                        )
                                    }
                                },
                                label = { Text(education) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Purple,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Salary Range
                item {
                    Text(
                        "Salary Range",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = filterState.minSalary,
                            onValueChange = { filterState = filterState.copy(minSalary = it) },
                            label = { Text("Min Salary") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = filterState.maxSalary,
                            onValueChange = { filterState = filterState.copy(maxSalary = it) },
                            label = { Text("Max Salary") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Location
                item {
                    Text(
                        "Location",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = filterState.location,
                        onValueChange = { filterState = filterState.copy(location = it) },
                        placeholder = { Text("Enter location") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Action Buttons
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                filterState = JobFilterState()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset")
                        }

                        Button(
                            onClick = {
                                onApplyFilter(filterState)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple
                            )
                        ) {
                            Text("Apply Filters")
                        }
                    }
                }
            }
        }
    }
}