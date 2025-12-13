package com.example.rojgar.view

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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.ui.theme.NormalBlue
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White

class SetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SetPasswordScreen()
        }
    }
}

@Composable
fun SetPasswordScreen() {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .background(Color.White)
        ) {
            Box {
                Image(
                    painter = painterResource(R.drawable.design1),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = -80.dp, y = -80.dp)
                        .rotate(10f)
                        .size(260.dp)
                )
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 230.dp, y = -50.dp)
                        .size(300.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.forgetpassworddesign),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(y = 150.dp)
                            .size(250.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(110.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "     Set \nPassword",
                    style = TextStyle(
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF201375)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))


            Row(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
            ) {
                PasswordTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leadingIcon = R.drawable.outline_lock_24
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Confirm Password Field using the new function
            Row(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
            ) {
                PasswordTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    leadingIcon = R.drawable.outline_lock_24
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Done Button
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple
                    ),
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier
                        .height(45.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 73.dp)
                ) {
                    Text(
                        "Done",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    )
                }
            }

            Image(
                painter = painterResource(R.drawable.design2),
                contentDescription = null,
                modifier = Modifier
                    .size(510.dp)
                    .offset(x = 110.dp, y = 115.dp)
            )
        }
    }
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: Int
) {
    var visibility by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (!visibility) PasswordVisualTransformation()
        else VisualTransformation.None,
        leadingIcon = {
            Icon(
                painter = painterResource(id = leadingIcon),
                contentDescription = null,
                tint = NormalBlue,
                modifier = Modifier.size(22.dp)
            )
        },
        trailingIcon = {
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
fun PreviewSetPasswordScreen2() {
    SetPasswordScreen()
}