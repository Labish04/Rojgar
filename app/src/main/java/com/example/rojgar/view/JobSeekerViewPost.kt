package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class JobSeekerViewPost: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerViewPostBody()
        }
    }
}

@Composable
fun JobSeekerViewPostBody() {


}
@Preview
@Composable
fun JobSeekerViewPostPreview() {
    CompanyUploadPostBody()

}