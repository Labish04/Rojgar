package com.example.rojgar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class DocumentCompanyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DocumentCompanyBody()
        }
    }
}

@Composable
fun DocumentCompanyBody() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ---------- LOGO TOP-RIGHT ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Image(
                painter = painterResource(id = R.drawable.design3),
                contentDescription = "Logo",
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .offset(x = 60.dp, y = (-10).dp)
            )
        }

        // ---------- ILLUSTRATION IMAGE ----------
        Image(
            painter = painterResource(id = R.drawable.design4),
            contentDescription = "Illustration",
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ---------- TITLE ----------
        Text(
            text = "Submit Your Company Registration Document",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5C4CCF),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ---------- BIG UPLOAD BOX ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
        ) {

            // ---- ADD IMAGE BUTTON (BOTTOM-RIGHT) ----
            Button(
                onClick = { println("Add image") },
                modifier = Modifier
                    .size(55.dp)
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.add_image_icon),
                    contentDescription = "Add Image",
                    modifier = Modifier.size(35.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        // ---------- SUBMIT BUTTON ----------
        Button(
            onClick = { println("Submit Pressed") },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(45.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8E53FF)
            )
        ) {
            Text(
                text = "SUBMIT",
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
                    .size(800.dp)
                    .offset(x = (-140).dp, y = 10.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDocumentCompany() {
    DocumentCompanyBody()
}
