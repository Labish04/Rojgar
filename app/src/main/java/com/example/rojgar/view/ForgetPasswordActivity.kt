package com.example.rojgar.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ForgetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForgetPasswordBody()
        }
    }
}

@Composable
fun ForgetPasswordBody() {
    val context = LocalContext.current
    val activity = context as Activity
    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .background(color = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box {
                    Image(
                        painter = painterResource(R.drawable.design1),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(x = (-80).dp, y = (-80).dp)
                            .rotate(10f)
                            .size(260.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(x = 230.dp, y = (-50).dp)
                            .size(300.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.forgetpassworddesign),
                            contentDescription = null,
                            modifier = Modifier
                                .offset(y = 180.dp)
                                .size(250.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(120.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "   Forget \nPassword",
                        style = TextStyle(
                            fontSize = 35.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF201375)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Enter your email Address",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
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
                        placeholder = { Text("e.g.aba@gmail.com") },
                        modifier = Modifier.height(60.dp),
                        shape = RoundedCornerShape(15.dp),
                        singleLine = true,
                        enabled = !isLoading,
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

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            if (email.isEmpty()) {
                                Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true

                            // Check user type
                            findUserTypeByEmail(
                                email = email,
                                onUserTypeFound = { userType ->
                                    when (userType) {
                                        "JOBSEEKER" -> {
                                            jobSeekerViewModel.forgetPassword(email) { success, message ->
                                                isLoading = false
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                if (success) {
                                                    activity.finish()
                                                }
                                            }
                                        }
                                        "COMPANY" -> {
                                            companyViewModel.forgetPassword(email) { success, message ->
                                                isLoading = false
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                if (success) {
                                                    activity.finish()
                                                }
                                            }
                                        }
                                        else -> {
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                "Email not found. Please check and try again.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                onError = { errorMessage ->
                                    isLoading = false
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple
                        ),
                        shape = RoundedCornerShape(15.dp),
                        enabled = !isLoading,
                        modifier = Modifier
                            .height(45.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 73.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Send",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            )
                        }
                    }
                }

                Image(
                    painter = painterResource(R.drawable.design2),
                    contentDescription = null,
                    modifier = Modifier
                        .size(500.dp)
                        .offset(x = 100.dp, y = 140.dp)
                )
            }
        }
    }
}

// Function to find user type
fun findUserTypeByEmail(
    email: String,
    onUserTypeFound: (String) -> Unit,
    onError: (String) -> Unit
) {
    val database = FirebaseDatabase.getInstance()
    var foundInJobSeekers = false
    var foundInCompany = false
    var jobSeekersChecked = false
    var companyChecked = false

    // Check JobSeekers database
    database.getReference("JobSeekers")
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                jobSeekersChecked = true

                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        try {
                            val jobSeeker = child.getValue(JobSeekerModel::class.java)
                            // JobSeekerModel has 'email' property
                            if (jobSeeker?.email?.equals(email, ignoreCase = true) == true) {
                                foundInJobSeekers = true
                                onUserTypeFound("JOBSEEKER")
                                return
                            }
                        } catch (e: Exception) {

                        }
                    }
                }

                // If both checked and not found
                if (companyChecked && !foundInJobSeekers && !foundInCompany) {
                    onUserTypeFound("NOT_FOUND")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError("Error accessing database: ${error.message}")
            }
        })

    // Check Company database
    database.getReference("Companys")
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                companyChecked = true

                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        try {
                            val company = child.getValue(CompanyModel::class.java)
                            if (company?.companyEmail?.equals(email, ignoreCase = true) == true) {
                                foundInCompany = true
                                onUserTypeFound("COMPANY")
                                return
                            }
                        } catch (e: Exception) {
                        }
                    }
                }

                // If both checked and not found
                if (jobSeekersChecked && !foundInJobSeekers && !foundInCompany) {
                    onUserTypeFound("NOT_FOUND")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError("Error accessing database: ${error.message}")
            }
        }
    )
}

@Preview
@Composable
fun ForgetPasswordPreview() {
    ForgetPasswordBody()
}