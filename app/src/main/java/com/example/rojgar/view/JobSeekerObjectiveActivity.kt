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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.repository.ObjectiveRepoImpl
import com.example.rojgar.viewmodel.ObjectiveViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.Gray

class JobSeekerObjectiveActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerObjectiveBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerObjectiveBody() {
    val context = LocalContext.current
    val activity = context as Activity

    val objectiveViewModel = remember { ObjectiveViewModel(ObjectiveRepoImpl()) }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val jobSeekerId = currentUser?.uid ?: ""

    var objectiveText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var hasExistingObjective by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    var topBarVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        topBarVisible = true

        if (jobSeekerId.isNotEmpty()) {
            isLoading = true
            objectiveViewModel.getObjectiveTextByJobSeekerId(jobSeekerId) { success, message, objective ->
                isLoading = false
                if (success) {
                    objective?.let {
                        objectiveText = it
                        hasExistingObjective = it.isNotEmpty()
                    }
                }
                showContent = true
            }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            showContent = true
        }
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
                                    text = "Objective",
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
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
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
                                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
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
                                            painter = painterResource(R.drawable.objectiveicon),
                                            contentDescription = "Objective",
                                            tint = DarkBlue2,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Career Objective",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF263238)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Describe yourself professionally",
                                            fontSize = 13.sp,
                                            color = Color(0xFF78909C)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        var textFieldVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(400)
                            textFieldVisible = true
                        }

                        AnimatedVisibility(
                            visible = textFieldVisible,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(500, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(500))
                        ) {
                            ModernObjectiveTextField(
                                value = objectiveText,
                                placeholder = "Enter your career objective and goals...\n\nExample: Results-driven professional with expertise in project management seeking opportunities to leverage skills and contribute to organizational success.",
                                onValueChange = { objectiveText = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Character Count
                        var countVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(objectiveText) {
                            countVisible = objectiveText.isNotEmpty()
                        }

                        AnimatedVisibility(
                            visible = countVisible,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = DarkBlue2.copy(alpha = 0.1f),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = "${objectiveText.length} characters",
                                        fontSize = 12.sp,
                                        color = DarkBlue2,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        var buttonsVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(600)
                            buttonsVisible = true
                        }

                        AnimatedVisibility(
                            visible = buttonsVisible,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(500, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(500))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Save/Update Button
                                if (objectiveText.trim().isNotEmpty()) {
                                    ModernActionButton(
                                        text = if (hasExistingObjective) "Update" else "Save",
                                        color = Color(0xFF2196F3),
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            if (jobSeekerId.isEmpty()) {
                                                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                                                return@ModernActionButton
                                            }

                                            objectiveViewModel.saveOrUpdateObjective(
                                                jobSeekerId = jobSeekerId,
                                                objective = objectiveText.trim()
                                            ) { success, message ->
                                                if (success) {
                                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                    hasExistingObjective = true
                                                    activity.finish()
                                                } else {
                                                    Toast.makeText(context, "Failed: $message", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                }

                                // Delete Button
                                if (hasExistingObjective && objectiveText.isNotEmpty()) {
                                    ModernActionButton(
                                        text = "Delete",
                                        color = Color(0xFFF44336),
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            objectiveViewModel.deleteObjective(jobSeekerId) { success, message ->
                                                if (success) {
                                                    Toast.makeText(context, "Objective deleted", Toast.LENGTH_SHORT).show()
                                                    objectiveText = ""
                                                    hasExistingObjective = false
                                                } else {
                                                    Toast.makeText(context, "Failed: $message", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                }

                                // Clear Button
                                if (objectiveText.isNotEmpty() && !hasExistingObjective) {
                                    ModernActionButton(
                                        text = "Clear",
                                        color = Gray,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            objectiveText = ""
                                            Toast.makeText(context, "Cleared", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ModernObjectiveTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) DarkBlue2 else Color(0xFFE0E0E0),
        animationSpec = tween(300)
    )

    val elevation by animateDpAsState(
        targetValue = if (isFocused) 12.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .shadow(elevation, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = Color(0xFF263238),
                    lineHeight = 22.sp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .onFocusChanged { isFocused = it.isFocused }
                    .padding(20.dp),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = Color(0xFFBDBDBD),
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
fun ModernActionButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
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
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
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

@Preview
@Composable
fun JobSeekerObjectivePreview() {
    JobSeekerObjectiveBody()
}