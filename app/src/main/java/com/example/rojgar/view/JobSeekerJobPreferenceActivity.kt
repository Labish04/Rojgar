package com.example.rojgar.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
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
import com.example.rojgar.R
import com.example.rojgar.model.PreferenceModel
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.White
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.example.rojgar.viewmodel.PreferenceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JobSeekerJobPreferenceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerJobPreferenceBody()
        }
    }
}

data class PreferenceItem(
    val name: String,
    var isSelected: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerJobPreferenceBody() {

    val context = LocalContext.current
    val activity = context as Activity

    // Initialize ViewModels
    val preferenceViewModel = remember { PreferenceViewModel() }
    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val currentUser = jobSeekerViewModel.getCurrentJobSeeker()

    // Observing ViewModel states
    val preferenceData by preferenceViewModel.preferenceData.observeAsState()
    val isLoading by preferenceViewModel.loading.observeAsState(false)

    // UI states
    var showBottomSheet by remember { mutableStateOf(false) }
    var currentSection by remember { mutableStateOf("category") }
    val scope = rememberCoroutineScope()

    // Job preferences state
    var selectedCategories by remember { mutableStateOf(listOf<String>()) }
    var selectedIndustries by remember { mutableStateOf(listOf<String>()) }
    var selectedJobTitles by remember { mutableStateOf(listOf<String>()) }
    var selectedAvailability by remember { mutableStateOf(listOf<String>()) }
    var locationInput by remember { mutableStateOf("") }

    // Animation states
    var topBarVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var fieldsVisible by remember { mutableStateOf(false) }
    var buttonsVisible by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Fetch existing preferences when activity starts
    LaunchedEffect(Unit) {
        delay(100)
        topBarVisible = true
        delay(200)
        contentVisible = true
        delay(300)
        fieldsVisible = true
        delay(400)
        buttonsVisible = true

        currentUser?.uid?.let { userId ->
            preferenceViewModel.getPreference(userId)
        }
    }

    // Update UI when preference data changes
    LaunchedEffect(preferenceData) {
        preferenceData?.let { preference ->
            selectedCategories = preference.categories
            selectedIndustries = preference.industries
            selectedJobTitles = preference.titles
            selectedAvailability = preference.availabilities
            locationInput = preference.location
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
                                contentDescription = "Back",
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
                                    "Job Preference",
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
                visible = contentVisible,
                enter = fadeIn(animationSpec = tween(600))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Header Card
                    AnimatedVisibility(
                        visible = contentVisible,
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
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(56.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Animated background circle
                                    var isAnimating by remember { mutableStateOf(false) }
                                    val infiniteTransition = rememberInfiniteTransition(label = "")
                                    val rotation by infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 360f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(3000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = ""
                                    )

                                    LaunchedEffect(Unit) {
                                        isAnimating = true
                                    }

                                    // Outer ring
                                    Surface(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .graphicsLayer { rotationZ = rotation },
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.Transparent,
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            width = 2.dp,
                                            brush = Brush.sweepGradient(
                                                colors = listOf(
                                                    Color(0xFF2196F3).copy(alpha = 0.3f),
                                                    Color(0xFF64B5F6).copy(alpha = 0.1f),
                                                    Color(0xFF2196F3).copy(alpha = 0.3f)
                                                )
                                            )
                                        )
                                    ) {}

                                    // Inner background
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF2196F3).copy(alpha = 0.15f)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.objectiveicon),
                                            contentDescription = "Preferences",
                                            tint = Color(0xFF2196F3),
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Job Preferences",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF263238)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Set your ideal job criteria",
                                        fontSize = 13.sp,
                                        color = Color(0xFF78909C)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Preference Fields with animations
                    AnimatedVisibility(
                        visible = fieldsVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(500))
                    ) {
                        Column {
                            // Job Category Display
                            ModernPreferenceField(
                                label = "Job Category",
                                selectedItems = selectedCategories,
                                icon = R.drawable.outline_keyboard_arrow_down_24,
                                onClick = {
                                    currentSection = "category"
                                    showBottomSheet = true
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Industry Display
                            ModernPreferenceField(
                                label = "Industry",
                                selectedItems = selectedIndustries,
                                icon = R.drawable.outline_keyboard_arrow_down_24,
                                onClick = {
                                    currentSection = "industry"
                                    showBottomSheet = true
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Job Title Display
                            ModernPreferenceField(
                                label = "Job Title",
                                selectedItems = selectedJobTitles,
                                icon = R.drawable.outline_keyboard_arrow_down_24,
                                onClick = {
                                    currentSection = "title"
                                    showBottomSheet = true
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Available For Display
                            ModernPreferenceField(
                                label = "Available For",
                                selectedItems = selectedAvailability,
                                icon = R.drawable.outline_keyboard_arrow_down_24,
                                onClick = {
                                    currentSection = "availability"
                                    showBottomSheet = true
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Location
                            ModernLocationField(
                                value = locationInput,
                                onValueChange = { locationInput = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Action Buttons
                    AnimatedVisibility(
                        visible = buttonsVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(500))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Back Button
                            ModernSecondaryButton(
                                text = "Back",
                                modifier = Modifier.weight(1f),
                                onClick = { activity.finish() }
                            )

                            // Save/Update Button
                            ModernPrimaryButton(
                                text = if (preferenceData == null) "Save" else "Update",
                                modifier = Modifier.weight(1f),
                                isLoading = isLoading,
                                onClick = {
                                    scope.launch {
                                        if (currentUser != null) {
                                            val existingPreference = preferenceData

                                            val preference = PreferenceModel(
                                                preferenceId = existingPreference?.preferenceId ?: "",
                                                categories = selectedCategories,
                                                industries = selectedIndustries,
                                                titles = selectedJobTitles,
                                                availabilities = selectedAvailability,
                                                location = locationInput,
                                                jobSeekerId = currentUser.uid
                                            )

                                            if (existingPreference == null) {
                                                preferenceViewModel.savePreference(preference) { success, message ->
                                                    if (success) {
                                                        Toast.makeText(context, "Preferences saved successfully!", Toast.LENGTH_SHORT).show()
                                                        activity.finish()
                                                    } else {
                                                        Toast.makeText(context, "Failed to save: $message", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            } else {
                                                preferenceViewModel.updatePreference(preference) { success, message ->
                                                    if (success) {
                                                        Toast.makeText(context, "Preferences updated successfully!", Toast.LENGTH_SHORT).show()
                                                        activity.finish()
                                                    } else {
                                                        Toast.makeText(context, "Failed to update: $message", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Bottom Sheet
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
            ) {
                JobPreferenceBottomSheet(
                    initialSection = currentSection,
                    onDismiss = { showBottomSheet = false },
                    onSave = { categories, industries, titles, availability ->
                        selectedCategories = categories
                        selectedIndustries = industries
                        selectedJobTitles = titles
                        selectedAvailability = availability
                        showBottomSheet = false
                    },
                    initialCategories = selectedCategories,
                    initialIndustries = selectedIndustries,
                    initialTitles = selectedJobTitles,
                    initialAvailability = selectedAvailability
                )
            }
        }
    }
}

@Composable
fun ModernPreferenceField(
    label: String,
    selectedItems: List<String>,
    icon: Int,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    // Shimmer effect when empty
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    // Get icon based on label
    val iconRes = when (label) {
        "Job Category" -> R.drawable.jobcategoryicon
        "Industry" -> R.drawable.company
        "Job Title" -> R.drawable.title
        "Available For" -> R.drawable.jobtype
        else -> icon
    }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF2196F3).copy(alpha = 0.15f)
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF263238),
                fontWeight = FontWeight.SemiBold
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable {
                    isPressed = true
                    onClick()
                }
                .shadow(
                    elevation = if (selectedItems.isNotEmpty()) 6.dp else 4.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedItems.isNotEmpty())
                    Color.White
                else
                    Color.White.copy(alpha = 0.95f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedItems.isEmpty()) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = Color(0xFF2196F3).copy(alpha = shimmerAlpha),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select $label",
                            color = Color(0xFFBDBDBD),
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedItems.joinToString(", "),
                            color = Color(0xFF263238),
                            fontSize = 14.sp,
                            maxLines = 2,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2196F3).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${selectedItems.size} selected",
                                fontSize = 11.sp,
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Animated background
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF2196F3).copy(alpha = if (selectedItems.isNotEmpty()) 0.2f else 0.12f)
                    ) {}

                    Icon(
                        painter = painterResource(R.drawable.outline_keyboard_arrow_down_24),
                        contentDescription = "Dropdown",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

@Composable
fun ModernLocationField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2196F3) else Color(0xFFE0E0E0),
        animationSpec = tween(300)
    )

    val elevation by animateDpAsState(
        targetValue = if (isFocused) 8.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    // Pulse animation for icon
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val iconPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF2196F3).copy(alpha = 0.15f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.locationicon),
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Job Preference Location",
                fontSize = 14.sp,
                color = Color(0xFF263238),
                fontWeight = FontWeight.SemiBold
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                leadingIcon = {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    scaleX = if (isFocused) iconPulse else 1f
                                    scaleY = if (isFocused) iconPulse else 1f
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2196F3).copy(alpha = if (isFocused) 0.2f else 0.12f)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.locationicon),
                                contentDescription = "Location",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        "Enter job preference location",
                        color = Color(0xFFBDBDBD),
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color(0xFF263238),
                    unfocusedTextColor = Color(0xFF263238)
                )
            )
        }
    }
}

@Composable
fun ModernPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
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

    // Gradient shimmer effect
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    Button(
        onClick = {
            if (!isLoading) {
                isPressed = true
                onClick()
            }
        },
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp
        ),
        enabled = !isLoading
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2196F3),
                            Color(0xFF1976D2),
                            Color(0xFF2196F3)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

@Composable
fun ModernSecondaryButton(
    text: String,
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

    OutlinedButton(
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
        shape = RoundedCornerShape(16.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
            )
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF2196F3)
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
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
fun JobPreferenceBottomSheet(
    initialSection: String,
    onDismiss: () -> Unit,
    onSave: (List<String>, List<String>, List<String>, List<String>) -> Unit,
    initialCategories: List<String>,
    initialIndustries: List<String>,
    initialTitles: List<String>,
    initialAvailability: List<String>
) {
    var currentSection by remember { mutableStateOf(initialSection) }

    // Job Category list
    val categoryList = remember {
        mutableStateListOf(
            PreferenceItem("Creative / Graphics / Designing", initialCategories.contains("Creative / Graphics / Designing")),
            PreferenceItem("UI / UX Design", initialCategories.contains("UI / UX Design")),
            PreferenceItem("Animation / VFX", initialCategories.contains("Animation / VFX")),
            PreferenceItem("Photography / Videography", initialCategories.contains("Photography / Videography")),
            PreferenceItem("Fashion / Textile Designing", initialCategories.contains("Fashion / Textile Designing")),
            PreferenceItem("Architecture / Interior Designing", initialCategories.contains("Architecture / Interior Designing")),
            PreferenceItem("IT & Telecommunication", initialCategories.contains("IT & Telecommunication")),
            PreferenceItem("Software Development", initialCategories.contains("Software Development")),
            PreferenceItem("Web Development", initialCategories.contains("Web Development")),
            PreferenceItem("Mobile App Development", initialCategories.contains("Mobile App Development")),
            PreferenceItem("Data Science / AI / ML", initialCategories.contains("Data Science / AI / ML")),
            PreferenceItem("Cyber Security", initialCategories.contains("Cyber Security")),
            PreferenceItem("Network / System Administration", initialCategories.contains("Network / System Administration")),
            PreferenceItem("DevOps / Cloud Computing", initialCategories.contains("DevOps / Cloud Computing")),
            PreferenceItem("QA / Software Testing", initialCategories.contains("QA / Software Testing")),
            PreferenceItem("General Management", initialCategories.contains("General Management")),
            PreferenceItem("Project Management", initialCategories.contains("Project Management")),
            PreferenceItem("Operations Management", initialCategories.contains("Operations Management")),
            PreferenceItem("Business Development", initialCategories.contains("Business Development")),
            PreferenceItem("Human Resource / HR", initialCategories.contains("Human Resource / HR")),
            PreferenceItem("Administration / Office Support", initialCategories.contains("Administration / Office Support")),
            PreferenceItem("Accounting / Finance", initialCategories.contains("Accounting / Finance")),
            PreferenceItem("Banking / Insurance / Financial Services", initialCategories.contains("Banking / Insurance / Financial Services")),
            PreferenceItem("Audit / Tax / Compliance", initialCategories.contains("Audit / Tax / Compliance")),
            PreferenceItem("Investment / Wealth Management", initialCategories.contains("Investment / Wealth Management")),
            PreferenceItem("Sales / Public Relations", initialCategories.contains("Sales / Public Relations")),
            PreferenceItem("Marketing / Advertising", initialCategories.contains("Marketing / Advertising")),
            PreferenceItem("Digital Marketing", initialCategories.contains("Digital Marketing")),
            PreferenceItem("Content Writing / Copywriting", initialCategories.contains("Content Writing / Copywriting")),
            PreferenceItem("Media / Journalism", initialCategories.contains("Media / Journalism")),
            PreferenceItem("Customer Service / Call Center", initialCategories.contains("Customer Service / Call Center")),
            PreferenceItem("Construction / Engineering / Architects", initialCategories.contains("Construction / Engineering / Architects")),
            PreferenceItem("Civil Engineering", initialCategories.contains("Civil Engineering")),
            PreferenceItem("Mechanical Engineering", initialCategories.contains("Mechanical Engineering")),
            PreferenceItem("Electrical / Electronics Engineering", initialCategories.contains("Electrical / Electronics Engineering")),
            PreferenceItem("Manufacturing / Production", initialCategories.contains("Manufacturing / Production")),
            PreferenceItem("Maintenance / Technician", initialCategories.contains("Maintenance / Technician")),
            PreferenceItem("Commercial / Logistics / Supply Chain", initialCategories.contains("Commercial / Logistics / Supply Chain")),
            PreferenceItem("Procurement / Purchasing", initialCategories.contains("Procurement / Purchasing")),
            PreferenceItem("Warehouse / Distribution", initialCategories.contains("Warehouse / Distribution")),
            PreferenceItem("Drivers / Delivery", initialCategories.contains("Drivers / Delivery")),
            PreferenceItem("Healthcare / Medical", initialCategories.contains("Healthcare / Medical")),
            PreferenceItem("Nursing / Caregiving", initialCategories.contains("Nursing / Caregiving")),
            PreferenceItem("Pharmacy", initialCategories.contains("Pharmacy")),
            PreferenceItem("Laboratory / Research", initialCategories.contains("Laboratory / Research")),
            PreferenceItem("Public Health", initialCategories.contains("Public Health")),
            PreferenceItem("Teaching / Education", initialCategories.contains("Teaching / Education")),
            PreferenceItem("Training / Coaching", initialCategories.contains("Training / Coaching")),
            PreferenceItem("Academic Research", initialCategories.contains("Academic Research")),
            PreferenceItem("Hotel / Hospitality", initialCategories.contains("Hotel / Hospitality")),
            PreferenceItem("Travel / Tourism", initialCategories.contains("Travel / Tourism")),
            PreferenceItem("Food & Beverage", initialCategories.contains("Food & Beverage")),
            PreferenceItem("Event Management", initialCategories.contains("Event Management")),
            PreferenceItem("Government Jobs", initialCategories.contains("Government Jobs")),
            PreferenceItem("Legal / Law / Compliance", initialCategories.contains("Legal / Law / Compliance")),
            PreferenceItem("NGO / INGO / Social Work", initialCategories.contains("NGO / INGO / Social Work")),
            PreferenceItem("Public Administration / Policy", initialCategories.contains("Public Administration / Policy")),
            PreferenceItem("Skilled Labor / Trades", initialCategories.contains("Skilled Labor / Trades")),
            PreferenceItem("Security Services", initialCategories.contains("Security Services")),
            PreferenceItem("Cleaning / Housekeeping", initialCategories.contains("Cleaning / Housekeeping")),
            PreferenceItem("Agriculture / Farming", initialCategories.contains("Agriculture / Farming"))
        )
    }

    val industryList = remember {
        mutableStateListOf(
            PreferenceItem("Software Companies", initialIndustries.contains("Software Companies")),
            PreferenceItem("Information / Computer / Technology", initialIndustries.contains("Information / Computer / Technology")),
            PreferenceItem("IT Services / Consulting", initialIndustries.contains("IT Services / Consulting")),
            PreferenceItem("Telecommunication", initialIndustries.contains("Telecommunication")),
            PreferenceItem("AI / Data / Cloud Services", initialIndustries.contains("AI / Data / Cloud Services")),
            PreferenceItem("Cyber Security Services", initialIndustries.contains("Cyber Security Services")),
            PreferenceItem("Manufacturing / Production", initialIndustries.contains("Manufacturing / Production")),
            PreferenceItem("Industrial Production", initialIndustries.contains("Industrial Production")),
            PreferenceItem("Textile / Garment Industry", initialIndustries.contains("Textile / Garment Industry")),
            PreferenceItem("Food & Beverage Manufacturing", initialIndustries.contains("Food & Beverage Manufacturing")),
            PreferenceItem("Pharmaceutical Manufacturing", initialIndustries.contains("Pharmaceutical Manufacturing")),
            PreferenceItem("Construction / Infrastructure", initialIndustries.contains("Construction / Infrastructure")),
            PreferenceItem("Civil Engineering Companies", initialIndustries.contains("Civil Engineering Companies")),
            PreferenceItem("Architecture / Interior Designing", initialIndustries.contains("Architecture / Interior Designing")),
            PreferenceItem("Mechanical / Electrical Engineering", initialIndustries.contains("Mechanical / Electrical Engineering")),
            PreferenceItem("Banking / Financial Institutions", initialIndustries.contains("Banking / Financial Institutions")),
            PreferenceItem("Insurance Companies", initialIndustries.contains("Insurance Companies")),
            PreferenceItem("Audit Firms / Tax Consultant", initialIndustries.contains("Audit Firms / Tax Consultant")),
            PreferenceItem("Microfinance / Cooperative", initialIndustries.contains("Microfinance / Cooperative")),
            PreferenceItem("Investment / Brokerage Firms", initialIndustries.contains("Investment / Brokerage Firms")),
            PreferenceItem("Trading / Wholesale", initialIndustries.contains("Trading / Wholesale")),
            PreferenceItem("Retail Industry", initialIndustries.contains("Retail Industry")),
            PreferenceItem("E-Commerce Companies", initialIndustries.contains("E-Commerce Companies")),
            PreferenceItem("Import / Export", initialIndustries.contains("Import / Export")),
            PreferenceItem("Logistics / Supply Chain", initialIndustries.contains("Logistics / Supply Chain")),
            PreferenceItem("Transportation / Courier Services", initialIndustries.contains("Transportation / Courier Services")),
            PreferenceItem("Warehouse / Distribution", initialIndustries.contains("Warehouse / Distribution")),
            PreferenceItem("Hotel / Resort", initialIndustries.contains("Hotel / Resort")),
            PreferenceItem("Travel / Tourism", initialIndustries.contains("Travel / Tourism")),
            PreferenceItem("Restaurant / Cafe", initialIndustries.contains("Restaurant / Cafe")),
            PreferenceItem("Event Management", initialIndustries.contains("Event Management")),
            PreferenceItem("Marketing / Advertising Agencies", initialIndustries.contains("Marketing / Advertising Agencies")),
            PreferenceItem("Digital Marketing Agencies", initialIndustries.contains("Digital Marketing Agencies")),
            PreferenceItem("Designing / Printing / Publishing", initialIndustries.contains("Designing / Printing / Publishing")),
            PreferenceItem("Media / Broadcasting", initialIndustries.contains("Media / Broadcasting")),
            PreferenceItem("Content / Creative Studios", initialIndustries.contains("Content / Creative Studios")),
            PreferenceItem("Hospitals / Clinics", initialIndustries.contains("Hospitals / Clinics")),
            PreferenceItem("Healthcare Services", initialIndustries.contains("Healthcare Services")),
            PreferenceItem("Pharmaceutical Companies", initialIndustries.contains("Pharmaceutical Companies")),
            PreferenceItem("Medical Equipment Suppliers", initialIndustries.contains("Medical Equipment Suppliers")),
            PreferenceItem("Schools / Colleges", initialIndustries.contains("Schools / Colleges")),
            PreferenceItem("Universities / Academic Institutions", initialIndustries.contains("Universities / Academic Institutions")),
            PreferenceItem("Training / Coaching Institutes", initialIndustries.contains("Training / Coaching Institutes")),
            PreferenceItem("EdTech Companies", initialIndustries.contains("EdTech Companies")),
            PreferenceItem("Government Organizations", initialIndustries.contains("Government Organizations")),
            PreferenceItem("NGO / INGO / Development Projects", initialIndustries.contains("NGO / INGO / Development Projects")),
            PreferenceItem("Legal / Law Firms", initialIndustries.contains("Legal / Law Firms")),
            PreferenceItem("Public Sector Enterprises", initialIndustries.contains("Public Sector Enterprises")),
            PreferenceItem("Associations", initialIndustries.contains("Associations")),
            PreferenceItem("Agriculture / Farming", initialIndustries.contains("Agriculture / Farming")),
            PreferenceItem("Agro-Based Industries", initialIndustries.contains("Agro-Based Industries")),
            PreferenceItem("Dairy / Poultry / Livestock", initialIndustries.contains("Dairy / Poultry / Livestock")),
            PreferenceItem("Renewable Energy / Power", initialIndustries.contains("Renewable Energy / Power")),
            PreferenceItem("Consulting Firms", initialIndustries.contains("Consulting Firms")),
            PreferenceItem("Human Resource / Recruitment Agencies", initialIndustries.contains("Human Resource / Recruitment Agencies")),
            PreferenceItem("Security Services", initialIndustries.contains("Security Services")),
            PreferenceItem("Facility Management / Cleaning Services", initialIndustries.contains("Facility Management / Cleaning Services")),
            PreferenceItem("Startup / Private Companies", initialIndustries.contains("Startup / Private Companies"))
        )
    }

    val jobTitleList = remember {
        mutableStateListOf(
            PreferenceItem("Baker", initialTitles.contains("Baker")),
            PreferenceItem("Pastry Chef", initialTitles.contains("Pastry Chef")),
            PreferenceItem("Sous Chef", initialTitles.contains("Sous Chef")),
            PreferenceItem("Banquet Sous Chef", initialTitles.contains("Banquet Sous Chef")),
            PreferenceItem("Executive Chef", initialTitles.contains("Executive Chef")),
            PreferenceItem("Cook / Line Cook", initialTitles.contains("Cook / Line Cook")),
            PreferenceItem("Restaurant Manager", initialTitles.contains("Restaurant Manager")),
            PreferenceItem("Banquet and Event Manager", initialTitles.contains("Banquet and Event Manager")),
            PreferenceItem("Hotel Manager", initialTitles.contains("Hotel Manager")),
            PreferenceItem("Food & Beverage Supervisor", initialTitles.contains("Food & Beverage Supervisor")),
            PreferenceItem("Event Coordinator", initialTitles.contains("Event Coordinator")),
            PreferenceItem("Event Manager", initialTitles.contains("Event Manager")),
            PreferenceItem("Basketball Coach", initialTitles.contains("Basketball Coach")),
            PreferenceItem("Futsal Coach", initialTitles.contains("Futsal Coach")),
            PreferenceItem("Sports Trainer", initialTitles.contains("Sports Trainer")),
            PreferenceItem("Fitness Instructor", initialTitles.contains("Fitness Instructor")),
            PreferenceItem("Backend Developer", initialTitles.contains("Backend Developer")),
            PreferenceItem("Backend Engineer", initialTitles.contains("Backend Engineer")),
            PreferenceItem("Frontend Developer", initialTitles.contains("Frontend Developer")),
            PreferenceItem("Full Stack Developer", initialTitles.contains("Full Stack Developer")),
            PreferenceItem("Mobile Application Developer", initialTitles.contains("Mobile Application Developer")),
            PreferenceItem("Software Engineer", initialTitles.contains("Software Engineer")),
            PreferenceItem("DevOps Engineer", initialTitles.contains("DevOps Engineer")),
            PreferenceItem("QA Engineer", initialTitles.contains("QA Engineer")),
            PreferenceItem("Automation Test Engineer", initialTitles.contains("Automation Test Engineer")),
            PreferenceItem("Associate Database Administrator", initialTitles.contains("Associate Database Administrator")),
            PreferenceItem("Database Administrator", initialTitles.contains("Database Administrator")),
            PreferenceItem("Data Analyst", initialTitles.contains("Data Analyst")),
            PreferenceItem("Data Engineer", initialTitles.contains("Data Engineer")),
            PreferenceItem("System Administrator", initialTitles.contains("System Administrator")),
            PreferenceItem("Network Engineer", initialTitles.contains("Network Engineer")),
            PreferenceItem("Cloud Engineer", initialTitles.contains("Cloud Engineer")),
            PreferenceItem("UI/UX Designer", initialTitles.contains("UI/UX Designer")),
            PreferenceItem("Graphic Designer", initialTitles.contains("Graphic Designer")),
            PreferenceItem("Motion Graphics Designer", initialTitles.contains("Motion Graphics Designer")),
            PreferenceItem("Video Editor", initialTitles.contains("Video Editor")),
            PreferenceItem("Content Creator", initialTitles.contains("Content Creator")),
            PreferenceItem("Sales Executive", initialTitles.contains("Sales Executive")),
            PreferenceItem("Marketing Officer", initialTitles.contains("Marketing Officer")),
            PreferenceItem("Digital Marketing Specialist", initialTitles.contains("Digital Marketing Specialist")),
            PreferenceItem("Business Development Officer", initialTitles.contains("Business Development Officer")),
            PreferenceItem("Account Manager", initialTitles.contains("Account Manager")),
            PreferenceItem("Customer Service Representative", initialTitles.contains("Customer Service Representative")),
            PreferenceItem("Accountant", initialTitles.contains("Accountant")),
            PreferenceItem("Accounts Officer", initialTitles.contains("Accounts Officer")),
            PreferenceItem("Finance Manager", initialTitles.contains("Finance Manager")),
            PreferenceItem("Audit Associate", initialTitles.contains("Audit Associate")),
            PreferenceItem("Tax Consultant", initialTitles.contains("Tax Consultant")),
            PreferenceItem("Administrative Officer", initialTitles.contains("Administrative Officer")),
            PreferenceItem("Office Assistant", initialTitles.contains("Office Assistant")),
            PreferenceItem("Staff Nurse", initialTitles.contains("Staff Nurse")),
            PreferenceItem("Medical Officer", initialTitles.contains("Medical Officer")),
            PreferenceItem("Pharmacist", initialTitles.contains("Pharmacist")),
            PreferenceItem("Lab Technician", initialTitles.contains("Lab Technician")),
            PreferenceItem("Healthcare Assistant", initialTitles.contains("Healthcare Assistant")),
            PreferenceItem("Civil Engineer", initialTitles.contains("Civil Engineer")),
            PreferenceItem("Site Engineer", initialTitles.contains("Site Engineer")),
            PreferenceItem("Mechanical Engineer", initialTitles.contains("Mechanical Engineer")),
            PreferenceItem("Electrical Engineer", initialTitles.contains("Electrical Engineer")),
            PreferenceItem("Maintenance Technician", initialTitles.contains("Maintenance Technician")),
            PreferenceItem("Intern", initialTitles.contains("Intern")),
            PreferenceItem("Trainee", initialTitles.contains("Trainee")),
            PreferenceItem("Junior Executive", initialTitles.contains("Junior Executive")),
            PreferenceItem("Assistant Manager", initialTitles.contains("Assistant Manager")),
            PreferenceItem("Operations Executive", initialTitles.contains("Operations Executive"))
        )
    }

    val availabilityList = remember {
        mutableStateListOf(
            PreferenceItem("Full Time", initialAvailability.contains("Full Time")),
            PreferenceItem("Part Time", initialAvailability.contains("Part Time")),
            PreferenceItem("Contract", initialAvailability.contains("Contract")),
            PreferenceItem("Temporary", initialAvailability.contains("Temporary")),
            PreferenceItem("Seasonal", initialAvailability.contains("Seasonal")),
            PreferenceItem("Freelance", initialAvailability.contains("Freelance")),
            PreferenceItem("Remote", initialAvailability.contains("Remote")),
            PreferenceItem("Hybrid", initialAvailability.contains("Hybrid")),
            PreferenceItem("On-site", initialAvailability.contains("On-site")),
            PreferenceItem("Internship", initialAvailability.contains("Internship")),
            PreferenceItem("Traineeship", initialAvailability.contains("Traineeship")),
            PreferenceItem("Apprenticeship", initialAvailability.contains("Apprenticeship")),
            PreferenceItem("Graduate Program", initialAvailability.contains("Graduate Program")),
            PreferenceItem("Volunteer", initialAvailability.contains("Volunteer")),
            PreferenceItem("Shift Based", initialAvailability.contains("Shift Based")),
            PreferenceItem("Project Based", initialAvailability.contains("Project Based"))
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .padding(horizontal = 20.dp)
    ) {
        when (currentSection) {
            "category" -> ModernSectionHeader(
                title = "Preferred Job Category",
                subtitle = "You can add upto 5 category.",
                count = categoryList.count { it.isSelected },
                maxCount = 5
            )
            "industry" -> ModernSectionHeader(
                title = "Preferred Job Industry",
                subtitle = "You can add upto 5 industry.",
                count = industryList.count { it.isSelected },
                maxCount = 5
            )
            "title" -> ModernSectionHeader(
                title = "Select Job Title",
                subtitle = "You can add upto 5 job title.",
                count = jobTitleList.count { it.isSelected },
                maxCount = 5
            )
            "availability" -> ModernSectionHeader(
                title = "Available For",
                subtitle = "",
                count = availabilityList.count { it.isSelected },
                maxCount = 7
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ModernSearchBar(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            placeholder = when (currentSection) {
                "category" -> "Search preferred job categories"
                "industry" -> "Search preferred job industry"
                "title" -> "Search preferred job title"
                else -> "Search"
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            when (currentSection) {
                "category" -> {
                    items(
                        items = categoryList.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        },
                        key = { it.name }
                    ) { category ->
                        val index = categoryList.indexOf(category)
                        ModernSelectableItem(
                            name = category.name,
                            isSelected = category.isSelected,
                            onToggle = {
                                val selectedCount = categoryList.count { it.isSelected }
                                if (!category.isSelected && selectedCount >= 5) return@ModernSelectableItem
                                categoryList[index] = category.copy(isSelected = !category.isSelected)
                            }
                        )
                    }
                }
                "industry" -> {
                    items(
                        items = industryList.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        },
                        key = { it.name }
                    ) { industry ->
                        val index = industryList.indexOf(industry)
                        ModernSelectableItem(
                            name = industry.name,
                            isSelected = industry.isSelected,
                            onToggle = {
                                val selectedCount = industryList.count { it.isSelected }
                                if (!industry.isSelected && selectedCount >= 5) return@ModernSelectableItem
                                industryList[index] = industry.copy(isSelected = !industry.isSelected)
                            }
                        )
                    }
                }
                "title" -> {
                    items(
                        items = jobTitleList.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        },
                        key = { it.name }
                    ) { title ->
                        val index = jobTitleList.indexOf(title)
                        ModernSelectableItem(
                            name = title.name,
                            isSelected = title.isSelected,
                            onToggle = {
                                val selectedCount = jobTitleList.count { it.isSelected }
                                if (!title.isSelected && selectedCount >= 5) return@ModernSelectableItem
                                jobTitleList[index] = title.copy(isSelected = !title.isSelected)
                            }
                        )
                    }
                }
                "availability" -> {
                    items(
                        items = availabilityList,
                        key = { it.name }
                    ) { availability ->
                        val index = availabilityList.indexOf(availability)
                        ModernSelectableItem(
                            name = availability.name,
                            isSelected = availability.isSelected,
                            onToggle = {
                                availabilityList[index] = availability.copy(isSelected = !availability.isSelected)
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernSecondaryButton(
                text = "Cancel",
                modifier = Modifier.weight(1f),
                onClick = { onDismiss() }
            )

            ModernPrimaryButton(
                text = "Done",
                modifier = Modifier.weight(1f),
                onClick = {
                    onSave(
                        categoryList.filter { it.isSelected }.map { it.name },
                        industryList.filter { it.isSelected }.map { it.name },
                        jobTitleList.filter { it.isSelected }.map { it.name },
                        availabilityList.filter { it.isSelected }.map { it.name }
                    )
                }
            )
        }
    }
}

@Composable
fun ModernSectionHeader(title: String, subtitle: String, count: Int, maxCount: Int) {
    var headerVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        headerVisible = true
    }

    AnimatedVisibility(
        visible = headerVisible,
        enter = slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(500))
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // Animated icon
                val infiniteTransition = rememberInfiniteTransition(label = "")
                val iconRotation by infiniteTransition.animateFloat(
                    initialValue = -10f,
                    targetValue = 10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = ""
                )

                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2196F3).copy(alpha = 0.15f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.objectiveicon),
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier
                            .padding(6.dp)
                            .graphicsLayer { rotationZ = iconRotation }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )
            }

            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF78909C),
                    modifier = Modifier.padding(start = 44.dp, bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.padding(start = 44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (count >= maxCount) Color(0xFFFFEBEE) else Color(0xFFE3F2FD),
                    modifier = Modifier.shadow(2.dp, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = if (count >= maxCount) Color(0xFFD32F2F) else Color(0xFF2196F3),
                            modifier = Modifier
                                .size(14.dp)
                                .graphicsLayer { rotationZ = 180f }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "$count/$maxCount",
                            fontSize = 14.sp,
                            color = if (count >= maxCount) Color(0xFFD32F2F) else Color(0xFF2196F3),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernSearchBar(searchQuery: String, onSearchChange: (String) -> Unit, placeholder: String) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2196F3) else Color.Transparent,
        animationSpec = tween(300)
    )

    val elevation by animateDpAsState(
        targetValue = if (isFocused) 8.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp, brush = Brush.linearGradient(listOf(borderColor, borderColor)))
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    placeholder,
                    color = Color(0xFFBDBDBD),
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Rotating search icon background
                    val infiniteTransition = rememberInfiniteTransition(label = "")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(3000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = ""
                    )

                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer { rotationZ = if (isFocused) rotation else 0f },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        border = if (isFocused) ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF2196F3).copy(alpha = 0.3f),
                                    Color(0xFF64B5F6).copy(alpha = 0.1f),
                                    Color(0xFF2196F3).copy(alpha = 0.3f)
                                )
                            )
                        ) else null
                    ) {}

                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2196F3).copy(alpha = if (isFocused) 0.2f else 0.12f)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.searchicon),
                            contentDescription = "Search",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged{ isFocused = it.isFocused },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color(0xFF263238),
                unfocusedTextColor = Color(0xFF263238)
            ),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ModernSelectableItem(name: String, isSelected: Boolean, onToggle: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    var itemVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        itemVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    // Shimmer effect for selected items
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    AnimatedVisibility(
        visible = itemVisible,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable {
                    isPressed = true
                    onToggle()
                }
                .shadow(
                    elevation = if (isSelected) 8.dp else 3.dp,
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2196F3).copy(alpha = 0.5f),
                        Color(0xFF64B5F6).copy(alpha = 0.3f)
                    )
                )
            ) else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category indicator dot
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) Color(0xFF2196F3) else Color(0xFFBDBDBD)
                    ) {}

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        name,
                        fontSize = 15.sp,
                        color = if (isSelected) Color(0xFF1565C0) else Color(0xFF263238),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Animated selection indicator
                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer {
                                    scaleX = 0.8f + (shimmer * 0.2f)
                                    scaleY = 0.8f + (shimmer * 0.2f)
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF2196F3)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .graphicsLayer { rotationZ = 180f }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent,
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFFE0E0E0), Color(0xFFE0E0E0))
                                )
                            )
                        ) {}
                    }
                }
            }
        }
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
fun JobSeekerJobPreferencePreview() {
    JobSeekerJobPreferenceBody()
}