package com.example.rojgar.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.R
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.FollowRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.utils.ImageUtils
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.FollowViewModel

class CompanyProfileActivity : ComponentActivity() {
    lateinit var imageUtils: ImageUtils

    var isPickingCover by mutableStateOf(false)
    var isPickingProfile by mutableStateOf(false)

    var selectedCoverUri by mutableStateOf<Uri?>(null)
    var selectedProfileUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        imageUtils = ImageUtils(this, this)
        imageUtils.registerLaunchers { uri ->
            if (uri != null) {
                if (isPickingCover) {
                    selectedCoverUri = uri
                } else if (isPickingProfile) {
                    selectedProfileUri = uri
                }

                isPickingCover = false
                isPickingProfile = false
            }
        }

        // Get the company ID from intent
        val companyId = intent.getStringExtra("COMPANY_ID") ?: ""
        val isOwnProfile = companyId.isEmpty() || companyId == getCurrentUserId()

        setContent {
            CompanyProfileBody(
                companyId = companyId,
                isOwnProfile = isOwnProfile,
                selectedCoverUri = selectedCoverUri,
                selectedProfileUri = selectedProfileUri,
                onPickCoverImage = {
                    isPickingCover = true
                    isPickingProfile = false
                    imageUtils.launchImagePicker()
                },
                onPickProfileImage = {
                    isPickingProfile = true
                    isPickingCover = false
                    imageUtils.launchImagePicker()
                }
            )
        }
    }

    private fun getCurrentUserId(): String {
        return CompanyRepoImpl().getCurrentCompany()?.uid ?: ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyProfileBody(
    companyId: String,
    isOwnProfile: Boolean,
    selectedCoverUri: Uri?,
    selectedProfileUri: Uri?,
    onPickCoverImage: () -> Unit,
    onPickProfileImage: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val followViewModel = remember { FollowViewModel(FollowRepoImpl()) }

    val company = companyViewModel.companyDetails.observeAsState(initial = null)
    val isFollowingState by followViewModel.isFollowing.observeAsState(initial = false)

    val followersCountState by followViewModel.followersCount.observeAsState(initial = 0)
    val followingCountState by followViewModel.followingCount.observeAsState(initial = 0)

    val currentCompany = remember { CompanyRepoImpl().getCurrentCompany() }
    val currentJobSeeker = remember { JobSeekerRepoImpl().getCurrentJobSeeker() }

    var isFollowing by remember { mutableStateOf(false) }
    var isUploadingCover by remember { mutableStateOf(false) }
    var isUploadingProfile by remember { mutableStateOf(false) }

    // Track displayed URLs
    var displayedCoverUrl by remember { mutableStateOf("") }
    var displayedProfileUrl by remember { mutableStateOf("") }

    var isDrawerOpen by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showConfirmPasswordDialog by remember { mutableStateOf(false) }



    val repository = remember { CompanyRepoImpl() }
    val jobSeekerRepository = remember { JobSeekerRepoImpl() }

    val currentUserId = remember {
        if (currentCompany != null) currentCompany.uid
        else if (currentJobSeeker != null) currentJobSeeker.uid
        else ""
    }

    val currentUserType = remember {
        when {
            currentCompany != null -> "Company"
            currentJobSeeker != null -> "JobSeeker"
            else -> ""
        }
    }

    LaunchedEffect(companyId) {
        if (companyId.isNotEmpty()) {
            if (isOwnProfile) {
                companyViewModel.fetchCurrentCompany()
            } else {
                companyViewModel.getCompanyDetails(companyId)
            }
        } else {
            companyViewModel.fetchCurrentCompany()
        }
    }

    LaunchedEffect(company.value) {
        company.value?.let {
            displayedCoverUrl = it.companyCoverPhoto
            displayedProfileUrl = it.companyProfileImage
        }
    }

    LaunchedEffect(selectedCoverUri) {
        if (selectedCoverUri != null && !isUploadingCover) {
            isUploadingCover = true
            companyViewModel.uploadCompanyCoverPhoto(context, selectedCoverUri) { uploadedUrl ->
                isUploadingCover = false
                if (uploadedUrl != null) {
                    displayedCoverUrl = uploadedUrl
                    company.value?.let { currentCompany ->
                        val updatedCompany = currentCompany.copy(companyCoverPhoto = uploadedUrl)
                        companyViewModel.addCompanyToDatabase(
                            currentCompany.companyId,
                            updatedCompany
                        ) { success, message ->
                            if (success) {
                                Toast.makeText(context, "Cover photo updated!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to save: $message", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Failed to upload cover photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Auto-upload profile photo when selected
    LaunchedEffect(selectedProfileUri) {
        if (selectedProfileUri != null && !isUploadingProfile) {
            isUploadingProfile = true
            companyViewModel.uploadCompanyProfileImage(context, selectedProfileUri) { uploadedUrl ->
                isUploadingProfile = false
                if (uploadedUrl != null) {
                    displayedProfileUrl = uploadedUrl

                    company.value?.let { currentCompany ->
                        val updatedCompany = currentCompany.copy(companyProfileImage = uploadedUrl)
                        companyViewModel.addCompanyToDatabase(
                            currentCompany.companyId,
                            updatedCompany
                        ) { success, message ->
                            if (success) {
                                Toast.makeText(context, "Profile photo updated!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to save: $message", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Failed to upload profile photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(company.value, currentUserId, companyId) {
        company.value?.let { targetCompany ->
            if (currentUserId.isNotEmpty() && companyId.isNotEmpty() && !isOwnProfile) {
                followViewModel.checkFollowStatus(currentUserId, companyId)
                followViewModel.getFollowersCount(companyId)
                followViewModel.getFollowingCount(companyId)
            }
        }
    }

    LaunchedEffect(followersCountState, followingCountState) {
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                if (selectedCoverUri != null || displayedCoverUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedCoverUri ?: displayedCoverUrl),
                        contentDescription = "Background Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (isOwnProfile) Modifier.clickable { onPickCoverImage() } else Modifier),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f))
                    )

                    if (isUploadingCover) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF667EEA),
                                        Color(0xFF764BA2),
                                        Color(0xFFF093FB)
                                    )
                                )
                            )
                            .then(if (isOwnProfile) Modifier.clickable { onPickCoverImage() } else Modifier)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .offset(x = 50.dp, y = 30.dp)
                                .background(
                                    Color.White.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .offset(x = 280.dp, y = 140.dp)
                                .background(
                                    Color.White.copy(alpha = 0.08f),
                                    CircleShape
                                )
                        )

                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Background",
                            modifier = Modifier
                                .size(56.dp)
                                .align(Alignment.Center),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { /* Navigate back */ },
                        modifier = Modifier
                            .shadow(8.dp, CircleShape)
                            .background(Color.White.copy(alpha = 0.95f), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1F2937)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { /* Share */ },
                            modifier = Modifier
                                .shadow(8.dp, CircleShape)
                                .background(Color.White.copy(alpha = 0.95f), CircleShape)
                                .size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color(0xFF1F2937)
                            )
                        }

                        if (isOwnProfile) {
                            IconButton(
                                onClick = { isDrawerOpen = true },
                                modifier = Modifier
                                    .shadow(8.dp, CircleShape)
                                    .background(Color.White.copy(alpha = 0.95f), CircleShape)
                                    .size(44.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_more_vert_24),
                                    contentDescription = "More",
                                    tint = Color(0xFF1F2937)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-70).dp)
                    .padding(horizontal = 20.dp)
            ) {
                Box(
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .shadow(16.dp, CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF667EEA),
                                        Color(0xFF764BA2)
                                    )
                                ),
                                CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(5.dp)
                                .then(if (isOwnProfile) Modifier.clickable { onPickProfileImage() } else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedProfileUri != null || displayedProfileUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        selectedProfileUri ?: displayedProfileUrl
                                    ),
                                    contentDescription = "Profile Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF667EEA),
                                                    Color(0xFF764BA2)
                                                )
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = company.value?.companyName?.firstOrNull()?.toString()
                                            ?: "L",
                                        fontSize = 52.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Show loading indicator for profile
                            if (isUploadingProfile) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Premium Camera Badge
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF667EEA),
                                        Color(0xFF764BA2)
                                    )
                                )
                            )
                            .border(4.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd)
                            .then(if (isOwnProfile) Modifier.clickable { onPickProfileImage() } else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = company.value?.companyName ?: "Loading...",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827),
                        letterSpacing = (-0.5).sp
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF3B82F6),
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Verified",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(20.dp)
                        )
                    }

                    // Premium Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Premium",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Enhanced Tagline
                Text(
                    text = "Leading Technology Solutions Provider",
                    fontSize = 15.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Premium Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EnhancedStatCard(
                        label = "Active Jobs",
                        value = "42",
                        icon = Icons.Default.Email,
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedStatCard(
                        label = "Employees",
                        value = "250+",
                        icon = Icons.Default.Face,
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedStatCard(
                        label = "Founded",
                        value = company.value?.companyEstablishedDate?.take(4) ?: "2015",
                        icon = Icons.Default.DateRange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!isOwnProfile) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (currentUserId.isNotEmpty() && currentUserType.isNotEmpty()) {
                                    if (isFollowingState) {
                                        followViewModel.unfollow(
                                            followerId = currentUserId,
                                            followerType = currentUserType,
                                            followingId = companyId,
                                            followingType = "Company",
                                            onComplete = { success, message ->
                                                if (success) {
                                                    Toast.makeText(
                                                        context,
                                                        "Unfollowed!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed: $message",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        )
                                    } else {
                                        followViewModel.follow(
                                            followerId = currentUserId,
                                            followerType = currentUserType,
                                            followingId = companyId,
                                            followingType = "Company",
                                            onComplete = { success, message ->
                                                if (success) {
                                                    Toast.makeText(
                                                        context,
                                                        "Followed!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed: $message",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please login to follow",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowingState) Color(0xFF10B981) else Color(
                                    0xFF6366F1
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = if (isFollowingState) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFollowingState) "Following" else "Follow",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                // Navigate to chat/message screen
                                Toast.makeText(
                                    context,
                                    "Message feature coming soon!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF6366F1),
                                containerColor = Color.White
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Message",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // About Section with gradient accent
                EnhancedInfoSection(title = "About Company") {
                    Text(
                        text = "Labish is a pioneering technology company dedicated to delivering innovative solutions that transform businesses. With a team of experienced professionals, we specialize in software development, cloud solutions, and digital transformation.",
                        fontSize = 15.sp,
                        color = Color(0xFF374151),
                        lineHeight = 24.sp,
                        letterSpacing = 0.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Company Details with icons
                EnhancedInfoSection(title = "Company Information") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        EnhancedDetailRow(
                            icon = Icons.Default.Email,
                            label = "Industry",
                            value = "Information Technology",
                            gradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                        )
                        EnhancedDetailRow(
                            icon = Icons.Default.LocationOn,
                            label = "Headquarters",
                            value = company.value?.companyLocation ?: "",
                            gradient = listOf(Color(0xFFEC4899), Color(0xFFF43F5E))
                        )
                        EnhancedDetailRow(
                            icon = Icons.Default.Phone,
                            label = "Phone",
                            value = company.value?.companyContactNumber ?: "",
                            gradient = listOf(Color(0xFF10B981), Color(0xFF059669))
                        )
                        EnhancedDetailRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = company.value?.companyEmail ?: "",
                            gradient = listOf(Color(0xFFF59E0B), Color(0xFFEAB308))
                        )
                        EnhancedDetailRow(
                            icon = Icons.Default.PlayArrow,
                            label = "Website",
                            value = "www.labish.com",
                            gradient = listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Specialties with gradient chips
                EnhancedInfoSection(title = "Core Specialties") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            "Software Development" to listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                            "Cloud Solutions" to listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
                            "AI & ML" to listOf(Color(0xFFEC4899), Color(0xFFF43F5E)),
                            "Mobile Apps" to listOf(Color(0xFF10B981), Color(0xFF059669)),
                            "Web Development" to listOf(Color(0xFFF59E0B), Color(0xFFEAB308)),
                            "Consulting" to listOf(Color(0xFF8B5CF6), Color(0xFFA855F7))
                        ).forEach { (specialty, gradient) ->
                            GradientSpecialtyChip(text = specialty, gradient = gradient)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { isDrawerOpen = false }
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(340.dp)
                        .align(Alignment.CenterEnd)
                        .shadow(24.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFAFAFA),
                                    Color.White
                                )
                            )
                        )
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF667EEA),
                                            Color(0xFF764BA2)
                                        )
                                    )
                                )
                        ) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .size(36.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .graphicsLayer(rotationZ = 180f)
                                        .clickable { isDrawerOpen = false }
                                        .padding(8.dp)
                                )
                            }

                            // Profile Section
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterStart)
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp,
                                        bottom = 16.dp,
                                        top = 56.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Card(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .border(3.dp, Color.White, CircleShape),
                                    shape = CircleShape,
                                    elevation = CardDefaults.cardElevation(8.dp)
                                ) {
                                    if (displayedProfileUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = displayedProfileUrl,
                                            contentDescription = "Company Logo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
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
                                            Text(
                                                text = company.value?.companyName?.firstOrNull()?.toString() ?: "C",
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = company.value?.companyName ?: "Company",
                                        style = TextStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        ),
                                        maxLines = 1
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Information Technology",
                                        style = TextStyle(
                                            fontSize = 13.sp,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // MENU ITEMS
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            CompanyDrawerMenuItem(
                                icon = R.drawable.outline_edit_24,
                                text = "Edit Profile",
                                subtitle = "Update company information",
                                iconColor = Color(0xFF2196F3),
                                onClick = {
                                    isDrawerOpen = false
                                    Toast.makeText(context, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
                                }
                            )

                            CompanyDrawerMenuItem(
                                icon = R.drawable.feedback,
                                text = "Help & Support",
                                subtitle = "Get help or send feedback",
                                iconColor = Color(0xFF2196F3),
                                onClick = {
                                    isDrawerOpen = false
                                    Toast.makeText(context, "Edit Profile clicked", Toast.LENGTH_SHORT).show()                                }
                            )

                            CompanyDrawerMenuItem(
                                icon = R.drawable.settings,
                                text = "Settings",
                                subtitle = "Manage preferences",
                                iconColor = Color(0xFF9C27B0),
                                onClick = {
                                    isDrawerOpen = false
                                    showSettingsDialog = true
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                color = Color.Gray.copy(alpha = 0.2f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // LOGOUT BUTTON
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                                .clickable {
                                    isDrawerOpen = false
                                    repository.logout(currentUserId) { success, message ->
                                        if (success) {
                                            Toast.makeText(
                                                context,
                                                "Logged out successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(context, LoginActivity::class.java)
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            context.startActivity(intent)
                                            (context as? ComponentActivity)?.finish()
                                        } else {
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_logout_24),
                                    contentDescription = "Logout",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Logout",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showSettingsDialog) {
            CompanySettingsDialog(
                onDismiss = { showSettingsDialog = false },
                onChangePasswordClick = {
                    showSettingsDialog = false
                    showChangePasswordDialog = true
                },
                onDeactivateClick = {
                    showSettingsDialog = false
                    showConfirmPasswordDialog = true
                },
                onDeleteAccountClick = {
                    showSettingsDialog = false
                    showDeleteAccountDialog = true
                },
                context = context
            )
        }

        if (showChangePasswordDialog) {
            ChangePasswordDialog(
                onDismiss = { showChangePasswordDialog = false },
                repository = jobSeekerRepository,
                context = context
            )
        }

        if (showDeleteAccountDialog) {
            DeleteAccountConfirmationDialog(
                onDismiss = { showDeleteAccountDialog = false },
                onConfirm = { password ->
                    val currentUser = repository.getCurrentCompany()
                    if (currentUser != null && currentUser.email != null) {
                        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
                            currentUser.email!!,
                            password
                        )

                        currentUser.reauthenticate(credential)
                            .addOnSuccessListener {
                                companyViewModel.deleteAccount(currentUserId) { success, message ->
                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            "Account deleted permanently",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        val intent = Intent(context, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                        activity.finish()
                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                    showDeleteAccountDialog = false
                                }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    context,
                                    "Incorrect password. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            context,
                            "Unable to verify user. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                context = context
            )
        }

        if (showConfirmPasswordDialog) {
            ConfirmPasswordForDeactivateDialog(
                onDismiss = {
                    showConfirmPasswordDialog = false
                },
                onConfirm = { password ->
                    // Verify password first
                    val currentUser = repository.getCurrentCompany()
                    if (currentUser != null && currentUser.email != null) {
                        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
                            currentUser.email!!,
                            password
                        )

                        currentUser.reauthenticate(credential)
                            .addOnSuccessListener {
                                // Password is correct, proceed with deactivation
                                companyViewModel.deactivateAccount(currentUserId) { success, message ->
                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            "Account deactivated successfully",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        // Navigate to login screen
                                        val intent = Intent(context, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                        activity.finish()
                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                    showConfirmPasswordDialog = false
                                }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    context,
                                    "Incorrect password. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            context,
                            "Unable to verify user. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                context = context
            )
        }
    }
}

@Composable
fun EnhancedStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFEEF2FF),
                                Color(0xFFE0E7FF)
                            )
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EnhancedInfoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFF8B5CF6)
                                )
                            ),
                            RoundedCornerShape(2.dp)
                        )
                )
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827),
                    letterSpacing = (-0.3).sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun EnhancedDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    gradient: List<Color>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                color = Color(0xFF111827),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GradientSpecialtyChip(text: String, gradient: List<Color>) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 3.dp,
        modifier = Modifier.wrapContentWidth()
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradient.map { it.copy(alpha = 0.15f) }))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(gradient),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                fontSize = 13.sp,
                color = gradient[0],
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
fun CompanyDrawerMenuItem(
    icon: Int,
    text: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = text,
                    tint = iconColor,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xFF78909C)
                    )
                )
            }

            Icon(
                painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                contentDescription = "Navigate",
                tint = Color(0xFFBDBDBD),
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer(rotationZ = 180f)
            )
        }
    }
}

@Composable
fun CompanySettingsDialog(
    onDismiss: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onDeactivateClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    context: Context
) {

    var isDarkMode by remember { mutableStateOf(false) }

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
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF667EEA)
                        )
                    )

                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = Color(0xFFF5F5F5)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = "Close",
//                            onClick = { isDrawerOpen = true
//                                showSettingsDialog = false },
                            tint = Color(0xFF546E7A),
                            modifier = Modifier
                                .padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SETTINGS OPTIONS
                CompanySettingsOption(
                    icon = R.drawable.outline_lock_24,
                    title = "Change Password",
                    subtitle = "Update your password",
                    iconColor = Color(0xFF4CAF50),
                    onClick = onChangePasswordClick
                )

                CompanySettingsOptionWithSwitch(
                    icon = R.drawable.darkmode,
                    title = "Dark Mode",
                    subtitle = if (isDarkMode) "Dark theme enabled" else "Light theme enabled",
                    iconColor = Color(0xFF9C27B0),
                    isChecked = isDarkMode,
                    onCheckedChange = { isDarkMode = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                Spacer(modifier = Modifier.height(8.dp))

                SettingsOption(
                    icon = R.drawable.deactivateaccount,
                    title = "Deactivate Account",
                    subtitle = "Temporarily disable your account",
                    iconColor = Color(0xFFFF9800),
                    onClick = onDeactivateClick
                )

                SettingsOption(
                    icon = R.drawable.deleteaccount,
                    title = "Delete Account",
                    subtitle = "Permanently remove your account",
                    iconColor = Color(0xFFF44336),
                    onClick = onDeleteAccountClick
                )
            }

        }
    }
}

@Composable
fun CompanySettingsOption(
    icon: Int,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFAFAFA)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xFF78909C)
                    )
                )
            }
        }
    }
}

@Composable
fun CompanySettingsOptionWithSwitch(
    icon: Int,
    title: String,
    subtitle: String,
    iconColor: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFAFAFA)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xFF78909C)
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = iconColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFBDBDBD)
                )
            )
        }
    }
}