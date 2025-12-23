package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.ui.theme.Purple

class VerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VerificationBody()
        }
    }
}

@Composable
fun VerificationBody() {
    var otpValue by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
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
                        .offset(x = (-80).dp, y = (-80).dp)
                        .rotate(10f)
                        .size(250.dp)
                )
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 220.dp, y = (-60).dp)
                        .size(300.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.otp),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(y = 150.dp)
                            .size(240.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Verify your code",
                        style = TextStyle(
                            fontSize = 35.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "We have sent a verification code",
                        style = TextStyle(
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                    )
                    Text(
                        "to your mobile number.",
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // OTP Input Boxes
            OtpInputField(
                otpText = otpValue,
                onOtpTextChange = { value, _ ->
                    otpValue = value
                },
                otpCount = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        if (otpValue.length == 4) {

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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "verify", style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Didn't recieve code?",
                    style = TextStyle(
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    "Resend",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Purple
                    ),
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null
                        ) {
                            // Handle resend
                        },
                )
            }
            Image(
                painter = painterResource(R.drawable.design2),
                contentDescription = null,
                modifier = Modifier
                    .size(400.dp)
                    .offset(x = 160.dp, y = 200.dp)
            )
        }
    }
}

@Composable
fun OtpInputField(
    modifier: Modifier = Modifier,
    otpText: String,
    otpCount: Int = 4,
    onOtpTextChange: (String, Boolean) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        modifier = modifier
            .focusRequester(focusRequester),
        value = TextFieldValue(otpText, selection = TextRange(otpText.length)),
        onValueChange = {
            if (it.text.length <= otpCount && it.text.all { char -> char.isDigit() }) {
                onOtpTextChange(it.text, it.text.length == otpCount)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(otpCount) { index ->
                    CharView(
                        index = index,
                        text = otpText
                    )
                    if (index != otpCount - 1) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }
        }
    )
}

@Composable
private fun CharView(
    index: Int,
    text: String
) {
    val isFocused = text.length == index
    val char = when {
        index >= text.length -> ""
        else -> text[index].toString()
    }

    Box(
        modifier = Modifier
            .size(70.dp)
            .border(
                width = 2.dp,
                color = when {
                    isFocused -> Purple
                    char.isNotEmpty() -> Purple.copy(alpha = 0.5f)
                    else -> Color.LightGray
                },
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Preview
@Composable
fun VerificationBodyPreview() {
    VerificationBody()
}