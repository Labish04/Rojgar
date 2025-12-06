package com.example.rojgar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White

class JobSeekerProfessionalLinksActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerProfessionalLinksBody()
        }
    }
}

data class ProfessionalLink(
    val accountName: String,
    val url: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerProfessionalLinksBody() {
    var showBottomSheet by remember { mutableStateOf(false) }
    var accountName by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var linksList by remember { mutableStateOf(listOf<ProfessionalLink>()) }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .height(140.dp)
                    .padding(top = 55.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(5.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBlue2),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Back navigation */ }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(80.dp))
                    Text(
                        "Professional Links",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Blue)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Which project or task highlights your professional strengths the most?",
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (linksList.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    linksList.forEach { link ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = link.accountName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = link.url,
                                    color = DarkBlue2,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(200.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.noexperience),
                        contentDescription = "no links",
                        tint = Color.Gray,
                        modifier = Modifier.size(110.dp)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        "You haven't added any professional links. Tap + to get started.",
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { showBottomSheet = true },
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .width(170.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue2,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.addexperience),
                        contentDescription = "Add",
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Add", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    accountName = ""
                    url = ""
                },
                containerColor = White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 40.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Add Professional Link",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }


                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.document),
                                contentDescription = "Account Name",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("Account Name") },
                        placeholder = { Text("e.g., LinkedIn, GitHub") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(15.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            disabledIndicatorColor = Color.Transparent,
                            disabledContainerColor = White,
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedIndicatorColor = Purple,
                            unfocusedIndicatorColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.linkicon),
                                contentDescription = "URL",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("URL") },
                        placeholder = { Text("https://example.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(15.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            disabledIndicatorColor = Color.Transparent,
                            disabledContainerColor = White,
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedIndicatorColor = Purple,
                            unfocusedIndicatorColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                showBottomSheet = false
                                accountName = ""
                                url = ""
                            },
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.LightGray,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_arrow_back_ios_24),
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (accountName.isNotBlank() && url.isNotBlank()) {
                                    linksList = linksList + ProfessionalLink(accountName, url)
                                    showBottomSheet = false
                                    accountName = ""
                                    url = ""
                                }
                            },
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier
                                .weight(4f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkBlue2,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Save ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun JobSeekerProfessionalLinksPreview() {
    JobSeekerProfessionalLinksBody()
}