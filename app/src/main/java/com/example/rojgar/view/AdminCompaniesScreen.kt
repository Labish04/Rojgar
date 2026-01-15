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
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.viewmodel.CompanyViewModel

@Composable
fun AdminCompaniesScreen(viewModel: CompanyViewModel) {
    var companies by remember { mutableStateOf<List<CompanyModel>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var selectedCompany by remember { mutableStateOf<CompanyModel?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter companies based on search query
    val filteredCompanies = remember(companies, searchQuery) {
        if (searchQuery.isBlank()) {
            companies
        } else {
            companies.filter { company ->
                company.companyName.contains(searchQuery, ignoreCase = true) ||
                        company.companyTagline.contains(searchQuery, ignoreCase = true) ||
                        company.companyIndustry.contains(searchQuery, ignoreCase = true) ||
                        company.companyLocation.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        loading = true
        viewModel.getAllCompany { success, message, fetchedCompanies ->
            loading = false
            if (success && fetchedCompanies != null) {
                companies = fetchedCompanies
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
                        placeholder = { Text("Search companies...") },
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
                                "Found ${filteredCompanies.size} results"
                            else
                                "All Companies (${companies.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    }
                }

                items(filteredCompanies) { company ->
                    CompanyCard(
                        company = company,
                        onClick = {
                            selectedCompany = company
                            showDetailsDialog = true
                        }
                    )
                }

                if (companies.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No companies found",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                if (companies.isNotEmpty() && filteredCompanies.isEmpty()) {
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
                                    text = "No companies match your search",
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

    if (showDetailsDialog && selectedCompany != null) {
        CompanyDetailsDialog(
            company = selectedCompany!!,
            onDismiss = {
                showDetailsDialog = false
                selectedCompany = null
            }
        )
    }
}

@Composable
fun CompanyCard(
    company: CompanyModel,
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
            // Company Logo
            if (company.companyProfileImage.isNotEmpty()) {
                AsyncImage(
                    model = company.companyProfileImage,
                    contentDescription = "Company Logo",
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
                        text = company.companyName.firstOrNull()?.toString()?.uppercase() ?: "C",
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
                    text = company.companyName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (company.companyTagline.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = company.companyTagline,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (company.companyIndustry.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF2196F3).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = company.companyIndustry,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
fun CompanyDetailsDialog(
    company: CompanyModel,
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
                if (company.companyProfileImage.isNotEmpty()) {
                    AsyncImage(
                        model = company.companyProfileImage,
                        contentDescription = "Company Logo",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F7FA)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = company.companyName,
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
                // Tagline
                if (company.companyTagline.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Star,
                            title = "Tagline",
                            content = company.companyTagline
                        )
                    }
                }

                // Information
                if (company.companyInformation.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Info,
                            title = "About",
                            content = company.companyInformation
                        )
                    }
                }

                // Industry
                if (company.companyIndustry.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Build,
                            title = "Industry",
                            content = company.companyIndustry
                        )
                    }
                }

                // Email
                if (company.companyEmail.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Email,
                            title = "Email",
                            content = company.companyEmail
                        )
                    }
                }

                // Contact
                if (company.companyContactNumber.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Phone,
                            title = "Contact",
                            content = company.companyContactNumber
                        )
                    }
                }

                // Location
                if (company.companyLocation.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.LocationOn,
                            title = "Location",
                            content = company.companyLocation
                        )
                    }
                }

                // Website
                if (company.companyWebsite.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.Search,
                            title = "Website",
                            content = company.companyWebsite
                        )
                    }
                }

                // Established Date
                if (company.companyEstablishedDate.isNotEmpty()) {
                    item {
                        DetailSection(
                            icon = Icons.Default.DateRange,
                            title = "Established",
                            content = company.companyEstablishedDate
                        )
                    }
                }

                // Specialties
                if (company.companySpecialties.isNotEmpty()) {
                    item {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = "Specialties",
                                    tint = Color(0xFF1565C0),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Specialties",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                            }
                            company.companySpecialties.forEach { specialty ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF2196F3).copy(alpha = 0.1f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = specialty,
                                        fontSize = 13.sp,
                                        color = Color(0xFF2196F3),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Status
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (company.isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = "Status",
                            tint = if (company.isActive) Color(0xFF4CAF50) else Color(0xFFFFA726),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Status: ${if (company.isActive) "Active" else "Inactive"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (company.isActive) Color(0xFF4CAF50) else Color(0xFFFFA726)
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
fun DetailSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF1565C0),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
        }
        Text(
            text = content,
            fontSize = 13.sp,
            color = Color.Gray,
            lineHeight = 18.sp
        )
    }
}