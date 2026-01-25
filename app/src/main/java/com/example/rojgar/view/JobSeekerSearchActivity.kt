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
import com.example.rojgar.ui.theme.*
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun JobSeekerSearchScreen(
    initialSearchQuery: String,
    initialFilterState: JobFilterState,
    onBackClick: () -> Unit
) {
    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val searchViewModel = remember { SearchViewModel(SearchRepoImpl()) }
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Jobs", "Companies", "Job Seekers")

    var searchQuery by remember { mutableStateOf(initialSearchQuery) }
    var filterState by remember { mutableStateOf(initialFilterState) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSearchHistory by remember { mutableStateOf(false) }
    val activeFiltersCount = remember(filterState) { countActiveFilters(filterState) }

    var allJobs by remember { mutableStateOf<List<JobModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var companyDetailsMap by remember { mutableStateOf<Map<String, CompanyModel>>(emptyMap()) }

    val companyDetails by companyViewModel.companyDetails.observeAsState()
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

    LaunchedEffect(companyDetails) {
        companyDetails?.let { company ->
            companyDetailsMap = companyDetailsMap + (company.companyId to company)
        }
    }

    LaunchedEffect(Unit) {
        jobViewModel.getAllJobPosts { success, message, posts ->
            if (!success || posts == null) {
                isLoading = false
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                return@getAllJobPosts
            }

            val activePosts = posts.filter { !isDeadlineExpired(it.deadline) }
            allJobs = activePosts
            isLoading = false

            val uniqueCompanyIds = activePosts.map { it.companyId }.distinct()
            uniqueCompanyIds.forEach { companyId ->
                companyViewModel.getCompanyDetails(companyId)
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
                        selectedCategories = filterState.selectedCategories,
                        selectedJobTypes = filterState.selectedJobTypes,
                        selectedExperience = filterState.selectedExperience,
                        selectedEducation = filterState.selectedEducation,
                        minSalary = filterState.minSalary,
                        maxSalary = filterState.maxSalary,
                        location = filterState.location
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
                            "Discover",
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
                                "Jobs" -> 0
                                "Companies" -> 1
                                "Job Seekers" -> 2
                                else -> 0
                            }
                            selectedTabIndex = targetTabIndex

                            historyItem.filterState?.let { savedFilters ->
                                filterState = JobFilterState(
                                    selectedCategories = savedFilters.selectedCategories,
                                    selectedJobTypes = savedFilters.selectedJobTypes,
                                    selectedExperience = savedFilters.selectedExperience,
                                    selectedEducation = savedFilters.selectedEducation,
                                    minSalary = savedFilters.minSalary,
                                    maxSalary = savedFilters.maxSalary,
                                    location = savedFilters.location
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

                if (selectedTabIndex == 0) {
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
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        "Search jobs, companies...",
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
                                        IconButton(onClick = { searchQuery = "" }) {
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
                                    onClick = { showFilterSheet = true },
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

                    AnimatedVisibility(
                        visible = activeFiltersCount > 0,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ActiveFiltersChips(
                            filterState = filterState,
                            onClearFilter = { filterState = it }
                        )
                    }
                }

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
                        activeFiltersCount = activeFiltersCount,
                        shimmerAlpha = shimmerAlpha
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
}

@Composable
fun SearchHistoryCard(
    searchHistory: List<SearchHistoryModel>,
    onSearchClick: (SearchHistoryModel) -> Unit,
    onClearHistory: () -> Unit,
    onDeleteItem: (SearchHistoryModel) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF3B82F6)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Recent Searches",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    )
                }
                TextButton(onClick = onClearHistory) {
                    Text(
                        "Clear All",
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            searchHistory.take(10).forEach { historyItem ->
                SearchHistoryItem(
                    historyItem = historyItem,
                    onClick = { onSearchClick(historyItem) },
                    onDelete = { onDeleteItem(historyItem) }
                )
            }
        }
    }
}

@Composable
fun SearchHistoryItem(
    historyItem: SearchHistoryModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = when (historyItem.searchType) {
                        "Jobs" -> Color(0xFF3B82F6).copy(alpha = 0.1f)
                        "Companies" -> Color(0xFF10B981).copy(alpha = 0.1f)
                        "Job Seekers" -> Color(0xFF8B5CF6).copy(alpha = 0.1f)
                        else -> Color(0xFF64748B).copy(alpha = 0.1f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (historyItem.searchType) {
                    "Jobs" -> Icons.Default.Search
                    "Companies" -> Icons.Default.AccountBox
                    "Job Seekers" -> Icons.Default.Person
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = when (historyItem.searchType) {
                    "Jobs" -> Color(0xFF3B82F6)
                    "Companies" -> Color(0xFF10B981)
                    "Job Seekers" -> Color(0xFF8B5CF6)
                    else -> Color(0xFF64748B)
                }
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = historyItem.query,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = when (historyItem.searchType) {
                        "Jobs" -> Color(0xFF3B82F6).copy(alpha = 0.1f)
                        "Companies" -> Color(0xFF10B981).copy(alpha = 0.1f)
                        "Job Seekers" -> Color(0xFF8B5CF6).copy(alpha = 0.1f)
                        else -> Color(0xFF64748B).copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = historyItem.searchType,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (historyItem.searchType) {
                                "Jobs" -> Color(0xFF3B82F6)
                                "Companies" -> Color(0xFF10B981)
                                "Job Seekers" -> Color(0xFF8B5CF6)
                                else -> Color(0xFF64748B)
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = formatTimestamps(historyItem.timestamp),
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                )
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF94A3B8)
            )
        }

        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Apply Search",
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer(rotationZ = 180f),
            tint = Color(0xFF94A3B8)
        )
    }
}

fun formatTimestamps(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> "Over a week ago"
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = true,
                onClick = { onClearFilter(JobFilterState()) },
                label = { Text("Clear All", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFEF4444),
                    selectedLabelColor = Color.White
                ),
                border = null
            )
        }

        filterState.selectedCategories.forEach { category ->
            item {
                AnimatedFilterChip(
                    label = category,
                    onRemove = { onClearFilter(filterState.copy(selectedCategories = filterState.selectedCategories - category)) }
                )
            }
        }

        filterState.selectedJobTypes.forEach { jobType ->
            item {
                AnimatedFilterChip(
                    label = jobType,
                    onRemove = { onClearFilter(filterState.copy(selectedJobTypes = filterState.selectedJobTypes - jobType)) }
                )
            }
        }

        if (filterState.selectedExperience.isNotEmpty()) {
            item {
                AnimatedFilterChip(
                    label = filterState.selectedExperience,
                    onRemove = { onClearFilter(filterState.copy(selectedExperience = "")) }
                )
            }
        }

        filterState.selectedEducation.forEach { education ->
            item {
                AnimatedFilterChip(
                    label = education,
                    onRemove = { onClearFilter(filterState.copy(selectedEducation = filterState.selectedEducation - education)) }
                )
            }
        }

        if (filterState.minSalary.isNotEmpty() || filterState.maxSalary.isNotEmpty()) {
            item {
                AnimatedFilterChip(
                    label = "₹${filterState.minSalary}-${filterState.maxSalary}",
                    onRemove = { onClearFilter(filterState.copy(minSalary = "", maxSalary = "")) }
                )
            }
        }

        if (filterState.location.isNotEmpty()) {
            item {
                AnimatedFilterChip(
                    label = filterState.location,
                    onRemove = { onClearFilter(filterState.copy(location = "")) }
                )
            }
        }
    }
}

@Composable
fun AnimatedFilterChip(
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

fun countActiveFilters(filterState: JobFilterState): Int {
    return filterState.selectedCategories.size +
            filterState.selectedJobTypes.size +
            (if (filterState.selectedExperience.isNotEmpty()) 1 else 0) +
            filterState.selectedEducation.size +
            (if (filterState.minSalary.isNotEmpty() || filterState.maxSalary.isNotEmpty()) 1 else 0) +
            (if (filterState.location.isNotEmpty()) 1 else 0)
}

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
    context: Context,
    activeFiltersCount: Int,
    shimmerAlpha: Float
) {
    val savedJobViewModel = remember { SavedJobViewModel(SavedJobRepoImpl()) }
    val savedJobIds by savedJobViewModel.savedJobIds.observeAsState(emptySet())

    val filteredJobs = remember(allJobs, searchQuery, filterState, companyDetailsMap) {
        val normalizedSearchQuery = searchQuery.trim().lowercase()

        val hasActiveFilters = normalizedSearchQuery.isNotEmpty() ||
                filterState.selectedCategories.isNotEmpty() ||
                filterState.selectedJobTypes.isNotEmpty() ||
                filterState.selectedExperience.isNotEmpty() ||
                filterState.selectedEducation.isNotEmpty() ||
                filterState.minSalary.isNotEmpty() ||
                filterState.maxSalary.isNotEmpty() ||
                filterState.location.isNotEmpty()

        if (!hasActiveFilters) {
            allJobs
        } else {
            allJobs.filter { job ->
                val companyName = companyDetailsMap[job.companyId]?.companyName ?: ""

                val matchesSearch = if (normalizedSearchQuery.isEmpty()) {
                    true
                } else {
                    val searchKeywords = normalizedSearchQuery.split(" ").filter { it.isNotBlank() }

                    val searchableFields = listOf(
                        job.title,
                        job.position,
                        job.jobDescription,
                        job.skills,
                        companyName,
                        job.categories.joinToString(" "),
                        job.jobType,
                        job.experience,
                        job.education
                    ).map { it.lowercase() }

                    searchKeywords.any { keyword ->
                        searchableFields.any { field ->
                            field.contains(keyword)
                        }
                    }
                }

                val matchesCategories = filterState.selectedCategories.isEmpty() ||
                        filterState.selectedCategories.any { category ->
                            job.categories.any { it.equals(category, ignoreCase = true) }
                        }

                val matchesJobType = filterState.selectedJobTypes.isEmpty() ||
                        filterState.selectedJobTypes.any {
                            job.jobType.equals(it, ignoreCase = true)
                        }

                val matchesExperience = filterState.selectedExperience.isEmpty() ||
                        job.experience.contains(filterState.selectedExperience, ignoreCase = true)

                val matchesEducation = filterState.selectedEducation.isEmpty() ||
                        filterState.selectedEducation.any {
                            job.education.contains(it, ignoreCase = true)
                        }

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
                        job.jobDescription.contains(filterState.location, ignoreCase = true) ||
                        companyName.contains(filterState.location, ignoreCase = true)

                matchesSearch && matchesCategories && matchesJobType &&
                        matchesExperience && matchesEducation && matchesSalary && matchesLocation
            }
        }
    }

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
                                "No jobs found",
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
                        isSaved = savedJobIds.contains(jobWithCompany.jobPost.postId),
                        onSaveClick = { postId ->
                            if (postId.isNotEmpty()) {
                                savedJobViewModel.toggleSaveJob(postId) { success, message, isSaved ->
                                    if (success) {
                                        val action = if (isSaved) "Saved" else "Unsaved"
                                        Toast.makeText(context, "$action successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed: $message", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
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

    LaunchedEffect(Unit) {
        companyViewModel.getAllCompany { success, message, companies ->
            if (success && companies != null) {
                allCompanies = companies
                isCompaniesLoading = false

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

    val filteredCompanies = remember(allCompanies, searchQuery) {
        val normalizedSearchQuery = searchQuery.trim().lowercase()

        if (normalizedSearchQuery.isEmpty()) {
            allCompanies
        } else {
            val searchKeywords = normalizedSearchQuery.split(" ").filter { it.isNotBlank() }

            allCompanies.filter { company ->
                val searchableFields = listOf(
                    company.companyName,
                    company.companyLocation,
                    company.companyInformation
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
                    "Search companies by name or location",
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

    LaunchedEffect(Unit) {
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
                    "Search by name, email, or profession",
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
            Box(
                modifier = Modifier
                    .size(75.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFEC4899), Color(0xFFF43F5E))
                            ),
                            shape = CircleShape
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
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
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profilePhotoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Job Seeker Profile Image",
                                modifier = Modifier
                                    .size(71.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                loading = {
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
                        .graphicsLayer(rotationZ = 180f),
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
            Box(
                modifier = Modifier
                    .size(75.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFEC4899), Color(0xFFF43F5E))
                            ),
                            shape = CircleShape
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
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
                        .graphicsLayer(rotationZ = 180f),
                    tint = Color(0xFF6366F1)
                )
            }
        }
    }
}