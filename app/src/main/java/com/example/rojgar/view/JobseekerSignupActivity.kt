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
import androidx.compose.foundation.*
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
import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.viewmodel.JobSeekerViewModel
import kotlinx.coroutines.delay

// Theme matching LoginActivity
object ModernSignUpTheme {
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
}

class JobseekerSignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerSignUpScreen()
        }
    }
}

@Composable
fun JobSeekerSignUpScreen() {
    val context = LocalContext.current
    val activity = context as Activity

    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var startAnimation by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000, delayMillis = 300), label = ""
    )

    val successScale by animateFloatAsState(
        targetValue = if (showSuccess) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ), label = ""
    )

    LaunchedEffect(Unit) {
        delay(150)
        startAnimation = true
    }

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            delay(3500)
            showSuccess = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ModernSignUpTheme.IceBlue,
                        ModernSignUpTheme.White,
                        ModernSignUpTheme.SurfaceLight,
                        ModernSignUpTheme.SkyBlue
                    )
                )
            )
    ) {
        AnimatedBackgroundCircles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Enhanced Logo Section
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(20.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    ModernSignUpTheme.LightBlue.copy(alpha = 0.4f),
                                    ModernSignUpTheme.PrimaryBlue.copy(alpha = 0.2f)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(ModernSignUpTheme.GlassWhite)
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ModernSignUpTheme.LightBlue,
                                    ModernSignUpTheme.PrimaryBlue,
                                    ModernSignUpTheme.AccentBlue
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.r),
                        contentDescription = "Logo",
                        tint = ModernSignUpTheme.PrimaryBlue,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Welcome Text
            Column(
                modifier = Modifier.alpha(contentAlpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Join Our Community",
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ModernSignUpTheme.DeepBlue,
                                ModernSignUpTheme.PrimaryBlue
                            )
                        )
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create your job seeker account",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = ModernSignUpTheme.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Glass Card Container for Form
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Full Name Field
                    EnhancedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name",
                        leadingIcon = R.drawable.user,
                        delay = 200
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Number Field
                    EnhancedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = "Phone Number",
                        leadingIcon = R.drawable.phoneiconoutlined,
                        delay = 300
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    EnhancedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        leadingIcon = R.drawable.email,
                        delay = 400
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    EnhancedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = R.drawable.outline_lock_24,
                        isPassword = true,
                        showPassword = showPassword,
                        onPasswordToggle = { showPassword = !showPassword },
                        delay = 500
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password Field
                    EnhancedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        leadingIcon = R.drawable.outline_lock_24,
                        isPassword = true,
                        showPassword = showConfirmPassword,
                        onPasswordToggle = { showConfirmPassword = !showConfirmPassword },
                        delay = 600
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up Button
            EnhancedButton(
                text = "Create Account",
                isLoading = isLoading,
                onClick = {
                    if (fullName.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    } else if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        jobSeekerViewModel.register(email, password) { success, message, jobSeekerId ->
                            if (success) {
                                val model = JobSeekerModel(
                                    jobSeekerId = jobSeekerId,
                                    fullName = fullName,
                                    phoneNumber = phoneNumber,
                                    email = email
                                )
                                jobSeekerViewModel.addJobSeekerToDatabase(jobSeekerId, model) { success, message ->
                                    isLoading = false
                                    if (success) {
                                        showSuccess = true
                                        activity.finish()
                                        val intent = Intent(context, LoginActivity::class.java)
                                        context.startActivity(intent)
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                isLoading = false
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                delay = 700
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In Link
            Row(
                modifier = Modifier.alpha(contentAlpha),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = ModernSignUpTheme.TextSecondary
                    )
                )
                Text(
                    text = "Sign In",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = ModernSignUpTheme.PrimaryBlue,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        activity.finish()
                    }
                )
            }
        }

        // Success Animation
        AnimatedVisibility(
            visible = showSuccess,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(successScale)
                        .clip(CircleShape)
                        .background(ModernSignUpTheme.GlassWhite)
                        .border(
                            width = 4.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ModernSignUpTheme.SuccessGreen,
                                    ModernSignUpTheme.AccentBlue
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.markasread),
                            contentDescription = "Success",
                            tint = ModernSignUpTheme.SuccessGreen,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Account Created!",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ModernSignUpTheme.TextPrimary
                            )
                        )
                        Text(
                            text = "Redirecting...",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = ModernSignUpTheme.TextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewSignupScreen() {
    JobSeekerSignUpScreen()
}