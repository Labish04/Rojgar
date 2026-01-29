package com.example.rojgar.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rojgar.R
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.utils.ImageUtils
import com.example.rojgar.view.AnalyticsScreen
import com.example.rojgar.viewmodel.AnalyticsViewModel
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.google.firebase.auth.FirebaseAuth

class CompanyDashboardActivity : ComponentActivity() {
    lateinit var imageUtils: ImageUtils

    var isPickingCover by mutableStateOf(false)
    var isPickingProfile by mutableStateOf(false)

    var selectedCoverUri by mutableStateOf<Uri?>(null)
    var selectedProfileUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ImageUtils
        imageUtils = ImageUtils(this, this)
        imageUtils.registerLaunchers { uri ->
            if (uri != null) {
                if (isPickingCover) {
                    selectedCoverUri = uri
                } else if (isPickingProfile) {
                    selectedProfileUri = uri
                }

                isPickingCover = false
                isPickingProfile = false
            }
        }

        setContent {
            CompanyDashboardBody(
                selectedCoverUri = selectedCoverUri,
                selectedProfileUri = selectedProfileUri,
                onPickCoverImage = {
                    isPickingCover = true
                    isPickingProfile = false
                    imageUtils.launchImagePicker()
                },
                onPickProfileImage = {
                    isPickingProfile = true
                    isPickingCover = false
                    imageUtils.launchImagePicker()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDashboardBody(
    selectedCoverUri: Uri? = null,
    selectedProfileUri: Uri? = null,
    onPickCoverImage: () -> Unit = {},
    onPickProfileImage: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as Activity
    val analyticsViewModel: AnalyticsViewModel = viewModel()
    val companyId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val companyViewModel: CompanyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }

    val company = companyViewModel.companyDetails.observeAsState(initial = null)


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
            label = "Message",
            selectedIcon = R.drawable.analysis_filled,
            unselectedIcon = R.drawable.analysis
        ),
        NavItem(
            label = "Post",
            selectedIcon = R.drawable.upload_filled,
            unselectedIcon = R.drawable.upload
        ),
        NavItem(
            label = "Map",
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
            companyViewModel.fetchCurrentCompany()
        }
    }


    Scaffold(
        bottomBar = {
            Surface(
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
                0 -> CompanyHomeScreenBody(
                    company = company.value
                )
                1 -> AnalyticsScreen(viewModel = analyticsViewModel, companyId = companyId)
                2 -> CompanyUploadPostScreen(
                    selectedProfileUri = selectedProfileUri,
                    onPickProfileImage = onPickProfileImage
                )
                3 -> CompanyProfileBody(
                    companyId = companyId,
                    isOwnProfile = true,
                    selectedCoverUri = selectedCoverUri,
                    selectedProfileUri = selectedProfileUri,
                    onPickCoverImage = onPickCoverImage,
                    onPickProfileImage = onPickProfileImage
                )
            }
        }
    }
}

@Preview
@Composable
fun CompanyDashboardBodyPreview() {
    CompanyDashboardBody()
}