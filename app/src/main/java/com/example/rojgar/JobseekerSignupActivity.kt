package com.example.rojgar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.ui.theme.Black

import com.example.rojgar.ui.theme.Purple

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.End
            ) { Image(
                painter = painterResource(id = R.drawable.rojgar),
                contentDescription = "Jobseeker Illustration",
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .offset(x = 70.dp,y = (-10).dp)
            )
            }

            // Illustration
            Image(
                painter = painterResource(id = R.drawable.illustration),
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
            Spacer(modifier = Modifier.height(20.dp))


            // Full Name Label + Field
            // FULL NAME FIELD
            OutlinedTextField(
                value = "",
                onValueChange = {},
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.identity),
                        contentDescription = "Name",
                        tint = Color.Gray,
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
                value = "",
                onValueChange = {},
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.phoneicon),
                        contentDescription = "Phone",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Phone Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(15.dp),
                singleLine = true
            )


            Spacer(modifier = Modifier.height(15.dp))

            // Email Address Label + Field
            // EMAIL FIELD
            OutlinedTextField(
                value = "",
                onValueChange = {},
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.mailicon),
                        contentDescription = "Mail",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(15.dp),
                singleLine = true
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
                painter = painterResource(id = R.drawable.drop),
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




