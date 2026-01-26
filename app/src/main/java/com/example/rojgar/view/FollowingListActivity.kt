package com.example.rojgar.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rojgar.R
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.FollowRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.repository.UserRepo
import com.example.rojgar.viewmodel.FollowViewModel
import com.example.rojgar.viewmodel.FollowViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers

data class FollowingUi(
    val id: String,
    val name: String,
    val profession: String,
    val followingType: String,
    val profileImageUrl: String?,
    var isFollowing: Boolean = true,
    var isOwnProfile: Boolean = false,
    val isLoaded: Boolean = true
)

class FollowingListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FollowingListBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingListBody() {
    val context = LocalContext.current
    val followViewModel = remember { FollowViewModel(FollowRepoImpl(context)) }
    val jobSeekerRepo = remember { JobSeekerRepoImpl() }
    val companyRepo = remember { CompanyRepoImpl() }
    val userRepo = remember { UserRepo() }

    // Premium color palette
    val primaryBlue = Color(0xFF0EA5E9)
    val secondaryBlue = Color(0xFF38BDF8)
    val accentBlue = Color(0xFF7DD3FC)
    val bgGradientStart = Color(0xFFF0F9FF)
    val bgGradientEnd = Color(0xFFE0F2FE)
    val cardBg = Color(0xFFFFFFFF)
    val unfollowButtonColor = Color(0xFFEF4444) // Red for unfollow

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    // Get intent extras
    val activity = context as? ComponentActivity
    val userId = activity?.intent?.getStringExtra("USER_ID") ?: ""
    val userType = activity?.intent?.getStringExtra("User_Type") ?: ""
    val isOwnProfile = activity?.intent?.getBooleanExtra("IS_OWN_PROFILE", false) ?: false


    // Current user info
    var currentUserId by remember { mutableStateOf("") }
    var currentUserType by remember { mutableStateOf<String?>(null) }
    var isLoadingUserType by remember { mutableStateOf(false) }

    // Using mutableStateListOf for reactive updates
    var followingDetails by remember { mutableStateOf<List<FollowingUi>>(emptyList()) }
    var isLoadingDetails by remember { mutableStateOf(false) }

    var reloadUserTypeTrigger by remember { mutableStateOf(0) }

    val reloadUserType: () -> Unit = {
        if (currentUserId.isNotEmpty()) {
            isLoadingUserType = true
            userRepo.getUserType { type ->
                currentUserType = type
                isLoadingUserType = false
                reloadUserTypeTrigger++
            }
        }
    }

    LaunchedEffect(Unit) {
        // If we have userType from intent, use it directly
        if (userType.isNotEmpty()) {
            currentUserType = userType
        } else {
            // Fallback to repository only if not passed
            currentUserId = userRepo.getCurrentUserId()
            if (currentUserId.isNotEmpty()) {
                userRepo.getUserType { type ->
                    currentUserType = type
                }
            }
        }
    }

    // Get current user info
    LaunchedEffect(Unit) {
        currentUserId = userRepo.getCurrentUserId()
        println("DEBUG: Current User ID = $currentUserId")
        println("DEBUG: Is User Logged In = ${userRepo.isUserLoggedIn()}")

        if (currentUserId.isNotEmpty()) {
            isLoadingUserType = true
            delay(100)

            userRepo.getUserType { type ->
                println("DEBUG: getUserType callback received: $type")
                currentUserType = type
                isLoadingUserType = false

                if (type == null) {
                    println("DEBUG: User type is null for user ID: $currentUserId")
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(1000)
                        println("DEBUG: Retrying getUserType...")
                        userRepo.getUserType { retryType ->
                            println("DEBUG: Retry getUserType callback: $retryType")
                            currentUserType = retryType
                            isLoadingUserType = false
                        }
                    }
                } else {
                    println("DEBUG: User type successfully set to: $type")
                }
            }
        } else {
            println("DEBUG: No current user ID found")
            isLoadingUserType = false
        }
    }

    // Manual Firebase check
    LaunchedEffect(Unit) {
        delay(2000)
        if (currentUserId.isNotEmpty()) {
            println("DEBUG: ==================== MANUAL FIREBASE CHECK ====================")

            val companiesRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("Companys")
                .child(currentUserId)

            companiesRef.get().addOnSuccessListener { snapshot ->
                println("DEBUG: Companies node exists: ${snapshot.exists()}")
                if (snapshot.exists()) {
                    println("DEBUG: Company data: ${snapshot.value}")
                }
            }.addOnFailureListener { error ->
                println("DEBUG: Companies check FAILED: ${error.message}")
            }

            val jobSeekersRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("JobSeeker")
                .child(currentUserId)

            jobSeekersRef.get().addOnSuccessListener { snapshot ->
                println("DEBUG: JobSeekers node exists: ${snapshot.exists()}")
                if (snapshot.exists()) {
                    println("DEBUG: JobSeeker data: ${snapshot.value}")
                }
            }.addOnFailureListener { error ->
                println("DEBUG: JobSeekers check FAILED: ${error.message}")
            }

            println("DEBUG: ================================================================")
        }
    }

    // Load following
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            followViewModel.getFollowing(userId)
        }
    }

    val followingList by followViewModel.following.observeAsState(initial = emptyList())
    val loadingState by followViewModel.loading.observeAsState(initial = false)

    // Fetch details for each following
    LaunchedEffect(followingList, currentUserId, currentUserType) {
        if (followingList.isEmpty()) {
            followingDetails = emptyList()
            return@LaunchedEffect
        }

        isLoadingDetails = true
        val loadedFollowing = mutableListOf<FollowingUi>()
        var loadedCount = 0
        val totalCount = followingList.size

        followingList.forEach { follow ->
            if (follow.followingType == "JobSeeker") {
                jobSeekerRepo.getJobSeekerById(follow.followingId) { success, _, model ->
                    if (success && model != null) {
                        val followingUi = FollowingUi(
                            id = follow.followingId,
                            name = model.fullName.takeIf { !it.isNullOrBlank() } ?: "Job Seeker",
                            profession = model.profession?.takeIf { it.isNotBlank() }
                                ?: "Looking for opportunities",
                            followingType = follow.followingType,
                            profileImageUrl = model.profilePhoto?.takeIf { it.isNotBlank() },
                            isOwnProfile = isOwnProfile,
                            isFollowing = true,
                            isLoaded = true
                        )

                        loadedFollowing.add(followingUi)
                        loadedCount++
                        if (loadedCount == totalCount) {
                            followingDetails = loadedFollowing.sortedBy { it.name }
                            isLoadingDetails = false
                        }
                    } else {
                        loadedFollowing.add(FollowingUi(
                            id = follow.followingId,
                            name = "Job Seeker",
                            profession = "Profile unavailable",
                            followingType = follow.followingType,
                            profileImageUrl = null,
                            isOwnProfile = isOwnProfile,
                            isFollowing = true,
                            isLoaded = true
                        ))
                        loadedCount++
                        if (loadedCount == totalCount) {
                            followingDetails = loadedFollowing.sortedBy { it.name }
                            isLoadingDetails = false
                        }
                    }
                }
            } else if (follow.followingType == "Company") {
                companyRepo.getCompanyById(follow.followingId) { success, message, model ->
                    if (success && model != null) {
                        val followingUi = FollowingUi(
                            id = follow.followingId,
                            name = model.companyName.takeIf { !it.isNullOrBlank() } ?: "Company",
                            profession = model.companyTagline?.takeIf { it.isNotBlank() }
                                ?: model.companyLocation?.takeIf { it.isNotBlank() }
                                ?: model.companyIndustry?.takeIf { it.isNotBlank() }
                                ?: "Company",
                            followingType = follow.followingType,
                            profileImageUrl = model.companyProfileImage?.takeIf { it.isNotBlank() },
                            isOwnProfile = isOwnProfile,
                            isFollowing = true,
                            isLoaded = true
                        )

                        loadedFollowing.add(followingUi)
                        loadedCount++
                        if (loadedCount == totalCount) {
                            followingDetails = loadedFollowing.sortedBy { it.name }
                            isLoadingDetails = false
                        }
                    } else {
                        loadedFollowing.add(FollowingUi(
                            id = follow.followingId,
                            name = "Company",
                            profession = "Profile unavailable",
                            followingType = follow.followingType,
                            profileImageUrl = null,
                            isOwnProfile = isOwnProfile,
                            isFollowing = true,
                            isLoaded = true
                        ))
                        loadedCount++
                        if (loadedCount == totalCount) {
                            followingDetails = loadedFollowing.sortedBy { it.name }
                            isLoadingDetails = false
                        }
                    }
                }
            } else {
                loadedCount++
                if (loadedCount == totalCount) {
                    followingDetails = loadedFollowing.sortedBy { it.name }
                    isLoadingDetails = false
                }
            }
        }
    }

    val filteredFollowing = remember(searchQuery, followingDetails) {
        if (searchQuery.isEmpty()) {
            followingDetails
        } else {
            followingDetails.filter {
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
                                "Following",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            FollowingSearchBar(
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
                                "Loading following...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }
                filteredFollowing.isEmpty() && searchQuery.isNotEmpty() -> {
                    FollowingEmptySearchState()
                }
                filteredFollowing.isEmpty() -> {
                    FollowingEmptyState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(filteredFollowing) { index, following ->
                            FollowingCard(
                                following = following,
                                index = index,
                                primaryBlue = primaryBlue,
                                secondaryBlue = secondaryBlue,
                                accentBlue = accentBlue,
                                cardBg = cardBg,
                                unfollowButtonColor = unfollowButtonColor,
                                onUnfollowToggle = {
                                    if (currentUserId.isNotEmpty() && currentUserType != null) {
                                        // Unfollow
                                        followViewModel.unfollow(
                                            followerId = currentUserId,
                                            followerType = currentUserType!!,
                                            followingId = following.id,
                                            followingType = following.followingType
                                        ) { success, message ->
                                            if (success) {
                                                // Remove from local state
                                                followingDetails = followingDetails.filter { it.id != following.id }
                                                Toast.makeText(
                                                    context,
                                                    "Unfollowed ${following.name}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to unfollow: $message",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Please login to unfollow",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                context = context,
                                followViewModel = followViewModel,
                                isLoadingUserType = isLoadingUserType,
                                currentUserId = currentUserId,
                                currentUserType = currentUserType,
                                userRepo = userRepo,
                                onReloadUserType = reloadUserType,
                                isOwnProfile = isOwnProfile
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
fun FollowingSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search following...", color = Color.White.copy(alpha = 0.7f)) },
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
fun FollowingEmptySearchState() {
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
            "No users found",
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
fun FollowingEmptyState() {
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
            "Not following anyone yet",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )
        Text(
            "Start following people to see them here",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

@Composable
fun FollowingCard(
    isOwnProfile: Boolean,
    following: FollowingUi,
    index: Int,
    primaryBlue: Color,
    secondaryBlue: Color,
    accentBlue: Color,
    cardBg: Color,
    unfollowButtonColor: Color,
    onUnfollowToggle: () -> Unit,
    context: Context,
    followViewModel: FollowViewModel,
    isLoadingUserType: Boolean,
    currentUserId: String,
    currentUserType: String?,
    userRepo: UserRepo,
    onReloadUserType: () -> Unit
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
                    FollowingEnhancedAvatar(
                        primaryBlue = primaryBlue,
                        secondaryBlue = secondaryBlue,
                        accentBlue = accentBlue,
                        name = following.name,
                        profileImageUrl = following.profileImageUrl,
                        followingType = following.followingType
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Following info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = following.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = following.profession,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            lineHeight = 20.sp,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Show different button based on status
                    if (isOwnProfile){
                    if (isLoadingUserType) {
                        // Show loading button
                        Button(
                            onClick = { /* Do nothing while loading */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(12.dp),
                            enabled = false
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (currentUserId.isNotEmpty() && currentUserType != null) {
                        // User is logged in and we know their type
                        Button(
                            onClick = {
                                onUnfollowToggle()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = unfollowButtonColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Unfollow")
                        }
                    } else if (currentUserId.isNotEmpty() && currentUserType == null) {
                        // User is logged in but we couldn't determine type
                        Button(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Reloading user type...",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onReloadUserType()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                    } else {
                        // User is not logged in
                        Button(
                            onClick = {
                                Toast.makeText(context, "Please login", Toast.LENGTH_SHORT).show()
                            },
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
}

@Composable
fun FollowingEnhancedAvatar(
    primaryBlue: Color,
    secondaryBlue: Color,
    accentBlue: Color,
    name: String,
    profileImageUrl: String?,
    followingType: String
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