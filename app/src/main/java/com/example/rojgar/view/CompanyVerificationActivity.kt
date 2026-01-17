package com.example.rojgar.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.R
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.viewmodel.CompanyViewModel

class CompanyVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val companyId = intent.getStringExtra("COMPANY_ID") ?: ""

        setContent {
            CompanyVerificationScreen(
                companyId = companyId,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyVerificationScreen(
    companyId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { CompanyRepoImpl() }
    val viewModel = remember { CompanyViewModel(repository) }

    var selectedDocumentUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var isRequesting by remember { mutableStateOf(false) }
    var verificationStatus by remember { mutableStateOf("") }
    var verificationDocument by remember { mutableStateOf("") }
    var rejectionReason by remember { mutableStateOf("") }

    // Document picker launcher
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedDocumentUri = it }
    }

    // Fetch company verification status
    LaunchedEffect(companyId) {
        if (companyId.isNotEmpty()) {
            repository.getCompanyDetails(companyId) { success, message, company ->
                if (success && company != null) {
                    verificationStatus = company.verificationStatus
                    verificationDocument = company.verificationDocument
                    rejectionReason = company.verificationRejectionReason
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Company Verification",
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Status Card
            VerificationStatusCard(
                status = verificationStatus,
                rejectionReason = rejectionReason
            )

            // Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Verification Requirements",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    }

                    Text(
                        text = "Please upload one of the following documents:",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )

                    RequirementItem("✓ Company Registration Certificate")
                    RequirementItem("✓ Business License")
                    RequirementItem("✓ Tax Registration Document")
                    RequirementItem("✓ Incorporation Certificate")

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Document must be clear, valid, and show company name",
                                fontSize = 12.sp,
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Document Upload Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Upload Document",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    // Show current document if exists
                    if (verificationDocument.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCFCE7),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFF10B981)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Document Uploaded",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46)
                                    )
                                    Text(
                                        text = "View uploaded document",
                                        fontSize = 12.sp,
                                        color = Color(0xFF059669)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        // Open document in browser
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(verificationDocument))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "View",
                                        tint = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }

                    // Document picker
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { documentPicker.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF3F4F6),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (selectedDocumentUri != null) Color(0xFF6366F1) else Color(0xFFD1D5DB)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (selectedDocumentUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedDocumentUri),
                                    contentDescription = "Selected Document",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = "Document selected",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF6366F1)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Select Document",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    text = "Tap to choose file",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }

                    // Upload button
                    if (selectedDocumentUri != null) {
                        Button(
                            onClick = {
                                isUploading = true
                                repository.uploadVerificationDocument(
                                    companyId,
                                    context,
                                    selectedDocumentUri!!
                                ) { success, message ->
                                    isUploading = false
                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            "Document uploaded successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        verificationDocument = message
                                        selectedDocumentUri = null
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Upload failed: $message",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isUploading
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Upload Document",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Request Verification Button
            if (verificationDocument.isNotEmpty() && verificationStatus != "approved") {
                Button(
                    onClick = {
                        isRequesting = true
                        repository.requestVerification(companyId) { success, message ->
                            isRequesting = false
                            if (success) {
                                Toast.makeText(
                                    context,
                                    "Verification request submitted!",
                                    Toast.LENGTH_LONG
                                ).show()
                                verificationStatus = "pending"
                            } else {
                                Toast.makeText(
                                    context,
                                    "Request failed: $message",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isRequesting
                ) {
                    if (isRequesting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Request Verification",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationStatusCard(
    status: String,
    rejectionReason: String
) {
    val (statusColor, statusIcon, statusText, statusBg) = when (status) {
        "approved" -> listOf(
            Color(0xFF10B981),
            Icons.Default.CheckCircle,
            "Verified Company",
            Color(0xFFDCFCE7)
        )
        "pending" -> listOf(
            Color(0xFFF59E0B),
            Icons.Default.Warning,
            "Verification Pending",
            Color(0xFFFEF3C7)
        )
        "rejected" -> listOf(
            Color(0xFFEF4444),
            Icons.Default.Close,
            "Verification Rejected",
            Color(0xFFFEE2E2)
        )
        else -> listOf(
            Color(0xFF6B7280),
            Icons.Default.Info,
            "Not Verified",
            Color(0xFFF3F4F6)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusBg as Color
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        statusColor as Color,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon as ImageVector,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusText as String,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )

                Text(
                    text = when (status) {
                        "approved" -> "Your company is verified"
                        "pending" -> "Under admin review"
                        "rejected" -> "Please resubmit documents"
                        else -> "Complete verification to unlock features"
                    },
                    fontSize = 14.sp,
                    color = statusColor.copy(alpha = 0.8f)
                )

                if (status == "rejected" && rejectionReason.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "Reason: $rejectionReason",
                            fontSize = 13.sp,
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RequirementItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF374151),
            fontWeight = FontWeight.Medium
        )
    }
}