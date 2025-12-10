package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R

class SignUpCompanyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SignUpCompanyBody()
        }
    }
}

@Composable
fun SignUpCompanyBody() {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
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
            // Logo Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Image(
                    painter = painterResource(id = R.drawable.design3),
                    contentDescription = "Jobseeker Illustration",
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                        .offset(x = 60.dp, y = (-10).dp)
                )
            }

            Image(
                painter = painterResource(id = R.drawable.design4),
                contentDescription = "Jobseeker Illustration",
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // SIGN UP TITLE
            Text(
                text = "SignUp",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF350089)
            )

            Spacer(modifier = Modifier.height(25.dp))

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

            // PHONE NUMBER FIELD
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

            Spacer(modifier = Modifier.height(25.dp))
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


            Spacer(modifier = Modifier.height(20.dp))

            // SIGNUP BUTTON
            Button(
                onClick = {
                    println("FULL NAME: $fullName")
                    println("PHONE: $phone")
                    println("EMAIL: $email")
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(45.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8E53FF)
                )
            ) {
                Text(
                    text = "SIGNUP",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.design5),
                    contentDescription = "Jobseeker Illustration",
                    modifier = Modifier
                        .size(300.dp)
                        .offset(x = (-80).dp, y = 10.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewSignUpCompany() {
    SignUpCompanyBody()
}
