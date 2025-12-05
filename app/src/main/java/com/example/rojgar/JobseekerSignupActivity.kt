package com.example.rojgar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.NormalBlue

import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White

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

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Logo
            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ){
                Image(
                    painter = painterResource(id = R.drawable.design3),
                    contentDescription = null,
                    modifier = Modifier
                        .size(500.dp)
                        .offset(x = 120.dp, y = -210.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(250.dp)
                        .offset(x = 200.dp, y = -90.dp)
                )
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Column(
                        modifier = Modifier
                            .padding(top = 120.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.design4),
                            contentDescription = "Jobseeker Illustration",
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )

                        Text(
                            text = "SignUp",
                            style = TextStyle(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                    }
                }
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
            Spacer(modifier = Modifier.height(25.dp))

            // Phone Number Label + Field
            Row (
                modifier = Modifier
                    .padding(horizontal = 30.dp)
            ){
                LoginTextField(
                    value = phoneNumber,
                    onValueChange = { fullName = it },
                    label = "Phone Number",
                    leadingIcon = R.drawable.phoneicon,
                    isPassword = false
                )
            }


            Spacer(modifier = Modifier.height(25.dp))


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

            Spacer(modifier = Modifier.height(30.dp))

            // SIGNUP Button
            Button(
                onClick = { },
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
                painter = painterResource(id = R.drawable.design5),
                contentDescription = "Jobseeker Illustration",
                modifier = Modifier
                    .size(250.dp)
                    .offset(x =
                        (-120).dp, y = 60.dp)

            )
        }
    }
}

@Preview
@Composable
fun PreviewSignupScreen(){
    JobSeekerSignUpScreen()
}




