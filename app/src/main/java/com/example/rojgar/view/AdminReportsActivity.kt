package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rojgar.model.ReportModel
import com.example.rojgar.viewmodel.ReportsViewModel
import java.text.SimpleDateFormat
import java.util.*

class AdminReportsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminReportsScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(reportsViewModel: ReportsViewModel = viewModel()) {
    val allReports by reportsViewModel.allReports.observeAsState(emptyList())
    val isLoading by reportsViewModel.isLoading.observeAsState(false)
    val errorMessage by reportsViewModel.errorMessage.observeAsState("")

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Load reports when the screen is first shown
    LaunchedEffect(Unit) {
        reportsViewModel.loadAllReports()
    }

    // Filter reports based on selected filter and search query
    val filteredReports = remember(allReports, selectedFilter, searchQuery) {
        var reports = when (selectedFilter) {
            "all" -> allReports
            "pending" -> allReports.filter { it.status == "pending" }
            "reviewing" -> allReports.filter { it.status == "reviewing" }
            "resolved" -> allReports.filter { it.status == "resolved" }
            "dismissed" -> allReports.filter { it.status == "dismissed" }
            else -> allReports
        }

        if (searchQuery.isNotEmpty()) {
            reports = reportsViewModel.searchReports(searchQuery)
        }

        reports.sortedByDescending { it.createdAt }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Company Reports",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "${filteredReports.size} total reports",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0)
                ),
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Filter",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { reportsViewModel.loadAllReports() },
                containerColor = Color(0xFF1565C0),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            if (isLoading && allReports.isEmpty()) {
                LoadingScreen()
            } else if (errorMessage.isNotEmpty() && allReports.isEmpty()) {
                ErrorScreen(
                    message = errorMessage,
                    onRetry = { reportsViewModel.loadAllReports() }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Search Bar
                    SearchBars(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        modifier = Modifier.padding(16.dp)
                    )

                    // Statistics Cards
                    val stats = reportsViewModel.getReportStatistics()
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        item {
                            AnimatedReportStatCard(
                                title = "Total",
                                value = stats["total"].toString(),
                                icon = Icons.Default.DateRange,
                                color = Color(0xFF2196F3),
                                isSelected = selectedFilter == "all",
                                onClick = { selectedFilter = "all" }
                            )
                        }
                        item {
                            AnimatedReportStatCard(
                                title = "Pending",
                                value = stats["pending"].toString(),
                                icon = Icons.Default.Star,
                                color = Color(0xFFF59E0B),
                                isSelected = selectedFilter == "pending",
                                onClick = { selectedFilter = "pending" }
                            )
                        }
                        item {
                            AnimatedReportStatCard(
                                title = "Reviewing",
                                value = stats["reviewing"].toString(),
                                icon = Icons.Default.Face,
                                color = Color(0xFF3B82F6),
                                isSelected = selectedFilter == "reviewing",
                                onClick = { selectedFilter = "reviewing" }
                            )
                        }
                        item {
                            AnimatedReportStatCard(
                                title = "Resolved",
                                value = stats["resolved"].toString(),
                                icon = Icons.Default.CheckCircle,
                                color = Color(0xFF10B981),
                                isSelected = selectedFilter == "resolved",
                                onClick = { selectedFilter = "resolved" }
                            )
                        }
                        item {
                            AnimatedReportStatCard(
                                title = "Dismissed",
                                value = stats["dismissed"].toString(),
                                icon = Icons.Default.Close,
                                color = Color(0xFF6B7280),
                                isSelected = selectedFilter == "dismissed",
                                onClick = { selectedFilter = "dismissed" }
                            )
                        }
                    }

                    // Reports List
                    if (filteredReports.isEmpty()) {
                        EmptyReportsScreen(filterType = selectedFilter)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredReports, key = { it.reportId }) { report ->
                                EnhancedReportCard(
                                    report = report,
                                    onStatusUpdate = { status, notes ->
                                        reportsViewModel.updateReportStatus(
                                            report.reportId,
                                            status,
                                            notes,
                                            "Admin"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Filter Dialog
            if (showFilterDialog) {
                FilterDialog(
                    currentFilter = selectedFilter,
                    onFilterSelected = { filter ->
                        selectedFilter = filter
                        showFilterDialog = false
                    },
                    onDismiss = { showFilterDialog = false }
                )
            }
        }
    }
}

@Composable
fun SearchBars(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search reports by company, reporter, or category...") },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF1565C0)
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color.Gray
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF1565C0),
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun AnimatedReportStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 4.dp,
        label = ""
    )

    Card(
        modifier = Modifier
            .width(140.dp)
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f)
                        else color.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) Color.White else color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF1565C0)
            )

            Text(
                text = title,
                fontSize = 12.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color.Gray
            )
        }
    }
}

@Composable
fun EnhancedReportCard(
    report: ReportModel,
    onStatusUpdate: (String, String) -> Unit
) {
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF2196F3), Color(0xFF1565C0))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = report.reportedCompanyName.firstOrNull()?.toString() ?: "C",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = report.reportedCompanyName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Reported by: ${report.reporterName}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (report.status) {
                        "pending" -> Color(0xFFF59E0B)
                        "reviewing" -> Color(0xFF3B82F6)
                        "resolved" -> Color(0xFF10B981)
                        "dismissed" -> Color(0xFF6B7280)
                        else -> Color.Gray
                    }
                ) {
                    Text(
                        text = report.status.replaceFirstChar { it.uppercase() },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color.Gray.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(12.dp))

            // Category Badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getCategoryColor(report.reportCategory).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(report.reportCategory),
                            contentDescription = null,
                            tint = getCategoryColor(report.reportCategory),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = report.reportCategory.replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = getCategoryColor(report.reportCategory)
                        )
                    }
                }

                Text(
                    text = formatDate(report.createdAt),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Report Reason
            Text(
                text = report.reportReason,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            // Expanded Details
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = report.description,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 18.sp
                    )

                    // Admin Notes
                    if (report.adminNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF3F4F6)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF1565C0),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Admin Notes",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = report.adminNotes,
                                    fontSize = 12.sp,
                                    color = Color(0xFF374151),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Evidence URLs
                    if (report.evidenceUrls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = null,
                                tint = Color(0xFF1565C0),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${report.evidenceUrls.size} evidence file(s) attached",
                                fontSize = 12.sp,
                                color = Color(0xFF1565C0),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDetailsDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF1565C0)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFF1565C0)
                            )
                        ) {
                            Icon(
                                Icons.Default.Face,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Details", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showStatusDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1565C0)
                            )
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Update", fontSize = 12.sp)
                        }
                    }

                    // Timestamps
                    Spacer(modifier = Modifier.height(8.dp))
                    if (report.resolvedAt > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Resolved: ${formatDate(report.resolvedAt)}",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                            if (report.resolvedBy.isNotEmpty()) {
                                Text(
                                    text = "By: ${report.resolvedBy}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Status Update Dialog
    if (showStatusDialog) {
        StatusUpdateDialog(
            currentStatus = report.status,
            onDismiss = { showStatusDialog = false },
            onConfirm = { status, notes ->
                onStatusUpdate(status, notes)
                showStatusDialog = false
            }
        )
    }

    // Details Dialog
    if (showDetailsDialog) {
        ReportDetailsDialog(
            report = report,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@Composable
fun StatusUpdateDialog(
    currentStatus: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var adminNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color(0xFF1565C0)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Report Status", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    "Select Status",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Status Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("pending", "reviewing", "resolved", "dismissed").forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = {
                                Text(
                                    status.replaceFirstChar { it.uppercase() },
                                    fontSize = 13.sp
                                )
                            },
                            leadingIcon = {
                                if (selectedStatus == status) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1565C0),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Admin Notes",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF374151)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = adminNotes,
                    onValueChange = { adminNotes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add notes about this action...", fontSize = 13.sp) },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1565C0)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedStatus, adminNotes) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0)
                )
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportDetailsDialog(
    report: ReportModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF1565C0)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Report Details", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    DetailItems("Report ID", report.reportId)
                }
                item {
                    DetailItems("Reported Company", report.reportedCompanyName)
                }
                item {
                    DetailItems("Company ID", report.reportedCompanyId)
                }
                item {
                    Divider()
                }
                item {
                    DetailItems("Reporter Name", report.reporterName)
                }
                item {
                    DetailItems("Reporter Type", report.reporterType)
                }
                item {
                    DetailItems("Reporter ID", report.reporterId)
                }
                item {
                    Divider()
                }
                item {
                    DetailItems("Category", report.reportCategory)
                }
                item {
                    DetailItems("Reason", report.reportReason)
                }
                item {
                    DetailItems("Description", report.description)
                }
                item {
                    DetailItems("Status", report.status.replaceFirstChar { it.uppercase() })
                }
                if (report.adminNotes.isNotEmpty()) {
                    item {
                        DetailItem("Admin Notes", report.adminNotes)
                    }
                }
                item {
                    Divider()
                }
                item {
                    DetailItems("Created At", formatDate(report.createdAt))
                }
                item {
                    DetailItems("Updated At", formatDate(report.updatedAt))
                }
                if (report.resolvedAt > 0) {
                    item {
                        DetailItems("Resolved At", formatDate(report.resolvedAt))
                    }
                    item {
                        DetailItems("Resolved By", report.resolvedBy)
                    }
                }
                if (report.evidenceUrls.isNotEmpty()) {
                    item {
                        Divider()
                    }
                    item {
                        Text(
                            "Evidence Files",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1565C0)
                        )
                    }
                    items(report.evidenceUrls.size) { index ->
                        Text(
                            "${index + 1}. ${report.evidenceUrls[index]}",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0)
                )
            ) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun DetailItems(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF374151)
        )
    }
}

@Composable
fun FilterDialog(
    currentFilter: String,
    onFilterSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Filter Reports", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterOption("All Reports", "all", currentFilter, onFilterSelected)
                FilterOption("Pending", "pending", currentFilter, onFilterSelected)
                FilterOption("Reviewing", "reviewing", currentFilter, onFilterSelected)
                FilterOption("Resolved", "resolved", currentFilter, onFilterSelected)
                FilterOption("Dismissed", "dismissed", currentFilter, onFilterSelected)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF1565C0))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun FilterOption(
    label: String,
    value: String,
    currentFilter: String,
    onSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(value) },
        colors = CardDefaults.cardColors(
            containerColor = if (currentFilter == value) Color(0xFF1565C0) else Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (currentFilter == value) Color.White else Color(0xFF374151)
            )
            if (currentFilter == value) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyReportsScreen(filterType: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "No Reports",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (filterType) {
                    "all" -> "No reports found"
                    else -> "No ${filterType} reports"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reports will appear here when available",
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF1565C0),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Loading reports...",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Clear,
                contentDescription = "Error",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Oops! Something went wrong",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF374151),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0)
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "spam" -> Color(0xFFF59E0B)
        "inappropriate" -> Color(0xFFEF4444)
        "fake" -> Color(0xFF8B5CF6)
        "harassment" -> Color(0xFFDC2626)
        "illegal" -> Color(0xFF991B1B)
        "scam" -> Color(0xFFF97316)
        "privacy" -> Color(0xFF0891B2)
        else -> Color(0xFF6B7280)
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "spam" -> Icons.Default.Warning
        "inappropriate" -> Icons.Default.Clear
        "fake" -> Icons.Default.Face
        "harassment" -> Icons.Default.Person
        "illegal" -> Icons.Default.Info
        "scam" -> Icons.Default.Warning
        "privacy" -> Icons.Default.Lock
        else -> Icons.Default.Info
    }
}