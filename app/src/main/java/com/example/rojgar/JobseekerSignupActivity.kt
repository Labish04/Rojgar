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

    var email by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(horizontal = 20.dp),
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
                            .padding(top = 100.dp),
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
                                color = Black
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = "",
                onValueChange = {},
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.identity),
                        contentDescription = "Name",
                        tint = NormalBlue,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Full Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(15.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(25.dp))

            // Phone Number Label + Field
            OutlinedTextField(
                value = email,
                onValueChange = { data ->
                    email = data
                },
                leadingIcon = {
                    Image(
                        painter = painterResource(R.drawable.phoneicon),
                        contentDescription = null,

                        modifier = Modifier
                            .size(20.dp)
                            .background(NormalBlue),
                    )
                },

                label = {
                    Text("Phone Number")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(15.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = NormalBlue,
                    unfocusedIndicatorColor = NormalBlue
                )
            )


            Spacer(modifier = Modifier.height(15.dp))


            OutlinedTextField(
                value = email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                onValueChange = { data ->
                    email = data
                },
                leadingIcon = {
                    Image(
                        painter = painterResource(R.drawable.outline_email_24),
                        contentDescription = null,
                        modifier = Modifier
                    )
                },

                label = {
                    Text("Email")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(15.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = NormalBlue,
                    unfocusedIndicatorColor = NormalBlue
                )
            )
            Spacer(modifier = Modifier.height(30.dp))

            // SIGNUP Button
            Button(
                onClick = { },
                modifier = Modifier
                    .width(100.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("SIGNUP", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Image(
                painter = painterResource(id = R.drawable.design5),
                contentDescription = "Jobseeker Illustration",
                modifier = Modifier
                    .size(250.dp)
                    .offset(x =
                        (-119).dp, y = 2.dp)

            )
        }
    }
}

@Preview
@Composable
fun PreviewSignupScreen(){
    JobSeekerSignUpScreen()
}




