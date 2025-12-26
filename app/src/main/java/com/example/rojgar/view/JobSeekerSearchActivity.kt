//package com.example.rojgar.view
//
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.rojgar.R
//import com.example.rojgar.model.CompanyModel
//import com.example.rojgar.model.JobModel
//import com.example.rojgar.repository.CompanyRepoImpl
//import com.example.rojgar.repository.JobRepoImpl
//import com.example.rojgar.ui.theme.*
//import com.example.rojgar.viewmodel.CompanyViewModel
//import com.example.rojgar.viewmodel.JobViewModel
//import com.google.gson.Gson
//
//class JobSeekerSearchActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        // Get search query and filter from intent
//        val searchQuery = intent.getStringExtra("searchQuery") ?: ""
//        val filterStateJson = intent.getStringExtra("filterState")
//        val filterState = if (filterStateJson != null) {
//            Gson().fromJson(filterStateJson, JobFilterState::class.java)
//        } else {
//            JobFilterState()
//        }
//
//        setContent {
//            RojgarTheme {
//                JobSeekerSearchScreen(
//                    initialSearchQuery = searchQuery,
//                    initialFilterState = filterState,
//                    onBackClick = { finish() }
//                )
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun JobSeekerSearchScreen(
//    initialSearchQuery: String,
//    initialFilterState: JobFilterState,
//    onBackClick: () -> Unit
//) {
//    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
//    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
//    val context = LocalContext.current
//
//    var searchQuery by remember { mutableStateOf(initialSearchQuery) }
//    var filterState by remember { mutableStateOf(initialFilterState) }
//    var showFilterSheet by remember { mutableStateOf(false) }
//    var activeFiltersCount by remember {
//        mutableStateOf(countActiveFilters(initialFilterState))
//    }
//
//    var allJobs by remember { mutableStateOf<List<JobModel>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(true) }
//    var jobPostsWithCompany by remember { mutableStateOf<List<JobPostWithCompany>>(emptyList()) }
//
//    // Map to store company details by companyId
//    var companyDetailsMap by remember { mutableStateOf<Map<String, CompanyModel>>(emptyMap()) }
//
//    // Observe company details - each time a company is fetched, update the map
//    val companyDetails by companyViewModel.companyDetails.observeAsState()
//
//    // Update company details map when new company details are received
//    LaunchedEffect(companyDetails) {
//        companyDetails?.let { company ->
//            companyDetailsMap = companyDetailsMap + (company.companyId to company)
//        }
//    }
//
//    // Update job posts when company details map changes
//    LaunchedEffect(companyDetailsMap, allJobs) {
//        if (allJobs.isNotEmpty()) {
//            jobPostsWithCompany = allJobs.map { job ->
//                val companyInfo = companyDetailsMap[job.companyId]
//                JobPostWithCompany(
//                    jobPost = job,
//                    companyName = companyInfo?.companyName ?: "",
//                    companyProfile = companyInfo?.companyProfileImage ?: "",
//                    isLoading = companyInfo == null
//                )
//            }
//        }
//    }
//
//    // Fetch all jobs
//    LaunchedEffect(Unit) {
//        jobViewModel.getAllJobPosts { success, message, posts ->
//            if (!success || posts == null) {
//                isLoading = false
//                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//                return@getAllJobPosts
//            }
//
//            // Filter out expired jobs
//            val activePosts = posts.filter { !isDeadlineExpired(it.deadline) }
//            allJobs = activePosts
//
//            isLoading = false
//
//            // Get unique company IDs and fetch company details
//            val uniqueCompanyIds = activePosts.map { it.companyId }.distinct()
//            uniqueCompanyIds.forEach { companyId ->
//                companyViewModel.getCompanyDetails(companyId)
//            }
//        }
//    }
//
//    // Filter jobs based on search query and filter state
//    val filteredJobs = remember(allJobs, searchQuery, filterState, companyDetailsMap) {
//        allJobs.filter { job ->
//            // Get company name from companyDetailsMap
//            val companyName = companyDetailsMap[job.companyId]?.companyName ?: ""
//
//            // Search query filter (search in title, position, description, skills, and company name)
//            val matchesSearch = searchQuery.isEmpty() ||
//                job.title.contains(searchQuery, ignoreCase = true) ||
//                job.position.contains(searchQuery, ignoreCase = true) ||
//                job.jobDescription.contains(searchQuery, ignoreCase = true) ||
//                job.skills.contains(searchQuery, ignoreCase = true) ||
//                companyName.contains(searchQuery, ignoreCase = true)
//
//            // Category filter
//            val matchesCategories = filterState.selectedCategories.isEmpty() ||
//                filterState.selectedCategories.any { category ->
//                    job.categories.contains(category)
//                }
//
//            // Job type filter
//            val matchesJobType = filterState.selectedJobTypes.isEmpty() ||
//                filterState.selectedJobTypes.contains(job.jobType)
//
//            // Experience filter
//            val matchesExperience = filterState.selectedExperience.isEmpty() ||
//                job.experience.contains(filterState.selectedExperience, ignoreCase = true)
//
//            // Education filter
//            val matchesEducation = filterState.selectedEducation.isEmpty() ||
//                filterState.selectedEducation.any { education ->
//                    job.education.contains(education, ignoreCase = true)
//                }
//
//            // Salary filter
//            val matchesSalary = if (filterState.minSalary.isNotEmpty() || filterState.maxSalary.isNotEmpty()) {
//                try {
//                    val jobSalary = extractSalaryValue(job.salary)
//                    val minSalary = if (filterState.minSalary.isNotEmpty()) {
//                        filterState.minSalary.replace(",", "").toDoubleOrNull() ?: 0.0
//                    } else 0.0
//                    val maxSalary = if (filterState.maxSalary.isNotEmpty()) {
//                        filterState.maxSalary.replace(",", "").toDoubleOrNull() ?: Double.MAX_VALUE
//                    } else Double.MAX_VALUE
//
//                    jobSalary in minSalary..maxSalary
//                } catch (e: Exception) {
//                    true // If parsing fails, include the job
//                }
//            } else {
//                true
//            }
//
//            // Location filter (if JobModel has location field - for now we'll skip as it's not in the model)
//            // Note: JobModel doesn't have a location field, so we'll skip this filter
//            // If you add location to JobModel, uncomment below:
//            // val matchesLocation = filterState.location.isEmpty() ||
//            //     job.location.contains(filterState.location, ignoreCase = true)
//
//            matchesSearch && matchesCategories && matchesJobType &&
//                matchesExperience && matchesEducation && matchesSalary
//        }
//    }
//
//    // Map filtered jobs to JobPostWithCompany
//    val filteredJobsWithCompany = remember(filteredJobs, jobPostsWithCompany) {
//        filteredJobs.map { job ->
//            jobPostsWithCompany.find { it.jobPost.postId == job.postId }
//                ?: JobPostWithCompany(jobPost = job, isLoading = true)
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Blue,
//                    navigationIconContentColor = Color.Black,
//                    titleContentColor = Color.Black
//                ),
//                title = { Text("Search Jobs") },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Back"
//                        )
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Blue)
//                .padding(padding)
//        ) {
//            // Search Bar with Filter Button
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(20.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                OutlinedTextField(
//                    value = searchQuery,
//                    onValueChange = { searchQuery = it },
//                    placeholder = {
//                        Text(
//                            "Search jobs",
//                            style = TextStyle(
//                                fontSize = 16.sp,
//                                color = Color.Gray
//                            )
//                        )
//                    },
//                    leadingIcon = {
//                        Icon(
//                            painter = painterResource(R.drawable.searchicon),
//                            contentDescription = null,
//                            modifier = Modifier.size(20.dp),
//                            tint = Gray
//                        )
//                    },
//                    trailingIcon = {
//                        if (searchQuery.isNotEmpty()) {
//                            IconButton(onClick = { searchQuery = "" }) {
//                                Icon(
//                                    imageVector = Icons.Default.Close,
//                                    contentDescription = "Clear",
//                                    modifier = Modifier.size(20.dp),
//                                    tint = Gray
//                                )
//                            }
//                        }
//                    },
//                    shape = RoundedCornerShape(15.dp),
//                    colors = TextFieldDefaults.colors(
//                        focusedContainerColor = White,
//                        unfocusedContainerColor = White,
//                        focusedIndicatorColor = NormalBlue,
//                        unfocusedIndicatorColor = Color.Transparent
//                    ),
//                    modifier = Modifier
//                        .height(56.dp)
//                        .weight(1f)
//                )
//
//                Spacer(modifier = Modifier.width(12.dp))
//
//                // Filter Button with Badge
//                BadgedBox(
//                    badge = {
//                        if (activeFiltersCount > 0) {
//                            Badge(
//                                containerColor = Purple
//                            ) {
//                                Text(
//                                    text = activeFiltersCount.toString(),
//                                    color = Color.White,
//                                    fontSize = 12.sp
//                                )
//                            }
//                        }
//                    }
//                ) {
//                    Button(
//                        onClick = { showFilterSheet = true },
//                        shape = RoundedCornerShape(12.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Purple
//                        ),
//                        contentPadding = PaddingValues(0.dp),
//                        modifier = Modifier
//                            .height(56.dp)
//                            .width(56.dp)
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.filter),
//                            contentDescription = "Filter",
//                            tint = Color.White,
//                            modifier = Modifier.size(24.dp)
//                        )
//                    }
//                }
//            }
//
//            // Active Filters Display
//            if (activeFiltersCount > 0) {
//                ActiveFiltersChips(
//                    filterState = filterState,
//                    onClearFilter = { updatedFilter ->
//                        filterState = updatedFilter
//                        activeFiltersCount = countActiveFilters(updatedFilter)
//                    }
//                )
//            }
//
//            // Search Results
//            if (isLoading) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(20.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            } else {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(horizontal = 20.dp),
//                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    item {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 12.dp),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                "Search Results",
//                                style = TextStyle(
//                                    fontSize = 20.sp,
//                                    fontWeight = FontWeight.Bold
//                                )
//                            )
//                            Text(
//                                "${filteredJobsWithCompany.size} jobs found",
//                                style = TextStyle(
//                                    fontSize = 14.sp,
//                                    color = Color.Gray
//                                )
//                            )
//                        }
//                    }
//
//                    if (filteredJobsWithCompany.isEmpty()) {
//                        item {
//                            Card(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(120.dp),
//                                colors = CardDefaults.cardColors(
//                                    containerColor = White
//                                ),
//                                shape = RoundedCornerShape(12.dp)
//                            ) {
//                                Box(
//                                    modifier = Modifier.fillMaxSize(),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text(
//                                        "No jobs found. Please adjust your search or filters.",
//                                        style = TextStyle(
//                                            fontSize = 16.sp,
//                                            color = Color.Gray
//                                        )
//                                    )
//                                }
//                            }
//                        }
//                    } else {
//                        items(filteredJobsWithCompany, key = { it.jobPost.postId }) { jobWithCompany ->
//                            JobSeekerPostCard(
//                                jobPostWithCompany = jobWithCompany,
//                                onClick = { },
//                                isSaved = false,
//                                onSaveClick = { postId ->
//                                    Toast.makeText(context, "Job saved", Toast.LENGTH_SHORT).show()
//                                },
//                                onShareClick = { job ->
//                                    val shareIntent = android.content.Intent().apply {
//                                        action = android.content.Intent.ACTION_SEND
//                                        putExtra(
//                                            android.content.Intent.EXTRA_TEXT,
//                                            "Check out this job: ${job.title}\n" +
//                                                    "Company: ${jobWithCompany.companyName}\n" +
//                                                    "Position: ${job.position}\n" +
//                                                    "Type: ${job.jobType}\n" +
//                                                    "Deadline: ${job.deadline}"
//                                        )
//                                        type = "text/plain"
//                                    }
//                                    context.startActivity(
//                                        android.content.Intent.createChooser(shareIntent, "Share Job")
//                                    )
//                                }
//                            )
//                        }
//                    }
//
//                    item {
//                        Spacer(modifier = Modifier.height(20.dp))
//                    }
//                }
//            }
//        }
//
//        // Filter Bottom Sheet
//        JobFilterBottomSheet(
//            showFilter = showFilterSheet,
//            onDismiss = { showFilterSheet = false },
//            onApplyFilter = { newFilterState ->
//                filterState = newFilterState
//                activeFiltersCount = countActiveFilters(newFilterState)
//            },
//            initialFilterState = filterState
//        )
//    }
//}
//
//@Composable
//fun ActiveFiltersChips(
//    filterState: JobFilterState,
//    onClearFilter: (JobFilterState) -> Unit
//) {
//    LazyRow(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 20.dp),
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        // Clear All Filter
//        item {
//            FilterChip(
//                selected = true,
//                onClick = { onClearFilter(JobFilterState()) },
//                label = { Text("Clear All") },
//                leadingIcon = {
//                    Icon(
//                        imageVector = Icons.Default.Close,
//                        contentDescription = null,
//                        modifier = Modifier.size(16.dp)
//                    )
//                },
//                colors = FilterChipDefaults.filterChipColors(
//                    selectedContainerColor = Purple,
//                    selectedLabelColor = Color.White
//                )
//            )
//        }
//
//        // Category Filters
//        filterState.selectedCategories.forEach { category ->
//            item {
//                FilterChip(
//                    selected = true,
//                    onClick = {
//                        onClearFilter(
//                            filterState.copy(
//                                selectedCategories = filterState.selectedCategories - category
//                            )
//                        )
//                    },
//                    label = { Text(category) },
//                    trailingIcon = {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp)
//                        )
//                    },
//                    colors = FilterChipDefaults.filterChipColors(
//                        selectedContainerColor = NormalBlue,
//                        selectedLabelColor = Color.White
//                    )
//                )
//            }
//        }
//
//        // Job Type Filters
//        filterState.selectedJobTypes.forEach { jobType ->
//            item {
//                FilterChip(
//                    selected = true,
//                    onClick = {
//                        onClearFilter(
//                            filterState.copy(
//                                selectedJobTypes = filterState.selectedJobTypes - jobType
//                            )
//                        )
//                    },
//                    label = { Text(jobType) },
//                    trailingIcon = {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp)
//                        )
//                    },
//                    colors = FilterChipDefaults.filterChipColors(
//                        selectedContainerColor = NormalBlue,
//                        selectedLabelColor = Color.White
//                    )
//                )
//            }
//        }
//
//        // Experience Filter
//        if (filterState.selectedExperience.isNotEmpty()) {
//            item {
//                FilterChip(
//                    selected = true,
//                    onClick = {
//                        onClearFilter(filterState.copy(selectedExperience = ""))
//                    },
//                    label = { Text(filterState.selectedExperience) },
//                    trailingIcon = {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp)
//                        )
//                    },
//                    colors = FilterChipDefaults.filterChipColors(
//                        selectedContainerColor = NormalBlue,
//                        selectedLabelColor = Color.White
//                    )
//                )
//            }
//        }
//
//        // Education Filters
//        filterState.selectedEducation.forEach { education ->
//            item {
//                FilterChip(
//                    selected = true,
//                    onClick = {
//                        onClearFilter(
//                            filterState.copy(
//                                selectedEducation = filterState.selectedEducation - education
//                            )
//                        )
//                    },
//                    label = { Text(education) },
//                    trailingIcon = {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp)
//                        )
//                    },
//                    colors = FilterChipDefaults.filterChipColors(
//                        selectedContainerColor = NormalBlue,
//                        selectedLabelColor = Color.White
//                    )
//                )
//            }
//        }
//
//        // Salary Filter
//        if (filterState.minSalary.isNotEmpty() || filterState.maxSalary.isNotEmpty()) {
//            item {
//                FilterChip(
//                    selected = true,
//                    onClick = {
//                        onClearFilter(
//                            filterState.copy(minSalary = "", maxSalary = "")
//                        )
//                    },
//                    label = {
//                        Text("Salary: ${filterState.minSalary}-${filterState.maxSalary}")
//                    },
//                    trailingIcon = {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp)
//                        )
//                    },
//                    colors = FilterChipDefaults.filterChipColors(
//                        selectedContainerColor = NormalBlue,
//                        selectedLabelColor = Color.White
//                    )
//                )
//            }
//        }
//
//        // Location Filter
//        if (filterState.location.isNotEmpty()) {
//            item {
//                FilterChip(
//                    selected = true,
//                    onClick = {
//                        onClearFilter(filterState.copy(location = ""))
//                    },
//                    label = { Text(filterState.location) },
//                    trailingIcon = {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp)
//                        )
//                    },
//                    colors = FilterChipDefaults.filterChipColors(
//                        selectedContainerColor = NormalBlue,
//                        selectedLabelColor = Color.White
//                    )
//                )
//            }
//        }
//    }
//}
//
//// Helper function to count active filters
//fun countActiveFilters(filterState: JobFilterState): Int {
//    var count = 0
//    count += filterState.selectedCategories.size
//    count += filterState.selectedJobTypes.size
//    if (filterState.selectedExperience.isNotEmpty()) count++
//    count += filterState.selectedEducation.size
//    if (filterState.minSalary.isNotEmpty() || filterState.maxSalary.isNotEmpty()) count++
//    if (filterState.location.isNotEmpty()) count++
//    return count
//}
//
//// Helper function to extract numeric value from salary string
//fun extractSalaryValue(salary: String): Double {
//    if (salary.isEmpty()) return 0.0
//    return try {
//        // Remove common non-numeric characters and extract first number
//        val cleaned = salary.replace(",", "").replace("Rs.", "").replace("NPR", "")
//            .replace("/", "").replace("-", "").trim()
//        val numbers = cleaned.filter { it.isDigit() || it == '.' }
//        numbers.toDoubleOrNull() ?: 0.0
//    } catch (e: Exception) {
//        0.0
//    }
//}