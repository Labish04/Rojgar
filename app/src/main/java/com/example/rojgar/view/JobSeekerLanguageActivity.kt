package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rojgar.R
import com.example.rojgar.model.LanguageModel
import com.example.rojgar.repository.LanguageRepoImpl
import com.example.rojgar.ui.theme.*
import com.example.rojgar.viewmodel.LanguageViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class JobSeekerLanguageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerLanguageBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun JobSeekerLanguageBody() {
    val context = LocalContext.current
    val languageViewModel = remember { LanguageViewModel(LanguageRepoImpl()) }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val jobSeekerId = currentUser?.uid ?: ""

    var languages by remember { mutableStateOf(listOf<LanguageModel>()) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf<LanguageModel?>(null) }
    var showDeleteAlert by remember { mutableStateOf(false) }
    var languageToDelete by remember { mutableStateOf<String?>(null) }

    var topBarVisible by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Form fields with star ratings (0-5)
    var languageName by remember { mutableStateOf("") }
    var readingRating by remember { mutableStateOf(0) }
    var speakingRating by remember { mutableStateOf(0) }
    var writingRating by remember { mutableStateOf(0) }
    var listeningRating by remember { mutableStateOf(0) }

    // Load languages with animation delay
    LaunchedEffect(Unit) {
        delay(100)
        topBarVisible = true
        if (jobSeekerId.isNotEmpty()) {
            languageViewModel.getLanguagesByJobSeekerId(jobSeekerId) { success, message, languageList ->
                if (success) {
                    languageList?.let {
                        languages = it
                    }
                } else {
                    Toast.makeText(context, "Failed to load languages: $message", Toast.LENGTH_SHORT).show()
                }
            }
            delay(500)
        }
        isLoading = false
        showContent = true
    }

    // Function to reset form
    fun resetForm() {
        languageName = ""
        readingRating = 0
        speakingRating = 0
        writingRating = 0
        listeningRating = 0
        selectedLanguage = null
        isEditing = false
    }

    // Function to calculate average rating
    fun calculateAverageRating(): Float {
        val total = readingRating + speakingRating + writingRating + listeningRating
        return if (total > 0) total / 4f else 0f
    }

    // Function to open add form
    fun openAddForm() {
        resetForm()
        showBottomSheet = true
    }

    // Function to open edit form
    fun openEditForm(language: LanguageModel) {
        languageName = language.language
        readingRating = language.readingLevel.toIntOrNull() ?: 0
        speakingRating = language.speakingLevel.toIntOrNull() ?: 0
        writingRating = language.writingLevel.toIntOrNull() ?: 0
        listeningRating = language.listeningLevel.toIntOrNull() ?: 0
        selectedLanguage = language
        isEditing = true
        showBottomSheet = true
    }

    // Function to save language
    fun saveLanguage() {
        if (languageName.isEmpty()) {
            Toast.makeText(context, "Please enter language name", Toast.LENGTH_SHORT).show()
            return
        }

        if (readingRating == 0 || speakingRating == 0 || writingRating == 0 || listeningRating == 0) {
            Toast.makeText(context, "Please rate all skills", Toast.LENGTH_SHORT).show()
            return
        }

        val languageModel = LanguageModel(
            languageId = if (isEditing) selectedLanguage?.languageId ?: "" else "",
            language = languageName,
            readingLevel = readingRating.toString(),
            speakingLevel = speakingRating.toString(),
            writingLevel = writingRating.toString(),
            listeningLevel = listeningRating.toString(),
            jobSeekerId = jobSeekerId
        )

        if (isEditing) {
            selectedLanguage?.languageId?.let { languageId ->
                languageViewModel.updateLanguage(languageId, languageModel) { success, message ->
                    if (success) {
                        Toast.makeText(context, "Language updated", Toast.LENGTH_SHORT).show()
                        languageViewModel.getLanguagesByJobSeekerId(jobSeekerId) { success2, message2, languageList ->
                            if (success2) {
                                languageList?.let { languages = it }
                            }
                        }
                        showBottomSheet = false
                        resetForm()
                    } else {
                        Toast.makeText(context, "Update failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            languageViewModel.addLanguage(languageModel) { success, message ->
                if (success) {
                    Toast.makeText(context, "Language added", Toast.LENGTH_SHORT).show()
                    languageViewModel.getLanguagesByJobSeekerId(jobSeekerId) { success2, message2, languageList ->
                        if (success2) {
                            languageList?.let { languages = it }
                        }
                    }
                    showBottomSheet = false
                    resetForm()
                } else {
                    Toast.makeText(context, "Add failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to delete language
    fun deleteLanguage(languageId: String) {
        languageToDelete = languageId
        showDeleteAlert = true
    }

    // Loading indicator
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF2196F3),
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
        }
    }

    // Delete Dialog
    if (showDeleteAlert) {
        ModernLanguageDeleteDialog(
            onDismiss = {
                showDeleteAlert = false
                languageToDelete = null
            },
            onConfirm = {
                languageToDelete?.let { langId ->
                    languageViewModel.deleteLanguage(langId) { success, message ->
                        if (success) {
                            Toast.makeText(context, "Language deleted", Toast.LENGTH_SHORT).show()
                            languageViewModel.getLanguagesByJobSeekerId(jobSeekerId) { success2, message2, languageList ->
                                if (success2) {
                                    languageList?.let { languages = it }
                                }
                            }
                            showDetailDialog = false
                        } else {
                            Toast.makeText(context, "Delete failed: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showDeleteAlert = false
                languageToDelete = null
            },
            languageName = languages.find { it.languageId == languageToDelete }?.language ?: ""
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
                                (context as? ComponentActivity)?.finish()
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
                                    text = "Languages",
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
                                        painter = painterResource(R.drawable.languageicon),
                                        contentDescription = "Language",
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Language Skills",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF263238)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Which languages do you know?",
                                        fontSize = 13.sp,
                                        color = Color(0xFF78909C)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (languages.isEmpty()) {
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
                                            contentDescription = "no languages",
                                            tint = Color(0xFF78909C),
                                            modifier = Modifier.size(70.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    "No Languages Added Yet",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF263238)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Add your language skills to showcase your abilities",
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
                            items(languages) { language ->
                                ModernLanguageCard(
                                    language = language,
                                    onClick = {
                                        selectedLanguage = language
                                        showDetailDialog = true
                                    },
                                    onEditClick = { openEditForm(language) },
                                    onDeleteClick = { deleteLanguage(language.languageId) }
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
                        ModernAddButtons(
                            text = if (languages.isEmpty()) "Add Language" else "Add Another",
                            onClick = { openAddForm() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Language Detail Dialog
    if (showDetailDialog && selectedLanguage != null) {
        ModernLanguageDetailDialog(
            language = selectedLanguage!!,
            onDismiss = {
                showDetailDialog = false
                selectedLanguage = null
            },
            onEdit = {
                showDetailDialog = false
                openEditForm(selectedLanguage!!)
                selectedLanguage = null
            },
            onDelete = {
                showDetailDialog = false
                deleteLanguage(selectedLanguage!!.languageId)
                selectedLanguage = null
            }
        )
    }

    // Bottom Sheet for adding/editing language
    if (showBottomSheet) {
        ModernLanguageFormDialog(
            isEditing = isEditing,
            languageName = languageName,
            readingRating = readingRating,
            speakingRating = speakingRating,
            writingRating = writingRating,
            listeningRating = listeningRating,
            onLanguageNameChange = { languageName = it },
            onReadingRatingChange = { readingRating = it },
            onSpeakingRatingChange = { speakingRating = it },
            onWritingRatingChange = { writingRating = it },
            onListeningRatingChange = { listeningRating = it },
            calculateAverageRating = { calculateAverageRating() },
            onDismiss = {
                showBottomSheet = false
                resetForm()
            },
            onSave = { saveLanguage() }
        )
    }
}

@Composable
fun ModernLanguageCard(
    language: LanguageModel,
    onClick: () -> Unit,
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
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
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
                            painter = painterResource(R.drawable.languageicon),
                            contentDescription = "Language",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = language.language,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF263238)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val avgRating = calculateAverageFromModel(language)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StarRatingDisplay(rating = avgRating.roundToInt(), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${String.format("%.1f", avgRating)}/5",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2196F3)
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

            Text(
                text = "R:${language.readingLevel} S:${language.speakingLevel} W:${language.writingLevel} L:${language.listeningLevel}",
                fontSize = 13.sp,
                color = Color(0xFF78909C),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ModernLanguageFormDialog(
    isEditing: Boolean,
    languageName: String,
    readingRating: Int,
    speakingRating: Int,
    writingRating: Int,
    listeningRating: Int,
    onLanguageNameChange: (String) -> Unit,
    onReadingRatingChange: (Int) -> Unit,
    onSpeakingRatingChange: (Int) -> Unit,
    onWritingRatingChange: (Int) -> Unit,
    onListeningRatingChange: (Int) -> Unit,
    calculateAverageRating: () -> Float,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit Language" else "Add Language",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Language Name
                    OutlinedTextField(
                        value = languageName,
                        onValueChange = onLanguageNameChange,
                        label = { Text("Language Name *") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.languageicon),
                                contentDescription = "Language",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        placeholder = { Text("e.g., English", color = Color(0xFFBDBDBD)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedLabelColor = Color(0xFF2196F3),
                            unfocusedLabelColor = Color(0xFF78909C)
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    // Reading Level
                    Text(
                        text = "Reading",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StarRatingRow(
                        rating = readingRating,
                        onRatingChange = onReadingRatingChange
                    )

                    Spacer(Modifier.height(20.dp))

                    // Speaking Level
                    Text(
                        text = "Speaking",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StarRatingRow(
                        rating = speakingRating,
                        onRatingChange = onSpeakingRatingChange
                    )

                    Spacer(Modifier.height(20.dp))

                    // Writing Level
                    Text(
                        text = "Writing",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StarRatingRow(
                        rating = writingRating,
                        onRatingChange = onWritingRatingChange
                    )

                    Spacer(Modifier.height(20.dp))

                    // Listening Level
                    Text(
                        text = "Listening",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF263238)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StarRatingRow(
                        rating = listeningRating,
                        onRatingChange = onListeningRatingChange
                    )

                    // Show average rating
                    Spacer(Modifier.height(24.dp))
                    val avgRating = calculateAverageRating()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2196F3).copy(alpha = 0.08f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Average Rating:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF263238)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StarRatingDisplay(rating = avgRating.roundToInt())
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = String.format("%.1f/5", avgRating),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.3f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF78909C)
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(0.7f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Text(
                            text = if (isEditing) "Update" else "Save",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernLanguageDetailDialog(
    language: LanguageModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Language Details",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238)
            )
        },
        text = {
            Column {
                DetailItem(title = "Language", value = language.language)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reading:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78909C)
                    )
                    StarRatingDisplay(
                        rating = language.readingLevel.toIntOrNull() ?: 0,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Speaking:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78909C)
                    )
                    StarRatingDisplay(
                        rating = language.speakingLevel.toIntOrNull() ?: 0,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Writing:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78909C)
                    )
                    StarRatingDisplay(
                        rating = language.writingLevel.toIntOrNull() ?: 0,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Listening:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78909C)
                    )
                    StarRatingDisplay(
                        rating = language.listeningLevel.toIntOrNull() ?: 0,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(16.dp))

                val avgRating = calculateAverageFromModel(language)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Average Rating:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StarRatingDisplay(rating = avgRating.roundToInt())
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("%.1f/5", avgRating),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun ModernLanguageDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    languageName: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF44336).copy(alpha = 0.1f)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        title = {
            Text(
                text = "Delete Language?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$languageName\"? This action cannot be undone.",
                fontSize = 15.sp,
                color = Color(0xFF78909C),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF78909C)
                    )
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun StarRatingDisplay(
    rating: Int,
    modifier: Modifier = Modifier.size(20.dp)
) {
    Row(
        horizontalArrangement = Arrangement.Start,
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
                modifier = modifier
            )
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

fun calculateAverageFromModel(language: LanguageModel): Float {
    val reading = language.readingLevel.toFloatOrNull() ?: 0f
    val speaking = language.speakingLevel.toFloatOrNull() ?: 0f
    val writing = language.writingLevel.toFloatOrNull() ?: 0f
    val listening = language.listeningLevel.toFloatOrNull() ?: 0f

    val total = reading + speaking + writing + listening
    return if (total > 0) total / 4 else 0f
}


@Preview
@Composable
fun JobSeekerLanguagePreview() {
    JobSeekerLanguageBody()
}