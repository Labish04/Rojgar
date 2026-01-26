package com.example.rojgar.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.R
import com.example.rojgar.model.JobCategory
import com.example.rojgar.model.JobModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.ui.theme.*
import com.example.rojgar.utils.ImageUtils
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobViewModel
import java.util.*

class CompanyUploadPost : ComponentActivity() {
    lateinit var imageUtils: ImageUtils
    var isPickingProfile by mutableStateOf(false)
    var selectedProfileUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        imageUtils = ImageUtils(this, this)
        imageUtils.registerLaunchers { uri ->
            if (uri != null) {
                if(isPickingProfile) {
                    selectedProfileUri = uri
                }
                isPickingProfile = false
            }
        }
        setContent {
            CompanyUploadPostScreen(
                selectedProfileUri = selectedProfileUri,
                onPickProfileImage = {
                    isPickingProfile = true
                    imageUtils.launchImagePicker()
                }
            )
        }
    }
}

@Composable
fun CompanyUploadPostScreen(
    selectedProfileUri: Uri?,
    onPickProfileImage: () -> Unit
) {
    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }

    val context = LocalContext.current
    val currentUser = companyViewModel.getCurrentCompany()
    val companyId = currentUser?.uid ?: ""

    var showCreate by remember { mutableStateOf(false) }
    var editingPost by remember { mutableStateOf<JobModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf<JobModel?>(null) }

    val jobPosts by jobViewModel.company.observeAsState(emptyList())
    val isLoading by jobViewModel.loading.observeAsState(false)

    LaunchedEffect(Unit) {
        if (companyId.isNotEmpty()) {
            jobViewModel.getJobPostsByCompanyId(companyId)
        }
    }

    LaunchedEffect(showCreate) {
        if (!showCreate && companyId.isNotEmpty()) {
            jobViewModel.getJobPostsByCompanyId(companyId)
        }
    }

    if (showCreate) {
        CompanyUploadPostBody(
            jobViewModel = jobViewModel,
            companyId = companyId,
            existingPost = editingPost,
            selectedBannerUri = selectedProfileUri,
            onPickBannerImage = onPickProfileImage,
            onPostCreated = {
                showCreate = false
                editingPost = null
                jobViewModel.getJobPostsByCompanyId(companyId)
            },
            onCancel = {
                showCreate = false
                editingPost = null
            }
        )
    } else {
        JobPostListScreen(
            jobPosts = jobPosts,
            isLoading = isLoading,
            onCreatePost = { showCreate = true },
            onEdit = {
                editingPost = it
                showCreate = true
            },
            onDelete = { showDeleteDialog = it }
        )
    }

    showDeleteDialog?.let { post ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Job Post", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${post.title}\"? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        jobViewModel.deleteJobPost(post.postId) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) {
                                jobViewModel.getJobPostsByCompanyId(companyId)
                            }
                            showDeleteDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun JobPostListScreen(
    jobPosts: List<JobModel>,
    isLoading: Boolean,
    onCreatePost: () -> Unit,
    onEdit: (JobModel) -> Unit,
    onDelete: (JobModel) -> Unit
) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "My Job Posts",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "${jobPosts.size} ${if (jobPosts.size == 1) "post" else "posts"}",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DarkBlue2)
                    }
                }
                jobPosts.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No job posts yet",
                                fontSize = 18.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Create your first job post",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(jobPosts, key = { it.postId }) { post ->
                            JobPostCard(
                                jobPost = post,
                                onEdit = { onEdit(post) },
                                onDelete = { onDelete(post) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCreatePost,
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue2,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Create Job Post",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun JobPostCard(
    jobPost: JobModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val displayImageUrl = jobPost.hiringBanner.ifEmpty { jobPost.imageUrl }

    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable {
                val intent = Intent(context, ApplicationActivity::class.java).apply {
                    putExtra("JOB_POST_ID", jobPost.postId)
                    putExtra("JOB_TITLE", jobPost.title)
                    putExtra("COMPANY_ID", jobPost.companyId)
                }
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (displayImageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(displayImageUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.jobpost_filled),
                        contentDescription = "No Image",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = jobPost.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = jobPost.position,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (jobPost.jobType.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = jobPost.jobType,
                            fontSize = 12.sp,
                            color = DarkBlue2,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyUploadPostBody(
    jobViewModel: JobViewModel,
    companyId: String,
    existingPost: JobModel? = null,
    selectedBannerUri: Uri?,
    onPickBannerImage: () -> Unit,
    onPostCreated: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(existingPost?.title ?: "") }
    var position by remember { mutableStateOf(existingPost?.position ?: "") }
    var selectedCategories by remember { mutableStateOf(existingPost?.categories ?: emptyList()) }
    var jobType by remember { mutableStateOf(existingPost?.jobType ?: "") }
    var experience by remember { mutableStateOf(existingPost?.experience ?: "") }
    var education by remember { mutableStateOf(existingPost?.education ?: "") }
    var skills by remember { mutableStateOf(existingPost?.skills ?: "") }
    var salary by remember { mutableStateOf(existingPost?.salary ?: "") }
    var deadline by remember { mutableStateOf(existingPost?.deadline ?: "") }
    var responsibilities by remember { mutableStateOf(existingPost?.responsibilities ?: "") }
    var jobDescription by remember { mutableStateOf(existingPost?.jobDescription ?: "") }

    var isUploadingBanner by remember { mutableStateOf(false) }
    var displayedBannerUrl by remember { mutableStateOf(existingPost?.hiringBanner ?: existingPost?.imageUrl ?: "") }

    var showCategoryBottomSheet by remember { mutableStateOf(false) }
    var showJobTypeBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(selectedBannerUri) {
        if (selectedBannerUri != null && !isUploadingBanner) {
            isUploadingBanner = true
            jobViewModel.uploadBannerImage(context, selectedBannerUri) { uploadedUrl ->
                isUploadingBanner = false
                if (uploadedUrl != null) {
                    displayedBannerUrl = uploadedUrl
                    Toast.makeText(context, "Banner uploaded successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to upload banner", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    deadline = String.format(
                        "%02d/%02d/%d %02d:%02d",
                        dayOfMonth, month + 1, year, hourOfDay, minute
                    )
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = if (existingPost != null) "Edit Job Post" else "Create Job Post",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray)
                    .clickable { onPickBannerImage() },
                contentAlignment = Alignment.Center
            ) {
                when {
                    selectedBannerUri != null -> {
                        Image(
                            painter = rememberAsyncImagePainter(selectedBannerUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    displayedBannerUrl.isNotEmpty() -> {
                        Image(
                            painter = rememberAsyncImagePainter(displayedBannerUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_upload_24),
                                contentDescription = "Upload",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Upload Hiring Banner", color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                }

                if (isUploadingBanner) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        JobPostTextField(
            value = title,
            onValueChange = { title = it },
            label = "Job Title*",
            icon = R.drawable.jobtitleicon,
            tint = Color(0xFF2196F3)
        )
        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(
            value = position,
            onValueChange = { position = it },
            label = "Position*",
            icon = R.drawable.jobpost_filled,
            tint = Color(0xFF2196F3)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Category Selector
        AnimatedDropdownButton(
            selectedText = if (selectedCategories.isEmpty()) "Select Category" else selectedCategories.joinToString(", "),
            icon = R.drawable.jobcategoryicon,
            tint = Color(0xFF2196F3),
            onClick = { showCategoryBottomSheet = true },
            hasSelection = selectedCategories.isNotEmpty(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Job Type Dropdown
        AnimatedDropdownButton(
            selectedText = jobType.ifEmpty { "Select Job Type" },
            icon = R.drawable.jobtype,
            tint = Color(0xFF2196F3),
            onClick = { showJobTypeBottomSheet = true },
            hasSelection = jobType.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(
            value = experience,
            onValueChange = { experience = it },
            label = "Experience Required",
            icon = R.drawable.experience_filled,
            tint = Color(0xFF2196F3)
        )
        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(
            value = education,
            onValueChange = { education = it },
            label = "Education",
            icon = R.drawable.educationboardicon,
            tint = Color(0xFF2196F3)
        )
        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(
            value = skills,
            onValueChange = { skills = it },
            label = "Required Skills",
            icon = R.drawable.skills_filledicon,
            tint = Color(0xFF2196F3)
        )
        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(
            value = salary,
            onValueChange = { salary = it },
            label = "Salary",
            icon = R.drawable.salaryicon,
            tint = Color(0xFF2196F3)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() }
        ) {
            OutlinedTextField(
                value = deadline,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.deadlineicon),
                        contentDescription = "Deadline",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.datetimeicon),
                        contentDescription = "Select Date",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Application Deadline") },
                placeholder = { Text("Select date and time") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = DarkBlue2,
                    disabledContainerColor = Color.White.copy(alpha = 0.7f),
                    disabledTextColor = Color.Black,
                    disabledLabelColor = Color.Gray
                )
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(
            value = responsibilities,
            onValueChange = { responsibilities = it },
            label = "Key Responsibilities",
            icon = R.drawable.responsibilityicon,
            minHeight = 100.dp,
            tint = Color(0xFF2196F3)
        )
        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(
            value = jobDescription,
            onValueChange = { jobDescription = it },
            label = "Job Description",
            icon = R.drawable.jobdescriptionicon,
            minHeight = 100.dp,
            tint = Color(0xFF2196F3)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("Cancel", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    if (title.isEmpty() || position.isEmpty()) {
                        Toast.makeText(context, "Please fill in required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (isUploadingBanner) {
                        Toast.makeText(context, "Please wait for banner upload to complete", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val jobPost = JobModel(
                        postId = existingPost?.postId ?: "",
                        companyId = companyId,
                        title = title,
                        position = position,
                        categories = selectedCategories,
                        jobType = jobType,
                        experience = experience,
                        education = education,
                        skills = skills,
                        salary = salary,
                        deadline = deadline,
                        responsibilities = responsibilities,
                        jobDescription = jobDescription,
                        hiringBanner = displayedBannerUrl,
                        imageUrl = displayedBannerUrl,
                        timestamp = existingPost?.timestamp ?: System.currentTimeMillis()
                    )

                    if (existingPost != null) {
                        jobViewModel.updateJobPost(jobPost) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) onPostCreated()
                        }
                    } else {
                        jobViewModel.createJobPost(jobPost) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) onPostCreated()
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue2),
                enabled = !isUploadingBanner
            ) {
                Text(
                    text = if (existingPost != null) "Update" else "Post Job",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showCategoryBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategoryBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            CategoryBottomSheet(
                onDismiss = { showCategoryBottomSheet = false },
                onSave = { categories ->
                    selectedCategories = categories
                    showCategoryBottomSheet = false
                },
                initialCategories = selectedCategories
            )
        }
    }

    if (showJobTypeBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showJobTypeBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            JobTypeBottomSheet(
                onDismiss = { showJobTypeBottomSheet = false },
                onSelect = { type ->
                    jobType = type
                    showJobTypeBottomSheet = false
                },
                initialSelection = jobType
            )
        }
    }
}

@Composable
fun AnimatedDropdownButton(
    selectedText: String,
    icon: Int,
    onClick: () -> Unit,
    hasSelection: Boolean,
    tint: Color
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = if (hasSelection) tint else tint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedText,
                    color = if (hasSelection) Color.Black else Color.Gray,
                    fontSize = if (hasSelection) 14.sp else 16.sp,
                    fontWeight = if (hasSelection) FontWeight.Medium else FontWeight.Normal
                )
            }

            val rotation by animateFloatAsState(
                targetValue = if (isPressed) 180f else 0f,
                animationSpec = tween(durationMillis = 300)
            )

            Icon(
                painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                contentDescription = "Dropdown",
                tint = Color(0xFF2196F3),
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation)
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun JobPostTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: Int,
    minHeight: Dp = 56.dp,
    tint: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        leadingIcon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(24.dp)
            )
        },
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.White.copy(alpha = 0.7f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
            focusedIndicatorColor = tint,
            unfocusedIndicatorColor = tint.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun JobTypeBottomSheet(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    initialSelection: String
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialSelection) }

    val jobTypes = listOf(
        "Full-time",
        "Part-time",
        "Contract",
        "Temporary",
        "Internship",
        "Freelance",
        "Apprenticeship",
        "Traineeship",
        "Seasonal",
        "Project-based",
        "Shift-based",
        "On-call",
        "Fixed-term",
        "Commission-based",
        "Entry-level"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header with animation
            var isVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                isVisible = true
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.jobtype),
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Select Job Type",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Choose the employment type for this position",
                        fontSize = 14.sp,
                        color = Color(0xFF424242)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Search Field
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(400, delayMillis = 100, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400, delayMillis = 100))
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search job types...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.jobtype),
                            contentDescription = "Search",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.9f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                        focusedIndicatorColor = Color(0xFF2196F3),
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val filteredTypes = jobTypes.filter {
                    it.contains(searchQuery, ignoreCase = true)
                }

                items(filteredTypes.size) { index ->
                    var itemVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 30L)
                        itemVisible = true
                    }

                    AnimatedVisibility(
                        visible = itemVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { 100 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeIn()
                    ) {
                        JobTypeItem(
                            name = filteredTypes[index],
                            isSelected = selectedType == filteredTypes[index],
                            onClick = { selectedType = filteredTypes[index] }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Buttons
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { 60 },
                    animationSpec = tween(400, delayMillis = 200, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400, delayMillis = 200))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, Color(0xFF2196F3)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(
                            "Cancel",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2191F3)
                        )
                    }

                    Button(
                        onClick = { onSelect(selectedType) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3),
                            disabledContainerColor = Color(0xFF2196F3).copy(alpha = 0.5f)
                        ),
                        enabled = selectedType.isNotEmpty(),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun JobTypeItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
        animationSpec = tween(300)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF2196F3), shape = RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    name,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFF1565C0) else Color(0xFF424242)
                )
            }

            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF2196F3), shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBottomSheet(
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
    initialCategories: List<String>
) {
    var searchQuery by remember { mutableStateOf("") }

    val categoryList = remember {
        mutableStateListOf(
            JobCategory("Creative / Graphics / Designing", initialCategories.contains("Creative / Graphics / Designing")),
            JobCategory("UI / UX Design", initialCategories.contains("UI / UX Design")),
            JobCategory("Animation / VFX", initialCategories.contains("Animation / VFX")),
            JobCategory("Photography / Videography", initialCategories.contains("Photography / Videography")),
            JobCategory("Fashion / Textile Designing", initialCategories.contains("Fashion / Textile Designing")),
            JobCategory("Architecture / Interior Designing", initialCategories.contains("Architecture / Interior Designing")),
            JobCategory("IT & Telecommunication", initialCategories.contains("IT & Telecommunication")),
            JobCategory("Software Development", initialCategories.contains("Software Development")),
            JobCategory("Web Development", initialCategories.contains("Web Development")),
            JobCategory("Mobile App Development", initialCategories.contains("Mobile App Development")),
            JobCategory("Data Science / AI / ML", initialCategories.contains("Data Science / AI / ML")),
            JobCategory("Cyber Security", initialCategories.contains("Cyber Security")),
            JobCategory("Network / System Administration", initialCategories.contains("Network / System Administration")),
            JobCategory("DevOps / Cloud Computing", initialCategories.contains("DevOps / Cloud Computing")),
            JobCategory("QA / Software Testing", initialCategories.contains("QA / Software Testing")),
            JobCategory("General Management", initialCategories.contains("General Management")),
            JobCategory("Project Management", initialCategories.contains("Project Management")),
            JobCategory("Operations Management", initialCategories.contains("Operations Management")),
            JobCategory("Business Development", initialCategories.contains("Business Development")),
            JobCategory("Human Resource / HR", initialCategories.contains("Human Resource / HR")),
            JobCategory("Administration / Office Support", initialCategories.contains("Administration / Office Support")),
            JobCategory("Accounting / Finance", initialCategories.contains("Accounting / Finance")),
            JobCategory("Banking / Insurance / Financial Services", initialCategories.contains("Banking / Insurance / Financial Services")),
            JobCategory("Audit / Tax / Compliance", initialCategories.contains("Audit / Tax / Compliance")),
            JobCategory("Investment / Wealth Management", initialCategories.contains("Investment / Wealth Management")),
            JobCategory("Sales / Public Relations", initialCategories.contains("Sales / Public Relations")),
            JobCategory("Marketing / Advertising", initialCategories.contains("Marketing / Advertising")),
            JobCategory("Digital Marketing", initialCategories.contains("Digital Marketing")),
            JobCategory("Content Writing / Copywriting", initialCategories.contains("Content Writing / Copywriting")),
            JobCategory("Media / Journalism", initialCategories.contains("Media / Journalism")),
            JobCategory("Customer Service / Call Center", initialCategories.contains("Customer Service / Call Center")),
            JobCategory("Construction / Engineering / Architects", initialCategories.contains("Construction / Engineering / Architects")),
            JobCategory("Civil Engineering", initialCategories.contains("Civil Engineering")),
            JobCategory("Mechanical Engineering", initialCategories.contains("Mechanical Engineering")),
            JobCategory("Electrical / Electronics Engineering", initialCategories.contains("Electrical / Electronics Engineering")),
            JobCategory("Manufacturing / Production", initialCategories.contains("Manufacturing / Production")),
            JobCategory("Maintenance / Technician", initialCategories.contains("Maintenance / Technician")),
            JobCategory("Commercial / Logistics / Supply Chain", initialCategories.contains("Commercial / Logistics / Supply Chain")),
            JobCategory("Procurement / Purchasing", initialCategories.contains("Procurement / Purchasing")),
            JobCategory("Warehouse / Distribution", initialCategories.contains("Warehouse / Distribution")),
            JobCategory("Drivers / Delivery", initialCategories.contains("Drivers / Delivery")),
            JobCategory("Healthcare / Medical", initialCategories.contains("Healthcare / Medical")),
            JobCategory("Nursing / Caregiving", initialCategories.contains("Nursing / Caregiving")),
            JobCategory("Pharmacy", initialCategories.contains("Pharmacy")),
            JobCategory("Laboratory / Research", initialCategories.contains("Laboratory / Research")),
            JobCategory("Public Health", initialCategories.contains("Public Health")),
            JobCategory("Teaching / Education", initialCategories.contains("Teaching / Education")),
            JobCategory("Training / Coaching", initialCategories.contains("Training / Coaching")),
            JobCategory("Academic Research", initialCategories.contains("Academic Research")),
            JobCategory("Hotel / Hospitality", initialCategories.contains("Hotel / Hospitality")),
            JobCategory("Travel / Tourism", initialCategories.contains("Travel / Tourism")),
            JobCategory("Food & Beverage", initialCategories.contains("Food & Beverage")),
            JobCategory("Event Management", initialCategories.contains("Event Management")),
            JobCategory("Government Jobs", initialCategories.contains("Government Jobs")),
            JobCategory("Legal / Law / Compliance", initialCategories.contains("Legal / Law / Compliance")),
            JobCategory("NGO / INGO / Social Work", initialCategories.contains("NGO / INGO / Social Work")),
            JobCategory("Public Administration / Policy", initialCategories.contains("Public Administration / Policy")),
            JobCategory("Skilled Labor / Trades", initialCategories.contains("Skilled Labor / Trades")),
            JobCategory("Security Services", initialCategories.contains("Security Services")),
            JobCategory("Cleaning / Housekeeping", initialCategories.contains("Cleaning / Housekeeping")),
            JobCategory("Agriculture / Farming", initialCategories.contains("Agriculture / Farming"))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Animated Header
            var isVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                isVisible = true
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.jobcategoryicon),
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Select Categories",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "You can select up to 5 categories",
                        fontSize = 14.sp,
                        color = Color(0xFF424242)
                    )

                    val selectedCount = categoryList.count { it.isSelected }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (selectedCount >= 5) Color(0xFFE57373) else Color(0xFF2196F3),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "$selectedCount/5 selected",
                            fontSize = 15.sp,
                            color = if (selectedCount >= 5) Color(0xFFC62828) else Color(0xFF1565C0),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Search Field
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(400, delayMillis = 100, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400, delayMillis = 100))
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search categories...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.jobcategoryicon),
                            contentDescription = "Search",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.9f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                        focusedIndicatorColor = Color(0xFF2196F3),
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val filteredList = categoryList.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }

                items(filteredList.size, key = { filteredList[it].name }) { index ->
                    val category = filteredList[index]
                    val categoryIndex = categoryList.indexOf(category)

                    var itemVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 20L)
                        itemVisible = true
                    }

                    AnimatedVisibility(
                        visible = itemVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { 100 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeIn()
                    ) {
                        SelectableCategoryItem(
                            name = category.name,
                            isSelected = category.isSelected
                        ) {
                            val currentSelected = categoryList.count { it.isSelected }
                            if (!category.isSelected && currentSelected >= 5) return@SelectableCategoryItem
                            categoryList[categoryIndex] = category.copy(isSelected = !category.isSelected)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Buttons
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { 60 },
                    animationSpec = tween(400, delayMillis = 200, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400, delayMillis = 200))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, Color(0xFF2196F3)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(
                            "Cancel",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2196F3)
                        )
                    }

                    Button(
                        onClick = { onSave(categoryList.filter { it.isSelected }.map { it.name }) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Done", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableCategoryItem(
    name: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
        animationSpec = tween(300)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF2196F3), shape = RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    name,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFF1565C0) else Color(0xFF424242)
                )
            }

            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF2196F3), shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CompanyUploadPostPreview(){}