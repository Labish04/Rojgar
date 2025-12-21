package com.example.rojgar.view

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.R
import com.example.rojgar.repository.CompanyRepoImpl

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
    val context = LocalContext.current
    val repository = remember { CompanyRepoImpl() }

    // Get current company ID
    val currentCompanyId = repository.getCurrentCompany()?.uid ?: ""

    // State variables
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(context, "Image Selected!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.design1),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (-80).dp, y = (-100).dp)
                        .rotate(10f)
                        .size(250.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(250.dp)
                        .offset(x = 250.dp, y = (-40).dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.design4),
                    contentDescription = "Illustration",
                    modifier = Modifier
                        .size(200.dp)
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .offset(y = 40.dp)
                )
            }

            // ---------- TITLE ----------
            Text(
                text = "Submit Your Company Registration Document",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5C4CCF),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ---------- BIG UPLOAD BOX ----------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(300.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Display selected image or placeholder
                if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = "Selected Document",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "No document selected",
                        color = Color.Gray
                    )
                }

                // ---- ADD IMAGE BUTTON (BOTTOM-RIGHT) ----
                Button(
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    modifier = Modifier
                        .size(55.dp)
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
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
                onClick = {
                    if (selectedImageUri != null) {
                        if (currentCompanyId.isNotEmpty()) {
                            isUploading = true
                            repository.uploadRegistrationDocument(
                                companyId = currentCompanyId,
                                imageUri = selectedImageUri!!
                            ) { success, message ->
                                isUploading = false
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                                if (success) {
                                    // Clear the selected image after successful upload
                                    selectedImageUri = null
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Please login first",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Please select a document first",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(45.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8E53FF)
                ),
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "SUBMIT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Image(
                    painter = painterResource(R.drawable.design2),
                    contentDescription = null,
                    modifier = Modifier
                        .size(500.dp)
                        .offset(x = 160.dp, y = 60.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDocumentCompany() {
    DocumentCompanyBody()
}