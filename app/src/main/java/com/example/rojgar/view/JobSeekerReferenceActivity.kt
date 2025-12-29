package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rojgar.R
import com.example.rojgar.model.ReferenceModel
import com.example.rojgar.repository.ReferenceRepoImpl
import com.example.rojgar.ui.theme.*
import com.example.rojgar.viewmodel.ReferenceViewModel
import com.google.firebase.auth.FirebaseAuth

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
    val referenceViewModel = remember { ReferenceViewModel(ReferenceRepoImpl()) }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val jobSeekerId = currentUser?.uid ?: ""

    var references by remember { mutableStateOf(listOf<ReferenceModel>()) }
    var showReferenceSheet by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var selectedReference by remember { mutableStateOf<ReferenceModel?>(null) }

    var refereeName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var contactType by remember { mutableStateOf("Mobile") }
    var currentReferenceId by remember { mutableStateOf("") }
    var showDeleteAlert by remember { mutableStateOf(false) }
    var referenceToDelete by remember { mutableStateOf<String?>(null) }

    val contactTypes = listOf("Mobile", "Work", "Home")

    // Load references
    LaunchedEffect(Unit) {
        if (jobSeekerId.isNotEmpty()) {
            referenceViewModel.getReferencesByJobSeekerId(jobSeekerId) { success, message, referenceList ->
                if (success) {
                    referenceList?.let {
                        references = it
                    }
                } else {
                    Toast.makeText(context, "Failed to load references: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to reset form
    fun resetForm() {
        refereeName = ""
        jobTitle = ""
        companyName = ""
        email = ""
        contactNumber = ""
        contactType = "Mobile"
        currentReferenceId = ""
        isEditing = false
    }

    // Function to add new reference
    fun openAddForm() {
        resetForm()
        showReferenceSheet = true
    }

    // Function to edit existing reference
    fun openEditForm(reference: ReferenceModel) {
        refereeName = reference.name
        jobTitle = reference.jobTitle
        companyName = reference.companyName
        email = reference.email
        contactNumber = reference.contactNumber
        contactType = reference.contactType
        currentReferenceId = reference.referenceId
        isEditing = true
        showReferenceSheet = true
    }

    // Function to save reference
    fun saveReference() {
        if (refereeName.isEmpty() || jobTitle.isEmpty() || companyName.isEmpty() ||
            email.isEmpty() || contactNumber.isEmpty()) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val referenceModel = ReferenceModel(
            referenceId = if (isEditing) currentReferenceId else "",
            name = refereeName,
            jobTitle = jobTitle,
            companyName = companyName,
            email = email,
            contactType = contactType,
            contactNumber = contactNumber,
            jobSeekerId = jobSeekerId
        )

        if (isEditing) {
            referenceViewModel.updateReference(currentReferenceId, referenceModel) { success, message ->
                if (success) {
                    Toast.makeText(context, "Reference updated", Toast.LENGTH_SHORT).show()
                    // Refresh list
                    referenceViewModel.getReferencesByJobSeekerId(jobSeekerId) { success2, message2, referenceList ->
                        if (success2) {
                            referenceList?.let { references = it }
                        }
                    }
                    showReferenceSheet = false
                    resetForm()
                } else {
                    Toast.makeText(context, "Update failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            referenceViewModel.addReference(referenceModel) { success, message ->
                if (success) {
                    Toast.makeText(context, "Reference added", Toast.LENGTH_SHORT).show()
                    // Refresh list
                    referenceViewModel.getReferencesByJobSeekerId(jobSeekerId) { success2, message2, referenceList ->
                        if (success2) {
                            referenceList?.let { references = it }
                        }
                    }
                    showReferenceSheet = false
                    resetForm()
                } else {
                    Toast.makeText(context, "Add failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun deleteReference(referenceId: String) {
        referenceToDelete = referenceId
        showDeleteAlert = true
    }

    // Delete Dialog
    if (showDeleteAlert) {
        AlertDialog(
            onDismissRequest = {
                showDeleteAlert = false
                referenceToDelete = null
            },
            title = {
                Text(
                    text = "Delete Reference",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this reference?",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Button
                    TextButton(
                        onClick = {
                            showDeleteAlert = false
                            referenceToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Delete Button
                    Button(
                        onClick = {
                            referenceToDelete?.let { referenceId ->
                                referenceViewModel.deleteReference(referenceId) { success, message ->
                                    if (success) {
                                        Toast.makeText(context, "Reference deleted", Toast.LENGTH_SHORT).show()
                                        referenceViewModel.getReferencesByJobSeekerId(jobSeekerId) { success2, message2, referenceList ->
                                            if (success2) {
                                                referenceList?.let { references = it }
                                            }
                                        }
                                        showDetailDialog = false
                                    } else {
                                        Toast.makeText(context, "Delete failed: $message", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            showDeleteAlert = false
                            referenceToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Delete", color = Color.White)
                    }
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
                colors = CardDefaults.cardColors(containerColor = DarkBlue2),
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
                    Spacer(modifier = Modifier.width(110.dp))
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
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Add your professional references here.",
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (references.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.noexperience),
                        contentDescription = "no references",
                        tint = Color.Gray,
                        modifier = Modifier.size(110.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "You haven't added any references. Tap the + button to add references.",
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { openAddForm() },
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
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(references) { reference ->
                        ReferenceCard(
                            reference = reference,
                            onClick = {
                                selectedReference = reference
                                showDetailDialog = true
                            },
                            onEditClick = { openEditForm(reference) },
                            onDeleteClick = { deleteReference(reference.referenceId) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { openAddForm() },
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
        }
    }

    // Reference Detail Dialog
    if (showDetailDialog && selectedReference != null) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = {
                Text(
                    text = "Reference Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = DarkBlue2
                )
            },
            text = {
                Column {
                    selectedReference?.let { reference ->
                        DetailItemes(title = "Referrer Name", value = reference.name)
                        DetailItemes(title = "Job Title", value = reference.jobTitle)
                        DetailItemes(title = "Company", value = reference.companyName)
                        DetailItemes(title = "Email", value = reference.email)
                        DetailItemes(title = "Contact Type", value = reference.contactType)
                        DetailItemes(title = "Contact Number", value = reference.contactNumber)
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            showDetailDialog = false
                            selectedReference?.let { openEditForm(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue2),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            selectedReference?.let {
                                referenceToDelete = it.referenceId
                                showDeleteAlert = true
                                showDetailDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        )
    }

    // Reference Form Dialog
    if (showReferenceSheet) {
        Dialog(
            onDismissRequest = {
                showReferenceSheet = false
                resetForm()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .clickable {
                        showReferenceSheet = false
                        resetForm()
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.65f),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (isEditing) "Edit Reference" else "Add Reference",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(16.dp))

                        // Referee's Name
                        OutlinedTextField(
                            value = refereeName,
                            onValueChange = { refereeName = it },
                            label = { Text("Referrer's Name *") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.companynameicon),
                                    contentDescription = "Name",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Job Title
                        OutlinedTextField(
                            value = jobTitle,
                            onValueChange = { jobTitle = it },
                            label = { Text("Job Title *") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.jobtitleicon),
                                    contentDescription = "Job",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Company Name
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Company Name *") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.companynameicon),
                                    contentDescription = "Company",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email *") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.emailicon),
                                    contentDescription = "Email",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,

                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Contact Type and Number Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Contact Type Dropdown
                            var expandedContact by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                            ) {
                                OutlinedTextField(
                                    value = contactType,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Type") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedContact = true },
                                    shape = RoundedCornerShape(15.dp),
                                    singleLine = true,
                                    trailingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.outline_keyboard_arrow_down_24),
                                            contentDescription = "Dropdown",
                                            modifier = Modifier.clickable { expandedContact = true }
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        disabledIndicatorColor = Color.LightGray,
                                        disabledContainerColor = White,
                                        disabledTextColor = Color.Black,
                                        focusedContainerColor = White,
                                        unfocusedContainerColor = White,
                                        focusedIndicatorColor = DarkBlue2,
                                        unfocusedIndicatorColor = Color.LightGray
                                    )
                                )

                                DropdownMenu(
                                    expanded = expandedContact,
                                    onDismissRequest = { expandedContact = false },
                                    modifier = Modifier
                                        .background(White)
                                        .width(100.dp)
                                ) {
                                    contactTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    type,
                                                    color = if (type == contactType) DarkBlue2 else Color.Black
                                                )
                                            },
                                            onClick = {
                                                contactType = type
                                                expandedContact = false
                                            },
                                            modifier = Modifier.background(
                                                if (type == contactType) Color(0xFFE3F2FD) else White
                                            )
                                        )
                                    }
                                }
                            }

                            // Contact Number
                            OutlinedTextField(
                                value = contactNumber,
                                onValueChange = { contactNumber = it },
                                label = { Text("Contact Number *") },
                                modifier = Modifier
                                    .weight(2f)
                                    .height(60.dp),
                                shape = RoundedCornerShape(15.dp),
                                singleLine = true,

                                colors = TextFieldDefaults.colors(
                                    disabledIndicatorColor = Color.LightGray,
                                    disabledContainerColor = White,
                                    disabledTextColor = Color.Black,
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
                                    focusedIndicatorColor = DarkBlue2,
                                    unfocusedIndicatorColor = Color.LightGray
                                )
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Back Button
                            OutlinedButton(
                                onClick = {
                                    showReferenceSheet = false
                                    resetForm()
                                },
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier
                                    .weight(0.4f)
                                    .height(50.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = DarkBlue2
                                )
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_arrow_back_ios_24),
                                    contentDescription = "Back",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Save Button
                            Button(
                                onClick = { saveReference() },
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier
                                    .weight(0.6f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkBlue2,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = if (isEditing) "Update" else "Save",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReferenceCard(
    reference: ReferenceModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reference.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = reference.companyName,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = reference.email,
                        fontSize = 14.sp,
                        color = DarkBlue2
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItemes(title: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview
@Composable
fun JobSeekerReferencePreview() {
    JobSeekerReferenceBody()
}