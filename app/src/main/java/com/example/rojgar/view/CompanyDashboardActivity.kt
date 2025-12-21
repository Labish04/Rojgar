package com.example.rojgar.view

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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rojgar.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.ui.theme.RojgarTheme

class CompanyDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompanyDashboardBody()
        }
    }
}



// Data class for Job Seeker
data class JobSeeker(
    val id: Int,
    val name: String,
    val profession: String,
    val location: String,
    val experience: String,
    val skills: List<String>,
    val availability: String
)

@Composable
fun CompanyHomeScreen() {
    var searchQuery by remember { mutableStateOf("") }

    // Sample job seekers data
    val jobSeekers = remember {
        listOf(
            JobSeeker(
                id = 1,
                name = "Rajesh Kumar",
                profession = "Android Developer",
                location = "Kathmandu",
                experience = "3 years",
                skills = listOf("Kotlin", "Jetpack Compose", "MVVM"),
                availability = "Immediately Available"
            ),
            JobSeeker(
                id = 2,
                name = "Sita Sharma",
                profession = "UI/UX Designer",
                location = "Pokhara",
                experience = "2 years",
                skills = listOf("Figma", "Adobe XD", "Prototyping"),
                availability = "Available in 2 weeks"
            ),
            JobSeeker(
                id = 3,
                name = "Amit Thapa",
                profession = "Full Stack Developer",
                location = "Lalitpur",
                experience = "5 years",
                skills = listOf("React", "Node.js", "MongoDB"),
                availability = "Immediately Available"
            ),
            JobSeeker(
                id = 4,
                name = "Priya Rai",
                profession = "Data Analyst",
                location = "Bhaktapur",
                experience = "1 year",
                skills = listOf("Python", "SQL", "Power BI"),
                availability = "Available in 1 month"
            ),
            JobSeeker(
                id = 5,
                name = "Bikash Gurung",
                profession = "DevOps Engineer",
                location = "Kathmandu",
                experience = "4 years",
                skills = listOf("AWS", "Docker", "Kubernetes"),
                availability = "Immediately Available"
            ),
            JobSeeker(
                id = 6,
                name = "Anita Maharjan",
                profession = "Digital Marketer",
                location = "Kathmandu",
                experience = "3 years",
                skills = listOf("SEO", "Social Media", "Google Ads"),
                availability = "Available in 2 weeks"
            )
        )
    }

    // Filter job seekers based on search query
    val filteredJobSeekers = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            jobSeekers
        } else {
            jobSeekers.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.profession.contains(searchQuery, ignoreCase = true) ||
                        it.location.contains(searchQuery, ignoreCase = true) ||
                        it.skills.any { skill -> skill.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Search Bar
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Results Count
        Text(
            text = "${filteredJobSeekers.size} Job Seekers Found",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Job Seekers List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredJobSeekers) { jobSeeker ->
                JobSeekerCard(jobSeeker = jobSeeker)
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        placeholder = {
            Text(
                text = "Search...",
                color = Color(0xFF9E9E9E),
                fontSize = 16.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF757575)
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun JobSeekerCard(jobSeeker: JobSeeker) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click to view full profile */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left side - Profile info
                Column(modifier = Modifier.weight(1f)) {
                    // Name
                    Text(
                        text = jobSeeker.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Profession
                    Text(
                        text = jobSeeker.profession,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Location and Experience
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoChip(
                            text = jobSeeker.location,
                            backgroundColor = Color(0xFFE3F2FD)
                        )
                        InfoChip(
                            text = jobSeeker.experience,
                            backgroundColor = Color(0xFFF3E5F5)
                        )
                    }
                }

                // Right side - Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1976D2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = jobSeeker.name.first().toString(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Skills
            Text(
                text = "Skills:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF757575)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                jobSeeker.skills.take(3).forEach { skill ->
                    SkillChip(skill = skill)
                }
                if (jobSeeker.skills.size > 3) {
                    SkillChip(skill = "+${jobSeeker.skills.size - 3} more")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Availability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = jobSeeker.availability,
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = { /* Handle view profile */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "View Profile",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(text: String, backgroundColor: Color) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            color = Color(0xFF424242)
        )
    }
}

@Composable
fun SkillChip(skill: String) {
    Surface(
        color = Color(0xFFE0E0E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = skill,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 11.sp,
            color = Color(0xFF424242)
        )
    }
}
@Preview()
@Composable
fun CompanyDashboardBody() {
    RojgarTheme {
        CompanyDashboardBody()
    }
}

