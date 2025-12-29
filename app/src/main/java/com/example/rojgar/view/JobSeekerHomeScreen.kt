package com.example.rojgar.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.Gray
import com.example.rojgar.ui.theme.NormalBlue
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import com.example.rojgar.model.PreferenceModel
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.viewmodel.JobViewModel
@Composable
fun JobSeekerHomeScreenBody() {

    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
    val preference = remember { PreferenceModel() }
    val recommendedJobs by jobViewModel.recommendedJobs.observeAsState(emptyList())
    val message by jobViewModel.message.observeAsState("")


    var search by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf(JobFilterState()) }

    LaunchedEffect(Unit) {
        jobViewModel.loadRecommendations(preference)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue)
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = {
                    Text(
                        "Search jobs", style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.searchicon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp),
                        tint = Gray,
                    )
                },
                shape = RoundedCornerShape(15.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = NormalBlue,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .height(50.dp)
                    .width(300.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { showFilterSheet = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .height(50.dp)
                    .width(56.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.filter),
                    contentDescription = "Filter",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Card(
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
            ) {
                Text(
                    "Profile Completed", style = TextStyle(
                        fontSize = 18.sp,
                        color = Color.DarkGray
                    ),
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Card(
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
            ) {
                Text(
                    "Calendar", style = TextStyle(
                        fontSize = 18.sp,
                        color = Color.DarkGray
                    ),
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp)
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recommended Jobs", style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "Show All", style = TextStyle(
                        fontSize = 18.sp
                    )
                )
            }
        }

        Card(
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(395.dp)
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {

                if (recommendedJobs.isEmpty()) {
                    Text(
                        text = if (message.isNotEmpty()) message else "No recommended jobs yet",
                        color = Color.Gray
                    )
                } else {
                    LazyColumn {
                        items(recommendedJobs.size) { index ->
                            val job = recommendedJobs[index]

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )

                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = job.title, color = Color.Black)
                                    Text(text = job.position, color = Color.DarkGray)
                                    Text(text = job.jobType, color = Color.Blue)
                                    Text(text = job.skills, color = Color.Gray)
                                    Text(text = job.salary, color = Color.Green)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}