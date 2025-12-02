package com.example.rojgar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.RojgarTheme

class JobSeekerObjectiveActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerObjectiveBody()
        }
    }
}

@Composable
fun JobSeekerObjectiveBody() {

    val context = LocalContext.current
    val activity = context as Activity

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Blue)
        ) {
            // Top Bar
            Card(
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkBlue2
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                        ,
                    )
                    Text(
                        "Objective",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.size(30.dp))
                }
            }


        }
    }
}




@Preview
@Composable
fun JobSeekerObjectivePreview() {
    JobSeekerObjectiveBody()

}