package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rojgar.R
import com.example.rojgar.ui.theme.Black
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.RojgarTheme
import com.example.rojgar.ui.theme.White

class CvViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CvViewBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CvViewBody() {
    Scaffold (
        topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        titleContentColor = Black,
                        actionIconContentColor = Black,
                        containerColor = Blue,
                        navigationIconContentColor = Black
                    ),
                    title = {
                        Text("Curriculum Vitae")
                    },
                    navigationIcon = {
                            IconButton(onClick = {}) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                                    contentDescription = null
                                )
                            }
                    },
                    actions = {}
                )
        }
    ){ padding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ){
            Card (
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(150.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Blue
                )
            ){
                Box(){
                    Row {
                        Image(
                            painter = painterResource(R.drawable.img),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }

}

@Preview
@Composable
fun GreetingPreview3() {
    CvViewBody()
}