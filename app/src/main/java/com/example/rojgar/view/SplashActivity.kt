package com.example.rojgar.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashBody()
        }
    }
}

data class Particle(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val targetX: Float,
    val targetY: Float,
    val size: Float,
    val color: Color
)

@Composable
fun SplashBody() {
    val context = LocalContext.current
    val activity = context as Activity

    // Animation states
    var startAnimation by remember { mutableStateOf(false) }

    // Logo animations
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )

    val logoRotation by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -180f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "logoRotation"
    )

    // Text animations
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 400),
        label = "textAlpha"
    )

    val textOffset by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 50.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "textOffset"
    )

    // Infinite animations
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val circleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "circleRotation"
    )

    // Particle system
    val particles = remember {
        List(30) { index ->
            val angle = (index * 12f) * (Math.PI / 180f)
            val radius = 300f
            Particle(
                id = index,
                startX = 0f,
                startY = 0f,
                targetX = cos(angle).toFloat() * radius,
                targetY = sin(angle).toFloat() * radius,
                size = Random.nextFloat() * 4f + 2f,
                color = listOf(
                    ModernLoginTheme.LightBlue,
                    ModernLoginTheme.SkyBlue,
                    ModernLoginTheme.AccentBlue,
                    ModernLoginTheme.IceBlue
                ).random()
            )
        }
    }

    val particleProgress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "particles"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000)
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
        activity.finish()
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ModernLoginTheme.DeepBlue,
                            ModernLoginTheme.PrimaryBlue,
                            ModernLoginTheme.LightBlue
                        )
                    )
                )
        ) {
            // Animated background circles
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(circleRotation)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2

                // Outer circles
                for (i in 0..2) {
                    val radius = (300f + i * 150f)
                    drawCircle(
                        color = ModernLoginTheme.GlassBlue,
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2f)
                    )
                }
            }

            // Particle system
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val centerY = size.height / 2

                particles.forEach { particle ->
                    val currentX = centerX + (particle.targetX * particleProgress)
                    val currentY = centerY + (particle.targetY * particleProgress)
                    val alpha = particleProgress * 0.6f

                    drawCircle(
                        color = particle.color.copy(alpha = alpha),
                        radius = particle.size,
                        center = Offset(currentX, currentY)
                    )
                }
            }

            // Shimmer effect overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gradient = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        ModernLoginTheme.White.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    start = Offset(shimmerOffset - 500f, shimmerOffset - 500f),
                    end = Offset(shimmerOffset, shimmerOffset)
                )

                drawRect(brush = gradient)
            }

            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo with animations
                Box(
                    modifier = Modifier
                        .scale(logoScale * pulseScale)
                        .alpha(logoAlpha)
                        .rotate(logoRotation)
                ) {
                    // Glow effect behind logo
                    Canvas(modifier = Modifier.size(420.dp)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ModernLoginTheme.AccentBlue.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.width / 2
                        )
                    }

                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(400.dp)
                            .align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Animated tagline
                Column(
                    modifier = Modifier
                        .offset(y = textOffset)
                        .alpha(textAlpha),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Rojgar",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernLoginTheme.White,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Find Your Dream Career",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = ModernLoginTheme.IceBlue,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))

                // Loading indicator
                LoadingDots(
                    modifier = Modifier.alpha(textAlpha),
                    color = ModernLoginTheme.White
                )
            }
        }
    }
}

@Composable
fun LoadingDots(
    modifier: Modifier = Modifier,
    color: Color = ModernLoginTheme.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0..2) {
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = i * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$i"
            )

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(scale)
                    .background(
                        color = color,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}