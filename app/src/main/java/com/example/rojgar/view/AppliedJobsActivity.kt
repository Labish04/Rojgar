package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.view.ui.theme.RojgarTheme

class AppliedJobsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RojgarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppliedJobsScreen(
                        modifier = Modifier.padding(innerPadding),
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun AppliedJobsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        "All" to 0,
        "Pending" to 0,
        "Viewed" to 0,
        "Shortlisted" to 0,
        "Offered" to 0,
        "Hired" to 0,
        "Rejected" to 0,
        "Withdrawn" to 0
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header with back arrow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF7C3AED))
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_arrow_back_ios_24),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Applied Jobs",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color.Black,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF7C3AED)
                    )
                }
            },
            divider = {
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, (title, count) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            color = if (selectedTab == index) Color(0xFF7C3AED) else Color.Gray,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                        )

                        if (count > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = if (selectedTab == index) Color(0xFFEF4444) else Color.LightGray,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = count.toString(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Column Headers with horizontal scroll and better spacing
        val scrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9FAFB))
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderCell(
                text = "Title",
                minWidth = 140.dp
            )
            HeaderCell(
                text = "Company",
                minWidth = 140.dp
            )
            HeaderCell(
                text = "Status",
                minWidth = 100.dp
            )
            HeaderCell(
                text = "Location",
                minWidth = 120.dp
            )
            HeaderCell(
                text = "Openings",
                minWidth = 100.dp
            )
            HeaderCell(
                text = "Applied Date",
                minWidth = 120.dp
            )
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

        // Content area - filters based on selected tab
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // When database is ready, you can filter and display jobs here based on selectedTab
            // For now, showing empty state
            Text(
                text = when (selectedTab) {
                    0 -> "All applied jobs will appear here"
                    1 -> "Pending applications will appear here"
                    2 -> "Viewed applications will appear here"
                    3 -> "Shortlisted applications will appear here"
                    4 -> "Offered applications will appear here"
                    5 -> "Hired applications will appear here"
                    6 -> "Rejected applications will appear here"
                    7 -> "Withdrawn applications will appear here"
                    else -> ""
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HeaderCell(
    text: String,
    minWidth: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .widthIn(min = minWidth),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6B7280),
            maxLines = 1
        )
    }
}

@Preview(showBackground = true, widthDp = 900, heightDp = 600)
@Composable
fun AppliedJobsScreenPreview() {
    RojgarTheme {
        AppliedJobsScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview4() {
    RojgarTheme {
        AppliedJobsScreen()
    }
}