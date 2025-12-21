package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.rojgar.model.SkillModel
import com.example.rojgar.repository.SkillRepoImpl
import com.example.rojgar.ui.theme.*
import com.example.rojgar.viewmodel.SkillViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.*

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

    // Initialize ViewModel
    val skillViewModel = remember { SkillViewModel(SkillRepoImpl()) }

    // Get current job seeker ID
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val jobSeekerId = currentUser?.uid ?: ""

    var skills by remember { mutableStateOf(listOf<SkillModel>()) }
    var showSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var currentSkillId by remember { mutableStateOf("") }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedSkill by remember { mutableStateOf<SkillModel?>(null) }

    // Form fields
    var skillName by remember { mutableStateOf("") }
    var skillLevel by remember { mutableFloatStateOf(0f) }

    // Dropdown states
    var showDeleteAlert by remember { mutableStateOf(false) }
    var skillToDelete by remember { mutableStateOf<String?>(null) }

    val skillLevels = listOf("Beginner", "Average", "Efficient", "Advanced", "Expert")
    val skillDescriptions = listOf(
        "At an initial learning phase",
        "Have basic understanding and experience",
        "Confident and can work independently",
        "Highly skilled with extensive experience",
        "Master level with deep expertise"
    )

    // Load skills when activity starts
    LaunchedEffect(Unit) {
        if (jobSeekerId.isNotEmpty()) {
            skillViewModel.getSkillsByJobSeekerId(jobSeekerId) { success, message, skillList ->
                if (success) {
                    skillList?.let {
                        skills = it
                    }
                } else {
                    Toast.makeText(context, "Failed to load skills: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to get skill level index
    fun getSkillLevelIndex(): Int {
        return when {
            skillLevel <= 0.2f -> 0
            skillLevel <= 0.4f -> 1
            skillLevel <= 0.6f -> 2
            skillLevel <= 0.8f -> 3
            else -> 4
        }
    }

    // Function to reset form
    fun resetForm() {
        skillName = ""
        skillLevel = 0f
        currentSkillId = ""
        isEditing = false
    }

    // Function to open form for adding new skill
    fun openAddForm() {
        resetForm()
        showSheet = true
    }

    // Function to open form for editing existing skill
    fun openEditForm(skill: SkillModel) {
        skillName = skill.skill
        // Find the index of the skill level in the list
        val levelIndex = skillLevels.indexOf(skill.level)
        skillLevel = if (levelIndex != -1) levelIndex * 0.25f else 0f
        currentSkillId = skill.skillId
        isEditing = true
        showSheet = true
    }

    // Function to save skill
    fun saveSkill() {
        if (skillName.isEmpty()) {
            Toast.makeText(context, "Please enter skill name", Toast.LENGTH_SHORT).show()
            return
        }

        val levelIndex = getSkillLevelIndex()
        val skillModel = SkillModel(
            skillId = if (isEditing) currentSkillId else "",
            skill = skillName,
            level = skillLevels[levelIndex],
            jobSeekerId = jobSeekerId
        )

        if (isEditing) {
            skillViewModel.updateSkill(currentSkillId, skillModel) { success, message ->
                if (success) {
                    Toast.makeText(context, "Skill updated", Toast.LENGTH_SHORT).show()
                    // Refresh list
                    skillViewModel.getSkillsByJobSeekerId(jobSeekerId) { success2, message2, skillList ->
                        if (success2) {
                            skillList?.let { skills = it }
                        }
                    }
                    showSheet = false
                    resetForm()
                } else {
                    Toast.makeText(context, "Update failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            skillViewModel.addSkill(skillModel) { success, message ->
                if (success) {
                    Toast.makeText(context, "Skill added", Toast.LENGTH_SHORT).show()
                    // Refresh list
                    skillViewModel.getSkillsByJobSeekerId(jobSeekerId) { success2, message2, skillList ->
                        if (success2) {
                            skillList?.let { skills = it }
                        }
                    }
                    showSheet = false
                    resetForm()
                } else {
                    Toast.makeText(context, "Add failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun deleteSkill(skillId: String) {
        // Set the skill to delete and show alert
        skillToDelete = skillId
        showDeleteAlert = true
    }

    // Delete Confirmation Dialog
    if (showDeleteAlert) {
        AlertDialog(
            onDismissRequest = {
                showDeleteAlert = false
                skillToDelete = null
            },
            title = {
                Text(
                    text = "Delete Skill",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this skill? This action cannot be undone.",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Button
                    TextButton(
                        onClick = {
                            showDeleteAlert = false
                            skillToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Delete Button
                    Button(
                        onClick = {
                            skillToDelete?.let { skillId ->
                                skillViewModel.deleteSkill(skillId) { success, message ->
                                    if (success) {
                                        Toast.makeText(context, "Skill deleted", Toast.LENGTH_SHORT).show()
                                        // Refresh list
                                        skillViewModel.getSkillsByJobSeekerId(jobSeekerId) { success2, message2, skillList ->
                                            if (success2) {
                                                skillList?.let { skills = it }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Delete failed: $message", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            showDeleteAlert = false
                            skillToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Delete", color = Color.White)
                    }
                }
            }
        )
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
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "What are your stand-out skills?",
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (skills.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.noexperience),
                        contentDescription = "no skills",
                        tint = Color.Gray,
                        modifier = Modifier.size(110.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Your skills list is currently empty.\nTap the + button to add skill",
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
                            onClick = { openAddForm() },
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
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(skills) { skill ->
                        SkillCard(
                            skill = skill,
                            onEditClick = { openEditForm(skill) },
                            onDeleteClick = { deleteSkill(skill.skillId) }
                        )
                    }
                }

                // Bottom Center Add Another Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { openAddForm() },
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
                        Text(text = "Add ")
                    }
                }
            }
        }
    }

    // Skill Detail Dialog
    if (showDetailDialog && selectedSkill != null) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = {
                Text(
                    text = "Skill Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = DarkBlue2
                )
            },
            text = {
                Column {
                    selectedSkill?.let { skill ->
                        DetailItems(title = "Skill", value = skill.skill)
                        DetailItems(title = "Level", value = skill.level)
                        DetailItems(
                            title = "Description",
                            value = when (skill.level) {
                                "Beginner" -> "At an initial learning phase"
                                "Average" -> "Have basic understanding and experience"
                                "Efficient" -> "Confident and can work independently"
                                "Advanced" -> "Highly skilled with extensive experience"
                                "Expert" -> "Master level with deep expertise"
                                else -> "Proven track record of impressive results"
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            showDetailDialog = false
                            selectedSkill?.let { openEditForm(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue2),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            selectedSkill?.let {
                                skillToDelete = it.skillId
                                showDeleteAlert = true
                                showDetailDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        )
    }

    // Add/Edit Skill Dialog
    if (showSheet) {
        Dialog(
            onDismissRequest = {
                showSheet = false
                resetForm()
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
                        .fillMaxHeight(0.55f),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Dialog Title
                        Text(
                            text = if (isEditing) "Edit Skill" else "Add Skill",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(16.dp))

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
                            label = { Text("Enter Your Skill *") },
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
                                    resetForm()
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
                                onClick = { saveSkill() },
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
                                    text = if (isEditing) "Update" else "Save",
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

@Composable
fun SkillCard(
    skill: SkillModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Skill Name and Edit/Delete Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = skill.skill,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                Row {
                    // Edit Icon
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))

                    // Delete Icon
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Skill Level
            Text(
                text = skill.level,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DarkBlue2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Skill Description
            val description = when (skill.level) {
                "Beginner" -> "At an initial learning phase"
                "Average" -> "Have basic understanding and experience"
                "Efficient" -> "Confident and can work independently"
                "Advanced" -> "Highly skilled with extensive experience"
                "Expert" -> "Master level with deep expertise"
                else -> "Proven track record of impressive results"
            }

            Text(
                text = description,
                color = Color.DarkGray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun DetailItems(title: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview
@Composable
fun JobSeekerSkillPreview() {
    JobSeekerSkillBody()
}