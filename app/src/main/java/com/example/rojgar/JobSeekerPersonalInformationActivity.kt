package com.example.rojgar

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter

class JobSeekerPersonalInformationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerPersonalInformationBody()
        }
    }
}

@Composable
fun JobSeekerPersonalInformationBody() {
    val context = LocalContext.current
    val activity = context as Activity

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult (
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }


    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Blue)
        ) {
            // Top Bar
            Card(
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkBlue2
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        "Personal Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.size(30.dp)) // Balance the layout
                }
            }

            // Make whole page scrollable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Box {

                    Card {
                        // Cover Photo Section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedImageUri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Camera Icon in top right corner
                            Icon(
                                painter = painterResource(id = R.drawable.addprofileicon),
                                contentDescription = "Add Cover Photo",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(60.dp)
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .clickable{
                                        launcher.launch("image/*")
                                    }
                            )
                        }
                    }



                    Card(
                        shape = RoundedCornerShape(500.dp),
                        modifier = Modifier
                            .size(130.dp)
                            .offset(x = 25.dp, y = 180.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (selectedImageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedImageUri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                        }

                    }
                    Icon(
                        painter = painterResource(id = R.drawable.addprofileicon),
                        contentDescription = "Add Profile Photo",
                        tint = Color.White,
                        modifier = Modifier
                            .size(35.dp)
                            .offset(x = 120.dp, y = 260.dp)
                            .clickable{
                                launcher.launch("image/*")
                            }
                    )


                }





                Spacer(modifier = Modifier.height(70.dp))

                // NAME TEXTFIELD
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.usericon),
                            contentDescription = "Name",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Enter Your Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // PHONE NUMBER TEXTFIELD
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.call),
                            contentDescription = "Phone Number",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Enter Your Phone Number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // GENDER TEXTFIELD
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.gendericon),
                            contentDescription = "Gender",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Select Your Gender") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                            contentDescription = "Dropdown",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // DATE OF BIRTH
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.birthdaydateicon),
                            contentDescription = "Calendar",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Select Your Date of Birth") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.calendaricon),
                            contentDescription = "Calendar Select",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // CURRENT ADDRESS
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.locationicon),
                            contentDescription = "Address",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Your Current Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // PERMANENT ADDRESS
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.locationicon),
                            contentDescription = "Permanent",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Your Permanent Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // EMAIL
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.emailicon),
                            contentDescription = "Mail",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Enter Your Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // BIO
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.bioicon),
                            contentDescription = "Bio",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Bio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JobSeekerPersonalInformationPreview() {
    JobSeekerPersonalInformationBody()
}