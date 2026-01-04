package com.example.rojgar.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
    // Create ViewModels at the top level and remember them
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

    // IMPORTANT: Load job posts immediately when screen opens
    LaunchedEffect(Unit) {
        if (companyId.isNotEmpty()) {
            jobViewModel.getJobPostsByCompanyId(companyId)
        }
    }

    // Refresh when returning from create/edit mode
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
                // Reload posts after creation/update
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

    // Delete Confirmation Dialog
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
            .background(Blue)
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

            // Create Post Button - Always at Bottom
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
            // LEFT SIDE - Image
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
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // RIGHT SIDE - Content
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

                // Job Type Badge
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

                // Action Buttons Row with Icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edit Button with Icon
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
                            tint = DarkBlue2,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete Button with Icon
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Auto-upload banner when selected
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
            .background(Blue)
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

        // Hiring Banner
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
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Upload Hiring Banner", color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                }

                // Show loading indicator while uploading
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

        // Form Fields
        JobPostTextField(value = title, onValueChange = { title = it }, label = "Job Title*", icon = R.drawable.jobtitleicon)
        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(value = position, onValueChange = { position = it }, label = "Position*", icon = R.drawable.jobpost_filled)
        Spacer(modifier = Modifier.height(16.dp))

        // Category Selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCategoryBottomSheet = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Blue),
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
                        painter = painterResource(id = R.drawable.jobcategoryicon),
                        contentDescription = "Category",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    if (selectedCategories.isEmpty()) {
                        Text("Select Category", color = Color.Gray, fontSize = 16.sp)
                    } else {
                        Text(
                            text = selectedCategories.joinToString(", "),
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }
                }
                Icon(
                    painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                    contentDescription = "Dropdown",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(value = jobType, onValueChange = { jobType = it }, label = "Job Type", icon = R.drawable.jobtype)
        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(value = experience, onValueChange = { experience = it }, label = "Experience Required", icon = R.drawable.experience_filled)
        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(value = education, onValueChange = { education = it }, label = "Education", icon = R.drawable.educationboardicon)
        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(value = skills, onValueChange = { skills = it }, label = "Required Skills", icon = R.drawable.skills_filledicon)
        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(value = salary, onValueChange = { salary = it }, label = "Salary", icon = R.drawable.salaryicon)
        Spacer(modifier = Modifier.height(16.dp))

        // Deadline
        OutlinedTextField(
            value = deadline,
            onValueChange = {},
            readOnly = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.deadlineicon),
                    contentDescription = "Deadline",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.datetimeicon),
                        contentDescription = "Select Date",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = { Text("Application Deadline") },
            placeholder = { Text("Select date and time") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = Blue,
                unfocusedContainerColor = Blue,
                focusedIndicatorColor = Purple,
                unfocusedIndicatorColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(
            value = responsibilities,
            onValueChange = { responsibilities = it },
            label = "Key Responsibilities",
            icon = R.drawable.responsibilityicon,
            minHeight = 100.dp
        )
        Spacer(modifier = Modifier.height(16.dp))

        JobPostTextField(
            value = jobDescription,
            onValueChange = { jobDescription = it },
            label = "Job Description",
            icon = R.drawable.jobdescriptionicon,
            minHeight = 100.dp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
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
                        imageUrl = displayedBannerUrl, // For backward compatibility
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
}

@Composable
fun JobPostTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: Int,
    minHeight: androidx.compose.ui.unit.Dp = 56.dp
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        leadingIcon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = Color.Black,
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
            focusedContainerColor = Blue,
            unfocusedContainerColor = Blue,
            focusedIndicatorColor = Purple,
            unfocusedIndicatorColor = Color.Black
        )
    )
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
            JobCategory("Accounting / Finance", initialCategories.contains("Accounting / Finance")),
            JobCategory("Architecture / Interior Designing", initialCategories.contains("Architecture / Interior Designing")),
            JobCategory("Banking / Insurance / Financial Services", initialCategories.contains("Banking / Insurance / Financial Services")),
            JobCategory("Commercial / Logistics / Supply Chain", initialCategories.contains("Commercial / Logistics / Supply Chain")),
            JobCategory("Construction / Engineering / Architects", initialCategories.contains("Construction / Engineering / Architects")),
            JobCategory("Fashion / Textile Designing", initialCategories.contains("Fashion / Textile Designing")),
            JobCategory("General Management", initialCategories.contains("General Management")),
            JobCategory("IT & Software", initialCategories.contains("IT & Software")),
            JobCategory("Marketing & Sales", initialCategories.contains("Marketing & Sales")),
            JobCategory("Human Resources", initialCategories.contains("Human Resources"))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .padding(20.dp)
    ) {
        Text("Select Job Categories", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("You can select up to 5 categories", fontSize = 14.sp, color = Color.Gray)

        val selectedCount = categoryList.count { it.isSelected }
        Text(
            "$selectedCount/5 selected",
            fontSize = 14.sp,
            color = if (selectedCount >= 5) Color.Red else DarkBlue2,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search categories") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            val filteredList = categoryList.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }

            items(filteredList, key = { it.name }) { category ->
                val index = categoryList.indexOf(category)

                SelectableCategoryItem(
                    name = category.name,
                    isSelected = category.isSelected
                ) {
                    val currentSelected = categoryList.count { it.isSelected }
                    if (!category.isSelected && currentSelected >= 5) return@SelectableCategoryItem
                    categoryList[index] = category.copy(isSelected = !category.isSelected)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", fontSize = 16.sp)
            }

            Button(
                onClick = { onSave(categoryList.filter { it.isSelected }.map { it.name }) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue2)
            ) {
                Text("Done", fontSize = 16.sp)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, fontSize = 16.sp)
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = DarkBlue2
                )
            }
        }
    }
}

@Preview
@Composable
fun CompanyUploadPostPreview(){
}