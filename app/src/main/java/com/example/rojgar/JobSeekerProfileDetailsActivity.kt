package com.example.rojgar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.RojgarTheme
import com.example.rojgar.ui.theme.White

class JobSeekerProfileDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerProfileDetailsBody()
        }
    }
}

@Composable
fun JobSeekerProfileDetailsBody() {

    val context = LocalContext.current
    val activity = context as Activity

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Blue
                )
        ) {
            Card(
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.height(200.dp),
                colors = CardDefaults.cardColors(
                    contentColor = DarkBlue2
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBlue2)
                ) {

                    // Top Bar Icons
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
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )

                        Icon(
                            painter = painterResource(R.drawable.outline_more_vert_24),
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }



                    // Profile Image + Text
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Image(
                            painter = painterResource(R.drawable.picture),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )



                        Text(
                            "“I am a dedicated IT student eager to learn new skills, gain experience, and grow in the field of technology.”",
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(start = 15.dp, top = 10.dp)
                            .fillMaxWidth()
                    ){
                        Text("Sarah Johnson",
                            style = TextStyle(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White          // ← Add this line

                            ))
                    }
                }

            }

            Spacer(modifier = Modifier.height(20.dp))
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =Arrangement.Center
            ){

                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(60.dp)
                        .width(400.dp)
                        .clickable(interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null    ){
                            val intent = Intent(context, JobSeekerPersonalInformationActivity ::class.java)
                            context.startActivity(intent)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = White
                    )

                )
                {


                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start

                    ) {
                        Icon(
                            painter = painterResource(R.drawable.usericon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(start =10.dp)
                        )
                        Text("Personal Information",
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            modifier = Modifier

                                .padding(start = 10.dp)

                        )
                        Icon(
                            painter = painterResource(R.drawable.outline_keyboard_arrow_right_24),
                            contentDescription = null,
                            modifier = Modifier
                                .size(150.dp)
                                .padding(start =80.dp)
                        )
                    }


                }

            }
            Spacer(modifier = Modifier.height(20.dp))


        }
    }
}

@Preview
@Composable
fun JobSeekerProfileDetailsBodyPreview() {
    JobSeekerProfileDetailsBody()
}