package com.example.rojgar

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.rojgar.ui.theme.RojgarTheme

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
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ){

        }
    }
}

@Preview
@Composable
fun JobSeekerProfileDetailsBodyPreview() {
    JobSeekerProfileDetailsBody()
}