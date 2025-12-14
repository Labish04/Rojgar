package com.example.rojgar.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.repository.JobSeekerRepoImpl

import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.viewmodel.JobSeekerViewModel

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerSignUpScreen()
        }
    }
}

@Composable
fun JobSeekerSignUpScreen() {

    val context = LocalContext.current
    val activity = context as Activity

    val userViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }


    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Logo
            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.design1),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = -80.dp, y = -100.dp)
                        .rotate(10f)
                        .size(250.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(250.dp)
                        .offset(x = 250.dp, y = -40.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.design4),
                    contentDescription = "Jobseeker Illustration",
                    modifier = Modifier
                        .size(200.dp)
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .offset(y = 40.dp)
                )

                Text(
                    text = "SignUp",
                    style = TextStyle(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y=140.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row (
                modifier = Modifier
                    .padding(horizontal = 30.dp)
            ){
                LoginTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name",
                    leadingIcon = R.drawable.user,
                    isPassword = false
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Phone Number Label + Field
            Row (
                modifier = Modifier
                    .padding(horizontal = 30.dp)
            ){
                LoginTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Phone Number",
                    leadingIcon = R.drawable.phoneiconoutlined,
                    isPassword = false
                )
            }


            Spacer(modifier = Modifier.height(20.dp))


            Row (
                    modifier = Modifier
                        .padding(horizontal = 30.dp)
                    ){
                LoginTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    leadingIcon = R.drawable.email,
                    isPassword = false
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row (
                modifier = Modifier
                    .padding(horizontal = 30.dp)
            ){
                LoginTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leadingIcon = R.drawable.outline_lock_24,
                    isPassword = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row (
                modifier = Modifier
                    .padding(horizontal = 30.dp)
            ){
                LoginTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    leadingIcon = R.drawable.outline_lock_24,
                    isPassword = true
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // SIGNUP Button
            Button(
                onClick = { userViewModel.register(email, password) { success, message, jobSeekerId ->
                    if (success) {
                        var model = JobSeekerModel(
                            jobSeekerId = jobSeekerId,
                            fullName = fullName,
                            phoneNumber = phoneNumber,
                            email = email
                        )
                        userViewModel.addJobSeekerToDatabase(jobSeekerId, model) { success, message ->
                            if (success) {
                                activity.finish()
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            } else {

                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                    }
                }},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 30.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("SignUp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            Image(
                painter = painterResource(R.drawable.design2),
                contentDescription = null,
                modifier = Modifier
                    .size(500.dp)
                    .offset(x = 160.dp, y = 40.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewSignupScreen(){
    JobSeekerSignUpScreen()
}



