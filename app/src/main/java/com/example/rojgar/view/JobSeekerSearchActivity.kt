package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobModel
import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.*
import com.example.rojgar.view.ui.theme.RojgarTheme
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.example.rojgar.viewmodel.JobViewModel
import com.google.gson.Gson
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale

data class JobFilterState(
    val selectedCategories: List<String> = emptyList(),
    val selectedJobTypes: List<String> = emptyList(),
    val selectedExperience: String = "",
    val selectedEducation: List<String> = emptyList(),
    val minSalary: String = "",
    val maxSalary: String = "",
    val location: String = ""
)

data class JobPostWithCompany(
    val jobPost: JobModel,
    val companyName: String,
    val companyProfile: String,
    val isLoading: Boolean
)

class JobSeekerSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val searchQuery = intent.getStringExtra("searchQuery") ?: ""
        val filterStateJson = intent.getStringExtra("filterState")
        val filterState = if (filterStateJson != null) {
            Gson().fromJson(filterStateJson, JobFilterState::class.java)
        } else {
            JobFilterState()
        }

        setContent {
            RojgarTheme {
                JobSeekerSearchScreen(
                    initialSearchQuery = searchQuery,
                    initialFilterState = filterState,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerSearchScreen(
    initialSearchQuery: String,
    initialFilterState: JobFilterState,
    onBackClick: () -> Unit
) {
    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Jobs", "Companies", "Job Seekers")

    var searchQuery by remember { mutableStateOf(initialSearchQuery) }
    var filterState by remember { mutableStateOf(initialFilterState) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val activeFiltersCount = remember(filterState) { countActiveFilters(filterState) }

    var allJobs by remember { mutableStateOf<List<JobModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var companyDetailsMap by remember { mutableStateOf<Map<String, CompanyModel>>(emptyMap()) }

    val companyDetails by companyViewModel.companyDetails.observeAsState()

    LaunchedEffect(companyDetails) {
        companyDetails?.let { company ->
            companyDetailsMap = companyDetailsMap + (company.companyId to company)
        }
    }

    // Fetch all jobs
    LaunchedEffect(Unit) {
        jobViewModel.getAllJobPosts { success, message, posts ->
            if (!success || posts == null) {
                isLoading = false
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                return@getAllJobPosts
            }

            // Filter out expired jobs
            val activePosts = posts.filter { !isDeadlineExpired(it.deadline) }
            allJobs = activePosts

            isLoading = false

            // Get unique company IDs and fetch company details
            val uniqueCompanyIds = activePosts.map { it.companyId }.distinct()
            uniqueCompanyIds.forEach { companyId ->
                companyViewModel.getCompanyDetails(companyId)
            }
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue,
                    navigationIconContentColor = Color.Black,
                    titleContentColor = Color.Black
                ),
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Blue)
                .padding(padding)
        ) {
            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Blue,
                contentColor = Color.Black,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Purple
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Search Bar with Filter Button (only for Jobs tab)
            if (selectedTabIndex == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search ",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.searchicon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Gray
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(20.dp),
                                    tint = Gray
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(15.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = NormalBlue,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Filter Button with Badge
                BadgedBox(
                    badge = {
                        if (activeFiltersCount > 0) {
                            Badge(
                                containerColor = Purple
                            ) {
                                Text(
                                    text = activeFiltersCount.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                ) {
                    Button(
                        onClick = { showFilterSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .height(56.dp)
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
            }

                // Active Filters Display
                if (activeFiltersCount > 0) {
                    ActiveFiltersChips(
                        filterState = filterState,
                        onClearFilter = { filterState = it }
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> JobsTabContent(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    filterState = filterState,
                    onFilterStateChange = { filterState = it },
                    showFilterSheet = showFilterSheet,
                    onShowFilterSheetChange = { showFilterSheet = it },
                    allJobs = allJobs,
                    isLoading = isLoading,
                    companyDetailsMap = companyDetailsMap,
                    context = context,
                    activeFiltersCount = activeFiltersCount
                )
                1 -> CompaniesTabContent(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    allJobs = allJobs,
                    companyDetailsMap = companyDetailsMap,
                    context = context
                )
                2 -> JobSeekersTabContent(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
            }

        }

            // Filter Bottom Sheet (only for Jobs tab)
            if (selectedTabIndex == 0) {
                JobFilterBottomSheet(
                    showFilter = showFilterSheet,
                    onDismiss = { showFilterSheet = false },
                    onApplyFilter = { filterState = it },
                    initialFilterState = filterState
                )
            }
        }
}

@Composable
fun ActiveFiltersChips(
    filterState: JobFilterState,
    onClearFilter: (JobFilterState) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = true,
                onClick = { onClearFilter(JobFilterState()) },
                label = { Text("Clear All") },
                leadingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Purple,
                    selectedLabelColor = Color.White
                )
            )
        }

        filterState.selectedCategories.forEach { category ->
            item {
                FilterChip(
                    selected = true,
                    onClick = { onClearFilter(filterState.copy(selectedCategories = filterState.selectedCategories - category)) },
                    label = { Text(category) },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NormalBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        filterState.selectedJobTypes.forEach { jobType ->
            item {
                FilterChip(
                    selected = true,
                    onClick = { onClearFilter(filterState.copy(selectedJobTypes = filterState.selectedJobTypes - jobType)) },
                    label = { Text(jobType) },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NormalBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (filterState.selectedExperience.isNotEmpty()) {
            item {
                FilterChip(
                    selected = true,
                    onClick = { onClearFilter(filterState.copy(selectedExperience = "")) },
                    label = { Text(filterState.selectedExperience) },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NormalBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        filterState.selectedEducation.forEach { education ->
            item {
                FilterChip(
                    selected = true,
                    onClick = { onClearFilter(filterState.copy(selectedEducation = filterState.selectedEducation - education)) },
                    label = { Text(education) },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NormalBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (filterState.minSalary.isNotEmpty() || filterState.maxSalary.isNotEmpty()) {
            item {
                FilterChip(
                    selected = true,
                    onClick = { onClearFilter(filterState.copy(minSalary = "", maxSalary = "")) },
                    label = { Text("Salary: ${filterState.minSalary}-${filterState.maxSalary}") },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NormalBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (filterState.location.isNotEmpty()) {
            item {
                FilterChip(
                    selected = true,
                    onClick = { onClearFilter(filterState.copy(location = "")) },
                    label = { Text(filterState.location) },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NormalBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

// Helper function to count active filters
fun countActiveFilters(filterState: JobFilterState): Int {
    return filterState.selectedCategories.size +
           filterState.selectedJobTypes.size +
           (if (filterState.selectedExperience.isNotEmpty()) 1 else 0) +
           filterState.selectedEducation.size +
           (if (filterState.minSalary.isNotEmpty() || filterState.maxSalary.isNotEmpty()) 1 else 0) +
           (if (filterState.location.isNotEmpty()) 1 else 0)
}

// Helper function to extract numeric value from salary string
fun extractSalaryValue(salary: String): Double {
    if (salary.isEmpty()) return 0.0
    return try {
        salary.replace(",", "").replace("Rs.", "").replace("NPR", "")
            .replace("/", "").replace("-", "").trim()
            .filter { it.isDigit() || it == '.' }
            .toDoubleOrNull() ?: 0.0
    } catch (e: Exception) {
        0.0
    }
}

@Composable
private fun FilterChipSection(
    title: String,
    options: List<String>,
    selectedItems: List<String>,
    onSelectionChange: (List<String>) -> Unit
) {
    Text(text = title, fontWeight = FontWeight.SemiBold)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val selected = selectedItems.contains(option)
            FilterChip(
                selected = selected,
                onClick = {
                    onSelectionChange(if (selected) selectedItems - option else selectedItems + option)
                },
                label = { Text(option) }
            )
        }
    }
}

@Composable
private fun SingleSelectFilterChipSection(
    title: String,
    options: List<String>,
    selected: String,
    onSelectionChange: (String) -> Unit
) {
    Text(text = title, fontWeight = FontWeight.SemiBold)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelectionChange(if (selected == option) "" else option) },
                label = { Text(option) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobFilterBottomSheet(
    showFilter: Boolean,
    onDismiss: () -> Unit,
    onApplyFilter: (JobFilterState) -> Unit,
    initialFilterState: JobFilterState
) {
    if (!showFilter) return

    val scrollState = rememberScrollState()
    var tempFilterState by remember(initialFilterState) { mutableStateOf(initialFilterState) }

    val categoryOptions = listOf(
        "IT & Telecommunication",
        "Creative / Graphics / Designing",
        "Accounting / Finance",
        "Sales / Public Relations",
        "General Management"
    )
    val jobTypeOptions = listOf("Full Time", "Part Time", "Internship", "Contract", "Remote")
    val experienceOptions = listOf("Fresher", "1-2 years", "3-5 years", "5+ years")
    val educationOptions = listOf("High School", "Diploma", "Bachelor", "Master", "PhD")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filters",
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
            )

            FilterChipSection(
                title = "Categories",
                options = categoryOptions,
                selectedItems = tempFilterState.selectedCategories,
                onSelectionChange = { tempFilterState = tempFilterState.copy(selectedCategories = it) }
            )

            FilterChipSection(
                title = "Job Type",
                options = jobTypeOptions,
                selectedItems = tempFilterState.selectedJobTypes,
                onSelectionChange = { tempFilterState = tempFilterState.copy(selectedJobTypes = it) }
            )

            SingleSelectFilterChipSection(
                title = "Experience",
                options = experienceOptions,
                selected = tempFilterState.selectedExperience,
                onSelectionChange = { tempFilterState = tempFilterState.copy(selectedExperience = it) }
            )

            FilterChipSection(
                title = "Education",
                options = educationOptions,
                selectedItems = tempFilterState.selectedEducation,
                onSelectionChange = { tempFilterState = tempFilterState.copy(selectedEducation = it) }
            )

            Text(text = "Salary Range (per month)", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = tempFilterState.minSalary,
                    onValueChange = { tempFilterState = tempFilterState.copy(minSalary = it) },
                    placeholder = { Text("Min Salary") },
                    label = { Text("Min") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = tempFilterState.maxSalary,
                    onValueChange = { tempFilterState = tempFilterState.copy(maxSalary = it) },
                    placeholder = { Text("Max Salary") },
                    label = { Text("Max") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Text(text = "Location", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = tempFilterState.location,
                onValueChange = { tempFilterState = tempFilterState.copy(location = it) },
                placeholder = { Text("e.g. Kathmandu") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { tempFilterState = JobFilterState() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = {
                        onApplyFilter(tempFilterState)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple)
                ) {
                    Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun JobsTabContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterState: JobFilterState,
    onFilterStateChange: (JobFilterState) -> Unit,
    showFilterSheet: Boolean,
    onShowFilterSheetChange: (Boolean) -> Unit,
    allJobs: List<JobModel>,
    isLoading: Boolean,
    companyDetailsMap: Map<String, CompanyModel>,
    context: android.content.Context,
    activeFiltersCount: Int
) {
    val filteredJobs = remember(allJobs, searchQuery, filterState, companyDetailsMap) {
        // First check if any filters are applied
        val hasActiveFilters = searchQuery.isNotEmpty() ||
            filterState.selectedCategories.isNotEmpty() ||
            filterState.selectedJobTypes.isNotEmpty() ||
            filterState.selectedExperience.isNotEmpty() ||
            filterState.selectedEducation.isNotEmpty() ||
            filterState.minSalary.isNotEmpty() ||
            filterState.maxSalary.isNotEmpty() ||
            filterState.location.isNotEmpty()

        if (!hasActiveFilters) {
            // No filters applied - show all jobs
            allJobs
        } else {
            // Filters applied - filter jobs
            val filteredResults = allJobs.filter { job ->
                val companyName = companyDetailsMap[job.companyId]?.companyName ?: ""

                val matchesSearch = searchQuery.isEmpty() ||
                    listOf(job.title, job.position, job.jobDescription, job.skills, companyName)
                        .any { it.contains(searchQuery, ignoreCase = true) }

                val matchesCategories = filterState.selectedCategories.isEmpty() ||
                    filterState.selectedCategories.any { job.categories.contains(it) }

                val matchesJobType = filterState.selectedJobTypes.isEmpty() ||
                    filterState.selectedJobTypes.contains(job.jobType)

                val matchesExperience = filterState.selectedExperience.isEmpty() ||
                    job.experience.contains(filterState.selectedExperience, ignoreCase = true)

                val matchesEducation = filterState.selectedEducation.isEmpty() ||
                    filterState.selectedEducation.any { job.education.contains(it, ignoreCase = true) }

                val matchesSalary = when {
                    filterState.minSalary.isEmpty() && filterState.maxSalary.isEmpty() -> true
                    else -> {
                        val jobSalary = extractSalaryValue(job.salary)
                        val min = filterState.minSalary.replace(",", "").toDoubleOrNull() ?: 0.0
                        val max = filterState.maxSalary.replace(",", "").toDoubleOrNull() ?: Double.MAX_VALUE
                        jobSalary in min..max
                    }
                }

                val matchesLocation = filterState.location.isEmpty() ||
                    (job.jobDescription.contains(filterState.location, ignoreCase = true) ||
                     companyName.contains(filterState.location, ignoreCase = true))

                matchesSearch && matchesCategories && matchesJobType &&
                    matchesExperience && matchesEducation && matchesSalary && matchesLocation
            }

            // If no jobs match the filters, show all jobs to ensure company jobs are visible
            if (filteredResults.isEmpty() && allJobs.isNotEmpty()) {
                allJobs
            } else {
                filteredResults
            }
        }
    }

    // Map filtered jobs to JobPostWithCompany
    val filteredJobsWithCompany = remember(filteredJobs, companyDetailsMap) {
        filteredJobs.map { job ->
            val companyInfo = companyDetailsMap[job.companyId]
            JobPostWithCompany(
                jobPost = job,
                companyName = companyInfo?.companyName ?: "",
                companyProfile = companyInfo?.companyProfileImage ?: "",
                isLoading = companyInfo == null
            )
        }
    }

    // Search Results
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Search Results",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        "${filteredJobsWithCompany.size} jobs found",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    )
                }
            }

            if (filteredJobsWithCompany.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No jobs available at the moment.",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            )
                        }
                    }
                }
            } else {
                items(filteredJobsWithCompany, key = { it.jobPost.postId }) { jobWithCompany ->
                    JobSeekerPostCard(
                        jobPostWithCompany = jobWithCompany,
                        onClick = {
                            if (jobWithCompany.jobPost.postId.isNotEmpty()) {
                                val intent = Intent(context, JobApplyActivity::class.java).apply {
                                    putExtra("POST_ID", jobWithCompany.jobPost.postId)
                                    putExtra("COMPANY_ID", jobWithCompany.jobPost.companyId)
                                }
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Job ID is empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        isSaved = false,
                        onSaveClick = { postId ->
                            Toast.makeText(context, "Job saved", Toast.LENGTH_SHORT).show()
                        },
                        onShareClick = { job ->
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    "Check out this job: ${job.title}\n" +
                                            "Company: ${jobWithCompany.companyName}\n" +
                                            "Position: ${job.position}\n" +
                                            "Type: ${job.jobType}\n" +
                                            "Deadline: ${job.deadline}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(
                                android.content.Intent.createChooser(shareIntent, "Share Job")
                            )
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun CompaniesTabContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    allJobs: List<JobModel>,
    companyDetailsMap: Map<String, CompanyModel>,
    context: android.content.Context
) {
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    var allCompanies by remember { mutableStateOf<List<CompanyModel>>(emptyList()) }
    var isCompaniesLoading by remember { mutableStateOf(true) }

    // Fetch all companies from realtime database
    LaunchedEffect(Unit) {
        companyViewModel.getAllCompany { success, message, companies ->
            if (success && companies != null) {
                allCompanies = companies
                isCompaniesLoading = false

                // Also fetch individual company details for any missing ones
                companies.forEach { company ->
                    if (companyDetailsMap[company.companyId] == null) {
                        companyViewModel.getCompanyDetails(company.companyId)
                    }
                }
            } else {
                isCompaniesLoading = false
            }
        }
    }

    // Filter companies based on search query
    val filteredCompanies = remember(allCompanies, searchQuery) {
        allCompanies.filter { company ->
            searchQuery.isEmpty() ||
            company.companyName.contains(searchQuery, ignoreCase = true) ||
            company.companyLocation.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar for Companies
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    "Search companies",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.searchicon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Gray
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(20.dp),
                            tint = Gray
                        )
                    }
                }
            },
            shape = RoundedCornerShape(15.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedIndicatorColor = NormalBlue,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .height(56.dp)
        )

        // Companies List
        if (isCompaniesLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Companies",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            "${filteredCompanies.size} companies found",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        )
                    }
                }

                if (filteredCompanies.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No companies found.",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                } else {
                    items(filteredCompanies, key = { it.companyId }) { company ->
                        CompanyProfileCard(
                            company = company,
                            onClick = {
                                // Navigate to company profile
                                val intent = Intent(context, CompanyProfileActivity::class.java).apply {
                                    putExtra("COMPANY_ID", company.companyId)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun JobSeekersTabContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    val context = LocalContext.current
    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    var allJobSeekers by remember { mutableStateOf<List<JobSeekerModel>>(emptyList()) }
    var isJobSeekersLoading by remember { mutableStateOf(true) }

    // Fetch all job seekers from realtime database
    LaunchedEffect(Unit) {
        jobSeekerViewModel.getAllJobSeeker { success, message, jobSeekers ->
            if (success && jobSeekers != null) {
                // Filter out invalid job seekers and ensure all have valid IDs
                val validJobSeekers = jobSeekers.filter { jobSeeker ->
                    jobSeeker.jobSeekerId.isNotEmpty() &&
                    jobSeeker.fullName.isNotEmpty()
                }
                allJobSeekers = validJobSeekers
                isJobSeekersLoading = false
            } else {
                allJobSeekers = emptyList()
                isJobSeekersLoading = false
            }
        }
    }

    // Filter job seekers based on search query with null-safety
    val filteredJobSeekers = remember(allJobSeekers, searchQuery) {
        allJobSeekers.filter { jobSeeker ->
            val safeSearchQuery = searchQuery.trim()
            safeSearchQuery.isEmpty() ||
            (jobSeeker.fullName.orEmpty().contains(safeSearchQuery, ignoreCase = true)) ||
            (jobSeeker.email.orEmpty().contains(safeSearchQuery, ignoreCase = true)) ||
            (jobSeeker.profession.orEmpty().contains(safeSearchQuery, ignoreCase = true)) ||
            (jobSeeker.bio.orEmpty().contains(safeSearchQuery, ignoreCase = true))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar for Job Seekers
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    "Search job seekers",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.searchicon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Gray
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(20.dp),
                            tint = Gray
                        )
                    }
                }
            },
            shape = RoundedCornerShape(15.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedIndicatorColor = NormalBlue,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .height(56.dp)
        )

        // Job Seekers List
        if (isJobSeekersLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Job Seekers",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            "${filteredJobSeekers.size} job seekers found",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        )
                    }
                }

                if (filteredJobSeekers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No job seekers found.",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                } else {
                    items(
                        filteredJobSeekers,
                        key = { jobSeeker ->
                            // Safe fallback ordering: jobSeekerId → firebase key → email → hash
                            jobSeeker.jobSeekerId.ifEmpty {
                                jobSeeker.email.ifEmpty {
                                    jobSeeker.hashCode().toString()
                                }
                            }
                        }
                    ) { jobSeeker ->
                        JobSeekerProfileCard(
                            jobSeeker = jobSeeker,
                            onClick = {
                                // Safe navigation with valid ID check
                                val validId = jobSeeker.jobSeekerId.ifEmpty {
                                    jobSeeker.email.ifEmpty {
                                        null
                                    }
                                }
                                if (validId != null) {
                                    // Navigate to job seeker profile
                                    // You can add navigation to JobSeekerProfileActivity here
                                } else {
                                    // Handle invalid ID case
                                    Toast.makeText(context, "Unable to open profile", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun JobSeekerProfileCard(
    jobSeeker: JobSeekerModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0xFF6366F1).copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Job Seeker Avatar - Enhanced Circular Profile Image
            Box(
                modifier = Modifier
                    .size(75.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring with gradient border effect
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFEC4899), Color(0xFFF43F5E))
                            ),
                            shape = CircleShape
                        )
                        .padding(2.dp), // Creates the border effect
                    contentAlignment = Alignment.Center
                ) {
                    // Inner circle for the actual profile image
                    Box(
                        modifier = Modifier
                            .size(71.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val profilePhotoUrl = jobSeeker.profilePhoto.orEmpty()
                        if (profilePhotoUrl.isNotEmpty()) {
                            // Load job seeker profile image from URL using Coil
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profilePhotoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Job Seeker Profile Image",
                                modifier = Modifier
                                    .size(71.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop, // Ensures the image fills the entire circular area and crops any excess
                                loading = {
                                    // Show loading indicator while image loads
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color(0xFF6366F1)
                                        )
                                    }
                                },
                                error = {
                                    // Show fallback with gradient background
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                                ),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = jobSeeker.fullName.orEmpty().firstOrNull()?.toString()?.uppercase() ?: "?",
                                            style = TextStyle(
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                },
                                success = {
                                    SubcomposeAsyncImageContent()
                                }
                            )
                        } else {
                            // Show first letter with gradient background if no image available
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = jobSeeker.fullName.orEmpty().firstOrNull()?.toString()?.uppercase() ?: "?",
                                    style = TextStyle(
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Job Seeker Details - Enhanced Typography
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = jobSeeker.fullName.orEmpty().ifEmpty { "Unknown User" },
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        letterSpacing = 0.3.sp
                    )
                )
            }

            // Enhanced Arrow Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color(0xFF6366F1).copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "View Profile",
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer(rotationZ = 180f), // Rotate to point right
                    tint = Color(0xFF6366F1)
                )
            }
        }
    }
}

@Composable
fun CompanyProfileCard(
    company: CompanyModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0xFF6366F1).copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Company Logo - Perfectly Circular Profile Image
            Box(
                modifier = Modifier
                    .size(75.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring with gradient border effect
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFEC4899), Color(0xFFF43F5E))
                            ),
                            shape = CircleShape
                        )
                        .padding(2.dp), // Creates the border effect
                    contentAlignment = Alignment.Center
                ) {
                    // Inner circle for the actual profile image
                    Box(
                        modifier = Modifier
                            .size(71.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (company.companyProfileImage.isNotEmpty()) {
                            // Load company profile image from URL using Coil
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(company.companyProfileImage)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Company Profile Image",
                                modifier = Modifier
                                    .size(71.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    // Show loading indicator while image loads
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color(0xFF6366F1)
                                        )
                                    }
                                },
                                error = {
                                    // Show fallback with gradient background
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                                ),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = company.companyName.first().toString().uppercase(),
                                            style = TextStyle(
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                },
                                success = {
                                    SubcomposeAsyncImageContent()
                                }
                            )
                        } else {
                            // Show first letter with gradient background if no image available
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = company.companyName.first().toString().uppercase(),
                                    style = TextStyle(
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Company Details - Enhanced Typography
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = company.companyName,
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Location with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = company.companyLocation.ifEmpty { "Location not available" },
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Company establishment year if available
                if (company.companyEstablishedDate.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF9CA3AF)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Founded ${company.companyEstablishedDate.take(4)}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF),
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFF6366F1).copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "View Company",
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer(rotationZ = 180f), // Rotate to point right
                    tint = Color(0xFF6366F1)
                )
            }
        }
    }
}