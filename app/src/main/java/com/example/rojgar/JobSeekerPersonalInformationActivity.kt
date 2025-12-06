package com.example.rojgar

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.Gray
import com.example.rojgar.ui.theme.Purple
import java.util.Calendar


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

//    Dropdown
    var gender by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf("") }

// Calendar
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

//  DatePickerDialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            selectedDate = "$d/${m + 1}/$y"
        },
        year,
        month,
        day
    )

//    CoverProfile
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

//    Profile
    var selectedProfileUri by remember { mutableStateOf<Uri?>(null) }
    val profile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedProfileUri = uri
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

                    IconButton (onClick = {
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

                            // Icon
                            Icon(
                                painter = painterResource(id = R.drawable.addprofileicon),
                                contentDescription = "Add Cover Photo",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(60.dp)
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .clickable {
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
                            if (selectedProfileUri != null) {
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
                            .clickable {
                                profile.launch("image/*")
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
                    value = "",
                    onValueChange = {},
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
                        onValueChange = {},
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
                            .clickable { expanded = true },       // Open Dropdown on click
                        shape = RoundedCornerShape(15.dp),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                                contentDescription = "Dropdown",
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { expanded = true } // Arrow also opens dropdown
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

                    // ------- DROPDOWN MENU -------
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Blue)
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
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // DATE OF BIRTH
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    placeholder = { Text("dd/mm/yyyy") },
                    enabled = false,  // User cannot type manually
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
                    value = "",
                    onValueChange = {},
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
                    value = "",
                    onValueChange = {},
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
                    value = "",
                    onValueChange = {},
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

                // BIO
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
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

                Row {
                    Spacer(modifier = Modifier.width(40.dp))

                    Button(
                        onClick = {
                        },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .width(110.dp)
                            .height(45.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkBlue2,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Save",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                modifier = Modifier.fillMaxWidth()

                            )
                        }

                    }

                    Spacer(modifier = Modifier.width(70.dp))
                    Button (
                        onClick = {
                            activity.finish()
                        },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .width(130.dp)
                            .height(45.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gray,
                            contentColor = Color.Black
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Cancel",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                modifier = Modifier.fillMaxWidth()

                            )

                        }

                    }

                }
                Spacer(modifier = Modifier.height(20.dp))

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun JobSeekerPersonalInformationPreview() {
    JobSeekerPersonalInformationBody()
}