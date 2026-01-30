package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.rojgar.model.HelpSupportModel
import com.example.rojgar.viewmodel.HelpSupportViewModel
import com.google.firebase.auth.FirebaseAuth

class AdminHelpAndSupportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdminHelpAndSupportScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHelpAndSupportScreen(
    viewModel: HelpSupportViewModel = viewModel()
) {
    val helpRequests by viewModel.myRequests.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedRequest by remember { mutableStateOf<HelpSupportModel?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    // Load all help requests on start
    LaunchedEffect(Unit) {
        viewModel.getAllHelpRequests()
    }

    // Filter requests based on selected filter
    val filteredRequests = when (selectedFilter) {
        "All" -> helpRequests
        "Pending" -> helpRequests.filter { it.status == "Pending" }
        "In Progress" -> helpRequests.filter { it.status == "In Progress" }
        "Resolved" -> helpRequests.filter { it.status == "Resolved" }
        "Urgent" -> helpRequests.filter { it.priority == "Urgent" }
        "Job Seeker" -> helpRequests.filter { it.userType == "JobSeeker" }
        "Company" -> helpRequests.filter { it.userType == "Company" }
        else -> helpRequests
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Help & Support",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${filteredRequests.size} requests",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0)
                ),
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.getAllHelpRequests() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Statistics Section
                StatisticsSection(helpRequests)

                // Filter Chips
                FilterChipsSection(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    requestCounts = helpRequests
                )

                // Requests List
                if (isLoading && helpRequests.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF1565C0))
                    }
                } else if (filteredRequests.isEmpty()) {
                    EmptyStateView(selectedFilter)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredRequests) { request ->
                            AnimatedRequestCard(
                                request = request,
                                onClick = {
                                    selectedRequest = request
                                    showDetailsDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // Loading Overlay
            if (isLoading && helpRequests.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }

    // Request Details Dialog
    if (showDetailsDialog && selectedRequest != null) {
        RequestDetailsDialog(
            request = selectedRequest!!,
            viewModel = viewModel,
            onDismiss = {
                showDetailsDialog = false
                selectedRequest = null
            }
        )
    }
}

@Composable
fun StatisticsSection(requests: List<HelpSupportModel>) {
    val pendingCount = requests.count { it.status == "Pending" }
    val inProgressCount = requests.count { it.status == "In Progress" }
    val resolvedCount = requests.count { it.status == "Resolved" }
    val urgentCount = requests.count { it.priority == "Urgent" }

    val stats = listOf(
        Triple("Pending", pendingCount.toString(), Color(0xFFFFA726)),
        Triple("In Progress", inProgressCount.toString(), Color(0xFF2196F3)),
        Triple("Resolved", resolvedCount.toString(), Color(0xFF4CAF50)),
        Triple("Urgent", urgentCount.toString(), Color(0xFFF44336))
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stats) { (title, value, color) ->
            AnimatedStatCard(title, value, color)
        }
    }
}

@Composable
fun AnimatedStatCard(title: String, value: String, color: Color) {
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

    Card(
        modifier = Modifier
            .width(140.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FilterChipsSection(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    requestCounts: List<HelpSupportModel>
) {
    val filters = listOf(
        "All",
        "Pending",
        "In Progress",
        "Resolved",
        "Urgent",
        "Job Seeker",
        "Company"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter,
                        fontSize = 13.sp,
                        fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF1565C0),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFE3F2FD),
                    labelColor = Color(0xFF1565C0)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == filter,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun AnimatedRequestCard(
    request: HelpSupportModel,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
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
                            Icon(
                                imageVector = if (request.userType == "Company")
                                    Icons.Default.Email else Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = request.userName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                text = request.userEmail,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Priority Badge
                    PriorityBadge(request.priority)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Problem Type & Request ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = request.problemType,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1565C0)
                        )
                    }

                    Text(
                        text = request.requestId,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = request.description,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User Type Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = request.userType,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1565C0),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Timestamp
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = request.createdAt.take(10),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // Status Badge
                        StatusBadge(request.status)
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val (color, icon) = when (priority) {
        "Urgent" -> Color(0xFFF44336) to Icons.Default.Warning
        "High" -> Color(0xFFFFA726) to Icons.Default.Star
        "Medium" -> Color(0xFF2196F3) to Icons.Default.Info
        "Low" -> Color(0xFF4CAF50) to Icons.Default.CheckCircle
        else -> Color.Gray to Icons.Default.Info
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = priority,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Pending" -> Color(0xFFFFA726)
        "In Progress" -> Color(0xFF2196F3)
        "Resolved" -> Color(0xFF4CAF50)
        "Closed" -> Color.Gray
        else -> Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color
    ) {
        Text(
            text = status,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyStateView(filter: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No $filter requests found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Try adjusting your filters",
                fontSize = 14.sp,
                color = Color.LightGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailsDialog(
    request: HelpSupportModel,
    viewModel: HelpSupportViewModel,
    onDismiss: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(request.status) }
    var adminNotes by remember { mutableStateOf(request.adminNotes) }
    var showStatusMenu by remember { mutableStateOf(false) }
    val submitSuccess by viewModel.submitSuccess.observeAsState()
    val submitMessage by viewModel.submitMessage.observeAsState()

    LaunchedEffect(submitSuccess) {
        if (submitSuccess == true) {
            onDismiss()
            viewModel.resetSubmitState()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Request Details",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Gray
                            )
                        }
                    }
                }

                // Request ID
                item {
                    DetailsRow(
                        icon = Icons.Default.Info,
                        label = "Request ID",
                        value = request.requestId
                    )
                }

                // User Information
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "User Information",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            DetailsRow(
                                icon = Icons.Default.Person,
                                label = "Name",
                                value = request.userName
                            )
                            DetailsRow(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = request.userEmail
                            )
                            DetailsRow(
                                icon = Icons.Default.Build,
                                label = "Type",
                                value = request.userType
                            )
                        }
                    }
                }

                // Problem Details
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Problem Details",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            DetailsRow(
                                icon = Icons.Default.Create,
                                label = "Type",
                                value = request.problemType
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "Priority:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                )
                                PriorityBadge(request.priority)
                            }
                        }
                    }
                }

                // Description
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Description",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                        Text(
                            text = request.description,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Screenshot
                if (request.screenshotUrl.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Screenshot",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            AsyncImage(
                                model = request.screenshotUrl,
                                contentDescription = "Screenshot",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                // Timestamps
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailsRow(
                                icon = Icons.Default.DateRange,
                                label = "Created",
                                value = request.createdAt
                            )
                            DetailsRow(
                                icon = Icons.Default.DateRange,
                                label = "Updated",
                                value = request.updatedAt
                            )
                            if (request.resolvedAt.isNotEmpty()) {
                                DetailsRow(
                                    icon = Icons.Default.CheckCircle,
                                    label = "Resolved",
                                    value = request.resolvedAt
                                )
                            }
                        }
                    }
                }

                // Update Status Section
                item {
                    HorizontalDivider()
                }

                item {
                    Text(
                        text = "Update Status",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }

                // Status Dropdown
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Status",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        ExposedDropdownMenuBox(
                            expanded = showStatusMenu,
                            onExpandedChange = { showStatusMenu = it }
                        ) {
                            OutlinedTextField(
                                value = selectedStatus,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (showStatusMenu)
                                            Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF1565C0),
                                    unfocusedBorderColor = Color.LightGray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false }
                            ) {
                                listOf("Pending", "In Progress", "Resolved", "Closed").forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status) },
                                        onClick = {
                                            selectedStatus = status
                                            showStatusMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Admin Notes
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Admin Notes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        OutlinedTextField(
                            value = adminNotes,
                            onValueChange = { adminNotes = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Add notes about this request...") },
                            minLines = 3,
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1565C0),
                                unfocusedBorderColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Submit Button
                item {
                    Button(
                        onClick = {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            viewModel.updateRequestStatus(
                                requestId = request.requestId,
                                status = selectedStatus,
                                adminNotes = adminNotes,
                                resolvedBy = currentUser?.email ?: "Admin"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1565C0)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Update Request",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Error/Success Message
                if (submitMessage?.isNotEmpty() == true) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (submitSuccess == true)
                                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                                else
                                    Color(0xFFF44336).copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = submitMessage ?: "",
                                fontSize = 12.sp,
                                color = if (submitSuccess == true) Color(0xFF4CAF50) else Color(0xFFF44336),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.4f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF1565C0),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$label:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color.DarkGray,
            modifier = Modifier.weight(0.6f),
            fontWeight = FontWeight.Medium
        )
    }
}