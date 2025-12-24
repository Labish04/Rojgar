package com.example.rojgar.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.viewmodel.CompanyViewModel

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
    val context = LocalContext.current
    val activity = context as Activity

    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }

    var companyName by remember { mutableStateOf("") }
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
                    contentDescription = "Illustration",
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
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = "Company Name",
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
                    label = "Contact Number",
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
                onClick = { companyViewModel.register(email, password) { success, message, companyId ->
                    if (success) {
                        var model = CompanyModel(
                            companyId = companyId,
                            companyName = companyName,
                            companyContactNumber = phoneNumber,
                            companyEmail = email
                        )
                        companyViewModel.addCompanyToDatabase(companyId, model) { success, message ->
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
fun PreviewSignUpCompany() {
    SignUpCompanyBody()
}
