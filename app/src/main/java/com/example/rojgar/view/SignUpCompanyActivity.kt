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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.viewmodel.CompanyViewModel
import kotlinx.coroutines.delay


class SignUpCompanyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SignUpCompanyBody()
        }
    }
}

@Composable
fun SignUpCompanyBody() {
    val context = LocalContext.current
    val activity = context as Activity

    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }

    var companyName by remember { mutableStateOf("") }
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
                                    ModernSignUpTheme.DeepBlue.copy(alpha = 0.4f),
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
                                    ModernSignUpTheme.DeepBlue,
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
                        contentDescription = "Company Logo",
                        tint = ModernSignUpTheme.DeepBlue,
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
                    text = "Join as Company",
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
                    text = "Find the perfect candidates for your team",
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
                    // Company Name Field
                    EnhancedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = "Company Name",
                        tag = "companyName",
                        leadingIcon = R.drawable.office,
                        delay = 200
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Number Field
                    EnhancedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = "Contact Number",
                        tag = "phoneNumber",
                        leadingIcon = R.drawable.phoneiconoutlined,
                        delay = 300
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    EnhancedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Business Email",
                        tag = "email",
                        leadingIcon = R.drawable.email,
                        delay = 400
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    EnhancedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        tag = "password",
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
                        tag = "confirmPassword",
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
            EnhancedCompanyButton(
                text = "Create Company Account",
                tag = "signup",
                isLoading = isLoading,
                onClick = {
                    if (companyName.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    } else if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        companyViewModel.register(email, password) { success, message, companyId ->
                            if (success) {
                                val model = CompanyModel(
                                    companyId = companyId,
                                    companyName = companyName,
                                    companyContactNumber = phoneNumber,
                                    companyEmail = email
                                )
                                companyViewModel.addCompanyToDatabase(companyId, model) { success, message ->
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
                        color = ModernSignUpTheme.DeepBlue,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
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
                        .clip(CircleShape)
                        .background(ModernSignUpTheme.GlassWhite)
                        .border(
                            width = 4.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ModernSignUpTheme.DeepBlue,
                                    ModernSignUpTheme.PrimaryBlue
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
                            text = "Company Created!",
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

@Composable
fun EnhancedCompanyButton(
    text: String,
    isLoading: Boolean,
    tag: String = "",
    onClick: () -> Unit,
    delay: Int = 0
) {
    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ), label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(700, delayMillis = delay), label = ""
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100L)
        startAnimation = true
    }

    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .height(62.dp)
            .scale(scale)
            .alpha(alpha),
        enabled = !isLoading,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 12.dp,
            pressedElevation = 6.dp,
            disabledElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            ModernSignUpTheme.DeepBlue,
                            ModernSignUpTheme.PrimaryBlue,
                            ModernSignUpTheme.AccentBlue
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = ModernSignUpTheme.White,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ModernSignUpTheme.White,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewSignUpCompany() {
    SignUpCompanyBody()
}