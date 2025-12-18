package com.example.rojgar.view

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Base64
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.*
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.example.rojgar.viewmodel.JobViewModel
import java.io.ByteArrayOutputStream
import java.util.*

class CompanyUploadPost : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = CompanyViewModel(CompanyRepoImpl())
            CompanyUploadPostScreen()
        }
    }
}

@Composable
fun CompanyUploadPostScreen() {
    val jobViewModel = remember { JobViewModel(JobRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }
    val context = LocalContext.current
    var jobPosts by remember { mutableStateOf<List<JobModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateForm by remember { mutableStateOf(false) }
    var editingPost by remember { mutableStateOf<JobModel?>(null) }
    var selectedPostForView by remember { mutableStateOf<JobModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf<JobModel?>(null) }

    val currentUser = companyViewModel.getCurrentCompany()
    val companyId = currentUser?.uid ?: ""

    // Load job posts
    LaunchedEffect(companyId) {
        if (companyId.isNotEmpty()) {
            jobViewModel.getJobPostsByCompanyId(companyId) { success, message, posts ->
                isLoading = false
                if (success && posts != null) {
                    jobPosts = posts
                    if (posts.isEmpty()) {
                        showCreateForm = true
                    }
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    when {
        selectedPostForView != null -> {
            JobPostDetailView(
                jobPost = selectedPostForView!!,
                onBack = { selectedPostForView = null },
                onEdit = {
                    editingPost = selectedPostForView
                    selectedPostForView = null
                    showCreateForm = true
                },
                onDelete = {
                    showDeleteDialog = selectedPostForView
                }
            )
        }
        showCreateForm || editingPost != null -> {
            CompanyUploadPostBody(
                jobViewModel = jobViewModel,
                companyId = companyId,
                existingPost = editingPost,
                onPostCreated = {
                    showCreateForm = false
                    editingPost = null
                    // Reload posts
                    jobViewModel.getJobPostsByCompanyId(companyId) { success, _, posts ->
                        if (success && posts != null) {
                            jobPosts = posts
                        }
                    }
                },
                onCancel = {
                    showCreateForm = false
                    editingPost = null
                }
            )
        }
        else -> {
            JobPostListScreen(
                jobPosts = jobPosts,
                isLoading = isLoading,
                onCreatePost = { showCreateForm = true },
                onPostClick = { selectedPostForView = it },
                onEdit = {
                    editingPost = it
                    showCreateForm = true
                },
                onDelete = { showDeleteDialog = it }
            )
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { post ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Job Post") },
            text = { Text("Are you sure you want to delete this job post?") },
            confirmButton = {
                Button(
                    onClick = {
                        jobViewModel.deleteJobPost(post.postId) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) {
                                jobPosts = jobPosts.filter { it.postId != post.postId }
                            }
                            showDeleteDialog = null
                            selectedPostForView = null
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
    onPostClick: (JobModel) -> Unit,
    onEdit: (JobModel) -> Unit,
    onDelete: (JobModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "My Job Posts",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(jobPosts, key = { it.postId }) { post ->
                    JobPostCard(
                        jobPost = post,
                        onClick = { onPostClick(post) },
                        onEdit = { onEdit(post) },
                        onDelete = { onDelete(post) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create Post Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onCreatePost,
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .width(170.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue2,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Create Post",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun JobPostCard(
    jobPost: JobModel,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image Section
            if (jobPost.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(jobPost.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = Color.Gray)
                }
            }

            // Content Section
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = jobPost.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = jobPost.position,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Job Type: ${jobPost.jobType}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = DarkBlue2,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JobPostDetailView(
    jobPost: JobModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Back button
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                contentDescription = "Back",
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image
        if (jobPost.imageUrl.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(jobPost.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(15.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = jobPost.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = jobPost.position,
                    fontSize = 18.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                DetailRow("Job Type", jobPost.jobType)
                DetailRow("Experience", jobPost.experience)
                DetailRow("Education", jobPost.education)
                DetailRow("Skills", jobPost.skills)
                DetailRow("Salary", jobPost.salary)
                DetailRow("Deadline", jobPost.deadline)

                if (jobPost.categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Categories:", fontWeight = FontWeight.Bold)
                    Text(jobPost.categories.joinToString(", "))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Responsibilities:", fontWeight = FontWeight.Bold)
                Text(jobPost.responsibilities)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Job Description:", fontWeight = FontWeight.Bold)
                Text(jobPost.jobDescription)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue2)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit")
            }

            Button(
                onClick = onDelete,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    if (value.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = "$label: ",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(120.dp)
            )
            Text(text = value, modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyUploadPostBody(
    jobViewModel: JobViewModel,
    companyId: String,
    existingPost: JobModel? = null,
    onPostCreated: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    // State variables
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
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageString by remember { mutableStateOf(existingPost?.imageUrl ?: "") }

    var showCategoryBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            imageString = it.toString()
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

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Blue)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (existingPost != null) "Edit Job Post" else "Create Job Post",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Cover Photo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        selectedImageUri != null -> {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        existingPost?.imageUrl?.isNotEmpty() == true -> {
                            Image(
                                painter = rememberAsyncImagePainter(existingPost.imageUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Text(
                                text = "Upload Photo",
                                style = TextStyle(fontSize = 18.sp, color = Color.Gray)
                            )
                        }
                    }

                    if (selectedImageUri == null && existingPost?.imageUrl?.isEmpty() != false) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_upload_24),
                            contentDescription = "Add Cover Photo",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(60.dp)
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Form Fields
            JobPostTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title",
                icon = R.drawable.jobtitleicon
            )
            Spacer(modifier = Modifier.height(20.dp))

            JobPostTextField(
                value = position,
                onValueChange = { position = it },
                label = "Position",
                icon = R.drawable.jobpost_filled
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Category Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryBottomSheet = true },
                shape = RoundedCornerShape(15.dp),
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

            Spacer(modifier = Modifier.height(20.dp))

            JobPostTextField(value = jobType, onValueChange = { jobType = it }, label = "Job Type", icon = R.drawable.jobtype)
            Spacer(modifier = Modifier.height(20.dp))

            JobPostTextField(value = experience, onValueChange = { experience = it }, label = "Experience", icon = R.drawable.experience_filled)
            Spacer(modifier = Modifier.height(20.dp))

            JobPostTextField(value = education, onValueChange = { education = it }, label = "Education", icon = R.drawable.educationboardicon)
            Spacer(modifier = Modifier.height(20.dp))

            JobPostTextField(value = skills, onValueChange = { skills = it }, label = "Skills", icon = R.drawable.skills_filledicon)
            Spacer(modifier = Modifier.height(20.dp))

            JobPostTextField(value = salary, onValueChange = { salary = it }, label = "Salary", icon = R.drawable.salaryicon)
            Spacer(modifier = Modifier.height(20.dp))

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
                label = { Text("Deadline") },
                placeholder = { Text("Select date and time") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Blue,
                    unfocusedContainerColor = Blue,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            JobPostTextField(
                value = responsibilities,
                onValueChange = { responsibilities = it },
                label = "Responsibilities",
                icon = R.drawable.responsibilityicon,
                minHeight = 60.dp
            )
            Spacer(modifier = Modifier.height(20.dp))

            JobPostTextField(
                value = jobDescription,
                onValueChange = { jobDescription = it },
                label = "Job Description",
                icon = R.drawable.jobdescriptionicon,
                minHeight = 60.dp
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        if (title.isEmpty() || position.isEmpty()) {
                            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
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
                            imageUrl = imageString,
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
                    shape = RoundedCornerShape(40.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue2)
                ) {
                    Text(if (existingPost != null) "Update" else "Post", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onCancel,
                    shape = RoundedCornerShape(40.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gray)
                ) {
                    Text("Cancel", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
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
}

@OptIn(ExperimentalMaterial3Api::class)
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
                shape = RoundedCornerShape(15.dp),
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

                Text(
                    "Select Job Category",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "You can select up to 5 categories",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                val selectedCount = categoryList.count { it.isSelected }
                Text(
                    "$selectedCount/5 selected",
                    color = if (selectedCount >= 5) Color.Red else DarkBlue2
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search categories") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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

                            categoryList[index] =
                                category.copy(isSelected = !category.isSelected)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onSave(categoryList.filter { it.isSelected }.map { it.name })
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue2)
                    ) {
                        Text("Done")
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


//
//@Composable
//fun SelectableCategoryItem(name: String, isSelected: Boolean, onToggle: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clickable { onToggle() },
//        colors = CardDefaults.cardColors(
//            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
//        ),
//        shape = RoundedCornerShape(8.dp),
//        border = ButtonDefaults.outlinedButtonBorder
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                name,
//                fontSize = 16.sp,
//                color = Color.Black,
//                modifier = Modifier.weight(1f)
//            )
//            if (isSelected) {
//                Icon(
//                    imageVector = Icons.Default.Check,
//                    contentDescription = "Selected",
//                    tint = DarkBlue2,
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//        }
//    }
//}
@Preview
@Composable
fun CompanyUploadPostPreview(){
}
