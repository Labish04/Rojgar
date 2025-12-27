

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
import com.example.rojgar.model.LanguageModel
import com.example.rojgar.repository.LanguageRepoImpl
import com.example.rojgar.ui.theme.*
import com.example.rojgar.viewmodel.LanguageViewModel
import com.google.firebase.auth.FirebaseAuth
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

@OptIn(ExperimentalMaterial3Api::class)
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

    // Form fields with star ratings (0-5)
    var languageName by remember { mutableStateOf("") }
    var readingRating by remember { mutableStateOf(0) }
    var speakingRating by remember { mutableStateOf(0) }
    var writingRating by remember { mutableStateOf(0) }
    var listeningRating by remember { mutableStateOf(0) }

    // Load languages
    LaunchedEffect(Unit) {
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
        }
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
                        // Refresh list
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
                    // Refresh list
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

    // Delete Dialog
    if (showDeleteAlert) {
        AlertDialog(
            onDismissRequest = {
                showDeleteAlert = false
                languageToDelete = null
            },
            title = {
                Text(
                    text = "Delete Language",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this language?",
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
                            languageToDelete = null
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
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Which languages do you know?",
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (languages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.noexperience),
                        contentDescription = "no languages",
                        tint = Color.Gray,
                        modifier = Modifier.size(110.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "You haven't added any languages. Tap + to get started.",
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
                    items(languages) { language ->
                        LanguageCard(
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
        }
    }

    // Language Detail Dialog
    if (showDetailDialog && selectedLanguage != null) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = {
                Text(
                    text = "Language Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = DarkBlue2
                )
            },
            text = {
                Column {
                    selectedLanguage?.let { lang ->
                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            DetailsItems(title = "Language", value = lang.language)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reading:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            StarRatingDisplay(
                                rating = lang.readingLevel.toIntOrNull() ?: 0,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Speaking:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            StarRatingDisplay(
                                rating = lang.speakingLevel.toIntOrNull() ?: 0,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Writing:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            StarRatingDisplay(
                                rating = lang.writingLevel.toIntOrNull() ?: 0,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Listening:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            StarRatingDisplay(
                                rating = lang.listeningLevel.toIntOrNull() ?: 0,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Calculate and show average rating
                        Spacer(modifier = Modifier.height(8.dp))
                        val avgRating = calculateAverageFromModel(lang)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Average Rating:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StarRatingDisplay(rating = avgRating.roundToInt())
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = String.format("%.1f/5", avgRating),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBlue2
                                )
                            }
                        }
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
                            selectedLanguage?.let { openEditForm(it) }
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

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            selectedLanguage?.let {
                                languageToDelete = it.languageId
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

    // Bottom Sheet for adding/editing language
    if (showBottomSheet) {
        Dialog(
            onDismissRequest = {
                showBottomSheet = false
                resetForm()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .clickable {
                        showBottomSheet = false
                        resetForm()
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (isEditing) "Edit Language" else "Add Language",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(16.dp))

                        // Language Name
                        OutlinedTextField(
                            value = languageName,
                            onValueChange = { languageName = it },
                            label = { Text("Language Name *") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.languageicon),
                                    contentDescription = "Language",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
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

                        // Reading Level
                        Text(
                            text = "Reading",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StarRatingRow(
                            rating = readingRating,
                            onRatingChange = { readingRating = it }
                        )

                        Spacer(Modifier.height(20.dp))

                        // Speaking Level
                        Text(
                            text = "Speaking",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StarRatingRow(
                            rating = speakingRating,
                            onRatingChange = { speakingRating = it }
                        )

                        Spacer(Modifier.height(20.dp))

                        // Writing Level
                        Text(
                            text = "Writing",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StarRatingRow(
                            rating = writingRating,
                            onRatingChange = { writingRating = it }
                        )

                        Spacer(Modifier.height(20.dp))

                        // Listening Level
                        Text(
                            text = "Listening",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StarRatingRow(
                            rating = listeningRating,
                            onRatingChange = { listeningRating = it }
                        )

                        // Show average rating
                        Spacer(Modifier.height(20.dp))
                        val avgRating = calculateAverageRating()
                        Text(
                            text = "Average Rating: ${String.format("%.1f", avgRating)}/5",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue2
                        )

                        // Star visualization for average
                        StarRatingDisplay(rating = avgRating.roundToInt())

                        Spacer(Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Back Button
                            Button(
                                onClick = {
                                    showBottomSheet = false
                                    resetForm()
                                },
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier
                                    .weight(0.4f)
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

                            Spacer(modifier = Modifier.width(12.dp))

                            // Save Button
                            Button(
                                onClick = { saveLanguage() },
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier
                                    .weight(0.6f)
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
fun LanguageCard(
    language: LanguageModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = language.language,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Average rating stars
                    val avgRating = calculateAverageFromModel(language)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StarRatingDisplay(rating = avgRating.roundToInt())
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${String.format("%.1f", avgRating)}/5",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkBlue2
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Individual ratings in smaller text
                    Text(
                        text = "R:${language.readingLevel} S:${language.speakingLevel} W:${language.writingLevel} L:${language.listeningLevel}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
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

@Composable
fun DetailsItems(title: String, value: String) {
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
fun JobSeekerLanguagePreview() {
    JobSeekerLanguageBody()
}
