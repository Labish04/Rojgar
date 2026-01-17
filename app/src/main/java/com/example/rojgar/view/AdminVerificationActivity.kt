package com.example.rojgar.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.repository.AdminRepoImpl
import com.example.rojgar.repository.UserRepo
import com.example.rojgar.viewmodel.AdminViewModel


class AdminVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AdminVerificationScreen(
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVerificationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { AdminRepoImpl() }
    val viewModel = remember { AdminViewModel(repository) }
    val userRepo = remember { UserRepo() }

    val pendingRequests by viewModel.pendingVerificationRequests.observeAsState(emptyList())
    val verifiedCompanies by viewModel.verifiedCompanies.observeAsState(emptyList())
    val rejectedCompanies by viewModel.rejectedCompanies.observeAsState(emptyList())
    val stats by viewModel.verificationStats.observeAsState(emptyMap())
    val loading by viewModel.loading.observeAsState(false)

    var selectedTab by remember { mutableStateOf(0) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var selectedCompany by remember { mutableStateOf<CompanyModel?>(null) }

    // Fetch data
    LaunchedEffect(Unit) {
        viewModel.fetchPendingVerificationRequests()
        viewModel.fetchVerifiedCompanies()
        viewModel.fetchRejectedCompanies()
        viewModel.fetchVerificationStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Verification Management",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6366F1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Statistics Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Pending",
                    value = "${stats["pending"] ?: 0}",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Approved",
                    value = "${stats["approved"] ?: 0}",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Rejected",
                    value = "${stats["rejected"] ?: 0}",
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF6366F1),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Pending (${pendingRequests.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Verified (${verifiedCompanies.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Rejected (${rejectedCompanies.size})",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Content
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> CompanyVerificationList(
                        companies = pendingRequests,
                        onCompanyClick = {
                            selectedCompany = it
                            showDetailsDialog = true
                        }
                    )
                    1 -> CompanyVerificationList(
                        companies = verifiedCompanies,
                        onCompanyClick = {
                            selectedCompany = it
                            showDetailsDialog = true
                        }
                    )
                    2 -> CompanyVerificationList(
                        companies = rejectedCompanies,
                        onCompanyClick = {
                            selectedCompany = it
                            showDetailsDialog = true
                        }
                    )
                }

                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6366F1))
                    }
                }
            }
        }

        // Details Dialog
        if (showDetailsDialog && selectedCompany != null) {
            CompanyVerificationDetailsDialog(
                company = selectedCompany!!,
                onDismiss = {
                    showDetailsDialog = false
                    selectedCompany = null
                },
                onApprove = {
                    val adminId = userRepo.getCurrentUserId()
                    viewModel.approveCompanyVerification(
                        selectedCompany!!.companyId,
                        adminId
                    )
                    Toast.makeText(
                        context,
                        "Company verified successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    showDetailsDialog = false
                    selectedCompany = null
                },
                onReject = { reason ->
                    val adminId = userRepo.getCurrentUserId()
                    viewModel.rejectCompanyVerification(
                        selectedCompany!!.companyId,
                        adminId,
                        reason
                    )
                    Toast.makeText(
                        context,
                        "Verification rejected",
                        Toast.LENGTH_SHORT
                    ).show()
                    showDetailsDialog = false
                    selectedCompany = null
                }
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CompanyVerificationList(
    companies: List<CompanyModel>,
    onCompanyClick: (CompanyModel) -> Unit
) {
    if (companies.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No companies found",
                    fontSize = 16.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(companies) { company ->
                CompanyVerificationCard(
                    company = company,
                    onClick = { onCompanyClick(company) }
                )
            }
        }
    }
}

@Composable
fun CompanyVerificationCard(
    company: CompanyModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Company Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF667EEA),
                                Color(0xFF764BA2)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (company.companyProfileImage.isNotEmpty()) {
                    AsyncImage(
                        model = company.companyProfileImage,
                        contentDescription = "Company Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = company.companyName.firstOrNull()?.toString() ?: "C",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Company Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = company.companyName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = company.companyEmail,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (company.verificationRequestDate.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Requested: ${company.verificationRequestDate}",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (company.verificationStatus) {
                    "approved" -> Color(0xFFDCFCE7)
                    "rejected" -> Color(0xFFFEE2E2)
                    else -> Color(0xFFFEF3C7)
                }
            ) {
                Text(
                    text = when (company.verificationStatus) {
                        "approved" -> "Verified"
                        "rejected" -> "Rejected"
                        else -> "Pending"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (company.verificationStatus) {
                        "approved" -> Color(0xFF065F46)
                        "rejected" -> Color(0xFF991B1B)
                        else -> Color(0xFF92400E)
                    }
                )
            }
        }
    }
}

@Composable
fun CompanyVerificationDetailsDialog(
    company: CompanyModel,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: (String) -> Unit
) {
    val context = LocalContext.current
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onDismiss() }
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF667EEA),
                                    Color(0xFF764BA2)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Verification Details",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { onDismiss() },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Company Info
                    DetailRow(
                        icon = Icons.Default.Face,
                        label = "Company Name",
                        value = company.companyName
                    )
                    DetailRow(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = company.companyEmail
                    )
                    DetailRow(
                        icon = Icons.Default.Phone,
                        label = "Phone",
                        value = company.companyContactNumber
                    )
                    DetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        value = company.companyLocation
                    )
                    DetailRow(
                        icon = Icons.Default.DateRange,
                        label = "Request Date",
                        value = company.verificationRequestDate
                    )

                    HorizontalDivider()

                    // Verification Document
                    Text(
                        text = "Verification Document",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    if (company.verificationDocument.isNotEmpty()) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(company.verificationDocument))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Document")
                        }
                    } else {
                        Text(
                            text = "No document uploaded",
                            fontSize = 14.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (company.verificationStatus == "rejected" && company.verificationRejectionReason.isNotEmpty()) {
                        HorizontalDivider()

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEE2E2)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Rejection Reason",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = company.verificationRejectionReason,
                                    fontSize = 14.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }
                    }
                }

                // Actions
                if (company.verificationStatus == "pending") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showRejectDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFEF4444)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    Color(0xFFEF4444)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reject",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = onApprove,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Approve",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reject Reason Dialog
        if (showRejectDialog) {
            AlertDialog(
                onDismissRequest = { showRejectDialog = false },
                title = { Text("Rejection Reason") },
                text = {
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        placeholder = { Text("Enter reason for rejection...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (rejectionReason.isNotBlank()) {
                                onReject(rejectionReason)
                                showRejectDialog = false
                            }
                        },
                        enabled = rejectionReason.isNotBlank()
                    ) {
                        Text("Reject")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRejectDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF6366F1),
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value.ifEmpty { "N/A" },
                fontSize = 15.sp,
                color = Color(0xFF111827),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}