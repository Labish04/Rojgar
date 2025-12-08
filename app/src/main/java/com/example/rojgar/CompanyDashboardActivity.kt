package com.example.rojgar

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class CompanyDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompanyDashboardBody()
        }
    }
}

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
            selectedIcon = R.drawable.chat_filled,
            unselectedIcon = R.drawable.chat
        ),
        NavItem(
            label = "Post",
            selectedIcon = R.drawable.upload_filled,
            unselectedIcon = R.drawable.upload
        ),
        NavItem(
            label = "Map",
            selectedIcon = R.drawable.analysis_filled,
            unselectedIcon = R.drawable.analysis
        )
    )


    Scaffold(
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
                0 -> Text("Home Screen")
                1 -> Text("Message Screen")
                2 -> Text("Upload Job Screen")
                3 -> Text("Analysis Screen")
            }
        }
    }
}

@Preview
@Composable
fun CompanyDashboardBodyPreview() {
    CompanyDashboardBody()
}