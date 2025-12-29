package com.example.rojgar.view

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.R

class CompanyProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompanyProfileBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyProfileBody() {
    var backgroundImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var isFollowing by remember { mutableStateOf(false) }

    val backgroundImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        backgroundImageUri = uri
    }

    val profileImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Enhanced Background with Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                if (backgroundImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(backgroundImageUri),
                        contentDescription = "Background Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { backgroundImagePicker.launch("image/*") },
                        contentScale = ContentScale.Crop
                    )
                    // Darker overlay for better contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF667EEA),
                                        Color(0xFF764BA2),
                                        Color(0xFFF093FB)
                                    )
                                )
                            )
                            .clickable { backgroundImagePicker.launch("image/*") }
                    ) {
                        // Animated floating circles for visual interest
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .offset(x = 50.dp, y = 30.dp)
                                .background(
                                    Color.White.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .offset(x = 280.dp, y = 140.dp)
                                .background(
                                    Color.White.copy(alpha = 0.08f),
                                    CircleShape
                                )
                        )

                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Background",
                            modifier = Modifier
                                .size(56.dp)
                                .align(Alignment.Center),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Glassmorphism Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { /* Navigate back */ },
                        modifier = Modifier
                            .shadow(8.dp, CircleShape)
                            .background(Color.White.copy(alpha = 0.95f), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1F2937)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { /* Share */ },
                            modifier = Modifier
                                .shadow(8.dp, CircleShape)
                                .background(Color.White.copy(alpha = 0.95f), CircleShape)
                                .size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color(0xFF1F2937)
                            )
                        }

                        IconButton(
                            onClick = { /* More options */ },
                            modifier = Modifier
                                .shadow(8.dp, CircleShape)
                                .background(Color.White.copy(alpha = 0.95f), CircleShape)
                                .size(44.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_more_vert_24),
                                contentDescription = "More",
                                tint = Color(0xFF1F2937)
                            )
                        }
                    }
                }
            }

            // Profile Content with elevated design
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-70).dp)
                    .padding(horizontal = 20.dp)
            ) {
                // Enhanced Profile Image with Ring
                Box(
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    // Outer glow ring
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .shadow(16.dp, CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF667EEA),
                                        Color(0xFF764BA2)
                                    )
                                ),
                                CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(5.dp)
                                .clickable { profileImagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(profileImageUri),
                                    contentDescription = "Profile Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF667EEA),
                                                    Color(0xFF764BA2)
                                                )
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "L",
                                        fontSize = 52.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Premium Camera Badge
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF667EEA),
                                        Color(0xFF764BA2)
                                    )
                                )
                            )
                            .border(4.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd)
                            .clickable { profileImagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Company Name with Premium Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Labish",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827),
                        letterSpacing = (-0.5).sp
                    )

                    // Premium Verified Badge
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF3B82F6),
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Verified",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(20.dp)
                        )
                    }

                    // Premium Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Premium",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Enhanced Tagline
                Text(
                    text = "Leading Technology Solutions Provider",
                    fontSize = 15.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Premium Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EnhancedStatCard(
                        label = "Active Jobs",
                        value = "42",
                        icon = Icons.Default.Email,
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedStatCard(
                        label = "Employees",
                        value = "250+",
                        icon = Icons.Default.Face,
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedStatCard(
                        label = "Founded",
                        value = "2015",
                        icon = Icons.Default.DateRange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Premium Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { isFollowing = !isFollowing },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) Color(0xFF10B981) else Color(0xFF6366F1)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = if (isFollowing) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFollowing) "Following" else "Follow",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { /* Message */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6366F1),
                            containerColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Message",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // About Section with gradient accent
                EnhancedInfoSection(title = "About Company") {
                    Text(
                        text = "Labish is a pioneering technology company dedicated to delivering innovative solutions that transform businesses. With a team of experienced professionals, we specialize in software development, cloud solutions, and digital transformation.",
                        fontSize = 15.sp,
                        color = Color(0xFF374151),
                        lineHeight = 24.sp,
                        letterSpacing = 0.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Company Details with icons
                EnhancedInfoSection(title = "Company Information") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        EnhancedDetailRow(
                            icon = Icons.Default.Email,
                            label = "Industry",
                            value = "Information Technology",
                            gradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                        )
                        EnhancedDetailRow(
                            icon = Icons.Default.LocationOn,
                            label = "Headquarters",
                            value = "Kathmandu, Nepal",
                            gradient = listOf(Color(0xFFEC4899), Color(0xFFF43F5E))
                        )
                        EnhancedDetailRow(
                            icon = Icons.Default.Phone,
                            label = "Phone",
                            value = "+977 1-234567",
                            gradient = listOf(Color(0xFF10B981), Color(0xFF059669))
                        )
                        EnhancedDetailRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = "info@labish.com",
                            gradient = listOf(Color(0xFFF59E0B), Color(0xFFEAB308))
                        )
                        EnhancedDetailRow(
                            icon = Icons.Default.PlayArrow,
                            label = "Website",
                            value = "www.labish.com",
                            gradient = listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Specialties with gradient chips
                EnhancedInfoSection(title = "Core Specialties") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            "Software Development" to listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                            "Cloud Solutions" to listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
                            "AI & ML" to listOf(Color(0xFFEC4899), Color(0xFFF43F5E)),
                            "Mobile Apps" to listOf(Color(0xFF10B981), Color(0xFF059669)),
                            "Web Development" to listOf(Color(0xFFF59E0B), Color(0xFFEAB308)),
                            "Consulting" to listOf(Color(0xFF8B5CF6), Color(0xFFA855F7))
                        ).forEach { (specialty, gradient) ->
                            GradientSpecialtyChip(text = specialty, gradient = gradient)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun EnhancedStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFEEF2FF),
                                Color(0xFFE0E7FF)
                            )
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EnhancedInfoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFF8B5CF6)
                                )
                            ),
                            RoundedCornerShape(2.dp)
                        )
                )
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827),
                    letterSpacing = (-0.3).sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun EnhancedDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    gradient: List<Color>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                color = Color(0xFF111827),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GradientSpecialtyChip(text: String, gradient: List<Color>) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 3.dp,
        modifier = Modifier.wrapContentWidth()
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradient.map { it.copy(alpha = 0.15f) }))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(gradient),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                fontSize = 13.sp,
                color = gradient[0],
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }
    }
}