package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White

data class Language(
    val name: String,
    val reading: Int,
    val speaking: Int,
    val writing: Int,
    val listening: Int
) {
    fun getAverageRating(): Float {
        return (reading + speaking + writing + listening) / 4f
    }
}

class JobSeekerLanguageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerLanguageBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerLanguageBody() {
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    val languagesList = remember { mutableStateListOf<Language>() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    IconButton(onClick = {
                        val intent = Intent(context, JobSeekerProfileDetailsActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(100.dp))
                    Text(
                        "Languages",
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
                "Which languages do you know?",
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (languagesList.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    languagesList.forEach { language ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = language.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Average Rating: ${String.format("%.1f", language.getAverageRating())}/5",
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
                        contentDescription = "no languages",
                        tint = Color.Gray,
                        modifier = Modifier.size(110.dp)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        "You haven't added any languages. Tap + to get started.",
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

        // Bottom Sheet
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                LanguageBottomSheetContent(
                    onDismiss = { showBottomSheet = false },
                    onSave = { language ->
                        languagesList.add(language)
                        showBottomSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageBottomSheetContent(
    onDismiss: () -> Unit,
    onSave: (Language) -> Unit
) {
    var languageName by remember { mutableStateOf("") }
    var readingRating by remember { mutableStateOf(0) }
    var speakingRating by remember { mutableStateOf(0) }
    var writingRating by remember { mutableStateOf(0) }
    var listeningRating by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Language TextField
        OutlinedTextField(
            value = languageName,
            onValueChange = { languageName = it },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.languageicon),
                    contentDescription = "Language",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Language") },
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

        Spacer(modifier = Modifier.height(24.dp))

        // Reading
        Text(
            text = "Reading",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        StarRatingRow(rating = readingRating, onRatingChange = { readingRating = it })

        Spacer(modifier = Modifier.height(20.dp))

        // Speaking
        Text(
            text = "Speaking",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        StarRatingRow(rating = speakingRating, onRatingChange = { speakingRating = it })

        Spacer(modifier = Modifier.height(20.dp))

        // Writing
        Text(
            text = "Writing",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        StarRatingRow(rating = writingRating, onRatingChange = { writingRating = it })

        Spacer(modifier = Modifier.height(20.dp))

        // Listening
        Text(
            text = "Listening",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        StarRatingRow(rating = listeningRating, onRatingChange = { listeningRating = it })

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Back Button
            Button(
                onClick = onDismiss,
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

            // Save Changes Button
            Button(
                onClick = {
                    if (languageName.isNotBlank()) {
                        onSave(
                            Language(
                                name = languageName,
                                reading = readingRating,
                                speaking = speakingRating,
                                writing = writingRating,
                                listening = listeningRating
                            )
                        )
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
                    text = "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun StarRatingRow(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            Icon(
                painter = if (i <= rating) {
                    painterResource(id = android.R.drawable.btn_star_big_on)
                } else {
                    painterResource(id = android.R.drawable.btn_star_big_off)
                },
                contentDescription = "Star $i",
                tint = if (i <= rating) Color(0xFF4CAF50) else Color.Gray,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChange(i) }
            )
        }
    }
}

@Preview
@Composable
fun JobSeekerLanguagePreview() {
    JobSeekerLanguageBody()
}