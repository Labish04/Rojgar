package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.ui.theme.Blue

class CompanyReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompanyReviewScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyReviewScreen() {
    val context = LocalContext.current
    var isSelectAllChecked by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = "Back",
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = "Reviews and Ratings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = {
                        // Handle delete action
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.delete_icon),
                            contentDescription = "Delete",
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // Select All Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelectAllChecked,
                    onCheckedChange = { isSelectAllChecked = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Select",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Reviews List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) { index ->
                    ReviewCard(
                        reviewerName = "User ${index + 1}",
                        rating = 4 + (index % 2),
                        reviewText = "Great company to work with! Professional environment and excellent management.",
                        date = "Dec ${25 - index}, 2024",
                        isSelected = isSelectAllChecked,
                        onSelectionChange = { }
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewCard(
    reviewerName: String,
    rating: Int,
    reviewText: String,
    date: String,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    var checked by remember { mutableStateOf(isSelected) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked || isSelected,
                onCheckedChange = {
                    checked = it
                    onSelectionChange(it)
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reviewerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = date,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "⭐".repeat(rating),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = reviewText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Preview
@Composable
fun CompanyReviewScreenPreview() {
    CompanyReviewScreen()
}