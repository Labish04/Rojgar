package com.example.rojgar.view

import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.Gray
import com.example.rojgar.ui.theme.NormalBlue
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White

@Composable
fun CompanyHomeScreenBody(){

    val context = LocalContext.current

    var search by remember { mutableStateOf("") }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Blue)
    ){

        Spacer(modifier = Modifier.height(20.dp))

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        context.startActivity(
                            Intent(context, JobSeekerSearchActivity::class.java)
                        )
                    }
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false, // Important
                    placeholder = {
                        Text(
                            "Search",
                            fontSize = 20.sp,
                            color = Color.Gray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.searchicon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Gray
                        )
                    },
                    shape = RoundedCornerShape(15.dp),
                    colors = TextFieldDefaults.colors(
                        disabledContainerColor = White,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ){
            Card (
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ){
                Text("Reviews & Ratings", style = TextStyle(
                    fontSize = 18.sp,
                    color = Color.DarkGray
                ),
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Card (
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(200.dp)
                    .width(200.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ){
                Text("Calendar", style = TextStyle(
                    fontSize = 18.sp,
                    color = Color.DarkGray
                ),
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp)
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                "Posted Jobs", style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            )
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp),
                horizontalArrangement = Arrangement.End
            ){
                Text(
                    "Show All", style = TextStyle(
                        fontSize = 18.sp
                    )
                )
            }
        }

        Card (
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(395.dp)
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ){

        }
    }
}