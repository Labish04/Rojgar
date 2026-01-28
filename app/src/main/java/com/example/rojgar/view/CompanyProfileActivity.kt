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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.graphicsLayer
import com.example.rojgar.repository.UserRepo
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.scale
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.viewmodel.JobViewModel
import kotlinx.coroutines.launch

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
        return UserRepo().getCurrentUserId()
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

    val repository = remember { CompanyRepoImpl() }
    val jobSeekerRepository = remember { JobSeekerRepoImpl() }
    val authRepo = remember { UserRepo() }

    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val followViewModel = remember { FollowViewModel(FollowRepoImpl(context)) }

    var currentUserRole by remember { mutableStateOf<String?>(null) }
    val currentUserId = remember { authRepo.getCurrentUserId() }

    val company = companyViewModel.companyDetails.observeAsState(initial = null)
    val isFollowingState by followViewModel.isFollowing.observeAsState(initial = false)
    val followersCountState by followViewModel.followersCount.observeAsState(initial = 0)
    val followingCountState by followViewModel.followingCount.observeAsState(initial = 0)

    var isUploadingCover by remember { mutableStateOf(false) }
    var isUploadingProfile by remember { mutableStateOf(false) }
    var displayedCoverUrl by remember { mutableStateOf("") }
    var displayedProfileUrl by remember { mutableStateOf("") }

    var isDrawerOpen by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showConfirmPasswordDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
    val actualCompanyId = if (isOwnProfile) company.value?.companyId ?: "" else companyId
    val jobPosts by jobViewModel.company.observeAsState(emptyList())

    fun shareCompanyProfile(context: Context, company: CompanyModel?) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            val shareText = buildString {
                append("Check out ${company?.companyName ?: "this company"}'s profile!\n\n")
                append("📍 Location: ${company?.companyLocation ?: "N/A"}\n")
                append("📧 Email: ${company?.companyEmail ?: "N/A"}\n")
                append("📞 Phone: ${company?.companyContactNumber ?: "N/A"}\n")
                append("🏢 Industry: Information Technology\n")
                append("🌐 Website: www.labish.com\n\n")
                append("Join us on our journey of innovation and growth!")
            }
            putExtra(Intent.EXTRA_SUBJECT, "${company?.companyName ?: "Company"} Profile")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Company Profile via"))
    }

    LaunchedEffect(companyId) {
        if (companyId.isNotEmpty()) {
            jobViewModel.getJobPostsByCompanyId(companyId)
        }
    }

    val activeJobCount = remember(jobPosts) {
        jobPosts.count { isJobActive(it.deadline) }
    }

    LaunchedEffect(Unit) {
        authRepo.getUserType { role ->
            currentUserRole = role
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
                        companyViewModel.addCompanyToDatabase(currentCompany.companyId, updatedCompany) { success, message ->
                            if (success) Toast.makeText(context, "Cover photo updated!", Toast.LENGTH_SHORT).show()
                            else Toast.makeText(context, "Failed to save: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else Toast.makeText(context, "Failed to upload cover photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(selectedProfileUri) {
        if (selectedProfileUri != null && !isUploadingProfile) {
            isUploadingProfile = true
            companyViewModel.uploadCompanyProfileImage(context, selectedProfileUri) { uploadedUrl ->
                isUploadingProfile = false
                if (uploadedUrl != null) {
                    displayedProfileUrl = uploadedUrl
                    company.value?.let { currentCompany ->
                        val updatedCompany = currentCompany.copy(companyProfileImage = uploadedUrl)
                        companyViewModel.addCompanyToDatabase(currentCompany.companyId, updatedCompany) { success, message ->
                            if (success) Toast.makeText(context, "Profile photo updated!", Toast.LENGTH_SHORT).show()
                            else Toast.makeText(context, "Failed to save: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else Toast.makeText(context, "Failed to upload profile photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(company.value, currentUserId, currentUserRole, isOwnProfile) {
        company.value?.let { targetCompany ->
            val profileIdToLoad = if (isOwnProfile) targetCompany.companyId else companyId
            if (profileIdToLoad.isNotEmpty()) {
                if (!isOwnProfile && currentUserId.isNotEmpty() && currentUserRole != null) {
                    followViewModel.checkFollowStatus(currentUserId, profileIdToLoad)
                }
                followViewModel.getFollowersCount(profileIdToLoad)
                followViewModel.getFollowingCount(profileIdToLoad)
                followViewModel.getFollowers(profileIdToLoad)
            }
        }
    }

    fun refreshFollowStatus() {
        if (!isOwnProfile && currentUserId.isNotEmpty() && currentUserRole != null && actualCompanyId.isNotEmpty()) {
            followViewModel.checkFollowStatus(currentUserId, actualCompanyId)  // Use actualCompanyId
        }
    }

    LaunchedEffect(Unit) {
        refreshFollowStatus()
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFFBBDEFB),
                            Color(0xFF90CAF9)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                    if (selectedCoverUri != null || displayedCoverUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedCoverUri ?: displayedCoverUrl),
                            contentDescription = "Background Image",
                            modifier = Modifier.fillMaxSize().then(if (isOwnProfile) Modifier.clickable { onPickCoverImage() } else Modifier),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
                        if (isUploadingCover) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2), Color(0xFFF093FB)))).then(if (isOwnProfile) Modifier.clickable { onPickCoverImage() } else Modifier)) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Background", modifier = Modifier.size(56.dp).align(Alignment.Center), tint = Color.White.copy(alpha = 0.7f))
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter), horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(onClick = { activity.finish() }, modifier = Modifier.shadow(8.dp, CircleShape).background(Color.White.copy(alpha = 0.95f), CircleShape).size(44.dp)) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1F2937))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { shareCompanyProfile(context, company.value) }, modifier = Modifier.shadow(8.dp, CircleShape).background(Color.White.copy(alpha = 0.95f), CircleShape).size(44.dp)) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF1F2937))
                            }
                            if (isOwnProfile) {
                                IconButton(onClick = { isDrawerOpen = true }, modifier = Modifier.shadow(8.dp, CircleShape).background(Color.White.copy(alpha = 0.95f), CircleShape).size(44.dp)) {
                                    Icon(painter = painterResource(id = R.drawable.outline_more_vert_24), contentDescription = "More", tint = Color(0xFF1F2937))
                                }
                            }
                        }
                    }

                    // NEW: Report Button - positioned below cover photo on the right
                    if (!isOwnProfile) {
                        AnimatedReportButton(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 16.dp),
                            onClick = {
                                Toast.makeText(context, "Report functionality coming soon!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth().offset(y = (-70).dp).padding(horizontal = 20.dp)) {
                    Box(modifier = Modifier.align(Alignment.Start)) {
                        Box(modifier = Modifier.size(140.dp).shadow(16.dp, CircleShape).background(Brush.linearGradient(colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))), CircleShape).padding(4.dp)) {
                            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.White).padding(5.dp).then(if (isOwnProfile) Modifier.clickable { onPickProfileImage() } else Modifier), contentAlignment = Alignment.Center) {
                                if (selectedProfileUri != null || displayedProfileUrl.isNotEmpty()) {
                                    Image(painter = rememberAsyncImagePainter(selectedProfileUri ?: displayedProfileUrl), contentDescription = "Profile Image", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                                } else {
                                    Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))), CircleShape), contentAlignment = Alignment.Center) {
                                        Text(text = company.value?.companyName?.firstOrNull()?.toString() ?: "L", fontSize = 52.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                if (isUploadingProfile) {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(30.dp))
                                    }
                                }
                            }
                        }
                        if (isOwnProfile) {
                            Box(modifier = Modifier.size(40.dp).shadow(8.dp, CircleShape).clip(CircleShape).background(Brush.linearGradient(colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)))).border(4.dp, Color.White, CircleShape).align(Alignment.BottomEnd).clickable { onPickProfileImage() }, contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Change Photo", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = company.value?.companyName ?: "Loading...", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827), letterSpacing = (-0.5).sp)
                        if (company.value?.isVerified == true) {
                            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFEF3C7), shadowElevation = 2.dp)
                            {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Verified",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }
                            }
                        }else{
                            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFEF3C7), shadowElevation = 2.dp)
                            {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFB91010),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Not Verified",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF5F0606)
                                    )
                                }
                            }
                        }
                    }



                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = company.value?.companyTagline ?: "Loading...",
                        fontSize = 15.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EnhancedStatCard(
                            label = "Active Jobs",
                            value = activeJobCount.toString(),
                            icon = R.drawable.jobtype,
                            modifier = Modifier.weight(1f).clickable {
                                val intent = Intent(context, ActiveJob::class.java)
                                context.startActivity(intent)
                            }
                        )
                        EnhancedStatCard(
                            label = "Followers",
                            value = formatNumber(followersCountState),
                            icon = R.drawable.followers,
                            modifier = Modifier.weight(1f).clickable {
                                val intent = Intent(context, FollowersListActivity::class.java)
                                intent.putExtra(
                                    "USER_ID",
                                    if (isOwnProfile) company.value?.companyId ?: "" else companyId
                                )
                                intent.putExtra("USER_TYPE", "Company")
                                intent.putExtra("IS_OWN_PROFILE", isOwnProfile)
                                context.startActivity(intent)
                            }
                        )
                        EnhancedStatCard(
                            label = "Following",
                            value = formatNumber(followingCountState),
                            icon = R.drawable.followers,
                            modifier = Modifier.weight(1f).clickable {
                                val intent = Intent(context, FollowingListActivity::class.java)
                                intent.putExtra(
                                    "USER_ID",
                                    if (isOwnProfile) company.value?.companyId ?: "" else companyId
                                )
                                intent.putExtra("USER_TYPE", "Company")
                                intent.putExtra("IS_OWN_PROFILE", isOwnProfile)
                                context.startActivity(intent)
                            }
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
                                    if (currentUserId.isNotEmpty() && currentUserRole != null) {
                                        if (isFollowingState) {
                                            followViewModel.unfollow(
                                                currentUserId,
                                                currentUserRole!!,
                                                actualCompanyId,  // FIXED: Use actualCompanyId instead of companyId
                                                "Company"
                                            ) { success, message ->
                                                if (success) {
                                                    followViewModel.getFollowersCount(companyId)
                                                    followViewModel.getFollowingCount(currentUserId)
                                                    Toast.makeText(
                                                        context,
                                                        "Unfollowed!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    // REMOVED redundant count refreshes - ViewModel handles this automatically
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed: $message",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } else {
                                            followViewModel.follow(
                                                currentUserId,
                                                currentUserRole!!,
                                                actualCompanyId,  // FIXED: Use actualCompanyId instead of companyId
                                                "Company"
                                            ) { success, message ->
                                                if (success) {
                                                    followViewModel.getFollowersCount(companyId)
                                                    followViewModel.getFollowingCount(currentUserId)
                                                    Toast.makeText(
                                                        context,
                                                        "Followed!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    // REMOVED redundant count refreshes - ViewModel handles this automatically
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed: $message",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Please login to follow",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(52.dp)
                                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowingState) Color(0xFF10B981) else Color(0xFF6366F1)
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
                                    Toast.makeText(
                                        context,
                                        "Message feature coming soon!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1f).height(52.dp)
                                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF6366F1), containerColor = Color.White
                                ),
                                border = BorderStroke(2.dp, Color(0xFF6366F1))
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

                    // Review Panel
                    ReviewPanel(
                        companyId = actualCompanyId,
                        context = context,
                        onClick = {
                            if (isOwnProfile) {
                                val intent = Intent(context, CompanyReviewActivity::class.java)
                                intent.putExtra("COMPANY_ID", actualCompanyId)  // Use the same ID!
                                intent.putExtra(
                                    "COMPANY_NAME",
                                    company.value?.companyName ?: "Company"
                                )
                                context.startActivity(intent)
                            } else {
                                val intent = Intent(context, JobSeekerReviewActivity::class.java)
                                intent.putExtra("COMPANY_ID", actualCompanyId)  // Use the same ID!
                                intent.putExtra(
                                    "COMPANY_NAME",
                                    company.value?.companyName ?: "Company"
                                )
                                context.startActivity(intent)
                            }
                        }
                    )


                    Spacer(modifier = Modifier.height(16.dp))

                    EnhancedInfoSection(title = "About Company") {
                        Text(
                            text = company.value?.companyInformation ?: "",
                            fontSize = 15.sp,
                            color = Color(0xFF374151),
                            lineHeight = 24.sp,
                            letterSpacing = 0.2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    EnhancedInfoSection(title = "Company Information") {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            EnhancedDetailRow(
                                icon = R.drawable.office,
                                label = "Industry",
                                value = company.value?.companyIndustry ?: "",
                                gradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                            )
                            EnhancedDetailRow(
                                icon = R.drawable.map_filled,
                                label = "Headquarter",
                                value = company.value?.companyLocation ?: "",
                                gradient = listOf(Color(0xFFEC4899), Color(0xFFF43F5E))
                            )
                            EnhancedDetailRow(
                                icon = R.drawable.call,
                                label = "Phone",
                                value = company.value?.companyContactNumber ?: "",
                                gradient = listOf(Color(0xFF10B981), Color(0xFF059669))
                            )
                            EnhancedDetailRow(
                                icon = R.drawable.emailicon,
                                label = "Email",
                                value = company.value?.companyEmail ?: "",
                                gradient = listOf(Color(0xFFF59E0B), Color(0xFFEAB308))
                            )
                            EnhancedDetailRow(
                                icon = R.drawable.founded,
                                label = "Founded",
                                value = company.value?.companyEstablishedDate?.take(4) ?: "2015",
                                gradient = listOf(Color(0xFF05C2B2), Color(0xFF25E4EB))
                            )
                            EnhancedDetailRow(
                                icon = R.drawable.web,
                                label = "Website",
                                value = company.value?.companyWebsite ?: "",
                                gradient = listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    EnhancedInfoSection(title = "Core Specialties") {
                        val specialties = company.value?.companySpecialties ?: emptyList()
                        if (specialties.isEmpty()) {
                            Text(
                                text = "No specialties added yet",
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val gradientColors = listOf(
                                    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                                    listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
                                    listOf(Color(0xFFEC4899), Color(0xFFF43F5E)),
                                    listOf(Color(0xFF10B981), Color(0xFF059669)),
                                    listOf(Color(0xFFF59E0B), Color(0xFFEAB308)),
                                    listOf(Color(0xFF8B5CF6), Color(0xFFA855F7)),
                                    listOf(Color(0xFF06B6D4), Color(0xFF0891B2)),
                                    listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                                    listOf(Color(0xFF14B8A6), Color(0xFF0D9488)),
                                    listOf(Color(0xFFF97316), Color(0xFFEA580C))
                                )
                                specialties.forEachIndexed { index, specialty ->
                                    GradientSpecialtyChip(
                                        text = specialty,
                                        gradient = gradientColors[index % gradientColors.size]
                                    )
                                }
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
                modifier = Modifier.fillMaxSize().zIndex(10f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f))
                            .clickable { isDrawerOpen = false })
                    Box(
                        modifier = Modifier.fillMaxHeight().width(340.dp).align(Alignment.CenterEnd)
                            .shadow(24.dp).background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFAFAFA),
                                    Color.White
                                )
                            )
                        )
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(140.dp).background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF667EEA), Color(0xFF764BA2)
                                        )
                                    )
                                )
                            ) {
                                Surface(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                                        .size(36.dp),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                                        contentDescription = "Close",
                                        tint = Color.White,
                                        modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                            .clickable { isDrawerOpen = false }.padding(8.dp)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterStart)
                                        .padding(
                                            start = 20.dp,
                                            end = 20.dp,
                                            bottom = 16.dp,
                                            top = 56.dp
                                        ), verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Card(
                                        modifier = Modifier.size(70.dp)
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
                                                modifier = Modifier.fillMaxSize().background(
                                                    Brush.linearGradient(
                                                        colors = listOf(
                                                            Color(0xFF667EEA),
                                                            Color(0xFF764BA2)
                                                        )
                                                    )
                                                ), contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = company.value?.companyName?.firstOrNull()
                                                        ?.toString() ?: "C",
                                                    fontSize = 32.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
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
                            Column(
                                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                            ) {
                                CompanyDrawerMenuItem(
                                    icon = R.drawable.outline_edit_24,
                                    text = "Edit Profile",
                                    subtitle = "Update company information",
                                    iconColor = Color(0xFF2196F3),
                                    onClick = {
                                        isDrawerOpen = false;
                                        showEditProfileDialog = true
                                    }
                                )

                                CompanyDrawerMenuItem(
                                    icon = R.drawable.feedback,
                                    text = "Help & Support",
                                    subtitle = "Get help or send feedback",
                                    iconColor = Color(0xFF2196F3),
                                    onClick = {
                                        isDrawerOpen = false
                                        val intent =
                                            Intent(context, HelpAndSupportActivity::class.java)
                                        context.startActivity(intent)
                                    }
                                )

                                CompanyDrawerMenuItem(
                                    icon = R.drawable.settings,
                                    text = "Settings",
                                    subtitle = "Manage preferences",
                                    iconColor = Color(0xFF9C27B0),
                                    onClick = {
                                        isDrawerOpen = false; showSettingsDialog = true
                                    }
                                )

                                CompanyDrawerMenuItem(
                                    icon = R.drawable.verified_badge,
                                    text = "Company Verification",
                                    subtitle = if (company.value?.isVerified == true) "Verified" else "Get verified",
                                    iconColor = Color(0xFF10B981),
                                    onClick = {
                                        isDrawerOpen = false
                                        val intent =
                                            Intent(context, CompanyVerificationActivity::class.java)
                                        intent.putExtra(
                                            "COMPANY_ID",
                                            company.value?.companyId ?: ""
                                        )
                                        context.startActivity(intent)
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    color = Color.Gray.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 24.dp).clickable {
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
                                        } else Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(18.dp),
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
                        showSettingsDialog = false; showChangePasswordDialog = true
                    },
                    onDeactivateClick = {
                        showSettingsDialog = false; showConfirmPasswordDialog = true
                    },
                    onDeleteAccountClick = {
                        showSettingsDialog = false; showDeleteAccountDialog = true
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
                            val credential =
                                com.google.firebase.auth.EmailAuthProvider.getCredential(
                                    currentUser.email!!,
                                    password
                                )
                            currentUser.reauthenticate(credential).addOnSuccessListener {
                                companyViewModel.deleteAccount(currentUserId) { success, message ->
                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            "Account deleted permanently",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        val intent = Intent(context, LoginActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                        activity.finish()
                                    } else Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                        .show()
                                    showDeleteAccountDialog = false
                                }
                            }.addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Incorrect password. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else Toast.makeText(
                            context,
                            "Unable to verify user. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    context = context
                )
            }
            if (showConfirmPasswordDialog) {
                ConfirmPasswordForDeactivateDialog(onDismiss = {
                    showConfirmPasswordDialog = false
                }, onConfirm = { password ->
                    val currentUser = repository.getCurrentCompany()
                    if (currentUser != null && currentUser.email != null) {
                        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
                            currentUser.email!!,
                            password
                        )
                        currentUser.reauthenticate(credential).addOnSuccessListener {
                            companyViewModel.deactivateAccount(currentUserId) { success, message ->
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "Account deactivated successfully",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                    activity.finish()
                                } else Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showConfirmPasswordDialog = false
                            }
                        }.addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Incorrect password. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else Toast.makeText(
                        context,
                        "Unable to verify user. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }, context = context)
            }
            if (showEditProfileDialog) {
                EditCompanyProfileDialog(
                    company = company.value,
                    onDismiss = { showEditProfileDialog = false },
                    onSave = { updatedCompany ->
                        companyViewModel.addCompanyToDatabase(
                            updatedCompany.companyId,
                            updatedCompany
                        ) { success, message ->
                            showEditProfileDialog = false
                            if (success) {
                                Toast.makeText(
                                    context,
                                    "Profile updated successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                companyViewModel.fetchCurrentCompany()
                            } else Toast.makeText(
                                context,
                                "Failed to update: $message",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    context = context
                )
            }
        }
    }

}

@Composable
fun EnhancedStatCard(
    label: String,
    value: String,
    icon: Int,
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
                    painter = painterResource(id = icon),
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
    icon: Int,
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
                painter = painterResource(id = icon),
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
                            tint = Color(0xFF546E7A),
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { onDismiss() }
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

// Add this composable function to your CompanyProfileActivity.kt file

@Composable
fun EditCompanyProfileDialog(
    company: CompanyModel?,
    onDismiss: () -> Unit,
    onSave: (CompanyModel) -> Unit,
    context: Context
) {
    var companyName by remember { mutableStateOf(company?.companyName ?: "") }
    var tagline by remember { mutableStateOf(company?.companyTagline ?: "") }
    var aboutCompany by remember { mutableStateOf(company?.companyInformation?:"") }
    var industry by remember { mutableStateOf(company?.companyIndustry ?: "") }
    var location by remember { mutableStateOf(company?.companyLocation ?: "") }
    var phone by remember { mutableStateOf(company?.companyContactNumber ?: "") }
    var email by remember { mutableStateOf(company?.companyEmail ?: "") }
    var website by remember { mutableStateOf(company?.companyWebsite ?: "") }
    var founded by remember { mutableStateOf(company?.companyEstablishedDate ?: "") }

    // Core Specialties
    var specialties by remember {
        mutableStateOf(listOf(
            *(company?.companySpecialties?.toTypedArray() ?: emptyArray())
        ))
    }
    var newSpecialty by remember { mutableStateOf("") }
    var showAddSpecialtyField by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(30f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { if (!isSaving) onDismiss() }
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
                modifier = Modifier
                    .fillMaxSize()
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
                            text = "Edit Profile",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { if (!isSaving) onDismiss() },
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

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Company Name
                    EditTextField(
                        label = "Company Name",
                        value = companyName,
                        onValueChange = { companyName = it },
                        icon = R.drawable.office
                    )

                    // Tagline
                    EditTextField(
                        label = "Tagline",
                        value = tagline,
                        onValueChange = { tagline = it },
                        icon = R.drawable.baseline_password_24
                    )

                    // About Company
                    EditTextFieldMultiline(
                        label = "About Company",
                        value = aboutCompany,
                        onValueChange = { aboutCompany = it },
                        icon = R.drawable.editmessage
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Company Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667EEA)
                    )

                    // Industry
                    EditTextField(
                        label = "Industry",
                        value = industry,
                        onValueChange = { industry = it },
                        icon = R.drawable.company
                    )

                    // Location
                    EditTextField(
                        label = "Location",
                        value = location,
                        onValueChange = { location = it },
                        icon = R.drawable.map_filled
                    )

                    // Phone
                    EditTextField(
                        label = "Phone",
                        value = phone,
                        onValueChange = { phone = it },
                        icon = R.drawable.call
                    )

                    // Email
                    EditTextField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        icon = R.drawable.emailicon,
                        enabled = true
                    )

                    // Founded
                    EditTextField(
                        label = "Founded",
                        value = founded,
                        onValueChange = { founded = it },
                        icon = R.drawable.founded,
                        enabled = true
                    )

                    // Website
                    EditTextField(
                        label = "Website",
                        value = website,
                        onValueChange = { website = it },
                        icon = R.drawable.web
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Core Specialties Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Core Specialties",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF667EEA)
                        )

                        IconButton(
                            onClick = { showAddSpecialtyField = !showAddSpecialtyField },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (showAddSpecialtyField) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = "Add Specialty",
                                tint = Color(0xFF667EEA)
                            )
                        }
                    }

                    // Add new specialty field
                    if (showAddSpecialtyField) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSpecialty,
                                onValueChange = { newSpecialty = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Enter specialty") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF667EEA),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    if (newSpecialty.isNotBlank()) {
                                        specialties = specialties + newSpecialty.trim()
                                        newSpecialty = ""
                                        showAddSpecialtyField = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF667EEA)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Add")
                            }
                        }
                    }

                    // Display specialties as removable chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        specialties.forEach { specialty ->
                            RemovableSpecialtyChip(
                                text = specialty,
                                onRemove = {
                                    specialties = specialties.filter { it != specialty }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }

                // Save Button (Fixed at bottom)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Button(
                        onClick = {
                            if (companyName.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Company name cannot be empty",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            isSaving = true

                            // Create updated company model
                            val updatedCompany = company?.copy(
                                companyName = companyName,
                                companyTagline = tagline,
                                companyInformation = aboutCompany,
                                companyIndustry = industry,
                                companyLocation = location,
                                companyContactNumber = phone,
                                companyEmail = email,
                                companyEstablishedDate = founded,
                                companyWebsite = website,
                                companySpecialties = specialties
                            ) ?: CompanyModel(
                                companyName = companyName,
                                companyTagline = tagline,
                                companyInformation = aboutCompany,
                                companyIndustry = industry,
                                companyLocation = location,
                                companyContactNumber = phone,
                                companyEmail = email,
                                companyEstablishedDate = founded,
                                companyWebsite = website,
                                companySpecialties = specialties
                            )

                            onSave(updatedCompany)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667EEA)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Save Changes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: Int,
    enabled: Boolean = true
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF374151),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    tint = Color(0xFF667EEA),
                    modifier = Modifier.size(22.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667EEA),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                disabledBorderColor = Color(0xFFE0E0E0),
                disabledTextColor = Color(0xFF9CA3AF)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = enabled
        )
    }
}

@Composable
fun EditTextFieldMultiline(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: Int
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF374151),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    tint = Color(0xFF667EEA),
                    modifier = Modifier.padding(bottom = 80.dp).size(22.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667EEA),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(12.dp),
            maxLines = 5
        )
    }
}

@Composable
fun RemovableSpecialtyChip(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF667EEA).copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Color(0xFF667EEA).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                color = Color(0xFF667EEA),
                fontWeight = FontWeight.Medium
            )

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color(0xFF667EEA),
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

fun formatNumber(count: Int): String {
    return when {
        count >= 1000000 -> {
            val millions = count / 1000000.0
            if (millions % 1 == 0.0) {
                "${count / 1000000}M"
            } else {
                "%.1fM".format(millions)
            }
        }
        count >= 1000 -> {
            val thousands = count / 1000.0
            if (thousands % 1 == 0.0) {
                "${count / 1000}K"
            } else {
                "%.1fK".format(thousands)
            }
        }
        else -> count.toString()
    }
}

@Composable
fun ReviewPanel(
    companyId: String,
    context: Context,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable {
                onClick()

            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFE0F2FE), // Light blue
                            Color(0xFFF3E5F5)  // Light purple
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700), // Gold
                                        Color(0xFFFFA500)  // Orange
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Reviews",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Reviews & Ratings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = "See what others are saying",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Navigate to Reviews",
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(24.dp)
                )
            }

        }
    }
}

@Composable
fun AnimatedReportButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFFEF4444) else Color(0xFFFEE2E2),
        animationSpec = tween(durationMillis = 200)
    )

    val iconColor by animateColorAsState(
        targetValue = if (isPressed) Color.White else Color(0xFFEF4444),
        animationSpec = tween(durationMillis = 200)
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 4.dp else 8.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFEF4444).copy(alpha = 0.3f),
                        Color(0xFFDC2626).copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                    // Reset after animation
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(200)
                        isPressed = false
                    }
                }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                iconColor.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Report",
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Report",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = iconColor,
                letterSpacing = 0.3.sp
            )
        }
    }
}