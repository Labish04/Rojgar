package com.example.rojgar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue
import com.example.rojgar.ui.theme.RojgarTheme
import com.example.rojgar.ui.theme.SkyBlue

class JobSeekerProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RojgarTheme {
                JobSeekerProfileBody()
            }
        }
    }
}

@Composable
fun JobSeekerProfileBody() {

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(color = Blue)
        ) {

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                    contentDescription = "Back",
                    modifier = Modifier.size(30.dp)
                )

                Icon(
                    painter = painterResource(R.drawable.outline_more_vert_24),
                    contentDescription = "Menu",
                    modifier = Modifier.size(30.dp)
                )
            }



            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 1.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                // LEFT SIDE TEXT
                Column(
                    modifier = Modifier.weight(1f)
                        .padding(10.dp)
                )
                {
                    Spacer(modifier = Modifier.height(110.dp))
                    Text(
                        text = "Sarah Johnson",
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Normal
                        )

                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "I am a dedicated IT student eager to learn new skills, gain experience, and grow in the field of technology.",
                        style = TextStyle(fontSize = 13.sp)
                    )
                }
                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .height(340.dp)
                        .background(Blue)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profilepicture),
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

            }
                Card (
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = 0.dp, y = -8.dp),
                    colors = CardDefaults.cardColors(
                        contentColor = DarkBlue
                    )
                ){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBlue)


                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {

                    Button (
                        onClick = {},
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .width(150.dp)
                            .height(45.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = SkyBlue,
                            contentColor = Color.Black
                        )
                    ) {
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(
                                painter = painterResource(id = R.drawable.round_info_outline_24),
                                contentDescription = "Details Icon",
                                modifier = Modifier.size(27.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Details",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,

                                    ),
                                modifier = Modifier.fillMaxWidth()

                            )
                        }


                    }

                }
                }
            }
        }
    }
}


@Preview()
@Composable
fun PreviewJobSeekerProfile() {
    RojgarTheme {
        JobSeekerProfileBody()
    }
}
