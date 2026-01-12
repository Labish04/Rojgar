package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rojgar.R
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.FollowRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.FollowViewModel
import com.example.rojgar.viewmodel.FollowViewModelFactory
import com.example.rojgar.viewmodel.JobSeekerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class FollowerUi(
    val id: String,
    val name: String,
    val profession: String,
    val followerType: String,
    val profileImageUrl: String?,
    var isFollowingBack: Boolean = false,
    val isLoaded: Boolean = true  // Track if data is loaded
)

class FollowersListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FollowersListBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersListBody() {
    val context = LocalContext.current
    val followViewModel: FollowViewModel = viewModel(factory = FollowViewModelFactory(FollowRepoImpl()))
    val jobSeekerRepo = remember { JobSeekerRepoImpl() }
    val companyRepo = remember { CompanyRepoImpl() }

    // Premium color palette
    val primaryBlue = Color(0xFF0EA5E9)
    val secondaryBlue = Color(0xFF38BDF8)
    val accentBlue = Color(0xFF7DD3FC)
    val bgGradientStart = Color(0xFFF0F9FF)
    val bgGradientEnd = Color(0xFFE0F2FE)
    val cardBg = Color(0xFFFFFFFF)

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    // Get intent extras
    val activity = context as? ComponentActivity
    val userId = activity?.intent?.getStringExtra("USER_ID") ?: ""
    val userType = activity?.intent?.getStringExtra("USER_TYPE") ?: "Company"
    val isOwnProfile = activity?.intent?.getBooleanExtra("IS_OWN_PROFILE", false) ?: false

    // Using mutableStateListOf for reactive updates
    var followerDetails by remember { mutableStateOf<List<FollowerUi>>(emptyList()) }
    var isLoadingDetails by remember { mutableStateOf(false) }

    // Load followers
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            followViewModel.getFollowers(userId)
        }
    }

    val followersList by followViewModel.followers.observeAsState(initial = emptyList())
    val loadingState by followViewModel.loading.observeAsState(initial = false)

    // Fetch details for each follower - FIXED VERSION
    LaunchedEffect(followersList) {
        if (followersList.isEmpty()) {
            followerDetails = emptyList()
            return@LaunchedEffect
        }

        isLoadingDetails = true
        val loadedFollowers = mutableListOf<FollowerUi>()
        var loadedCount = 0
        val totalCount = followersList.size

        android.util.Log.d("FollowersList", "Starting to load ${followersList.size} followers")

        followersList.forEach { follow ->
            android.util.Log.d("FollowersList", "Processing follower - ID: ${follow.followerId}, Type: ${follow.followerType}")
            if (follow.followerType == "JobSeeker") {
                jobSeekerRepo.getJobSeekerById(follow.followerId) { success, _, model ->
                    if (success && model != null) {
                        loadedFollowers.add(FollowerUi(
                            id = follow.followerId,
                            name = model.fullName.takeIf { !it.isNullOrBlank() } ?: "Job Seeker",
                            profession = model.profession?.takeIf { it.isNotBlank() }
                                ?: model.profession?.takeIf { it.isNotBlank() }
                                ?: "Looking for opportunities",
                            followerType = follow.followerType,
                            profileImageUrl = model.profilePhoto?.takeIf { it.isNotBlank() },
                            isFollowingBack = false,
                            isLoaded = true
                        ))
                    } else {
                        // Add placeholder for failed load
                        loadedFollowers.add(FollowerUi(
                            id = follow.followerId,
                            name = "Job Seeker",
                            profession = "Profile unavailable",
                            followerType = follow.followerType,
                            profileImageUrl = null,
                            isFollowingBack = false,
                            isLoaded = true
                        ))
                    }

                    loadedCount++
                    if (loadedCount == totalCount) {
                        followerDetails = loadedFollowers.sortedBy { it.name }
                        isLoadingDetails = false
                    }
                }
            } else if (follow.followerType == "Company") {
                companyRepo.getCompanyById(follow.followerId) { success, message, model ->
                    android.util.Log.d("FollowersList", "Company fetch - ID: ${follow.followerId}, Success: $success, Message: $message, Model: $model")

                    if (success && model != null) {
                        loadedFollowers.add(FollowerUi(
                            id = follow.followerId,
                            name = model.companyName.takeIf { !it.isNullOrBlank() } ?: "Company",
                            profession = model.companyTagline?.takeIf { it.isNotBlank() }
                                ?: model.companyLocation?.takeIf { it.isNotBlank() }
                                ?: model.companyIndustry?.takeIf { it.isNotBlank() }
                                ?: "Company",
                            followerType = follow.followerType,
                            profileImageUrl = model.companyProfileImage?.takeIf { it.isNotBlank() },
                            isFollowingBack = false,
                            isLoaded = true
                        ))
                    } else {
                        // Add placeholder for failed load with error details
                        loadedFollowers.add(FollowerUi(
                            id = follow.followerId,
                            name = "Company",
                            profession = "Profile unavailable${if (message.isNotEmpty()) ": $message" else ""}",
                            followerType = follow.followerType,
                            profileImageUrl = null,
                            isFollowingBack = false,
                            isLoaded = true
                        ))
                    }

                    loadedCount++
                    if (loadedCount == totalCount) {
                        followerDetails = loadedFollowers.sortedBy { it.name }
                        isLoadingDetails = false
                    }
                }
            } else {
                // Unknown follower type
                loadedCount++
                if (loadedCount == totalCount) {
                    followerDetails = loadedFollowers.sortedBy { it.name }
                    isLoadingDetails = false
                }
            }
        }
    }

    val filteredFollowers = remember(searchQuery, followerDetails) {
        if (searchQuery.isEmpty()) {
            followerDetails
        } else {
            followerDetails.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.profession.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = isSearching,
                        transitionSpec = {
                            fadeIn(tween(300)) + slideInVertically() togetherWith
                                    fadeOut(tween(300)) + slideOutVertically()
                        },
                        label = "titleAnimation"
                    ) { searching ->
                        if (!searching) {
                            Text(
                                "Followers",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            FollowersSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onClear = { searchQuery = "" }
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSearching = !isSearching
                            if (!isSearching) searchQuery = ""
                        }
                    ) {
                        Icon(
                            if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearching) "Close Search" else "Search",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryBlue
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(bgGradientStart, bgGradientEnd, Color.White),
                        startY = 0f,
                        endY = 1500f
                    )
                )
                .padding(padding)
        ) {
            when {
                loadingState || isLoadingDetails -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = primaryBlue)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Loading followers...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }
                filteredFollowers.isEmpty() && searchQuery.isNotEmpty() -> {
                    FollowersEmptySearchState()
                }
                filteredFollowers.isEmpty() -> {
                    FollowersEmptyState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(filteredFollowers) { index, follower ->
                            FollowerCard(
                                follower = follower,
                                index = index,
                                primaryBlue = primaryBlue,
                                secondaryBlue = secondaryBlue,
                                accentBlue = accentBlue,
                                cardBg = cardBg,
                                onFollowBackToggle = {
                                    // TODO: Implement follow back logic
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search followers...", color = Color.White.copy(alpha = 0.7f)) },
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.White
        ),
        singleLine = true,
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                }
            }
        }
    )
}

@Composable
fun FollowersEmptySearchState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No followers found",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )
        Text(
            "Try searching with different keywords",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

@Composable
fun FollowersEmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No followers yet",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )
        Text(
            "When someone follows this profile, they'll appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

@Composable
fun FollowerCard(
    follower: FollowerUi,
    index: Int,
    primaryBlue: Color,
    secondaryBlue: Color,
    accentBlue: Color,
    cardBg: Color,
    onFollowBackToggle: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 80L)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + expandVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = primaryBlue.copy(alpha = 0.2f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBg
            )
        ) {
            Box {
                // Decorative gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    accentBlue.copy(alpha = 0.1f),
                                    secondaryBlue.copy(alpha = 0.05f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Enhanced Avatar
                    FollowerEnhancedAvatar(
                        primaryBlue = primaryBlue,
                        secondaryBlue = secondaryBlue,
                        accentBlue = accentBlue,
                        name = follower.name,
                        profileImageUrl = follower.profileImageUrl,
                        followerType = follower.followerType
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Follower info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = follower.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = follower.profession,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            lineHeight = 20.sp,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onFollowBackToggle,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View")
                    }
                }
            }
        }
    }
}

@Composable
fun FollowerEnhancedAvatar(
    primaryBlue: Color,
    secondaryBlue: Color,
    accentBlue: Color,
    name: String,
    profileImageUrl: String?,
    followerType: String
) {
    Box(
        modifier = Modifier.size(70.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rotating gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            accentBlue,
                            secondaryBlue,
                            primaryBlue,
                            accentBlue
                        )
                    )
                )
        )

        // Inner circle
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (!profileImageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = name,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(primaryBlue, secondaryBlue)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (name.isNotEmpty()) name.first().toString().uppercase() else "?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFollowersList() {
    FollowersListBody()
}