package com.example.rojgar.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.rojgar.model.*
import com.example.rojgar.repository.*
import com.example.rojgar.view.ui.theme.RojgarTheme
import com.example.rojgar.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import java.util.*

// Company-specific filter state
data class CompanyFilterState(
    val selectedIndustries: List<String> = emptyList(),
    val selectedCompanyTypes: List<String> = emptyList(),
    val selectedEmployeeSize: String = "",
    val selectedLocation: String = "",
    val foundedYearMin: String = "",
    val foundedYearMax: String = ""
)

class CompanySearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val searchQuery = intent.getStringExtra("searchQuery") ?: ""
        val filterStateJson = intent.getStringExtra("filterState")
        val filterState = if (filterStateJson != null) {
            Gson().fromJson(filterStateJson, CompanyFilterState::class.java)
        } else {
            CompanyFilterState()
        }

        setContent {
            RojgarTheme {
                CompanySearchScreen(
                    initialSearchQuery = searchQuery,
                    initialFilterState = filterState,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CompanySearchScreen(
    initialSearchQuery: String,
    initialFilterState: CompanyFilterState,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val searchViewModel = remember { SearchViewModel(SearchRepoImpl()) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Companies", "Job Seekers")

    var searchQuery by remember { mutableStateOf(initialSearchQuery) }
    var filterState by remember { mutableStateOf(initialFilterState) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSearchHistory by remember { mutableStateOf(false) }
    val activeFiltersCount = remember(filterState) { countActiveCompanyFilters(filterState) }

    var allCompanies by remember { mutableStateOf<List<CompanyModel>>(emptyList()) }
    var allJobSeekers by remember { mutableStateOf<List<JobSeekerModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isJobSeekersLoading by remember { mutableStateOf(true) }

    val searchHistory by searchViewModel.recentSearches.observeAsState(emptyList())

    var lastSavedSearch by remember { mutableStateOf("") }
    var lastSavedTab by remember { mutableStateOf(-1) }
    var searchHistoryRefreshTrigger by remember { mutableStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    LaunchedEffect(currentUser?.uid, searchHistoryRefreshTrigger) {
        currentUser?.uid?.let { userId ->
            searchViewModel.getRecentSearches(userId, 10)
        }
    }

    LaunchedEffect(Unit) {
        // Load companies
        companyViewModel.getAllCompany { success, message, companies ->
            if (!success || companies == null) {
                isLoading = false
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                return@getAllCompany
            }

            allCompanies = companies
            isLoading = false
        }

        // Load job seekers
        jobSeekerViewModel.getAllJobSeeker { success, message, jobSeekers ->
            if (success && jobSeekers != null) {
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

    LaunchedEffect(searchQuery, selectedTabIndex) {
        if (searchQuery.isNotEmpty() && currentUser?.uid != null) {
            val isDifferentSearch = searchQuery != lastSavedSearch || selectedTabIndex != lastSavedTab

            if (isDifferentSearch) {
                delay(1000)

                val searchType = tabs[selectedTabIndex]
                val search = SearchHistoryModel(
                    userId = currentUser.uid,
                    userType = "JobSeeker",
                    query = searchQuery,
                    searchType = searchType,
                    filterState = FilterStateData(
                        selectedCategories = filterState.selectedIndustries,
                        selectedJobTypes = emptyList(),
                        selectedExperience = "",
                        selectedEducation = emptyList(),
                        minSalary = "",
                        maxSalary = "",
                        location = filterState.selectedLocation
                    )
                )

                searchViewModel.saveSearch(search) { success, message ->
                    if (success) {
                        lastSavedSearch = searchQuery
                        lastSavedTab = selectedTabIndex
                        searchHistoryRefreshTrigger++
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF60A5FA),
                                Color(0xFF3B82F6),
                                Color(0xFF2563EB)
                            )
                        )
                    )
            ) {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = Color.White,
                        titleContentColor = Color.White
                    ),
                    title = {
                        Text(
                            "Search",
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        BadgedBox(
                            badge = {
                                if (searchHistory.isNotEmpty()) {
                                    Badge(
                                        containerColor = Color(0xFFEF4444),
                                        modifier = Modifier.scale(0.8f)
                                    ) {
                                        Text(
                                            text = searchHistory.size.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            IconButton(
                                onClick = { showSearchHistory = !showSearchHistory },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (showSearchHistory)
                                            Color.White.copy(alpha = 0.3f)
                                        else
                                            Color.White.copy(alpha = 0.2f)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Search History",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFEFF6FF),
                            Color(0xFFDBEAFE),
                            Color(0xFFBFDBFE)
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Tab Row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF2563EB),
                        indicator = { tabPositions ->
                            Box(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF3B82F6),
                                                Color(0xFF60A5FA)
                                            )
                                        )
                                    )
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                AnimatedContent(
                                    targetState = selectedTabIndex == index,
                                    transitionSpec = {
                                        fadeIn() + scaleIn() with fadeOut() + scaleOut()
                                    },
                                    label = "tabAnimation"
                                ) { isSelected ->
                                    Text(
                                        title,
                                        style = TextStyle(
                                            fontSize = if (isSelected) 16.sp else 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Search History Panel
                AnimatedVisibility(
                    visible = showSearchHistory && searchHistory.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SearchHistoryCard(
                        searchHistory = searchHistory,
                        onSearchClick = { historyItem ->
                            searchQuery = historyItem.query

                            val targetTabIndex = when (historyItem.searchType) {
                                "Companies" -> 0
                                "Job Seekers" -> 1
                                else -> 0
                            }
                            selectedTabIndex = targetTabIndex

                            historyItem.filterState?.let { savedFilters ->
                                filterState = CompanyFilterState(
                                    selectedIndustries = savedFilters.selectedCategories,
                                    selectedLocation = savedFilters.location
                                )
                            }

                            showSearchHistory = false
                        },
                        onClearHistory = {
                            currentUser?.uid?.let { userId ->
                                searchViewModel.clearAllSearchHistory(userId) { success, message ->
                                    if (success) {
                                        searchHistoryRefreshTrigger++
                                    }
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onDeleteItem = { historyItem ->
                            currentUser?.uid?.let { userId ->
                                searchViewModel.deleteSearchHistory(
                                    userId = userId,
                                    timestamp = historyItem.timestamp
                                ) { success, message ->
                                    if (success) {
                                        searchHistoryRefreshTrigger++
                                    }
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }

                when (selectedTabIndex) {
                    0 -> CompaniesTabContent(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        filterState = filterState,
                        onFilterStateChange = { filterState = it },
                        showFilterSheet = showFilterSheet,
                        onShowFilterSheetChange = { showFilterSheet = it },
                        allCompanies = allCompanies,
                        isLoading = isLoading,
                        context = context,
                        activeFiltersCount = activeFiltersCount,
                        shimmerAlpha = shimmerAlpha
                    )
                    1 -> JobSeekersTabContent(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        allJobSeekers = allJobSeekers,
                        isLoading = isJobSeekersLoading,
                        context = context
                    )
                }
            }

            // Filter Bottom Sheet (only for Companies tab)
            if (selectedTabIndex == 0) {
                CompanyFilterBottomSheet(
                    showFilter = showFilterSheet,
                    onDismiss = { showFilterSheet = false },
                    onApplyFilter = { filterState = it },
                    initialFilterState = filterState
                )
            }
        }
    }
}

@Composable
fun CompaniesTabContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterState: CompanyFilterState,
    onFilterStateChange: (CompanyFilterState) -> Unit,
    showFilterSheet: Boolean,
    onShowFilterSheetChange: (Boolean) -> Unit,
    allCompanies: List<CompanyModel>,
    isLoading: Boolean,
    context: Context,
    activeFiltersCount: Int,
    shimmerAlpha: Float
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search and Filter Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            "Search companies",
                            style = TextStyle(
                                fontSize = 15.sp,
                                color = Color(0xFF94A3B8)
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.searchicon),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = Color(0xFF3B82F6)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedIndicatorColor = Color(0xFF3B82F6),
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                BadgedBox(
                    badge = {
                        if (activeFiltersCount > 0) {
                            Badge(
                                containerColor = Color(0xFFEF4444),
                                modifier = Modifier.scale(1.2f)
                            ) {
                                Text(
                                    text = activeFiltersCount.toString(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Button(
                        onClick = { onShowFilterSheetChange(true) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .width(56.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = Color(0xFF3B82F6).copy(alpha = 0.4f)
                            )
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
        }

        // Active Filters Chips
        AnimatedVisibility(
            visible = activeFiltersCount > 0,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            ActiveCompanyFiltersChips(
                filterState = filterState,
                onClearFilter = { onFilterStateChange(it) }
            )
        }

        // Companies List
        CompaniesContent(
            searchQuery = searchQuery,
            filterState = filterState,
            allCompanies = allCompanies,
            isLoading = isLoading,
            context = context
        )
    }
}

@Composable
fun CompaniesContent(
    searchQuery: String,
    filterState: CompanyFilterState,
    allCompanies: List<CompanyModel>,
    isLoading: Boolean,
    context: Context
) {
    val filteredCompanies = remember(allCompanies, searchQuery, filterState) {
        val normalizedSearchQuery = searchQuery.trim().lowercase()

        allCompanies.filter { company ->
            val matchesSearch = if (normalizedSearchQuery.isEmpty()) {
                true
            } else {
                val searchableFields = listOf(
                    company.companyName,
                    company.companyLocation,
                    company.companyInformation,
                    company.companyIndustry ?: "",
                ).map { it.lowercase() }

                searchableFields.any { field ->
                    field.contains(normalizedSearchQuery)
                }
            }

            val matchesIndustry = filterState.selectedIndustries.isEmpty() ||
                    (company.companyIndustry?.let { industry ->
                        filterState.selectedIndustries.any { selectedIndustry ->
                            industry.contains(selectedIndustry, ignoreCase = true)
                        }
                    } ?: false)

            val matchesLocation = filterState.selectedLocation.isEmpty() ||
                    company.companyLocation.contains(filterState.selectedLocation, ignoreCase = true)

            val matchesFoundedYear = when {
                filterState.foundedYearMin.isEmpty() && filterState.foundedYearMax.isEmpty() -> true
                company.companyEstablishedDate.isEmpty() -> false
                else -> {
                    val year = company.companyEstablishedDate.take(4).toIntOrNull() ?: 0
                    val minYear = filterState.foundedYearMin.toIntOrNull() ?: 0
                    val maxYear = filterState.foundedYearMax.toIntOrNull() ?: Int.MAX_VALUE
                    year in minYear..maxYear
                }
            }

            matchesSearch && matchesIndustry  &&
                    matchesLocation  && matchesFoundedYear
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF3B82F6))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
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
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    )
                    Text(
                        "${filteredCompanies.size} companies found",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    )
                }
            }

            if (filteredCompanies.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No companies found",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Try adjusting your search or filters",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color(0xFF64748B)
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredCompanies, key = { it.companyId }) { company ->
                    CompanyProfileCard(
                        company = company,
                        onClick = {
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

@Composable
fun JobSeekersTabContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    allJobSeekers: List<JobSeekerModel>,
    isLoading: Boolean,
    context: Context
) {
    val filteredJobSeekers = remember(allJobSeekers, searchQuery) {
        val normalizedSearchQuery = searchQuery.trim().lowercase()

        if (normalizedSearchQuery.isEmpty()) {
            allJobSeekers
        } else {
            val searchKeywords = normalizedSearchQuery.split(" ").filter { it.isNotBlank() }

            allJobSeekers.filter { jobSeeker ->
                val searchableFields = listOf(
                    jobSeeker.fullName.orEmpty(),
                    jobSeeker.email.orEmpty(),
                    jobSeeker.profession.orEmpty(),
                    jobSeeker.bio.orEmpty()
                ).map { it.lowercase() }

                searchKeywords.any { keyword ->
                    searchableFields.any { field ->
                        field.contains(keyword)
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    "Search job seekers",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color(0xFF94A3B8)
                    )
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.searchicon),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color(0xFF3B82F6)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFF3B82F6),
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .height(56.dp)
        )

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
                                .padding(vertical = 20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No job seekers found",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Try a different search term",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B)
                                    )
                                )
                            }
                        }
                    }
                } else {
                    items(
                        filteredJobSeekers,
                        key = { jobSeeker ->
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
                                val validId = jobSeeker.jobSeekerId.ifEmpty {
                                    jobSeeker.email.ifEmpty { null }
                                }
                                if (validId != null) {
                                    val intent = Intent(context, JobSeekerProfileActivity::class.java).apply {
                                        putExtra("JOB_SEEKER_ID", validId)
                                    }
                                    context.startActivity(intent)
                                } else {
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
fun ActiveCompanyFiltersChips(
    filterState: CompanyFilterState,
    onClearFilter: (CompanyFilterState) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = true,
                onClick = { onClearFilter(CompanyFilterState()) },
                label = { Text("Clear All", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFEF4444),
                    selectedLabelColor = Color.White
                ),
                border = null
            )
        }

        filterState.selectedIndustries.forEach { industry ->
            item {
                AnimatedCompanyFilterChip(
                    label = industry,
                    onRemove = { onClearFilter(filterState.copy(selectedIndustries = filterState.selectedIndustries - industry)) }
                )
            }
        }

        filterState.selectedCompanyTypes.forEach { companyType ->
            item {
                AnimatedCompanyFilterChip(
                    label = companyType,
                    onRemove = { onClearFilter(filterState.copy(selectedCompanyTypes = filterState.selectedCompanyTypes - companyType)) }
                )
            }
        }

        if (filterState.selectedEmployeeSize.isNotEmpty()) {
            item {
                AnimatedCompanyFilterChip(
                    label = filterState.selectedEmployeeSize,
                    onRemove = { onClearFilter(filterState.copy(selectedEmployeeSize = "")) }
                )
            }
        }

        if (filterState.selectedLocation.isNotEmpty()) {
            item {
                AnimatedCompanyFilterChip(
                    label = filterState.selectedLocation,
                    onRemove = { onClearFilter(filterState.copy(selectedLocation = "")) }
                )
            }
        }

        if (filterState.foundedYearMin.isNotEmpty() || filterState.foundedYearMax.isNotEmpty()) {
            item {
                AnimatedCompanyFilterChip(
                    label = "${filterState.foundedYearMin}${if (filterState.foundedYearMax.isNotEmpty()) "-${filterState.foundedYearMax}" else "+"}",
                    onRemove = { onClearFilter(filterState.copy(foundedYearMin = "", foundedYearMax = "")) }
                )
            }
        }
    }
}

@Composable
fun AnimatedCompanyFilterChip(
    label: String,
    onRemove: () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        FilterChip(
            selected = true,
            onClick = {
                isVisible = false
                onRemove()
            },
            label = { Text(label, fontSize = 13.sp) },
            trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF3B82F6),
                selectedLabelColor = Color.White
            ),
            border = null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyFilterBottomSheet(
    showFilter: Boolean,
    onDismiss: () -> Unit,
    onApplyFilter: (CompanyFilterState) -> Unit,
    initialFilterState: CompanyFilterState
) {
    if (!showFilter) return

    val scrollState = rememberScrollState()
    var tempFilterState by remember(initialFilterState) { mutableStateOf(initialFilterState) }

    val industryOptions = listOf(
        "IT & Telecommunication",
        "Software Development",
        "Banking & Finance",
        "Healthcare",
        "Education",
        "Retail & E-commerce",
        "Manufacturing",
        "Construction",
        "Hospitality",
        "Consulting"
    )

    val companyTypeOptions = listOf(
        "Private Limited",
        "Public Limited",
        "Partnership",
        "Sole Proprietorship",
        "Non-Profit",
        "Government",
        "Startup",
        "Multinational"
    )

    val employeeSizeOptions = listOf("1-10", "11-50", "51-200", "201-500", "500+")

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
                text = "Company Filters",
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
            )

            // Industry/Category Filter
            CompanyFilterChipSection(
                title = "Industry/Category",
                options = industryOptions,
                selectedItems = tempFilterState.selectedIndustries,
                onSelectionChange = { tempFilterState = tempFilterState.copy(selectedIndustries = it) }
            )

            // Company Type Filter
            CompanyFilterChipSection(
                title = "Company Type",
                options = companyTypeOptions,
                selectedItems = tempFilterState.selectedCompanyTypes,
                onSelectionChange = { tempFilterState = tempFilterState.copy(selectedCompanyTypes = it) }
            )

            // Employee Size Filter
            CompanySingleSelectFilterChipSection(
                title = "Employee Size",
                options = employeeSizeOptions,
                selected = tempFilterState.selectedEmployeeSize,
                onSelectionChange = { tempFilterState = tempFilterState.copy(selectedEmployeeSize = it) }
            )

            // Location Filter
            Text(text = "Location", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = tempFilterState.selectedLocation,
                onValueChange = { tempFilterState = tempFilterState.copy(selectedLocation = it) },
                placeholder = { Text("e.g. Kathmandu, Pokhara") },
                modifier = Modifier.fillMaxWidth()
            )

            // Founded Year Range
            Text(text = "Founded Year Range", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = tempFilterState.foundedYearMin,
                    onValueChange = { tempFilterState = tempFilterState.copy(foundedYearMin = it) },
                    placeholder = { Text("Min Year") },
                    label = { Text("From") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = tempFilterState.foundedYearMax,
                    onValueChange = { tempFilterState = tempFilterState.copy(foundedYearMax = it) },
                    placeholder = { Text("Max Year") },
                    label = { Text("To") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { tempFilterState = CompanyFilterState() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CompanyFilterChipSection(
    title: String,
    options: List<String>,
    selectedItems: List<String>,
    onSelectionChange: (List<String>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val selected = selectedItems.contains(option)
                FilterChip(
                    selected = selected,
                    onClick = {
                        onSelectionChange(if (selected) selectedItems - option else selectedItems + option)
                    },
                    label = { Text(option, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF3B82F6),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun CompanySingleSelectFilterChipSection(
    title: String,
    options: List<String>,
    selected: String,
    onSelectionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelectionChange(if (selected == option) "" else option) },
                    label = { Text(option, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF3B82F6),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

fun countActiveCompanyFilters(filterState: CompanyFilterState): Int {
    return filterState.selectedIndustries.size +
            filterState.selectedCompanyTypes.size +
            (if (filterState.selectedEmployeeSize.isNotEmpty()) 1 else 0) +
            (if (filterState.selectedLocation.isNotEmpty()) 1 else 0) +
            (if (filterState.foundedYearMin.isNotEmpty() || filterState.foundedYearMax.isNotEmpty()) 1 else 0)
}