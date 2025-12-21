package com.example.rojgar.view

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rojgar.R
import com.example.rojgar.ui.theme.*

data class Skill(
    val name: String,
    val level: String,
    val levelDescription: String
)

class JobSeekerSkillActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerSkillBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerSkillBody() {
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }
    var skillList by remember { mutableStateOf(listOf<Skill>()) }
    var skillName by remember { mutableStateOf("") }
    var skillLevel by remember { mutableFloatStateOf(0f) }

    val skillLevels = listOf("Beginner", "Average", "Efficient", "Advanced", "Expert")
    val skillDescriptions = listOf(
        "At an initial learning phase",
        "Have basic understanding and experience",
        "Confident and can work independently",
        "Highly skilled with extensive experience",
        "Master level with deep expertise"
    )

    fun getSkillLevelIndex(): Int {
        return when {
            skillLevel <= 0.2f -> 0
            skillLevel <= 0.4f -> 1
            skillLevel <= 0.6f -> 2
            skillLevel <= 0.8f -> 3
            else -> 4
        }
    }

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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
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

                    Spacer(modifier = Modifier.width(130.dp))

                    Text(
                        "Skill",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.weight(1f))
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
                "What are your stand - out skills?",
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(200.dp))

            if (skillList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(520.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.noexperience),
                        contentDescription = "no experience",
                        tint = Color.Gray,
                        modifier = Modifier.size(110.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Your skills list is currently empty.Tap the + button \n to add skill",
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { showSheet = true },
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
                            Text(
                                text = "Add",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    skillList.forEach { skill ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = skill.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = skill.level, color = Color.Gray)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = skill.levelDescription,
                                    color = Color.DarkGray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(300.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { showSheet = true },
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier
                                .width(250.dp)
                                .height(45.dp),
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Add Another")
                        }
                    }
                }
            }
        }

        if (showSheet) {
            Dialog(
                onDismissRequest = {
                    showSheet = false
                    skillName = ""
                    skillLevel = 0f
                },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.45f),
                        colors = CardDefaults.cardColors(
                            containerColor = White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Skill Name TextField
                            OutlinedTextField(
                                value = skillName,
                                onValueChange = { skillName = it },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.skillicon),
                                        contentDescription = "Skill",
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("Enter Your Skill") },
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
                            Spacer(Modifier.height(24.dp))

                            // Skill Level Section
                            Text(
                                text = "Skill Level",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Rate your skill that defines your competency",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )

                            Spacer(Modifier.height(16.dp))

                            // Skill level labels
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                skillLevels.forEach { level ->
                                    Text(
                                        text = level,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray,
                                        modifier = Modifier.weight(1f),
                                        textAlign = if (level == "Beginner") TextAlign.Start
                                        else if (level == "Expert") TextAlign.End
                                        else TextAlign.Center
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // Slider
                            Slider(
                                value = skillLevel,
                                onValueChange = { skillLevel = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = DarkBlue2,
                                    activeTrackColor = DarkBlue2,
                                    inactiveTrackColor = Color.LightGray
                                )
                            )

                            Spacer(Modifier.height(8.dp))

                            // Current skill level description
                            Text(
                                text = skillDescriptions[getSkillLevelIndex()],
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )

                            Spacer(Modifier.height(32.dp))

                            // Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Back Button
                                OutlinedButton(
                                    onClick = {
                                        showSheet = false
                                        skillName = ""
                                        skillLevel = 0f
                                    },
                                    shape = RoundedCornerShape(15.dp),
                                    modifier = Modifier
                                        .weight(0.5f)
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

                                Spacer(Modifier.width(15.dp))

                                // Save Button
                                Button(
                                    onClick = {
                                        if (skillName.isNotBlank()) {
                                            val newSkill = Skill(
                                                name = skillName,
                                                level = skillLevels[getSkillLevelIndex()],
                                                levelDescription = skillDescriptions[getSkillLevelIndex()]
                                            )
                                            skillList = skillList + newSkill
                                            showSheet = false
                                            skillName = ""
                                            skillLevel = 0f
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(0.7f)
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkBlue2,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "Save",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun JobSeekerSkillPreview() {
    JobSeekerSkillBody()
}