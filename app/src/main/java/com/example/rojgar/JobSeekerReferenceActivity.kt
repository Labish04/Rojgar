package com.example.rojgar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.RojgarTheme
import com.example.rojgar.ui.theme.White
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType

data class ReferenceData(
    val refereeName: String,
    val jobTitle: String,
    val companyName: String,
    val email: String,
    val contactType: String,
    val contactNumber: String
)

class JobSeekerReferenceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerReferenceBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerReferenceBody() {

    val context = LocalContext.current

    var showBottomSheet by remember { mutableStateOf(false) }
    val referencesList = remember { mutableStateListOf<ReferenceData>() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .height(140.dp)
                    .padding(top = 55.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(5.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBlue2),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                    Spacer(modifier = Modifier.width(80.dp))
                    Text(
                        "Reference",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Blue)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Write your reference here.",
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (referencesList.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    referencesList.forEach { reference ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = reference.refereeName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${reference.jobTitle} at ${reference.companyName}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = reference.email,
                                    color = DarkBlue2,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${reference.contactType}: ${reference.contactNumber}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(200.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.noexperience),
                        contentDescription = "no references",
                        tint = Color.Gray,
                        modifier = Modifier.size(110.dp)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        "You haven't added any references. Tap + to get started.",
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { showBottomSheet = true },
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .width(170.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue2,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.addexperience),
                        contentDescription = "Add",
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Add", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Bottom Sheet
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = White
            ) {
                ReferenceBottomSheetContent(
                    onDismiss = { showBottomSheet = false },
                    onSave = { reference ->
                        referencesList.add(reference)
                        scope.launch {
                            sheetState.hide()
                            showBottomSheet = false
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceBottomSheetContent(
    onDismiss: () -> Unit,
    onSave: (ReferenceData) -> Unit
) {
    var refereeName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedContactType by remember { mutableStateOf("Mobile") }
    val contactTypes = listOf("Mobile", "Work", "Home")



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Add Reference",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }


        // Referee's Name
        OutlinedTextField(
            value = refereeName,
            onValueChange = { refereeName = it },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.companynameicon),
                    contentDescription = "Name",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Referrer's Name") },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(15.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                disabledIndicatorColor = Color.Transparent,
                disabledContainerColor = White,
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedIndicatorColor = Purple,
                unfocusedIndicatorColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Job Title
        OutlinedTextField(
            value = jobTitle,
            onValueChange = { jobTitle = it },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.jobtitleicon),
                    contentDescription = "Job",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Job Title") },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(15.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                disabledIndicatorColor = Color.Transparent,
                disabledContainerColor = White,
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedIndicatorColor = Purple,
                unfocusedIndicatorColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Organization Name
        OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.companynameicon),
                    contentDescription = "Organization",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Company Name") },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(15.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                disabledIndicatorColor = Color.Transparent,
                disabledContainerColor = White,
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedIndicatorColor = Purple,
                unfocusedIndicatorColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.emailicon),
                    contentDescription = "Email",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(15.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                disabledIndicatorColor = Color.Transparent,
                disabledContainerColor = White,
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedIndicatorColor = Purple,
                unfocusedIndicatorColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Contact Type and Number Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Contact Type Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedContactType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = TextFieldDefaults.colors(
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = White,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = Purple,
                        unfocusedIndicatorColor = Color.Black
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    contactTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedContactType = type
                                expanded = false
                            },


                        )
                    }
                }
            }


            OutlinedTextField(
                value = contactNumber,
                onValueChange = { contactNumber = it },
                label = { Text("Contact Number") },
                modifier = Modifier
                    .weight(2f)
                    .height(60.dp),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = White,
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = Purple,
                    unfocusedIndicatorColor = Color.Black
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Back Button
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_arrow_back_ios_24),
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Save Changes Button
            Button(
                onClick = {
                    val reference = ReferenceData(
                        refereeName = refereeName,
                        jobTitle = jobTitle,
                        companyName = companyName,
                        email = email,
                        contactType = selectedContactType,
                        contactNumber = contactNumber
                    )
                    onSave(reference)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue2,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview
@Composable
fun JobSeekerReferencePreview() {
    JobSeekerReferenceBody()
}