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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    // Single ImageUtils instance
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
                    Log.d("ImageSelection", "Cover photo selected: $uri")
                } else if (isPickingProfile) {
                    selectedProfileUri = uri
                    Log.d("ImageSelection", "Profile photo selected: $uri")
                }

                isPickingCover = false
                isPickingProfile = false
            }
        }

        setContent {
            val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
            JobSeekerPersonalInformationBody(
                jobSeekerViewModel = jobSeekerViewModel,
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
                },
                onClearSelectedImages = {
                    selectedCoverUri = null
                    selectedProfileUri = null
                }
            )
        }
    }
}

@Composable
fun JobSeekerPersonalInformationBody(
    jobSeekerViewModel: JobSeekerViewModel,
    selectedCoverUri: Uri?,
    selectedProfileUri: Uri?,
    onPickCoverImage: () -> Unit,
    onPickProfileImage: () -> Unit,
    onClearSelectedImages: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    val currentUser = jobSeekerViewModel.getCurrentJobSeeker()

    var showSampleDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            selectedDate = "$d/${m + 1}/$y"
        },
        year,
        month,
        day
    )

    var existingJobSeeker by remember { mutableStateOf<JobSeekerModel?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadedCoverPhotoUrl by remember { mutableStateOf("") }
    var uploadedProfilePhotoUrl by remember { mutableStateOf("") }

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
                    uploadedCoverPhotoUrl = jobSeeker.coverPhoto
                    uploadedProfilePhotoUrl = jobSeeker.profilePhoto
                    existingJobSeeker = jobSeeker
                } else {
                    Toast.makeText(context, "Failed to load profile: $message", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }

    if (showSampleDialog) {
        Dialog(onDismissRequest = { showSampleDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cover Photo Requirements",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue2
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .height(308.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.addprofileicon),
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Sample Cover Photo",
                                    color = Color.DarkGray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Required Dimensions:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Text(
                        text = "1080 × 1668 pixels",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Aspect Ratio: 9:13.9\n• Portrait orientation\n• High quality image recommended",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            onClick = { showSampleDialog = false }
                        ) {
                            Text("Close", color = Gray)
                        }

                        Button(
                            onClick = {
                                showSampleDialog = false
                                onPickCoverImage()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkBlue2
                            )
                        ) {
                            Text("Upload Photo")
                        }
                    }
                }
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(
                    text = "Upload Failed",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue2
                    )
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        showSampleDialog = true
                    }
                ) {
                    Text("View Requirements", color = Purple)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .height(140.dp)
                    .padding(top = 55.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(5.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBlue2)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {

                    IconButton(onClick = {
                        val intent = Intent(context, JobSeekerProfileDetailsActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(50.dp))
                    Text(
                        "Personal Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Blue)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    // Cover Photo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onPickCoverImage()
                            }
                            .padding(10.dp)
                    ) {
                        when {
                            selectedCoverUri != null -> {
                                AsyncImage(
                                    model = selectedCoverUri,
                                    contentDescription = "cover Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            uploadedCoverPhotoUrl.isNotEmpty() -> {
                                AsyncImage(
                                    model = uploadedCoverPhotoUrl,
                                    contentDescription = "cover Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Image(
                                    painterResource(R.drawable.coveremptypic),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(y = 50.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(Color.White, CircleShape)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    onPickProfileImage()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .background(Color.LightGray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    selectedProfileUri != null -> {
                                        AsyncImage(
                                            model = selectedProfileUri,
                                            contentDescription = "Selected Profile Photo",
                                            modifier = Modifier.fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    uploadedProfilePhotoUrl.isNotEmpty() -> {
                                        AsyncImage(
                                            model = uploadedProfilePhotoUrl,
                                            contentDescription = "Profile Photo",
                                            modifier = Modifier.fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            painter = painterResource(id = R.drawable.addprofileicon),
                                            contentDescription = "Add Profile",
                                            modifier = Modifier.size(50.dp),
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(70.dp))

                // NAME TEXTFIELD
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.usericon),
                            contentDescription = "Name",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Enter Your Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = Blue,
                        focusedContainerColor = Blue,
                        unfocusedContainerColor = Blue,
                        focusedIndicatorColor = Purple,
                        unfocusedIndicatorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // PHONE NUMBER TEXTFIELD
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.call),
                            contentDescription = "Phone Number",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Enter Your Phone Number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = Blue,
                        focusedContainerColor = Blue,
                        unfocusedContainerColor = Blue,
                        focusedIndicatorColor = Purple,
                        unfocusedIndicatorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // GENDER TEXTFIELD
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        readOnly = true,
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.gendericon),
                                contentDescription = "Gender",
                                tint = Color.Black,
                                modifier = Modifier.size(27.dp)
                            )
                        },
                        label = { Text("Select Your Gender") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable { expanded = true },
                        shape = RoundedCornerShape(15.dp),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                                contentDescription = "Dropdown",
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { expanded = true }
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            disabledIndicatorColor = Color.Black,
                            disabledContainerColor = Blue,
                            focusedContainerColor = Blue,
                            unfocusedContainerColor = Blue,
                            focusedIndicatorColor = Purple,
                            unfocusedIndicatorColor = Color.Black,
                            disabledTextColor = Color.Black,
                        )
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(White)
                            .fillMaxWidth()
                    ) {
                        DropdownMenuItem(
                            text = { Text("Male") },
                            onClick = {
                                gender = "Male"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Female") },
                            onClick = {
                                gender = "Female"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Other") },
                            onClick = {
                                gender = "Other"
                                expanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // DATE OF BIRTH
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    placeholder = { Text("dd/mm/yyyy") },
                    enabled = false,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.birthdaydateicon),
                            contentDescription = "Calendar",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.calendaricon),
                            contentDescription = "Open Calendar",
                            tint = Color.Black,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { datePickerDialog.show() }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable { datePickerDialog.show() },
                    shape = RoundedCornerShape(15.dp),
                    colors = TextFieldDefaults.colors(
                        disabledIndicatorColor = Color.Black,
                        disabledContainerColor = Blue,
                        disabledTextColor = Color.Black,
                        focusedContainerColor = Color(0xFFE3F2FD),
                        unfocusedContainerColor = Color(0xFFE3F2FD),
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // CURRENT ADDRESS
                OutlinedTextField(
                    value = currentAddress,
                    onValueChange = { currentAddress = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.locationicon),
                            contentDescription = "Address",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Your Current Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = Blue,
                        focusedContainerColor = Blue,
                        unfocusedContainerColor = Blue,
                        focusedIndicatorColor = Purple,
                        unfocusedIndicatorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // PERMANENT ADDRESS
                OutlinedTextField(
                    value = permanentAddress,
                    onValueChange = { permanentAddress = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.locationicon),
                            contentDescription = "Permanent",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Your Permanent Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = Blue,
                        focusedContainerColor = Blue,
                        unfocusedContainerColor = Blue,
                        focusedIndicatorColor = Purple,
                        unfocusedIndicatorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // EMAIL
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.emailicon),
                            contentDescription = "Mail",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Enter Your Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = Blue,
                        focusedContainerColor = Blue,
                        unfocusedContainerColor = Blue,
                        focusedIndicatorColor = Purple,
                        unfocusedIndicatorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = religion,
                    onValueChange = { religion = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.religionicon),
                            contentDescription = "Religion",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Enter Your Religion") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = Blue,
                        focusedContainerColor = Blue,
                        unfocusedContainerColor = Blue,
                        focusedIndicatorColor = Purple,
                        unfocusedIndicatorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = nationality,
                    onValueChange = { nationality = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.nationalityicon),
                            contentDescription = "Nationality",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Enter Your Nationality") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = Blue,
                        focusedContainerColor = Blue,
                        unfocusedContainerColor = Blue,
                        focusedIndicatorColor = Purple,
                        unfocusedIndicatorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = maritalStatus,
                        onValueChange = { maritalStatus = it },
                        readOnly = true,
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.maritalstatusicon),
                                contentDescription = "Marital Status",
                                tint = Color.Black,
                                modifier = Modifier.size(27.dp)
                            )
                        },
                        label = { Text("Select Your Marital Status") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable { expandedStatus = true },
                        shape = RoundedCornerShape(15.dp),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                                contentDescription = "Dropdown",
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { expandedStatus = true }
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            disabledIndicatorColor = Color.Black,
                            disabledContainerColor = Blue,
                            focusedContainerColor = Blue,
                            unfocusedContainerColor = Blue,
                            focusedIndicatorColor = Purple,
                            unfocusedIndicatorColor = Color.Black,
                            disabledTextColor = Color.Black,
                        )
                    )

                    DropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false },
                        modifier = Modifier
                            .background(White)
                            .fillMaxWidth()
                    ) {
                        DropdownMenuItem(
                            text = { Text("Single") },
                            onClick = {
                                maritalStatus = "Single"
                                expandedStatus = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Married") },
                            onClick = {
                                maritalStatus = "Married"
                                expandedStatus = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Divorced") },
                            onClick = {
                                maritalStatus = "Divorced"
                                expandedStatus = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Widowed") },
                            onClick = {
                                maritalStatus = "Widowed"
                                expandedStatus = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = profession,
                    onValueChange = { profession = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.professionicon),
                            contentDescription = "profession",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Enter Your Profession") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = Blue,
                        focusedContainerColor = Blue,
                        unfocusedContainerColor = Blue,
                        focusedIndicatorColor = Purple,
                        unfocusedIndicatorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // BIO
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.bioicon),
                            contentDescription = "Bio",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Bio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = false,
                    maxLines = 3,
                    colors = TextFieldDefaults.colors(
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = Blue,
                        focusedContainerColor = Blue,
                        unfocusedContainerColor = Blue,
                        focusedIndicatorColor = Purple,
                        unfocusedIndicatorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Save Button
                    Button(
                        onClick = {
                            if (isUploading) return@Button

                            currentUser?.uid?.let { userId ->
                                Log.d("Save", "Starting save process for user: $userId")

                                isUploading = true

                                fun saveProfile(coverUrl: String?, profileUrl: String?) {
                                    val finalCoverPhotoUrl = coverUrl ?: uploadedCoverPhotoUrl
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
                                        coverPhoto = finalCoverPhotoUrl,
                                        religion = religion,
                                        nationality = nationality,
                                        maritalStatus = maritalStatus,
                                        video = existingJobSeeker?.video ?: "",
                                        followers = existingJobSeeker?.followers ?: emptyList(),
                                        appliedJobs = existingJobSeeker?.appliedJobs ?: emptyList()
                                    )

                                    jobSeekerViewModel.updateProfile(updatedModel) { success, message ->
                                        isUploading = false
                                        if (success) {
                                            Toast.makeText(context, "Personal Information Updated Successfully!", Toast.LENGTH_SHORT).show()
                                            existingJobSeeker = updatedModel
                                            onClearSelectedImages()

                                            uploadedCoverPhotoUrl = finalCoverPhotoUrl
                                            uploadedProfilePhotoUrl = finalProfilePhotoUrl
                                        } else {
                                            Toast.makeText(context, "Update Failed: $message", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                                if (selectedCoverUri != null) {
                                    jobSeekerViewModel.updateCoverPhoto(context, selectedCoverUri) { coverUrl ->
                                        Log.d("Upload", "Cover photo upload result: $coverUrl")

                                        if (selectedProfileUri != null) {
                                            jobSeekerViewModel.uploadProfileImage(context, selectedProfileUri) { profileUrl ->
                                                Log.d("Upload", "Profile photo upload result: $profileUrl")
                                                saveProfile(coverUrl, profileUrl)
                                            }
                                        } else {
                                            saveProfile(coverUrl, null)
                                        }
                                    }
                                } else {
                                    if (selectedProfileUri != null) {
                                        jobSeekerViewModel.uploadProfileImage(context, selectedProfileUri) { profileUrl ->
                                            saveProfile(null, profileUrl)
                                        }
                                    } else {
                                        saveProfile(null, null)
                                    }
                                }
                            } ?: run {
                                Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
                                isUploading = false
                            }
                        },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .width(130.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkBlue2,
                            contentColor = Color.White
                        ),
                        enabled = !isUploading
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Save",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Cancel Button
                    Button(
                        onClick = {
                            activity.finish()
                        },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .width(130.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gray,
                            contentColor = Color.Black
                        ),
                        enabled = !isUploading
                    ) {
                        Text(
                            text = "Cancel",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Preview
@Composable
fun JobSeekerPersonalInformationPreview() {
    JobSeekerPersonalInformationBody(
        jobSeekerViewModel = JobSeekerViewModel(JobSeekerRepoImpl()),
        selectedCoverUri = null,
        selectedProfileUri = null,
        onPickCoverImage = {},
        onPickProfileImage = {},
        onClearSelectedImages = {}
    )
}