package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val bio: String,
    var isFollowing: Boolean = true
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
    // Premium color palette
    val primaryBlue = Color(0xFF0EA5E9)
    val secondaryBlue = Color(0xFF38BDF8)
    val accentBlue = Color(0xFF7DD3FC)
    val darkBlue = Color(0xFF0284C7)
    val bgGradientStart = Color(0xFFF0F9FF)
    val bgGradientEnd = Color(0xFFE0F2FE)
    val cardBg = Color(0xFFFFFFFF)

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val allUsers = remember {
        listOf(
            User(1, "Aisha Sharma", "@aisha_dev", "Android Developer | Tech Enthusiast ✨"),
            User(2, "Rajesh Kumar", "@raj_designs", "UI/UX Designer | Creative Mind 🎨"),
            User(3, "Priya Patel", "@priya_code", "Full Stack Developer | Open Source 💻"),
            User(4, "Arjun Singh", "@arjun_tech", "Software Engineer | AI/ML 🤖"),
            User(5, "Neha Gupta", "@neha_builds", "Mobile App Developer | Kotlin Lover 📱"),
            User(6, "Vikram Joshi", "@vikram_dev", "Backend Engineer | Cloud Expert ☁️"),
            User(7, "Sanya Reddy", "@sanya_creates", "Product Designer | Innovation 🚀"),
            User(8, "Kabir Mehta", "@kabir_codes", "Frontend Developer | React Native 💙"),
            User(9, "Diya Verma", "@diya_tech", "Data Scientist | Python Expert 📊"),
            User(10, "Rohan Shah", "@rohan_builds", "DevOps Engineer | Kubernetes Pro ⚙️")
        )
    }

    var users by remember { mutableStateOf(allUsers) }

    val filteredUsers = remember(searchQuery, users) {
        if (searchQuery.isEmpty()) {
            users
        } else {
            users.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.username.contains(searchQuery, ignoreCase = true) ||
                        it.bio.contains(searchQuery, ignoreCase = true)
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
                            SearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onClear = { searchQuery = "" }
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
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
            AnimatedContent(
                targetState = filteredUsers.isEmpty() && searchQuery.isNotEmpty(),
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                },
                label = "emptyState"
            ) { isEmpty ->
                if (isEmpty) {
                    EmptySearchState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(filteredUsers) { index, user ->
                            UserCard(
                                user = user,
                                index = index,
                                primaryBlue = primaryBlue,
                                secondaryBlue = secondaryBlue,
                                accentBlue = accentBlue,
                                darkBlue = darkBlue,
                                cardBg = cardBg,
                                onFollowToggle = {
                                    users = users.map {
                                        if (it.id == user.id) it.copy(isFollowing = !it.isFollowing)
                                        else it
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search users...", color = Color.White.copy(alpha = 0.7f)) },
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
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
fun EmptySearchState() {
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
fun UserCard(
    user: User,
    index: Int,
    primaryBlue: Color,
    secondaryBlue: Color,
    accentBlue: Color,
    darkBlue: Color,
    cardBg: Color,
    onFollowToggle: () -> Unit
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
                    EnhancedAvatar(primaryBlue, secondaryBlue, accentBlue, user.name)

                    Spacer(modifier = Modifier.width(16.dp))

                    // User info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = primaryBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Enhanced Follow button
                    EnhancedFollowButton(
                        isFollowing = user.isFollowing,
                        primaryBlue = primaryBlue,
                        darkBlue = darkBlue,
                        onClick = onFollowToggle
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedAvatar(
    primaryBlue: Color,
    secondaryBlue: Color,
    accentBlue: Color,
    name: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

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
                    text = name.first().toString().uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun EnhancedFollowButton(
    isFollowing: Boolean,
    primaryBlue: Color,
    darkBlue: Color,
    onClick: () -> Unit
) {
    var clicked by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (clicked) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isFollowing) primaryBlue else Color(0xFFE2E8F0),
        animationSpec = tween(300),
        label = "buttonColor"
    )

    LaunchedEffect(clicked) {
        if (clicked) {
            delay(150)
            clicked = false
        }
    }

    Button(
        onClick = {
            clicked = true
            onClick()
        },
        modifier = Modifier
            .scale(scale)
            .height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        ),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        AnimatedContent(
            targetState = isFollowing,
            transitionSpec = {
                (fadeIn(tween(200)) + scaleIn(tween(200))) togetherWith
                        (fadeOut(tween(200)) + scaleOut(tween(200)))
            },
            label = "buttonText"
        ) { following ->
            Text(
                text = if (following) "Following" else "Follow",
                fontWeight = FontWeight.Bold,
                color = if (following) Color.White else primaryBlue,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFollowingList() {
    FollowingListBody()
}