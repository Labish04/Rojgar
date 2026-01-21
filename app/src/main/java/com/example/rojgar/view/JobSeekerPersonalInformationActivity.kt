package com.example.rojgar.view

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rojgar.R
import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.Gray
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White
import com.example.rojgar.utils.ImageUtils
import com.example.rojgar.viewmodel.JobSeekerViewModel
import java.util.Calendar

class JobSeekerPersonalInformationActivity : ComponentActivity() {
    lateinit var imageUtils: ImageUtils
    var selectedProfileUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        imageUtils = ImageUtils(this, this)
        imageUtils.registerLaunchers { uri ->
            if (uri != null) {
                selectedProfileUri = uri
            }
        }

        setContent {
            val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
            JobSeekerPersonalInformationBody(
                jobSeekerViewModel = jobSeekerViewModel,
                selectedProfileUri = selectedProfileUri,
                onPickProfileImage = {
                    imageUtils.launchImagePicker()
                },
                onClearSelectedImages = {
                    selectedProfileUri = null
                }
            )
        }
    }
}

@Composable
fun JobSeekerPersonalInformationBody(
    jobSeekerViewModel: JobSeekerViewModel,
    selectedProfileUri: Uri?,
    onPickProfileImage: () -> Unit,
    onClearSelectedImages: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val currentUser = jobSeekerViewModel.getCurrentJobSeeker()

    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var currentAddress by remember { mutableStateOf("") }
    var permanentAddress by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var religion by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("") }
    var maritalStatus by remember { mutableStateOf("") }
    var expandedStatus by remember { mutableStateOf(false) }
    var bio by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }
    var existingJobSeeker by remember { mutableStateOf<JobSeekerModel?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadedProfilePhotoUrl by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d -> selectedDate = "$d/${m + 1}/$y" },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            jobSeekerViewModel.getJobSeekerById(userId) { success, message, jobSeeker ->
                if (success && jobSeeker != null) {
                    name = jobSeeker.fullName
                    phoneNumber = jobSeeker.phoneNumber
                    gender = jobSeeker.gender
                    selectedDate = jobSeeker.dob
                    currentAddress = jobSeeker.currentAddress
                    permanentAddress = jobSeeker.permanentAddress
                    email = jobSeeker.email
                    religion = jobSeeker.religion
                    nationality = jobSeeker.nationality
                    maritalStatus = jobSeeker.maritalStatus
                    bio = jobSeeker.bio
                    profession = jobSeeker.profession
                    uploadedProfilePhotoUrl = jobSeeker.profilePhoto
                    existingJobSeeker = jobSeeker
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFFBBDEFB),
                            Color(0xFF90CAF9)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, JobSeekerProfileDetailsActivity::class.java)
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = "Back",
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Personal Information",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Profile Picture Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .shadow(12.dp, CircleShape)
                            .background(White, CircleShape)
                            .clickable { onPickProfileImage() }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                                    ),
                                    CircleShape
                                )
                                .border(3.dp, Purple.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                selectedProfileUri != null -> {
                                    AsyncImage(
                                        model = selectedProfileUri,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                uploadedProfilePhotoUrl.isNotEmpty() -> {
                                    AsyncImage(
                                        model = uploadedProfilePhotoUrl,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.addprofileicon),
                                            contentDescription = "Add Profile",
                                            modifier = Modifier.size(48.dp),
                                            tint = Purple
                                        )
                                        Text(
                                            text = "Add Photo",
                                            fontSize = 12.sp,
                                            color = DarkBlue2,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap to change photo",
                        fontSize = 13.sp,
                        color = Color(0xFF032950).copy(alpha = 0.9f),
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Form Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Profile Details",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue2,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Name
                        ModernTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Full Name",
                            icon = R.drawable.usericon
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phone
                        ModernTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = "Phone Number",
                            icon = R.drawable.call
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Gender Dropdown
                        ModernDropdown(
                            value = gender,
                            label = "Gender",
                            icon = R.drawable.gendericon,
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            options = listOf("Male", "Female", "Other"),
                            onOptionSelected = { gender = it; expanded = false }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Date of Birth
                        ModernTextField(
                            value = selectedDate,
                            onValueChange = {},
                            label = "Date of Birth",
                            icon = R.drawable.birthdaydateicon,
                            readOnly = true,
                            onClick = { datePickerDialog.show() },
                            trailingIcon = R.drawable.calendaricon
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Current Address
                        ModernTextField(
                            value = currentAddress,
                            onValueChange = { currentAddress = it },
                            label = "Current Address",
                            icon = R.drawable.locationicon
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Permanent Address
                        ModernTextField(
                            value = permanentAddress,
                            onValueChange = { permanentAddress = it },
                            label = "Permanent Address",
                            icon = R.drawable.locationicon
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email
                        ModernTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            icon = R.drawable.emailicon
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Religion
                        ModernTextField(
                            value = religion,
                            onValueChange = { religion = it },
                            label = "Religion",
                            icon = R.drawable.religionicon
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nationality
                        ModernTextField(
                            value = nationality,
                            onValueChange = { nationality = it },
                            label = "Nationality",
                            icon = R.drawable.nationalityicon
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Marital Status
                        ModernDropdown(
                            value = maritalStatus,
                            label = "Marital Status",
                            icon = R.drawable.maritalstatusicon,
                            expanded = expandedStatus,
                            onExpandedChange = { expandedStatus = it },
                            options = listOf("Single", "Married", "Divorced", "Widowed"),
                            onOptionSelected = { maritalStatus = it; expandedStatus = false }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Profession
                        ModernTextField(
                            value = profession,
                            onValueChange = { profession = it },
                            label = "Profession",
                            icon = R.drawable.professionicon
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bio
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.bioicon),
                                    contentDescription = null,
                                    tint = DarkBlue2,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    "Bio",
                                    color = Gray,
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(16.dp),
                            maxLines = 4,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F9FF),
                                unfocusedContainerColor = Color(0xFFF5F9FF),
                                focusedIndicatorColor = Purple,
                                unfocusedIndicatorColor = Color(0xFFE0E0E0),
                                cursorColor = Purple
                            )
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { activity.finish() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gray.copy(alpha = 0.2f),
                                    contentColor = Color.DarkGray
                                ),
                                elevation = ButtonDefaults.buttonElevation(0.dp),
                                enabled = !isUploading
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = {
                                    if (isUploading) return@Button
                                    currentUser?.uid?.let { userId ->
                                        isUploading = true

                                        fun saveProfile(profileUrl: String?) {
                                            val finalProfilePhotoUrl = profileUrl ?: uploadedProfilePhotoUrl

                                            val updatedModel = JobSeekerModel(
                                                jobSeekerId = userId,
                                                fullName = name,
                                                email = email,
                                                phoneNumber = phoneNumber,
                                                gender = gender,
                                                dob = selectedDate,
                                                currentAddress = currentAddress,
                                                permanentAddress = permanentAddress,
                                                bio = bio,
                                                profession = profession,
                                                profilePhoto = finalProfilePhotoUrl,
                                                religion = religion,
                                                nationality = nationality,
                                                maritalStatus = maritalStatus,
                                                video = existingJobSeeker?.video ?: ""
                                            )

                                            jobSeekerViewModel.updateProfile(updatedModel) { success, message ->
                                                isUploading = false
                                                if (success) {
                                                    Toast.makeText(context, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                                                    existingJobSeeker = updatedModel
                                                    onClearSelectedImages()
                                                    uploadedProfilePhotoUrl = finalProfilePhotoUrl
                                                } else {
                                                    Toast.makeText(context, "Update Failed: $message", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }

                                        if (selectedProfileUri != null) {
                                            jobSeekerViewModel.uploadProfileImage(context, selectedProfileUri!!) { profileUrl ->
                                                saveProfile(profileUrl)
                                            }
                                        } else {
                                            saveProfile(null)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3)
                                ),
                                elevation = ButtonDefaults.buttonElevation(4.dp),
                                enabled = !isUploading
                            ) {
                                if (isUploading) {
                                    CircularProgressIndicator(
                                        color = White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Save Profile",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: Int,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingIcon: Int? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        leadingIcon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = DarkBlue2,
                modifier = Modifier.size(22.dp)
            )
        },
        trailingIcon = trailingIcon?.let {
            {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onClick?.invoke() }
                )
            }
        },
        label = {
            Text(
                label,
                color = Gray,
                fontSize = 14.sp
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        readOnly = readOnly,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F9FF),
            unfocusedContainerColor = Color(0xFFF5F9FF),
            disabledContainerColor = Color(0xFFF5F9FF),
            focusedIndicatorColor = Purple,
            unfocusedIndicatorColor = Color(0xFFE0E0E0),
            disabledIndicatorColor = Color(0xFFE0E0E0),
            disabledTextColor = Color.Black,
            cursorColor = Purple
        )
    )
}

@Composable
fun ModernDropdown(
    value: String,
    label: String,
    icon: Int,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = DarkBlue2,
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    label,
                    color = Gray,
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clickable { onExpandedChange(true) },
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onExpandedChange(true) }
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                disabledContainerColor = Color(0xFFF5F9FF),
                disabledIndicatorColor = Color(0xFFE0E0E0),
                disabledTextColor = Color.Black
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .background(White)
                .fillMaxWidth(0.9f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            color = DarkBlue2,
                            fontWeight = if (value == option) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }
}