package com.example.rojgar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.rojgar.ui.theme.RojgarTheme

class JobSeekerJobPreferenceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerJobPreferenceBody()
        }
    }
}

@Composable
fun JobSeekerJobPreferenceBody(){
}

@Preview
@Composable
fun JobSeekerJobPreferencePreview() {
    JobSeekerJobPreferenceBody()
}