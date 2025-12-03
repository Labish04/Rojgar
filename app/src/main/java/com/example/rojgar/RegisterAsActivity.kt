package com.example.rojgar

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.ui.theme.Purple


class RegisterAsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegisterBody()
        }
    }
}
@Composable
fun RegisterBody() {

    val context = LocalContext.current
    val activity = context as Activity

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .background(Color.White)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

                Box {
                    Image(
                        painter = painterResource(id = R.drawable.design3),
                        contentDescription = null,
                        modifier = Modifier
                            .size(500.dp)
                            .offset(x = 120.dp, y = -230.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(250.dp)
                            .offset(x = 200.dp, y = -40.dp)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.design4),
                        contentDescription = "Jobseeker Illustration",
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .offset(y = 200.dp)
                    )

                    Row (
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ){
                        Text(
                            text = "SignUp",
                            style = TextStyle(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            modifier = Modifier
                                .offset(y = 410.dp)
                        )
                    }
                }

                Text(
                    text = "Which type of account do you want to sign up?",
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

            Spacer(modifier = Modifier.height(30.dp))

            // Buttons
            Button(
                onClick = { /* Navigate to Job Seeker Registration */ },
                modifier = Modifier
                    .height(50.dp)
                    .width(300.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("JOB SEEKER", color = Color.White, style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                ))
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { /* Navigate to Company Registration */ },
                modifier = Modifier
                    .height(50.dp)
                    .width(300.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("COMPANY", color = Color.White, style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,))
            }

            Spacer(modifier = Modifier.height(30.dp))
            Text(
                buildAnnotatedString {
                    append("Already have account?")
                    withStyle(
                        style = SpanStyle(
                            color = Purple,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        
                    }
                },
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier
                    .clickable { /* Navigate to Login */ }
            )
            Spacer(modifier = Modifier.height(15.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("LOGIN", color = Color.White, style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,))
            }
            Column (
                modifier = Modifier
                    .fillMaxSize()
            ){
                Image(
                    painter = painterResource(id = R.drawable.design5),
                    contentDescription = "Jobseeker Illustration",
                    modifier = Modifier
                        .size(600.dp)
                        .offset(x = 120.dp, y = 80.dp)
                )
            }
        }
    }
}


@Preview
@Composable
fun PreviewRegister(){
    RegisterBody()
}
