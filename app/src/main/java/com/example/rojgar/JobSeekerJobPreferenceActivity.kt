package com.example.rojgar

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White

class JobSeekerJobPreferenceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerJobPreferenceBody()
        }
    }
}

// Data classes
data class JobCategory(val name: String, var isSelected: Boolean = false)
data class Industry(val name: String, var isSelected: Boolean = false)
data class JobTitle(val name: String, var isSelected: Boolean = false)
data class AvailableFor(val name: String, var isSelected: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerJobPreferenceBody() {
    var showBottomSheet by remember { mutableStateOf(false) }
    var currentSection by remember { mutableStateOf("category") }

    // Job preferences state
    var selectedCategories by remember { mutableStateOf(listOf<String>()) }
    var selectedIndustries by remember { mutableStateOf(listOf<String>()) }
    var selectedJobTitles by remember { mutableStateOf(listOf<String>()) }
    var selectedAvailability by remember { mutableStateOf(listOf<String>()) }
    var locationInput by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                        // Navigate back
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(70.dp))
                    Text(
                        "Job Preference",
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
                .background(White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Define your job preferences to receive more relevant and tailored job matches.",
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Job Category Display
            PreferenceField(
                label = "Job Category",
                selectedItems = selectedCategories,
                onClick = {
                    currentSection = "category"
                    showBottomSheet = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Industry Display
            PreferenceField(
                label = "Industry",
                selectedItems = selectedIndustries,
                onClick = {
                    currentSection = "industry"
                    showBottomSheet = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Job Title Display
            PreferenceField(
                label = "Job Title",
                selectedItems = selectedJobTitles,
                onClick = {
                    currentSection = "title"
                    showBottomSheet = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Available For Display
            PreferenceField(
                label = "Available For",
                selectedItems = selectedAvailability,
                onClick = {
                    currentSection = "availability"
                    showBottomSheet = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location Input Field (Regular TextField - No Bottom Sheet)
            Column {
                Text(
                    text = "Job Preference Location",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.locationicon),
                            contentDescription = "Location",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    placeholder = { Text("Enter job preference location", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedBorderColor = DarkBlue2,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Back Button
                OutlinedButton(
                    onClick ={},
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier
                        .weight(0.7f)
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

                Spacer(Modifier.width(10.dp))

                Button(
                    onClick = { /* Save changes */ },
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

        // Bottom Sheet (Only for category, industry, title, availability)
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
            ) {
                JobPreferenceBottomSheet(
                    initialSection = currentSection,
                    onDismiss = { showBottomSheet = false },
                    onSave = { categories, industries, titles, availability ->
                        selectedCategories = categories
                        selectedIndustries = industries
                        selectedJobTitles = titles
                        selectedAvailability = availability
                        showBottomSheet = false
                    },
                    initialCategories = selectedCategories,
                    initialIndustries = selectedIndustries,
                    initialTitles = selectedJobTitles,
                    initialAvailability = selectedAvailability
                )
            }
        }
    }
}

@Composable
fun PreferenceField(
    label: String,
    selectedItems: List<String>,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedItems.isEmpty()) {
                    Text(
                        text = "Select $label",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = selectedItems.joinToString(", "),
                        color = Color.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
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
}

@Composable
fun JobPreferenceBottomSheet(
    initialSection: String,
    onDismiss: () -> Unit,
    onSave: (List<String>, List<String>, List<String>, List<String>) -> Unit,
    initialCategories: List<String>,
    initialIndustries: List<String>,
    initialTitles: List<String>,
    initialAvailability: List<String>
) {
    var currentSection by remember { mutableStateOf(initialSection) }

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

    // Industries list
    val industryList = remember {
        mutableStateListOf(
            Industry("Software Companies", initialIndustries.contains("Software Companies")),
            Industry("Information / Computer / Technology", initialIndustries.contains("Information / Computer / Technology")),
            Industry("NGO / INGO / Development Projects", initialIndustries.contains("NGO / INGO / Development Projects")),
            Industry("Designing / Printing / Publishing", initialIndustries.contains("Designing / Printing / Publishing")),
            Industry("Associations", initialIndustries.contains("Associations")),
            Industry("Audit Firms / Tax Consultant", initialIndustries.contains("Audit Firms / Tax Consultant"))
        )
    }

    // Job Titles list
    val jobTitleList = remember {
        mutableStateListOf(
            JobTitle("Baker", initialTitles.contains("Baker")),
            JobTitle("Backend Engineer", initialTitles.contains("Backend Engineer")),
            JobTitle("Backend Developer", initialTitles.contains("Backend Developer")),
            JobTitle("Associate Database Administrator", initialTitles.contains("Associate Database Administrator")),
            JobTitle("Banquet and Event Manager", initialTitles.contains("Banquet and Event Manager")),
            JobTitle("Basketball/Futsal Coach", initialTitles.contains("Basketball/Futsal Coach")),
            JobTitle("Banquet Sous Chef", initialTitles.contains("Banquet Sous Chef"))
        )
    }

    // Available For list
    val availabilityList = remember {
        mutableStateListOf(
            AvailableFor("Full Time", initialAvailability.contains("Full Time")),
            AvailableFor("Part Time", initialAvailability.contains("Part Time")),
            AvailableFor("Freelance", initialAvailability.contains("Freelance")),
            AvailableFor("Temporary", initialAvailability.contains("Temporary")),
            AvailableFor("Internship", initialAvailability.contains("Internship")),
            AvailableFor("Traineeship", initialAvailability.contains("Traineeship")),
            AvailableFor("Volunteer", initialAvailability.contains("Volunteer"))
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .padding(horizontal = 20.dp)
    ) {
        // Header with tabs
        when (currentSection) {
            "category" -> SectionHeader(
                title = "Preferred Job Category",
                subtitle = "You can add upto 5 category.",
                count = categoryList.count { it.isSelected },
                maxCount = 5
            )
            "industry" -> SectionHeader(
                title = "Preferred Job Industry",
                subtitle = "You can add upto 5 industry.",
                count = industryList.count { it.isSelected },
                maxCount = 5
            )
            "title" -> SectionHeader(
                title = "Select Job Title",
                subtitle = "You can add upto 5 job title.",
                count = jobTitleList.count { it.isSelected },
                maxCount = 5
            )
            "availability" -> SectionHeader(
                title = "Available For",
                subtitle = "",
                count = availabilityList.count { it.isSelected },
                maxCount = 7
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        SearchBar(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            placeholder = when (currentSection) {
                "category" -> "Search preferred job categories"
                "industry" -> "Search preferred job industry"
                "title" -> "Search preferred job title"
                else -> "Search"
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // List of items
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            when (currentSection) {
                "category" -> {
                    items(
                        items = categoryList.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        },
                        key = { it.name }
                    ) { category ->
                        val index = categoryList.indexOf(category)
                        SelectableItem(
                            name = category.name,
                            isSelected = category.isSelected,
                            onToggle = {
                                val selectedCount = categoryList.count { it.isSelected }
                                if (!category.isSelected && selectedCount >= 5) return@SelectableItem
                                categoryList[index] = category.copy(isSelected = !category.isSelected)
                            }
                        )
                    }
                }
                "industry" -> {
                    items(
                        items = industryList.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        },
                        key = { it.name }
                    ) { industry ->
                        val index = industryList.indexOf(industry)
                        SelectableItem(
                            name = industry.name,
                            isSelected = industry.isSelected,
                            onToggle = {
                                val selectedCount = industryList.count { it.isSelected }
                                if (!industry.isSelected && selectedCount >= 5) return@SelectableItem
                                industryList[index] = industry.copy(isSelected = !industry.isSelected)
                            }
                        )
                    }
                }
                "title" -> {
                    items(
                        items = jobTitleList.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        },
                        key = { it.name }
                    ) { title ->
                        val index = jobTitleList.indexOf(title)
                        SelectableItem(
                            name = title.name,
                            isSelected = title.isSelected,
                            onToggle = {
                                val selectedCount = jobTitleList.count { it.isSelected }
                                if (!title.isSelected && selectedCount >= 5) return@SelectableItem
                                jobTitleList[index] = title.copy(isSelected = !title.isSelected)
                            }
                        )
                    }
                }
                "availability" -> {
                    items(
                        items = availabilityList,
                        key = { it.name }
                    ) { availability ->
                        val index = availabilityList.indexOf(availability)
                        SelectableItem(
                            name = availability.name,
                            isSelected = availability.isSelected,
                            onToggle = {
                                availabilityList[index] = availability.copy(isSelected = !availability.isSelected)
                            }
                        )
                    }
                }
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
                    onSave(
                        categoryList.filter { it.isSelected }.map { it.name },
                        industryList.filter { it.isSelected }.map { it.name },
                        jobTitleList.filter { it.isSelected }.map { it.name },
                        availabilityList.filter { it.isSelected }.map { it.name }
                    )
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
fun SectionHeader(title: String, subtitle: String, count: Int, maxCount: Int) {
    Column {
        Text(
            title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        if (subtitle.isNotEmpty()) {
            Text(
                subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        Text(
            "$count/$maxCount",
            fontSize = 14.sp,
            color = if (count >= maxCount) Color.Red else DarkBlue2,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SearchBar(searchQuery: String, onSearchChange: (String) -> Unit, placeholder: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = DarkBlue2,
                unfocusedBorderColor = Color.LightGray
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = { },
            modifier = Modifier
                .size(48.dp)
                .background(DarkBlue2, RoundedCornerShape(8.dp))
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                contentDescription = "Search",
                tint = Color.White
            )
        }
    }
}

@Composable
fun SelectableItem(name: String, isSelected: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        shape = RoundedCornerShape(8.dp)
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
                color = Color.Black
            )
            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                    contentDescription = "Selected",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.addexperience),
                    contentDescription = "Add",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun JobSeekerJobPreferencePreview() {
    JobSeekerJobPreferenceBody()
}