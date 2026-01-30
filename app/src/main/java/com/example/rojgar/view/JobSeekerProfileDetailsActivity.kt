package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rojgar.R
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.RojgarTheme
import com.example.rojgar.viewmodel.JobSeekerViewModel
import kotlinx.coroutines.delay

class JobSeekerProfileDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RojgarTheme {
                val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
                JobSeekerProfileDetailsBody(jobSeekerViewModel = jobSeekerViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerProfileDetailsBody(jobSeekerViewModel: JobSeekerViewModel) {
    val context = LocalContext.current
    var currentJobSeeker by remember { mutableStateOf<com.example.rojgar.model.JobSeekerModel?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(Unit) {
        val currentUser = jobSeekerViewModel.getCurrentJobSeeker()
        if (currentUser != null) {
            isLoading = true
            jobSeekerViewModel.getJobSeekerById(currentUser.uid) { success, message, jobSeeker ->
                isLoading = false
                if (success && jobSeeker != null) {
                    currentJobSeeker = jobSeeker
                }
                showContent = true
            }
        } else {
            showContent = true
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val scrollFraction = scrollBehavior.state.collapsedFraction

            LargeTopAppBar(
                title = {
                    val collapsed = scrollFraction > 0.35f
                    val profileSize by animateDpAsState(
                        targetValue = if (collapsed) 40.dp else 80.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )

                    val profileAlpha by animateFloatAsState(
                        targetValue = 1f - (scrollFraction * 0.3f),
                        animationSpec = tween(300)
                    )

                    if (!collapsed) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { alpha = profileAlpha },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(profileSize),
                                contentAlignment = Alignment.Center
                            ) {
                                val glowAlpha by animateFloatAsState(
                                    targetValue = if (collapsed) 0f else 0.3f,
                                    animationSpec = tween(500)
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { alpha = glowAlpha }
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF2196F3).copy(alpha = 0.4f),
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = CircleShape
                                        )
                                )

                                if (currentJobSeeker?.profilePhoto?.isNotEmpty() == true) {
                                    AsyncImage(
                                        model = currentJobSeeker!!.profilePhoto,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier
                                            .size(profileSize)
                                            .clip(CircleShape)
                                            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(R.drawable.picture)
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(R.drawable.profileemptypic),
                                        contentDescription = "Default Profile",
                                        modifier = Modifier
                                            .size(profileSize)
                                            .clip(CircleShape)
                                            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            // Name and Bio
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .graphicsLayer {
                                        alpha = 1f - scrollFraction
                                        translationY = scrollFraction * 20f
                                    }
                            ) {
                                Text(
                                    text = currentJobSeeker?.fullName?.ifEmpty { "Your Name" }
                                        ?: "Your Name",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                Text(
                                    text = currentJobSeeker?.bio?.ifEmpty {
                                        "Add your bio in Personal Information"
                                    } ?: "Add your bio in Personal Information",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 2,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = scrollFraction
                                    translationX = (1f - scrollFraction) * 50f
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Profile Photo - Small size
                            Box(
                                modifier = Modifier.size(profileSize),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentJobSeeker?.profilePhoto?.isNotEmpty() == true) {
                                    AsyncImage(
                                        model = currentJobSeeker!!.profilePhoto,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier
                                            .size(profileSize)
                                            .clip(CircleShape)
                                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(R.drawable.picture)
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(R.drawable.picture),
                                        contentDescription = "Default Profile",
                                        modifier = Modifier
                                            .size(profileSize)
                                            .clip(CircleShape)
                                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            // Name only in collapsed state
                            Text(
                                text = currentJobSeeker?.fullName?.ifEmpty { "Your Name" }
                                    ?: "Your Name",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                },
                navigationIcon = {
                    var backPressed by remember { mutableStateOf(false) }
                    val backScale by animateFloatAsState(
                        targetValue = if (backPressed) 0.8f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )

                    IconButton(
                        onClick = {
                            backPressed = true
                            (context as? ComponentActivity)?.finish()
                        },
                        modifier = Modifier.graphicsLayer {
                            scaleX = backScale
                            scaleY = backScale
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                actions = {
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    scrolledContainerColor = Color(0xFF2196F3)
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFFBBDEFB),
                            Color(0xFF90CAF9)
                        )
                    )
                )
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF1976D2),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(600))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // OPTIONS LIST
                        val options = listOf(
                            ProfileOption("Personal Information", R.drawable.user, Color(0xFF2196F3)) {
                                context.startActivity(Intent(context, JobSeekerPersonalInformationActivity::class.java))
                            },
                            ProfileOption("Objective", R.drawable.objectiveicon, Color(0xFF4CAF50)) {
                                context.startActivity(Intent(context, JobSeekerObjectiveActivity::class.java))
                            },
                            ProfileOption("Experience", R.drawable.experienceicon, Color(0xFFFF9800)) {
                                context.startActivity(Intent(context, JobSeekerExperienceActivity::class.java))
                            },
                            ProfileOption("Skill", R.drawable.skillicon, Color(0xFF9C27B0)) {
                                context.startActivity(Intent(context, JobSeekerSkillActivity::class.java))
                            },
                            ProfileOption("Education", R.drawable.educationicon, Color(0xFFF44336)) {
                                context.startActivity(Intent(context, JobSeekerEducationActivity::class.java))
                            },
                            ProfileOption("Training", R.drawable.trainingicon, Color(0xFF00BCD4)) {
                                context.startActivity(Intent(context, JobSeekerTrainingActivity::class.java))
                            },
                            ProfileOption("Job Preference", R.drawable.jobpreferenceicon, Color(0xFF673AB7)) {
                                context.startActivity(Intent(context, JobSeekerJobPreferenceActivity::class.java))
                            },
                            ProfileOption("Portfolio Accounts", R.drawable.linkicon, Color(0xFF3F51B5)) {
                                context.startActivity(Intent(context, JobSeekerPortfolioAccountsActivity::class.java))
                            },
                            ProfileOption("Language", R.drawable.languageicon, Color(0xFFE91E63)) {
                                context.startActivity(Intent(context, JobSeekerLanguageActivity::class.java))
                            },
                            ProfileOption("Reference", R.drawable.bioicon, Color(0xFF009688)) {
                                context.startActivity(Intent(context, JobSeekerReferenceActivity::class.java))
                            }
                        )

                        options.forEachIndexed { index, option ->
                            var isVisible by remember { mutableStateOf(false) }

                            LaunchedEffect(Unit) {
                                delay(index * 50L)
                                isVisible = true
                            }

                            AnimatedVisibility(
                                visible = isVisible,
                                enter = slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) + fadeIn(animationSpec = tween(400))
                            ) {
                                ModernOptionCard(
                                    option = option,
                                    index = index
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

data class ProfileOption(
    val title: String,
    val icon: Int,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun ModernOptionCard(option: ProfileOption, index: Int) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    option.onClick()
                }
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = option.color.copy(alpha = 0.12f)
                ) {
                    Icon(
                        painter = painterResource(id = option.icon),
                        contentDescription = option.title,
                        tint = option.color,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = option.title,
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF263238)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tap to ${if (option.title.contains("Information")) "view" else "add"} details",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xFF78909C)
                        )
                    )
                }
            }

            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = option.color.copy(alpha = 0.1f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                    contentDescription = "Navigate",
                    tint = option.color,
                    modifier = Modifier
                        .padding(10.dp)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
        }
    }
}

@Preview
@Composable
fun JobSeekerProfileDetailsBodyPreview() {
    RojgarTheme {
        JobSeekerProfileDetailsBody(
            jobSeekerViewModel = JobSeekerViewModel(JobSeekerRepoImpl())
        )
    }
}