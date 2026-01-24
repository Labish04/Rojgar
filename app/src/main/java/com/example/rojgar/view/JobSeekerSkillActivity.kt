package com.example.rojgar.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.viewmodel.SkillViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

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
    val activity = context as Activity

    val skillViewModel = remember { SkillViewModel(SkillRepoImpl()) }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val jobSeekerId = currentUser?.uid ?: ""

    var skills by remember { mutableStateOf(listOf<SkillModel>()) }
    var showSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var currentSkillId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    var skillName by remember { mutableStateOf("") }
    var skillLevel by remember { mutableFloatStateOf(0f) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedSkill by remember { mutableStateOf<SkillModel?>(null) }  // Added this
    var showDeleteAlert by remember { mutableStateOf(false) }
    var skillToDelete by remember { mutableStateOf<String?>(null) }
    var topBarVisible by remember { mutableStateOf(false) }

    val skillLevels = listOf("Beginner", "Average", "Efficient", "Advanced", "Expert")
    val skillDescriptions = listOf(
        "At an initial learning phase",
        "Have basic understanding and experience",
        "Confident and can work independently",
        "Highly skilled with extensive experience",
        "Master level with deep expertise"
    )

    LaunchedEffect(Unit) {
        delay(100)
        topBarVisible = true

        if (jobSeekerId.isNotEmpty()) {
            isLoading = true
            skillViewModel.getSkillsByJobSeekerId(jobSeekerId) { success, message, skillList ->
                isLoading = false
                if (success) {
                    skillList?.let { skills = it }
                } else {
                    Toast.makeText(context, "Failed to load skills: $message", Toast.LENGTH_SHORT).show()
                }
                showContent = true
            }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            showContent = true
        }
    }

    fun getSkillLevelIndex(): Int {
        return when {
            skillLevel <= 0.2f -> 0
            skillLevel <= 0.4f -> 1
            skillLevel <= 0.6f -> 2
            skillLevel <= 0.8f -> 3
            else -> 4
        }
    }

    fun resetForm() {
        skillName = ""
        skillLevel = 0f
        currentSkillId = ""
        isEditing = false
    }

    fun openAddForm() {
        resetForm()
        showSheet = true
    }

    fun openEditForm(skill: SkillModel) {
        skillName = skill.skill
        val levelIndex = skillLevels.indexOf(skill.level)
        skillLevel = if (levelIndex != -1) levelIndex * 0.25f else 0f
        currentSkillId = skill.skillId
        isEditing = true
        showSheet = true
    }

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
                    skillViewModel.getSkillsByJobSeekerId(jobSeekerId) { success2, _, skillList ->
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
                    skillViewModel.getSkillsByJobSeekerId(jobSeekerId) { success2, _, skillList ->
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
        skillToDelete = skillId
        showDeleteAlert = true
    }

    // Delete Alert Dialog
    if (showDeleteAlert) {
        ModernSkillDeleteDialogs(
            onDismiss = {
                showDeleteAlert = false
                skillToDelete = null
            },
            onConfirm = {
                skillToDelete?.let { skillId ->
                    skillViewModel.deleteSkill(skillId) { success, message ->
                        if (success) {
                            Toast.makeText(context, "Skill deleted", Toast.LENGTH_SHORT).show()
                            skillViewModel.getSkillsByJobSeekerId(jobSeekerId) { success2, _, skillList ->
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
            }
        )
    }

    // Skill Detail Dialog
    if (showDetailDialog && selectedSkill != null) {
        ModernSkillDetailDialog(
            skill = selectedSkill!!,
            onDismiss = {
                showDetailDialog = false
                selectedSkill = null
            },
            onEdit = {
                showDetailDialog = false
                openEditForm(selectedSkill!!)
                selectedSkill = null
            },
            onDelete = {
                showDetailDialog = false
                deleteSkill(selectedSkill!!.skillId)
                selectedSkill = null
            }
        )
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = topBarVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Card(
                    modifier = Modifier
                        .height(140.dp)
                        .padding(top = 55.dp)
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(5.dp)),
                    shape = RoundedCornerShape(5.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        var backPressed by remember { mutableStateOf(false) }
                        val backScale by animateFloatAsState(
                            targetValue = if (backPressed) 0.85f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )

                        IconButton(
                            onClick = {
                                backPressed = true
                                activity.finish()
                            },
                            modifier = Modifier.graphicsLayer {
                                scaleX = backScale
                                scaleY = backScale
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        var titleVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(300)
                            titleVisible = true
                        }

                        AnimatedVisibility(
                            visible = titleVisible,
                            enter = fadeIn(animationSpec = tween(500)) +
                                    slideInHorizontally(
                                        initialOffsetX = { it / 2 },
                                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                                    ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Skills",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFFBBDEFB),
                            Color(0xFF90CAF9)
                        )
                    )
                )
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = DarkBlue2,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(600))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        var headerVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(200)
                            headerVisible = true
                        }

                        AnimatedVisibility(
                            visible = headerVisible,
                            enter = slideInVertically(
                                initialOffsetY = { -it / 2 },
                                animationSpec = tween(500, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(500))
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFF2196F3).copy(alpha = 0.12f)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.skillicon),
                                            contentDescription = "Skills",
                                            tint = DarkBlue2,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Stand-out Skills",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF263238)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Power up your skill set",
                                            fontSize = 13.sp,
                                            color = Color(0xFF78909C)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (skills.isEmpty()) {
                            var emptyStateVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(400)
                                emptyStateVisible = true
                            }

                            AnimatedVisibility(
                                visible = emptyStateVisible,
                                enter = fadeIn(animationSpec = tween(600)) +
                                        scaleIn(
                                            initialScale = 0.8f,
                                            animationSpec = tween(600, easing = FastOutSlowInEasing)
                                        )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        modifier = Modifier.size(120.dp),
                                        shape = RoundedCornerShape(60.dp),
                                        color = Color.White.copy(alpha = 0.5f)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.noexperience),
                                                contentDescription = "no skills",
                                                tint = Color(0xFF78909C),
                                                modifier = Modifier.size(70.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        "No Skills Added Yet",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF263238)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        "Add your skills to showcase your expertise",
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF78909C),
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(skills) { skill ->
                                    ModernSkillCard(
                                        skill = skill,
                                        onCardClick = {
                                            selectedSkill = skill
                                            showDetailDialog = true
                                        },
                                        onEditClick = { openEditForm(skill) },
                                        onDeleteClick = { deleteSkill(skill.skillId) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        var buttonVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(600)
                            buttonVisible = true
                        }

                        AnimatedVisibility(
                            visible = buttonVisible,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(500, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(500))
                        ) {
                            ModernAddButton(
                                text = if (skills.isEmpty()) "Add Skill" else "Add Another",
                                onClick = { openAddForm() }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModernSkillDialog(
            isEditing = isEditing,
            skillName = skillName,
            skillLevel = skillLevel,
            skillLevels = skillLevels,
            skillDescriptions = skillDescriptions,
            getSkillLevelIndex = { getSkillLevelIndex() },
            onSkillNameChange = { skillName = it },
            onSkillLevelChange = { skillLevel = it },
            onDismiss = {
                showSheet = false
                resetForm()
            },
            onSave = { saveSkill() }
        )
    }
}

@Composable
fun ModernSkillCard(
    skill: SkillModel,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onCardClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2196F3).copy(alpha = 0.12f)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.skill),
                            contentDescription = "Skill",
                            tint = DarkBlue2,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = skill.skill,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF263238)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkBlue2.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = skill.level,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = DarkBlue2,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row {
                    var editPressed by remember { mutableStateOf(false) }
                    val editScale by animateFloatAsState(
                        targetValue = if (editPressed) 0.85f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    IconButton(
                        onClick = {
                            editPressed = true
                            onEditClick()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = editScale
                                scaleY = editScale
                            }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2196F3).copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    LaunchedEffect(editPressed) {
                        if (editPressed) {
                            delay(150)
                            editPressed = false
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    var deletePressed by remember { mutableStateOf(false) }
                    val deleteScale by animateFloatAsState(
                        targetValue = if (deletePressed) 0.85f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    IconButton(
                        onClick = {
                            deletePressed = true
                            onDeleteClick()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = deleteScale
                                scaleY = deleteScale
                            }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF44336).copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    LaunchedEffect(deletePressed) {
                        if (deletePressed) {
                            delay(150)
                            deletePressed = false
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                color = Color(0xFF78909C),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}


@Composable
fun ModernAddButton(
    text: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 24.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2196F3)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.addexperience),
            contentDescription = "Add",
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

@Composable
fun ModernSkillDialog(
    isEditing: Boolean,
    skillName: String,
    skillLevel: Float,
    skillLevels: List<String>,
    skillDescriptions: List<String>,
    getSkillLevelIndex: () -> Int,
    onSkillNameChange: (String) -> Unit,
    onSkillLevelChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var dialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        dialogVisible = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = dialogVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(400))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(28.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF2196F3).copy(alpha = 0.12f)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.skill),
                                    contentDescription = "Skill",
                                    tint = DarkBlue2,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = if (isEditing) "Edit Skill" else "Add New Skill",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF263238)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        var isFocused by remember { mutableStateOf(false) }
                        val borderColor by animateColorAsState(
                            targetValue = if (isFocused) DarkBlue2 else Color(0xFFE0E0E0),
                            animationSpec = tween(300)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    if (isFocused) 8.dp else 4.dp,
                                    RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            OutlinedTextField(
                                value = skillName,
                                onValueChange = onSkillNameChange,
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.skill),
                                        contentDescription = "Skill",
                                        tint = DarkBlue2,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("Skill Name *") },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = DarkBlue2,
                                    unfocusedIndicatorColor = Color(0xFFE0E0E0),
                                    focusedLabelColor = DarkBlue2
                                )
                            )
                        }

                        Spacer(Modifier.height(32.dp))

                        Text(
                            text = "Skill Level",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF263238)
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Rate your competency level",
                            fontSize = 14.sp,
                            color = Color(0xFF78909C)
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            skillLevels.forEach { level ->
                                Text(
                                    text = level,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78909C),
                                    modifier = Modifier.weight(1f),
                                    textAlign = when (level) {
                                        "Beginner" -> TextAlign.Start
                                        "Expert" -> TextAlign.End
                                        else -> TextAlign.Center
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Slider(
                            value = skillLevel,
                            onValueChange = onSkillLevelChange,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF2196F3),
                                activeTrackColor = Color(0xFF2196F3),
                                inactiveTrackColor = Color(0xFFE0E0E0)
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2196F3).copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = skillDescriptions[getSkillLevelIndex()],
                                fontSize = 14.sp,
                                color = Color(0xFF263238),
                                modifier = Modifier.padding(16.dp),
                                lineHeight = 20.sp
                            )
                        }

                        Spacer(Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            var cancelPressed by remember { mutableStateOf(false) }
                            val cancelScale by animateFloatAsState(
                                targetValue = if (cancelPressed) 0.95f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )

                            OutlinedButton(
                                onClick = {
                                    cancelPressed = true
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(0.4f)
                                    .height(56.dp)
                                    .graphicsLayer {
                                        scaleX = cancelScale
                                        scaleY = cancelScale
                                    },
                                border = BorderStroke(2.dp, Color(0xFFE0E0E0)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF78909C)
                                )
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LaunchedEffect(cancelPressed) {
                                if (cancelPressed) {
                                    delay(150)
                                    cancelPressed = false
                                }
                            }

                            var savePressed by remember { mutableStateOf(false) }
                            val saveScale by animateFloatAsState(
                                targetValue = if (savePressed) 0.95f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )

                            Button(
                                onClick = {
                                    savePressed = true
                                    onSave()
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(0.6f)
                                    .height(56.dp)
                                    .graphicsLayer {
                                        scaleX = saveScale
                                        scaleY = saveScale
                                    },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3)
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                )
                            ) {
                                Text(
                                    text = if (isEditing) "Update" else "Save",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            LaunchedEffect(savePressed) {
                                if (savePressed) {
                                    delay(150)
                                    savePressed = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernSkillDeleteDialogs(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var dialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        dialogVisible = true
    }

    AnimatedVisibility(
        visible = dialogVisible,
        enter = fadeIn(animationSpec = tween(300)) +
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFFF44336).copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Delete Skill?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF263238),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "This action cannot be undone. Are you sure you want to delete this skill?",
                    fontSize = 15.sp,
                    color = Color(0xFF78909C),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var deletePressed by remember { mutableStateOf(false) }
                    val deleteScale by animateFloatAsState(
                        targetValue = if (deletePressed) 0.95f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    Button(
                        onClick = {
                            deletePressed = true
                            onConfirm()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .graphicsLayer {
                                scaleX = deleteScale
                                scaleY = deleteScale
                            },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Text(
                            "Delete",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    LaunchedEffect(deletePressed) {
                        if (deletePressed) {
                            delay(150)
                            deletePressed = false
                        }
                    }

                    var cancelPressed by remember { mutableStateOf(false) }
                    val cancelScale by animateFloatAsState(
                        targetValue = if (cancelPressed) 0.95f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    OutlinedButton(
                        onClick = {
                            cancelPressed = true
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .graphicsLayer {
                                scaleX = cancelScale
                                scaleY = cancelScale
                            },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, Color(0xFFE0E0E0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF78909C)
                        )
                    ) {
                        Text(
                            "Cancel",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    LaunchedEffect(cancelPressed) {
                        if (cancelPressed) {
                            delay(150)
                            cancelPressed = false
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ModernSkillDetailDialog(
    skill: SkillModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var dialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        dialogVisible = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = dialogVisible,
            enter = fadeIn(animationSpec = tween(300)) +
                    scaleIn(
                        initialScale = 0.9f,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    // Header with Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = RoundedCornerShape(32.dp),
                            color = Color(0xFF2196F3).copy(alpha = 0.12f)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.skillicon),
                                contentDescription = "Skill",
                                tint = DarkBlue2,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Skill Details",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Skill Name
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Skill Name",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78909C),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = skill.skill,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF263238)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Skill Level
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Proficiency Level",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78909C),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkBlue2.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = skill.level,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBlue2,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Description",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78909C),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
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
                                fontSize = 15.sp,
                                color = Color(0xFF263238),
                                lineHeight = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        var editPressed by remember { mutableStateOf(false) }
                        val editScale by animateFloatAsState(
                            targetValue = if (editPressed) 0.95f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )

                        Button(
                            onClick = {
                                editPressed = true
                                onEdit()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .graphicsLayer {
                                    scaleX = editScale
                                    scaleY = editScale
                                },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LaunchedEffect(editPressed) {
                            if (editPressed) {
                                delay(150)
                                editPressed = false
                            }
                        }

                        var deletePressed by remember { mutableStateOf(false) }
                        val deleteScale by animateFloatAsState(
                            targetValue = if (deletePressed) 0.95f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )

                        Button(
                            onClick = {
                                deletePressed = true
                                onDelete()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .graphicsLayer {
                                    scaleX = deleteScale
                                    scaleY = deleteScale
                                },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF44336)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LaunchedEffect(deletePressed) {
                            if (deletePressed) {
                                delay(150)
                                deletePressed = false
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    var closePressed by remember { mutableStateOf(false) }
                    val closeScale by animateFloatAsState(
                        targetValue = if (closePressed) 0.95f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    OutlinedButton(
                        onClick = {
                            closePressed = true
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .graphicsLayer {
                                scaleX = closeScale
                                scaleY = closeScale
                            },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, Color(0xFFE0E0E0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF78909C)
                        )
                    ) {
                        Text(
                            text = "Close",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LaunchedEffect(closePressed) {
                        if (closePressed) {
                            delay(150)
                            closePressed = false
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