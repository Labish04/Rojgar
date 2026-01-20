package com.example.rojgar.view

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.model.ReviewModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.ReviewRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.Gray
import com.example.rojgar.ui.theme.NormalBlue
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.ReviewViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CompanyHomeScreenBody(){

    val context = LocalContext.current

    var search by remember { mutableStateOf("") }
    
    // Initialize ViewModels
    val reviewViewModel = remember { ReviewViewModel(ReviewRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    
    // Get company ID and user ID
    val companyId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    
    // Observe reviews and average rating
    val reviews by reviewViewModel.reviews.observeAsState(emptyList())
    val averageRating by reviewViewModel.averageRating.observeAsState(0.0)
    val company by companyViewModel.companyDetails.observeAsState(null)
    
    // Fetch company details
    LaunchedEffect(companyId) {
        if (companyId.isNotEmpty()) {
            companyViewModel.fetchCurrentCompany()
        }
    }
    
    // Setup real-time updates
    LaunchedEffect(companyId, currentUserId) {
        if (companyId.isNotEmpty() && currentUserId.isNotEmpty()) {
            reviewViewModel.setupRealTimeUpdates(companyId, currentUserId)
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Blue)
    ){

        Spacer(modifier = Modifier.height(20.dp))

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        context.startActivity(
                            Intent(context, JobSeekerSearchActivity::class.java)
                        )
                    }
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false, // Important
                    placeholder = {
                        Text(
                            "Search",
                            fontSize = 20.sp,
                            color = Color.Gray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.searchicon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Gray
                        )
                    },
                    shape = RoundedCornerShape(15.dp),
                    colors = TextFieldDefaults.colors(
                        disabledContainerColor = White,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ){
            // Reviews & Ratings Card with overall rating display
            ReviewsRatingsCard(
                averageRating = averageRating,
                totalReviews = reviews.size,
                reviews = reviews,
                companyId = companyId,
                companyName = company?.companyName ?: "Company",
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
            )

            Spacer(modifier = Modifier.width(20.dp))

            Card (
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ){
                Text("Calendar", style = TextStyle(
                    fontSize = 18.sp,
                    color = Color.DarkGray
                ),
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp)
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                "Posted Jobs", style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            )
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp),
                horizontalArrangement = Arrangement.End
            ){
                Text(
                    "Show All", style = TextStyle(
                        fontSize = 18.sp
                    )
                )
            }
        }

        Card (
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(395.dp)
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ){

        }
    }
}

@Composable
fun ReviewsRatingsCard(
    averageRating: Double,
    totalReviews: Int,
    reviews: List<ReviewModel>,
    companyId: String,
    companyName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Calculate rating distribution
    val ratingDistribution = remember(reviews) {
        val distribution = mutableMapOf<Int, Int>()
        for (i in 1..5) {
            distribution[i] = reviews.count { it.rating == i }
        }
        distribution
    }
    
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    val intent = Intent(context, CompanyReviewActivity::class.java)
                    intent.putExtra("COMPANY_ID", companyId)
                    intent.putExtra("COMPANY_NAME", companyName)
                    context.startActivity(intent)
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header with title and info icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ratings and reviews",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Overall rating
                Column(
                    modifier = Modifier.weight(0.4f)
                ) {
                    Text(
                        text = String.format("%.1f", averageRating),
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                    
                    // Star rating display with half-star support
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { index ->
                            val starValue = index + 1
                            val isFilled = starValue <= averageRating.toInt()
                            val isHalfFilled = starValue == averageRating.toInt() + 1 && 
                                              averageRating - averageRating.toInt() >= 0.5
                            
                            Box(modifier = Modifier.size(14.dp)) {
                                // Background (gray) star
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFCCCCCC)
                                )
                                
                                // Foreground (green) star - clipped for half stars
                                if (isFilled || isHalfFilled) {
                                    Box(
                                        modifier = Modifier.size(14.dp)
                                    ) {
                                        if (isHalfFilled) {
                                            // Half star using clipping
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.5f)
                                                    .fillMaxHeight()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = Color(0xFF4CAF50)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = Color(0xFF4CAF50)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Text(
                        text = "${totalReviews}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Right side: Rating distribution
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Display rating bars for 5, 4, 3, 2, 1 stars
                    (5 downTo 1).forEach { star ->
                        RatingDistributionBar(
                            star = star,
                            count = ratingDistribution[star] ?: 0,
                            total = totalReviews
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingDistributionBar(
    star: Int,
    count: Int,
    total: Int
) {
    val percentage = if (total > 0) (count.toFloat() / total.toFloat()) else 0f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Star label
        Text(
            text = "$star",
            style = TextStyle(
                fontSize = 10.sp,
                color = Color.Black,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.width(10.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Progress bar
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    color = Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(2.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage.coerceIn(0f, 1f))
                    .background(
                        color = Color(0xFF4CAF50), // Green color
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}