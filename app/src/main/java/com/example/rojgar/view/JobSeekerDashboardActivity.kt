package com.example.rojgar.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rojgar.R
import com.example.rojgar.model.PreferenceModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.repository.SavedJobRepoImpl
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.RojgarTheme
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.example.rojgar.viewmodel.SavedJobViewModel
import com.google.firebase.auth.FirebaseAuth

class JobSeekerDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RojgarTheme {
                JobSeekerDashboardBody()
            }
        }
    }
}

// Define theme colors for the bottom navigation bar
object JobSeekerBottomBarTheme {
    val PrimaryBlue = Blue
    val GlassWhite = Color.White.copy(alpha = 0.95f)
    val White = Color.White
    val TextSecondary = Color.Black
    val SurfaceLight = Color(0xFFF8F9FA)
    val DeepBlue = Color(0xFF0B69CB)
    val LightBlue = Color(0xFF69B3FF)
}

// Modern Bottom Navigation Bar
@Composable
fun JobSeekerBottomNavigationBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Glass morphism background with gradient
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = JobSeekerBottomBarTheme.LightBlue.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                JobSeekerBottomBarTheme.GlassWhite,
                                JobSeekerBottomBarTheme.LightBlue.copy(alpha = 0.95f)
                            )
                        )
                    )
            ) {
                // Subtle top border gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    JobSeekerBottomBarTheme.PrimaryBlue.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        JobSeekerNavigationItem(
                            item = item,
                            isSelected = selectedIndex == index,
                            onClick = { onItemSelected(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JobSeekerNavigationItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val iconSize by animateDpAsState(
        targetValue = if (isSelected) 28.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconSize"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) JobSeekerBottomBarTheme.LightBlue else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "containerColor"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) JobSeekerBottomBarTheme.White else JobSeekerBottomBarTheme.TextSecondary,
        animationSpec = tween(durationMillis = 300),
        label = "iconTint"
    )

    val offsetY by animateDpAsState(
        targetValue = if (isSelected) (-4).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "offsetY"
    )

    Box(
        modifier = Modifier
            .offset(y = offsetY)
            .scale(scale)
            .size(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated glow effect when selected
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                JobSeekerBottomBarTheme.DeepBlue,
                                JobSeekerBottomBarTheme.DeepBlue.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(
                    if (isSelected) item.selectedIcon else item.unselectedIcon
                ),
                contentDescription = item.label,
                modifier = Modifier.size(iconSize),
                tint = iconTint
            )

            // Animated label
            if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = iconTint,
                    modifier = Modifier.graphicsLayer(alpha = 0.9f)
                )
            }
        }

        // Ripple indicator dot
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-2).dp)
                    .size(4.dp)
                    .background(
                        color = JobSeekerBottomBarTheme.White,
                        shape = CircleShape
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerDashboardBody() {
    val context = LocalContext.current
    val activity = context as Activity

    val companyViewModel: CompanyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val savedViewModel = remember { SavedJobViewModel(SavedJobRepoImpl()) }

    val jobSeeker = jobSeekerViewModel.jobSeeker.observeAsState(initial = null)

    var selectedIndex by remember { mutableStateOf(0) }

    val listItem = listOf(
        NavItem(
            label = "Home",
            selectedIcon = R.drawable.home_filled,
            unselectedIcon = R.drawable.home
        ),
        NavItem(
            label = "Post",
            selectedIcon = R.drawable.jobpost_filled,
            unselectedIcon = R.drawable.jobpost
        ),
        NavItem(
            label = "Map",
            selectedIcon = R.drawable.map_filled,
            unselectedIcon = R.drawable.map
        ),
        NavItem(
            label = "Profile",
            selectedIcon = R.drawable.profile_filled,
            unselectedIcon = R.drawable.profile
        )
    )

    LaunchedEffect(Unit) {
        // Get current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val jobSeekerId = currentUser.uid

            // Fetch all profile data
            jobSeekerViewModel.fetchCurrentJobSeeker()
        }
    }

    Scaffold(
        containerColor = JobSeekerBottomBarTheme.SurfaceLight,
        bottomBar = {
            JobSeekerBottomNavigationBar(
                items = listItem,
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            when (selectedIndex) {
                0 -> JobSeekerHomeScreenBody()
                1 -> JobSeekerViewPostBody(savedViewModel)
                2 -> MapScreen(viewModel = companyViewModel, context = context)
                3 -> JobSeekerProfileBody()
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun JobSeekerDashboardBodyPreview() {
    RojgarTheme {
        JobSeekerDashboardBody()
    }
}