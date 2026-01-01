package com.example.rojgar.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rojgar.R
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.Blue

class CompanyDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompanyDashboardBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDashboardBody() {
    val context = LocalContext.current
    val activity = context as Activity

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


    Scaffold(
        topBar = {
            val showTopBar = selectedIndex in listOf(0, 1)

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
                                .padding(start = 20.dp)
                        ) {
                            IconButton(onClick = {}) {
                                Image(
                                    painter = painterResource(R.drawable.forgetpassworddesign),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(shape = CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Apple")
                                Text("Let's hire top talent.")
                            }
                        }
                    },
                    actions = {
                        Row(
                            modifier = Modifier
                                .width(130.dp)
                        ) {
                            IconButton(onClick = {}) {
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
                0 -> CompanyHomeScreenBody()
                1 -> Text("Analysis Screen")
                2 -> CompanyUploadPostScreen()
                3 -> CompanyProfileBody()
            }
        }
    }
}

@Preview
@Composable
fun CompanyDashboardBodyPreview() {
    CompanyDashboardBody()
}