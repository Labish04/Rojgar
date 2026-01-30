package com.example.rojgar.view

import android.app.Activity
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay

// Enhanced Theme Colors matching LoginActivity
object ModernForgetPasswordTheme {
    val PrimaryBlue = Color(0xFF3B82F6)
    val LightBlue = Color(0xFF60A5FA)
    val DeepBlue = Color(0xFF2563EB)
    val SkyBlue = Color(0xFFBAE6FD)
    val IceBlue = Color(0xFFE0F2FE)
    val AccentBlue = Color(0xFF0EA5E9)
    val DarkBlue = Color(0xFF1E3A8A)
    val SurfaceLight = Color(0xFFF0F9FF)
    val White = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF64748B)
    val GlassWhite = Color(0xCCFFFFFF)
    val GlassBlue = Color(0x33BFDBFE)
    val SuccessGreen = Color(0xFF10B981)
    val ErrorRed = Color(0xFFEF4444)
}

class ForgetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ModernForgetPasswordScreen()
        }
    }
}

@Composable
fun ModernForgetPasswordScreen() {
    val context = LocalContext.current
    val activity = context as Activity
    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var startAnimation by remember { mutableStateOf(false) }
    var showSuccessState by remember { mutableStateOf(false) }

    // Animations
    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(800, delayMillis = 300),
        label = "contentAlpha"
    )

    val cardOffsetY by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 60.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardOffsetY"
    )

    LaunchedEffect(Unit) {
        delay(150)
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ModernForgetPasswordTheme.IceBlue,
                        ModernForgetPasswordTheme.White,
                        ModernForgetPasswordTheme.SurfaceLight,
                        ModernForgetPasswordTheme.SkyBlue
                    )
                )
            )
    ) {
        // Animated Background
        AnimatedBackgroundCircles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(75.dp))

            // Main Content
            AnimatedVisibility(
                visible = !showSuccessState,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .alpha(contentAlpha)
                        .offset(y = cardOffsetY)
                ) {
                    // Icon Section
                    Box(
                        modifier = Modifier
                            .size(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(20.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            ModernForgetPasswordTheme.LightBlue.copy(alpha = 0.4f),
                                            ModernForgetPasswordTheme.PrimaryBlue.copy(alpha = 0.2f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(ModernForgetPasswordTheme.GlassWhite)
                                .border(
                                    width = 3.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            ModernForgetPasswordTheme.LightBlue,
                                            ModernForgetPasswordTheme.PrimaryBlue,
                                            ModernForgetPasswordTheme.AccentBlue
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_lock_24),
                                contentDescription = "Password Reset",
                                tint = ModernForgetPasswordTheme.PrimaryBlue,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Title
                    Text(
                        text = "Forgot Password?",
                        style = TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ModernForgetPasswordTheme.DeepBlue,
                                    ModernForgetPasswordTheme.PrimaryBlue
                                )
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = "Don't worry! Enter your email and we'll send you a password reset link.",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = ModernForgetPasswordTheme.TextSecondary,
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Glass Card for Input
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            // Email Field
                            EnhancedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email Address",
                                leadingIcon = R.drawable.email,
                                delay = 400
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Reset Button
                            EnhancedButton(
                                text = "Send Reset Link",
                                isLoading = isLoading,
                                onClick = {
                                    if (email.isEmpty()) {
                                        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                        return@EnhancedButton
                                    }

                                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                        Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                                        return@EnhancedButton
                                    }

                                    isLoading = true

                                    // Check user type
                                    FindUserTypeByEmail(
                                        email = email,
                                        onUserTypeFound = { userType ->
                                            when (userType) {
                                                "JOBSEEKER" -> {
                                                    jobSeekerViewModel.forgetPassword(email) { success, message ->
                                                        isLoading = false
                                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                        if (success) {
                                                            showSuccessState = true
                                                        }
                                                    }
                                                }
                                                "COMPANY" -> {
                                                    companyViewModel.forgetPassword(email) { success, message ->
                                                        isLoading = false
                                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                        if (success) {
                                                            showSuccessState = true
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    isLoading = false
                                                    Toast.makeText(
                                                        context,
                                                        "Email not found. Please check and try again.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        },
                                        onError = { errorMessage ->
                                            isLoading = false
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                delay = 600
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Back to Login
                    Text(
                        text = "Remember your password? Sign in",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = ModernForgetPasswordTheme.DeepBlue
                        ),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            activity.finish()
                        }
                    )
                }
            }

            // Success State
            AnimatedVisibility(
                visible = showSuccessState,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                SuccessState(
                    onBackToLogin = { activity.finish() },
                    onResend = {
                        showSuccessState = false
                        // Optionally implement resend logic
                    }
                )
            }
        }
    }
}

@Composable
fun SuccessState(
    onBackToLogin: () -> Unit,
    onResend: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        // Success Icon with Animation
        val infiniteTransition = rememberInfiniteTransition()
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(pulseScale),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ModernForgetPasswordTheme.SuccessGreen.copy(alpha = 0.4f),
                                Color.Green.copy(alpha = 0.2f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(ModernForgetPasswordTheme.GlassWhite)
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ModernForgetPasswordTheme.SuccessGreen,
                                Color(0xFF34D399)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.markasread),
                    contentDescription = "Success",
                    tint = ModernForgetPasswordTheme.SuccessGreen,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Check Your Email",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ModernForgetPasswordTheme.TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "We've sent a password reset link to your email address. Please check your inbox and follow the instructions.",
            style = TextStyle(
                fontSize = 16.sp,
                color = ModernForgetPasswordTheme.TextSecondary,
                fontWeight = FontWeight.Medium
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Back to Login Button
            Button(
                onClick = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    ModernForgetPasswordTheme.DeepBlue,
                                    ModernForgetPasswordTheme.PrimaryBlue,
                                    ModernForgetPasswordTheme.AccentBlue
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Back to Login",
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernForgetPasswordTheme.White
                        )
                    )
                }
            }

            // Resend Link Button
            OutlinedButton(
                onClick = onResend,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, ModernForgetPasswordTheme.PrimaryBlue),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ModernForgetPasswordTheme.PrimaryBlue
                )
            ) {
                Text(
                    text = "Resend Link",
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}




// Function to find user type (keeping existing logic)
fun FindUserTypeByEmail(
    email: String,
    onUserTypeFound: (String) -> Unit,
    onError: (String) -> Unit
) {
    val database = FirebaseDatabase.getInstance()
    var foundInJobSeekers = false
    var foundInCompany = false
    var jobSeekersChecked = false
    var companyChecked = false

    // Check JobSeekers database
    database.getReference("JobSeekers")
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                jobSeekersChecked = true

                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        try {
                            val jobSeeker = child.getValue(com.example.rojgar.model.JobSeekerModel::class.java)
                            if (jobSeeker?.email?.equals(email, ignoreCase = true) == true) {
                                foundInJobSeekers = true
                                onUserTypeFound("JOBSEEKER")
                                return
                            }
                        } catch (e: Exception) {
                            // Continue to next entry
                        }
                    }
                }

                // If both checked and not found
                if (companyChecked && !foundInJobSeekers && !foundInCompany) {
                    onUserTypeFound("NOT_FOUND")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError("Error accessing database: ${error.message}")
            }
        })

    // Check Company database
    database.getReference("Companys")
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                companyChecked = true

                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        try {
                            val company = child.getValue(com.example.rojgar.model.CompanyModel::class.java)
                            if (company?.companyEmail?.equals(email, ignoreCase = true) == true) {
                                foundInCompany = true
                                onUserTypeFound("COMPANY")
                                return
                            }
                        } catch (e: Exception) {
                            // Continue to next entry
                        }
                    }
                }

                // If both checked and not found
                if (jobSeekersChecked && !foundInJobSeekers && !foundInCompany) {
                    onUserTypeFound("NOT_FOUND")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError("Error accessing database: ${error.message}")
            }
        })
}

@Preview
@Composable
fun ForgetPasswordPreview() {
    ModernForgetPasswordScreen()
}