package com.example.rojgar.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.NormalBlue
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginBody()

        }
    }
}

@Composable
fun LoginBody() {

    val context = LocalContext.current
    val activity = context as Activity

    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }


    var email by remember { mutableStateOf("") }
    var password by remember {mutableStateOf("")}

    var rememberMe by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .background(color = Color.White)
        ) {
            Box {
                Image(
                    painter = painterResource(R.drawable.design1),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = -80.dp, y = -80.dp)
                        .rotate(10f)
                        .size(250.dp)
                )
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 220.dp, y = -60.dp)
                        .size(300.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.mandesign1),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(y = 120.dp)
                            .size(200.dp)
                    )

                }
            }
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Column {
                    Text("Welcome to Rojgar",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text("Find your dream job or hire top talent.",
                        style = TextStyle(
                            fontSize = 12.sp
                        ),
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                    )
                }
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

            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(5.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            ){
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                                modifier = Modifier
                                .offset(y = -5.dp)
                    )

                    Text(
                        text = "Remember me.",
                        style = TextStyle(
                            fontSize = 18.sp
                        ),
                        modifier = Modifier
                            .offset(y = -5.dp)

                    )
                }
                Spacer(modifier = Modifier.width(45.dp))
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End

                ) {
                    Text(
                        "Forget Password?", style = TextStyle(
                            fontSize = 15.sp,
                            color = Purple
                        ),
                        modifier = Modifier
                            .clickable(interactionSource = remember {
                                MutableInteractionSource()
                            },
                                indication = null    ){
                                val intent = Intent(context, ForgetPasswordActivity::class.java)
                                context.startActivity(intent)
                            },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                Button(
                    onClick = {
                        if (email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(context, "Email and password required", Toast.LENGTH_SHORT).show()
                        } else {
                            findUserTypeByEmail(
                                email = email,
                                onUserTypeFound = { userType ->
                                    when (userType) {
                                        "JOBSEEKER" -> {
                                            jobSeekerViewModel.login(email, password) { success, message ->
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                if (success) {
                                                    Toast.makeText(context, "Login Successful as JobSeeker", Toast.LENGTH_SHORT).show()
                                                    val intent = Intent(context, JobSeekerDashboardActivity::class.java)
                                                    context.startActivity(intent)
                                                    activity.finish()
                                                }
                                            }
                                        }
                                        "COMPANY" -> {
                                            companyViewModel.login(email, password) { success, message ->
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                if (success) {
                                                    Toast.makeText(context, "Login Successful as Company", Toast.LENGTH_SHORT).show()
                                                    val intent = Intent(context, CompanyDashboardActivity::class.java)
                                                    context.startActivity(intent)
                                                    activity.finish()
                                                }
                                            }
                                        }
                                        else -> {
                                            Toast.makeText(
                                                context,
                                                "Email not found. Please check and try again.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                onError = { errorMessage ->
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                ) {
                    Text("Login", style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text("Don't hanve an account?",
                    style = TextStyle(
                        fontSize = 18.sp
                    ))
                Spacer(modifier = Modifier.width(5.dp))
                Text("SignUp",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Purple
                    ),
                    modifier = Modifier
                        .clickable(interactionSource = remember {
                            MutableInteractionSource()
                        },
                            indication = null    ){
                            val intent = Intent(context, RegisterAsActivity ::class.java)
                            context.startActivity(intent)
                        },
                )
            }

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                HorizontalDivider(
                    modifier = Modifier.weight(1f)
                )
                Text("OR", modifier = Modifier.padding(horizontal = 15.dp))
                HorizontalDivider(
                    modifier = Modifier.weight(1f)
                )
            }
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                ) {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ){
                        Image(
                            painter = painterResource(R.drawable.google),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(horizontal = 10.dp)
                        )
                        Text(
                            "Login with Google", style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column (
                modifier = Modifier
                    .fillMaxSize(),
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("You can only login with google as a JobSeeker.")
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
}

@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: Int,
    isPassword: Boolean = false
) {
    var visibility by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,

        label = { Text(label) },

        visualTransformation =
            if (isPassword && !visibility) PasswordVisualTransformation()
            else VisualTransformation.None,

        leadingIcon = {
            Icon(
                painter = painterResource(id = leadingIcon),
                contentDescription = null,
                tint = NormalBlue,
                modifier = Modifier.
                size(22.dp)
            )
        },

        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { visibility = !visibility }) {
                    Icon(
                        painter = painterResource(
                            id = if (visibility)
                                R.drawable.baseline_visibility_off_24
                            else R.drawable.baseline_visibility_24
                        ),
                        contentDescription = null,
                        tint = NormalBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(15.dp),

        colors = TextFieldDefaults.colors(
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            focusedIndicatorColor = NormalBlue,
            unfocusedIndicatorColor = NormalBlue
        )
    )
}




@Preview
@Composable
fun GreetingPreview2() {
    LoginBody()
}