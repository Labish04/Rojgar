package com.example.rojgar.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import kotlinx.coroutines.delay

object ModernRegisterTheme {
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
    val JobSeekerGreen = Color(0xFF10B981)
    val CompanyPurple = Color(0xFF8B5CF6)
}

class RegisterAsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegisterBody()
        }
    }
}

@Composable
fun RegisterBody() {
    val context = LocalContext.current
    val activity = context as Activity

    var startAnimation by remember { mutableStateOf(false) }
    var selectedCard by remember { mutableStateOf<String?>(null) }

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
                        ModernRegisterTheme.IceBlue,
                        ModernRegisterTheme.White,
                        ModernRegisterTheme.SurfaceLight,
                        ModernRegisterTheme.SkyBlue
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
                                    ModernRegisterTheme.LightBlue.copy(alpha = 0.4f),
                                    ModernRegisterTheme.PrimaryBlue.copy(alpha = 0.2f)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(ModernRegisterTheme.GlassWhite)
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ModernRegisterTheme.LightBlue,
                                    ModernRegisterTheme.PrimaryBlue,
                                    ModernRegisterTheme.AccentBlue
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.r),
                        contentDescription = "Logo",
                        tint = ModernRegisterTheme.PrimaryBlue,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Welcome Text
            Column(
                modifier = Modifier.alpha(contentAlpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Account",
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ModernRegisterTheme.DeepBlue,
                                ModernRegisterTheme.PrimaryBlue
                            )
                        )
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Choose your account type to get started",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = ModernRegisterTheme.TextSecondary,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Account Type Selection Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Job Seeker Card
                AccountTypeCard(
                    title = "Job Seeker",
                    description = "Find your dream job and grow your career",
                    icon = R.drawable.employee,
                    iconColor = ModernRegisterTheme.JobSeekerGreen,
                    gradientColors = listOf(
                        ModernRegisterTheme.JobSeekerGreen,
                        ModernRegisterTheme.AccentBlue
                    ),
                    isSelected = selectedCard == "JOBSEEKER",
                    onClick = {
                        selectedCard = "JOBSEEKER"

                            val intent = Intent(context, JobseekerSignUpActivity::class.java)
                            context.startActivity(intent)

                    },
                    delay = 200
                )

                // Company Card
                AccountTypeCard(
                    title = "Company",
                    description = "Hire top talent and build your team",
                    icon = R.drawable.office,
                    iconColor = ModernRegisterTheme.CompanyPurple,
                    gradientColors = listOf(
                        ModernRegisterTheme.CompanyPurple,
                        ModernRegisterTheme.PrimaryBlue
                    ),
                    isSelected = selectedCard == "COMPANY",
                    onClick = {
                        selectedCard = "COMPANY"
                            val intent = Intent(context, SignUpCompanyActivity::class.java)
                            context.startActivity(intent)
                    },
                    delay = 400
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    ModernRegisterTheme.SkyBlue
                                )
                            )
                        )
                )
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = TextStyle(
                        color = ModernRegisterTheme.TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    ModernRegisterTheme.SkyBlue,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Sign In Section
            Column(
                modifier = Modifier.alpha(contentAlpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Already have an account?",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = ModernRegisterTheme.TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
                        activity.finish()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        2.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                ModernRegisterTheme.PrimaryBlue,
                                ModernRegisterTheme.LightBlue
                            )
                        )
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ModernRegisterTheme.PrimaryBlue
                    )
                ) {
                    Text(
                        text = "Sign In",
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AccountTypeCard(
    title: String,
    description: String,
    icon: Int,
    iconColor: Color,
    gradientColors: List<Color>,
    isSelected: Boolean,
    onClick: () -> Unit,
    delay: Int = 0
) {
    var startAnimation by remember { mutableStateOf(false) }

    val offsetY by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 30.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ), label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(700, delayMillis = delay), label = ""
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100L)
        startAnimation = true
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .offset(y = offsetY)
            .alpha(alpha)
            .scale(scale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = ModernRegisterTheme.White
        ),
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            brush = if (isSelected) Brush.linearGradient(gradientColors) else Brush.linearGradient(
                colors = listOf(
                    ModernRegisterTheme.GlassBlue,
                    ModernRegisterTheme.GlassBlue
                )
            )
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 12.dp else 6.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                iconColor.copy(alpha = 0.15f),
                                iconColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                iconColor,
                                iconColor.copy(alpha = 0.5f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Text Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernRegisterTheme.TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = ModernRegisterTheme.TextSecondary,
                        lineHeight = 20.sp
                    )
                )
            }

            // Selection Indicator
            Icon(
                painter = painterResource(
                    if (isSelected) R.drawable.outline_arrow_back_ios_24
                    else R.drawable.outline_keyboard_arrow_right_24
                ),
                contentDescription = if (isSelected) "Selected" else "Not Selected",
                tint = if (isSelected) gradientColors.first() else ModernRegisterTheme.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Preview
@Composable
fun PreviewRegister() {
    RegisterBody()
}