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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
            FollowersListBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersListBody() {
    val lightBlue = Color(0xFF4FC3F7)
    val mediumBlue = Color(0xFF29B6F6)
    val darkBlue = Color(0xFF0288D1)
    val lightBlueBg = Color(0xFFE1F5FE)

    var users by remember {
        mutableStateOf(
            listOf(
                User(1, "Aisha Sharma", "@aisha_dev", "Android Developer | Tech Enthusiast"),
                User(2, "Rajesh Kumar", "@raj_designs", "UI/UX Designer | Creative Mind"),
                User(3, "Priya Patel", "@priya_code", "Full Stack Developer | Open Source"),
                User(4, "Arjun Singh", "@arjun_tech", "Software Engineer | AI/ML"),
                User(5, "Neha Gupta", "@neha_builds", "Mobile App Developer | Kotlin Lover"),
                User(6, "Vikram Joshi", "@vikram_dev", "Backend Engineer | Cloud Expert"),
                User(7, "Sanya Reddy", "@sanya_creates", "Product Designer | Innovation")
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Following",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = mediumBlue
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(lightBlueBg, Color.White)
                    )
                )
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(users) { user ->
                    UserCard(
                        user = user,
                        lightBlue = lightBlue,
                        mediumBlue = mediumBlue,
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

@Composable
fun UserCard(
    user: User,
    lightBlue: Color,
    mediumBlue: Color,
    onFollowToggle: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) +
                slideInHorizontally(
                    animationSpec = tween(400),
                    initialOffsetX = { it }
                )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp,
                hoveredElevation = 8.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with pulse animation
                AvatarWithPulse(lightBlue, mediumBlue)

                Spacer(modifier = Modifier.width(16.dp))

                // User info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = mediumBlue
                    )
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Follow button with animation
                AnimatedFollowButton(
                    isFollowing = user.isFollowing,
                    lightBlue = lightBlue,
                    mediumBlue = mediumBlue,
                    onClick = onFollowToggle
                )
            }
        }
    }
}

@Composable
fun AvatarWithPulse(lightBlue: Color, mediumBlue: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(lightBlue, mediumBlue)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun AnimatedFollowButton(
    isFollowing: Boolean,
    lightBlue: Color,
    mediumBlue: Color,
    onClick: () -> Unit
) {
    var clicked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (clicked) 0.9f else 1f,
        animationSpec = tween(100),
        label = "buttonScale"
    )

    LaunchedEffect(clicked) {
        if (clicked) {
            delay(100)
            clicked = false
        }
    }

    Button(
        onClick = {
            clicked = true
            onClick()
        },
        modifier = Modifier.scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowing) mediumBlue else Color.LightGray
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        AnimatedContent(
            targetState = isFollowing,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "buttonText"
        ) { following ->
            Text(
                text = if (following) "Following" else "Follow",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFollowersList() {
    FollowersListBody()
}