package com.example.rojgar.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
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
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.RojgarTheme
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerDashboardBody() {

    val context = LocalContext.current
    val activity = context as Activity

    val companyViewModel: CompanyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }

    val jobSeeker = jobSeekerViewModel.jobSeeker.observeAsState(initial = null)

    data class NavItem(
        val label: String,
        val selectedIcon: Int,
        val unselectedIcon: Int
    )

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

        topBar = {
            val showTopBar = selectedIndex in listOf(0)

            if (showTopBar) {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        titleContentColor = Black,
                        actionIconContentColor = Black,
                        containerColor = Blue,
                        navigationIconContentColor = Black
                    ),
                    title = {
                        Text("")
                    },
                    navigationIcon = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50)), // change color if you want
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = jobSeeker.value?.profilePhoto,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = "Hi! ${jobSeeker.value?.fullName}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Let's find your dream job.",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                    },
                    actions = {
                        Row(
                            modifier = Modifier
                                .width(130.dp)
                        ) {
                            IconButton(onClick = {
                                val intent = Intent(context, MessageActivity::class.java)
                                context.startActivity(intent)                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.chat),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            IconButton(onClick = {}) {
                                Icon(
                                    painter = painterResource(R.drawable.notification),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp)
                                )
                            }
                        }
                    }
                )
            }

        },

        bottomBar = {
            Surface (
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .fillMaxWidth(),
                color = Color.White,
                shadowElevation = 10.dp
            ) {
                NavigationBar(
                    containerColor = Color.Transparent
                ) {
                    listItem.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        if (selectedIndex == index) item.selectedIcon else item.unselectedIcon
                                    ),
                                    contentDescription = item.label,
                                    modifier = Modifier.size(25.dp)
                                )
                            },
                            label = { Text(item.label) },
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index }
                        )
                    }
                }
            }
        }

    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedIndex) {
                0 -> JobSeekerHomeScreenBody()
                1 -> JobSeekerViewPostBody()
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
