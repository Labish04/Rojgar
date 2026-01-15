package com.example.rojgar.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.viewmodel.JobSeekerViewModel

@Composable
fun AdminJobSeekerScreen(viewModel: JobSeekerViewModel) {
    var jobSeekers by remember { mutableStateOf<List<JobSeekerModel>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var selectedJobSeeker by remember { mutableStateOf<JobSeekerModel?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter job seekers based on search query
    val filteredJobSeekers = remember(jobSeekers, searchQuery) {
        if (searchQuery.isBlank()) {
            jobSeekers
        } else {
            jobSeekers.filter { jobSeeker ->
                jobSeeker.fullName.contains(searchQuery, ignoreCase = true) ||
                        jobSeeker.email.contains(searchQuery, ignoreCase = true) ||
                        jobSeeker.profession.contains(searchQuery, ignoreCase = true) ||
                        jobSeeker.currentAddress.contains(searchQuery, ignoreCase = true) ||
                        jobSeeker.phoneNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        loading = true
        viewModel.getAllJobSeeker { success, message, fetchedJobSeekers ->
            loading = false
            if (success && fetchedJobSeekers != null) {
                jobSeekers = fetchedJobSeekers
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF1565C0)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        placeholder = { Text("Search job seekers...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF1565C0)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1565C0),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            cursorColor = Color(0xFF1565C0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty())
                                "Found ${filteredJobSeekers.size} results"
                            else
                                "All Job Seekers (${jobSeekers.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    }
                }

                items(filteredJobSeekers) { jobSeeker ->
                    JobSeekerCard(
                        jobSeeker = jobSeeker,
                        onClick = {
                            selectedJobSeeker = jobSeeker
                            showDetailsDialog = true
                        }
                    )
                }

                if (jobSeekers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No job seekers found",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                if (jobSeekers.isNotEmpty() && filteredJobSeekers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "No results",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No job seekers match your search",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDetailsDialog && selectedJobSeeker != null) {
        JobSeekerDetailsDialog(
            jobSeeker = selectedJobSeeker!!,
            onDismiss = {
                showDetailsDialog = false
                selectedJobSeeker = null
            }
        )
    }
}

@Composable
fun JobSeekerCard(
    jobSeeker: JobSeekerModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Photo
            if (jobSeeker.profilePhoto.isNotEmpty()) {
                AsyncImage(
                    model = jobSeeker.profilePhoto,
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F7FA)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2196F3), Color(0xFF1565C0))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = jobSeeker.fullName.firstOrNull()?.toString()?.uppercase() ?: "U",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = jobSeeker.fullName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (jobSeeker.profession.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = jobSeeker.profession,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (jobSeeker.email.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = jobSeeker.email,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "View Details",
                tint = Color(0xFF1565C0),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun JobSeekerDetailsDialog(
    jobSeeker: JobSeekerModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF1565C0))
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (jobSeeker.profilePhoto.isNotEmpty()) {
                    AsyncImage(
                        model = jobSeeker.profilePhoto,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F7FA)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = jobSeeker.fullName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profession
                if (jobSeeker.profession.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Build,
                            title = "Profession",
                            content = jobSeeker.profession
                        )
                    }
                }

                // Bio
                if (jobSeeker.bio.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Info,
                            title = "Bio",
                            content = jobSeeker.bio
                        )
                    }
                }

                // Email
                if (jobSeeker.email.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Email,
                            title = "Email",
                            content = jobSeeker.email
                        )
                    }
                }

                // Phone
                if (jobSeeker.phoneNumber.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Phone,
                            title = "Phone Number",
                            content = jobSeeker.phoneNumber
                        )
                    }
                }

                // Gender
                if (jobSeeker.gender.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Person,
                            title = "Gender",
                            content = jobSeeker.gender
                        )
                    }
                }

                // Date of Birth
                if (jobSeeker.dob.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.DateRange,
                            title = "Date of Birth",
                            content = jobSeeker.dob
                        )
                    }
                }

                // Current Address
                if (jobSeeker.currentAddress.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.LocationOn,
                            title = "Current Address",
                            content = jobSeeker.currentAddress
                        )
                    }
                }

                // Permanent Address
                if (jobSeeker.permanentAddress.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Home,
                            title = "Permanent Address",
                            content = jobSeeker.permanentAddress
                        )
                    }
                }

                // Nationality
                if (jobSeeker.nationality.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Place,
                            title = "Nationality",
                            content = jobSeeker.nationality
                        )
                    }
                }

                // Religion
                if (jobSeeker.religion.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Star,
                            title = "Religion",
                            content = jobSeeker.religion
                        )
                    }
                }

                // Marital Status
                if (jobSeeker.maritalStatus.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Favorite,
                            title = "Marital Status",
                            content = jobSeeker.maritalStatus
                        )
                    }
                }

                // Status
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (jobSeeker.isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = "Status",
                            tint = if (jobSeeker.isActive) Color(0xFF4CAF50) else Color(0xFFFFA726),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Status: ${if (jobSeeker.isActive) "Active" else "Inactive"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (jobSeeker.isActive) Color(0xFF4CAF50) else Color(0xFFFFA726)
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}