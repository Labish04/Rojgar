package com.example.rojgar.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.repository.ObjectiveRepoImpl
import com.example.rojgar.viewmodel.ObjectiveViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.LaunchedEffect
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.Gray

class JobSeekerObjectiveActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerObjectiveBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerObjectiveBody() {
    val context = LocalContext.current
    val activity = context as Activity

    val objectiveViewModel = remember { ObjectiveViewModel(ObjectiveRepoImpl()) }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val jobSeekerId = currentUser?.uid ?: ""

    var objectiveText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var hasExistingObjective by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (jobSeekerId.isNotEmpty()) {
            isLoading = true
            objectiveViewModel.getObjectiveTextByJobSeekerId(jobSeekerId) { success, message, objective ->
                isLoading = false
                if (success) {
                    objective?.let {
                        objectiveText = it
                        hasExistingObjective = it.isNotEmpty()
                    }
                } else {
                    Toast.makeText(context, "Failed to load objective: $message", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
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
                        activity.finish()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(90.dp))

                    Text(
                        "Objective",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "How can you describe yourself professionally?",
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            CustomObjectiveTextField(
                value = objectiveText,
                placeholder = "Enter your career objective and goals...",
                onValueChange = { objectiveText = it }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ){
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Button(
                    onClick = {
                        if (jobSeekerId.isEmpty()) {
                            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (objectiveText.trim().isEmpty()) {
                            Toast.makeText(context, "Please enter your objective", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        objectiveViewModel.saveOrUpdateObjective(
                            jobSeekerId = jobSeekerId,
                            objective = objectiveText.trim()
                        ) { success, message ->
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                hasExistingObjective = true
                                // Go back to previous activity
                                activity.finish()
                            } else {
                                Toast.makeText(context, "Failed to save: $message", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.width(150.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue2),
                    enabled = !isLoading && objectiveText.trim().isNotEmpty()
                ) {
                    Text(
                        text = if (hasExistingObjective && objectiveText.isNotEmpty()) "Update" else "Save",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            if (hasExistingObjective && !isLoading && objectiveText.isNotEmpty()) {
                Button(
                    onClick = {
                        objectiveViewModel.deleteObjective(jobSeekerId) { success, message ->
                            if (success) {
                                Toast.makeText(context, "Objective deleted", Toast.LENGTH_SHORT).show()
                                objectiveText = ""
                                hasExistingObjective = false
                            } else {
                                Toast.makeText(context, "Failed to delete: $message", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.width(150.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            if (objectiveText.isNotEmpty() && !hasExistingObjective && !isLoading) {
                Button(
                    onClick = {
                        objectiveText = ""
                        Toast.makeText(context, "Objective cleared", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.width(150.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gray),
                ) {
                    Text(
                        text = "Clear",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }
            }

            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CustomObjectiveTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) DarkBlue2 else Color(0xFFCCCCCC)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { isFocused = it.isFocused },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Preview
@Composable
fun JobSeekerObjectivePreview() {
    JobSeekerObjectiveBody()
}