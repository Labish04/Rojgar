package com.example.rojgar.view

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.rojgar.R
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.utils.FCMTokenManager
import com.example.rojgar.utils.RememberMeManager
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlin.math.sin

// Enhanced Theme Colors with Glassmorphism
object ModernLoginTheme {
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
}

class LoginActivity : ComponentActivity() {

    private var selectedUserType: String = "JOBSEEKER"
    private lateinit var rememberMeManager: RememberMeManager
    private var pendingUserId: String? = null
    private var pendingUserType: String? = null

    private val jobSeekerViewModel by lazy { JobSeekerViewModel(JobSeekerRepoImpl()) }
    private val companyViewModel by lazy { CompanyViewModel(CompanyRepoImpl()) }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "Notification permission granted")
            pendingUserId?.let { userId ->
                pendingUserType?.let { userType ->
                    FCMTokenManager.registerFCMToken(userId, userType)
                }
            }
        } else {
            Log.d("FCM", "Notification permission denied")
            Toast.makeText(
                this,
                "Enable notifications to receive job alerts and updates",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    val idToken = it.idToken
                    val fullName = it.displayName ?: ""
                    val email = it.email ?: ""
                    val photoUrl = it.photoUrl?.toString() ?: ""

                    idToken?.let { token ->
                        handleGoogleSignInWithSelectedType(token, fullName, email, photoUrl)
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        rememberMeManager = RememberMeManager(this)

        setContent {
            ModernLoginScreen(
                rememberMeManager = rememberMeManager,
                jobSeekerViewModel = jobSeekerViewModel,
                companyViewModel = companyViewModel,
                onGoogleSignInClick = { userType -> startGoogleSignIn(userType) },
                onLoginSuccess = { userId, userType ->
                    requestNotificationPermissionAndRegisterToken(userId, userType)
                    logFCMToken(userId)
                }
            )
        }
    }

    private fun handleGoogleSignInWithSelectedType(
        idToken: String,
        fullName: String,
        email: String,
        photoUrl: String
    ) {
        if (selectedUserType == "JOBSEEKER") {
            jobSeekerViewModel.signInWithGoogle(idToken, fullName, email, photoUrl) { success, message, userId ->
                if (success && userId != null) {
                    requestNotificationPermissionAndRegisterToken(userId, "JOBSEEKER")
                    logFCMToken(userId)
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, JobSeekerDashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            companyViewModel.signInWithGoogle(idToken, fullName, email, photoUrl) { success, message, userId ->
                if (success && userId != null) {
                    requestNotificationPermissionAndRegisterToken(userId, "COMPANY")
                    logFCMToken(userId)
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, CompanyDashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun startGoogleSignIn(userType: String) {
        selectedUserType = userType
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun requestNotificationPermissionAndRegisterToken(userId: String, userType: String) {
        pendingUserId = userId
        pendingUserType = userType

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    FCMTokenManager.registerFCMToken(userId, userType)
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            FCMTokenManager.registerFCMToken(userId, userType)
        }
    }

    private fun logFCMToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_DEBUG", "Token for $userId: $token")
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("FCM Token", token)
                clipboard.setPrimaryClip(clip)
            }
        }
    }
}

@Composable
fun ModernLoginScreen(
    rememberMeManager: RememberMeManager? = null,
    jobSeekerViewModel: JobSeekerViewModel,
    companyViewModel: CompanyViewModel,
    onGoogleSignInClick: (String) -> Unit = {},
    onLoginSuccess: (userId: String, userType: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var showReactivationDialog by remember { mutableStateOf(false) }
    var pendingReactivationUserId by remember { mutableStateOf<String?>(null) }
    var pendingUserType by remember { mutableStateOf<String?>(null) }

    // Enhanced Animations
    var startAnimation by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    val logoRotation by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -180f,
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
        rememberMeManager?.let { manager ->
            if (manager.isRememberMeEnabled()) {
                email = manager.getSavedEmail() ?: ""
                password = manager.getSavedPassword() ?: ""
                rememberMe = true
            }
        }
        delay(150)
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ModernLoginTheme.IceBlue,
                        ModernLoginTheme.White,
                        ModernLoginTheme.SurfaceLight,
                        ModernLoginTheme.SkyBlue
                    )
                )
            )
    ) {
        EnhancedAnimatedBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Enhanced Logo Section with Glassmorphism
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .rotate(logoRotation)
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
                                    ModernLoginTheme.LightBlue.copy(alpha = 0.4f),
                                    ModernLoginTheme.PrimaryBlue.copy(alpha = 0.2f)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(ModernLoginTheme.GlassWhite)
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ModernLoginTheme.LightBlue,
                                    ModernLoginTheme.PrimaryBlue,
                                    ModernLoginTheme.AccentBlue
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.r),
                        contentDescription = "Logo",
                        tint = ModernLoginTheme.PrimaryBlue,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Enhanced Welcome Text
            Column(
                modifier = Modifier.alpha(contentAlpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome Back!",
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ModernLoginTheme.DeepBlue,
                                ModernLoginTheme.PrimaryBlue
                            )
                        )
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sign in to continue your journey",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = ModernLoginTheme.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Glass Card Container
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha)
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

                    Spacer(modifier = Modifier.height(20.dp))

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

                    Spacer(modifier = Modifier.height(20.dp))

                    // Remember Me & Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = ModernLoginTheme.PrimaryBlue,
                                    uncheckedColor = ModernLoginTheme.TextSecondary
                                )
                            )
                            Text(
                                text = "Remember me",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = ModernLoginTheme.TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Text(
                            text = "Forgot Password?",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = ModernLoginTheme.PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                context.startActivity(Intent(context, ForgetPasswordActivity::class.java))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Enhanced Login Button
            EnhancedButton(
                text = "Sign In",
                isLoading = isLoading,
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        findUserTypeByEmail(
                            email = email,
                            onUserTypeFound = { userType ->
                                when (userType) {
                                    "JOBSEEKER" -> {
                                        jobSeekerViewModel.login(email, password) { success, message ->
                                            isLoading = false
                                            if (success) {
                                                rememberMeManager?.saveCredentials(email, password, rememberMe)
                                                val currentUser = jobSeekerViewModel.getCurrentJobSeeker()
                                                if (currentUser != null) {
                                                    jobSeekerViewModel.checkAccountStatus(currentUser.uid) { isActive, statusMessage ->
                                                        if (!isActive && statusMessage == "Account is deactivated") {
                                                            pendingReactivationUserId = currentUser.uid
                                                            pendingUserType = "JOBSEEKER"
                                                            showReactivationDialog = true
                                                        } else {
                                                            onLoginSuccess(currentUser.uid, "JOBSEEKER")
                                                            Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                                                            val intent = Intent(context, JobSeekerDashboardActivity::class.java)
                                                            context.startActivity(intent)
                                                            activity?.finish()
                                                        }
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                    "COMPANY" -> {
                                        companyViewModel.login(email, password) { success, message ->
                                            isLoading = false
                                            if (success) {
                                                rememberMeManager?.saveCredentials(email, password, rememberMe)
                                                val currentUser = companyViewModel.getCurrentCompany()
                                                if (currentUser != null) {
                                                    companyViewModel.checkAccountStatus(currentUser.uid) { isActive, statusMessage ->
                                                        if (!isActive && statusMessage == "Account is deactivated") {
                                                            pendingReactivationUserId = currentUser.uid
                                                            pendingUserType = "COMPANY"
                                                            showReactivationDialog = true
                                                        } else {
                                                            onLoginSuccess(currentUser.uid, "COMPANY")
                                                            Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                                                            val intent = Intent(context, CompanyDashboardActivity::class.java)
                                                            context.startActivity(intent)
                                                            activity?.finish()
                                                        }
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                    else -> {
                                        isLoading = false
                                        Toast.makeText(context, "Email not found", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onError = { errorMessage ->
                                isLoading = false
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                delay = 600
            )

            Spacer(modifier = Modifier.height(28.dp))

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
                                    ModernLoginTheme.SkyBlue
                                )
                            )
                        )
                )
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = TextStyle(
                        color = ModernLoginTheme.TextSecondary,
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
                                    ModernLoginTheme.SkyBlue,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Enhanced Google Sign In Button
            EnhancedGoogleButton(
                onClick = { showGoogleDialog = true },
                delay = 700
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Sign Up Link
            Row(
                modifier = Modifier.alpha(contentAlpha),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = ModernLoginTheme.TextSecondary
                    )
                )
                Text(
                    text = "Sign Up",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = ModernLoginTheme.PrimaryBlue,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        context.startActivity(Intent(context, RegisterAsActivity::class.java))
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showGoogleDialog) {
        ModernGoogleSignInDialog(
            onDismiss = { showGoogleDialog = false },
            onUserTypeSelected = { userType ->
                showGoogleDialog = false
                onGoogleSignInClick(userType)
            }
        )
    }

    if (showReactivationDialog) {
        ModernReactivationDialog(
            onDismiss = {
                showReactivationDialog = false
                pendingReactivationUserId = null
                pendingUserType = null
                when (pendingUserType) {
                    "JOBSEEKER" -> jobSeekerViewModel.logout("") { _, _ -> }
                    "COMPANY" -> companyViewModel.logout("") { _, _ -> }
                }
            },
            onConfirm = {
                pendingReactivationUserId?.let { userId ->
                    when (pendingUserType) {
                        "JOBSEEKER" -> {
                            jobSeekerViewModel.reactivateAccount(userId) { success, message ->
                                if (success) {
                                    Toast.makeText(context, "Account reactivated!", Toast.LENGTH_LONG).show()
                                    showReactivationDialog = false
                                    val intent = Intent(context, JobSeekerDashboardActivity::class.java)
                                    context.startActivity(intent)
                                    activity?.finish()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    showReactivationDialog = false
                                }
                            }
                        }
                        "COMPANY" -> {
                            companyViewModel.reactivateAccount(userId) { success, message ->
                                if (success) {
                                    Toast.makeText(context, "Account reactivated!", Toast.LENGTH_LONG).show()
                                    showReactivationDialog = false
                                    val intent = Intent(context, CompanyDashboardActivity::class.java)
                                    context.startActivity(intent)
                                    activity?.finish()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    showReactivationDialog = false
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun EnhancedAnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Floating orbs
        Box(
            modifier = Modifier
                .offset(
                    x = (sin(Math.toRadians(wave1.toDouble())) * 60).dp,
                    y = 120.dp + (sin(Math.toRadians(wave1.toDouble() * 0.5)) * 40).dp
                )
                .size(220.dp)
                .blur(60.dp)
                .background(
                    ModernLoginTheme.LightBlue.copy(alpha = 0.25f),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(
                    x = (sin(Math.toRadians(wave2.toDouble())) * 50).dp,
                    y = (-40).dp + (sin(Math.toRadians(wave2.toDouble() * 0.7)) * 30).dp
                )
                .size(200.dp)
                .blur(50.dp)
                .background(
                    ModernLoginTheme.AccentBlue.copy(alpha = 0.2f),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(
                    x = (sin(Math.toRadians(wave3.toDouble())) * 40).dp,
                    y = 80.dp + (sin(Math.toRadians(wave3.toDouble() * 0.6)) * 35).dp
                )
                .size(240.dp)
                .blur(70.dp)
                .background(
                    ModernLoginTheme.PrimaryBlue.copy(alpha = 0.18f),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(
                    x = (sin(Math.toRadians(wave1.toDouble() * 1.3)) * 45).dp,
                    y = 60.dp + (sin(Math.toRadians(wave1.toDouble() * 0.4)) * 30).dp
                )
                .size(180.dp)
                .blur(55.dp)
                .background(
                    ModernLoginTheme.SkyBlue.copy(alpha = 0.3f),
                    CircleShape
                )
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = ModernLoginTheme.GlassWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            ModernLoginTheme.White.copy(alpha = 0.8f),
                            ModernLoginTheme.LightBlue.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            content()
        }
    }
}

@Composable
fun EnhancedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: Int,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    delay: Int = 0
) {
    var startAnimation by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

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

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) ModernLoginTheme.PrimaryBlue else ModernLoginTheme.SkyBlue,
        animationSpec = tween(300), label = ""
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100L)
        startAnimation = true
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                style = TextStyle(
                    fontWeight = FontWeight.Medium
                )
            )
        },
        visualTransformation = if (isPassword && !showPassword)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFocused) ModernLoginTheme.PrimaryBlue.copy(alpha = 0.1f)
                        else ModernLoginTheme.SkyBlue.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(leadingIcon),
                    contentDescription = null,
                    tint = if (isFocused) ModernLoginTheme.PrimaryBlue else ModernLoginTheme.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onPasswordToggle) {
                    Icon(
                        painter = painterResource(
                            if (showPassword) R.drawable.baseline_visibility_24
                            else R.drawable.baseline_visibility_off_24
                        ),
                        contentDescription = null,
                        tint = if (isFocused) ModernLoginTheme.PrimaryBlue else ModernLoginTheme.TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = offsetY)
            .alpha(alpha),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ModernLoginTheme.White,
            unfocusedContainerColor = ModernLoginTheme.IceBlue.copy(alpha = 0.3f),
            focusedBorderColor = ModernLoginTheme.PrimaryBlue,
            unfocusedBorderColor = borderColor,
            focusedLabelColor = ModernLoginTheme.PrimaryBlue,
            unfocusedLabelColor = ModernLoginTheme.TextSecondary,
            focusedTextColor = ModernLoginTheme.TextPrimary,
            unfocusedTextColor = ModernLoginTheme.TextPrimary
        ),
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
fun EnhancedButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    delay: Int = 0
) {
    var startAnimation by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = when {
            !startAnimation -> 0.8f
            isPressed -> 0.95f
            else -> 1f
        },
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

    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
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
                            ModernLoginTheme.DeepBlue,
                            ModernLoginTheme.PrimaryBlue,
                            ModernLoginTheme.AccentBlue
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = ModernLoginTheme.White,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ModernLoginTheme.White,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@Composable
fun EnhancedGoogleButton(
    onClick: () -> Unit,
    delay: Int = 0
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(700, delayMillis = delay), label = ""
    )

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
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
            .height(62.dp)
            .alpha(alpha)
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ModernLoginTheme.White
        ),
        border = BorderStroke(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    ModernLoginTheme.SkyBlue,
                    ModernLoginTheme.LightBlue.copy(alpha = 0.5f)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.google),
                contentDescription = "Google",
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Continue with Google",
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ModernLoginTheme.TextPrimary
                )
            )
        }
    }
}

@Composable
fun ModernGoogleSignInDialog(
    onDismiss: () -> Unit,
    onUserTypeSelected: (String) -> Unit
) {
    var selectedType by remember { mutableStateOf("JOBSEEKER") }
    var showAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showAnimation = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        AnimatedVisibility(
            visible = showAnimation,
            enter = fadeIn(tween(400)) + scaleIn(
                initialScale = 0.7f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
            exit = fadeOut(tween(300)) + scaleOut(targetScale = 0.8f)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ModernLoginTheme.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 16.dp
                )
            ) {
                Box {
                    // Decorative gradient bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        ModernLoginTheme.DeepBlue,
                                        ModernLoginTheme.PrimaryBlue,
                                        ModernLoginTheme.LightBlue,
                                        ModernLoginTheme.AccentBlue
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Google Icon with animation
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            ModernLoginTheme.IceBlue,
                                            ModernLoginTheme.SkyBlue.copy(alpha = 0.5f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.google),
                                contentDescription = "Google",
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Sign in with Google",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        ModernLoginTheme.DeepBlue,
                                        ModernLoginTheme.PrimaryBlue
                                    )
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Choose your account type to continue",
                            style = TextStyle(
                                fontSize = 15.sp,
                                color = ModernLoginTheme.TextSecondary,
                                fontWeight = FontWeight.Medium
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        ModernUserTypeOption(
                            title = "Job Seeker",
                            description = "Find your dream job",
                            icon = R.drawable.profileemptypic,
                            isSelected = selectedType == "JOBSEEKER",
                            onClick = { selectedType = "JOBSEEKER" }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ModernUserTypeOption(
                            title = "Company",
                            description = "Hire top talent",
                            icon = R.drawable.profileemptypic,
                            isSelected = selectedType == "COMPANY",
                            onClick = { selectedType = "COMPANY" }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(2.dp, ModernLoginTheme.SkyBlue)
                            ) {
                                Text(
                                    text = "Cancel",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ModernLoginTheme.TextSecondary
                                    )
                                )
                            }

                            Button(
                                onClick = { onUserTypeSelected(selectedType) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(0.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 8.dp
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    ModernLoginTheme.DeepBlue,
                                                    ModernLoginTheme.PrimaryBlue
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Continue",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ModernLoginTheme.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernUserTypeOption(
    title: String,
    description: String,
    icon: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                ModernLoginTheme.IceBlue
            else
                ModernLoginTheme.White
        ),
        border = BorderStroke(
            width = if (isSelected) 3.dp else 2.dp,
            brush = if (isSelected) {
                Brush.linearGradient(
                    colors = listOf(
                        ModernLoginTheme.PrimaryBlue,
                        ModernLoginTheme.AccentBlue
                    )
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        ModernLoginTheme.SkyBlue,
                        ModernLoginTheme.SkyBlue
                    )
                )
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = ModernLoginTheme.PrimaryBlue,
                    unselectedColor = ModernLoginTheme.TextSecondary
                )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected)
                            Brush.radialGradient(
                                colors = listOf(
                                    ModernLoginTheme.PrimaryBlue.copy(alpha = 0.2f),
                                    ModernLoginTheme.LightBlue.copy(alpha = 0.1f)
                                )
                            )
                        else
                            Brush.radialGradient(
                                colors = listOf(
                                    ModernLoginTheme.SkyBlue.copy(alpha = 0.5f),
                                    ModernLoginTheme.IceBlue
                                )
                            )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = title,
                    tint = if (isSelected) ModernLoginTheme.PrimaryBlue else ModernLoginTheme.TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) ModernLoginTheme.DeepBlue else ModernLoginTheme.TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = ModernLoginTheme.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ModernLoginTheme.PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_visibility_24),
                        contentDescription = "Selected",
                        tint = ModernLoginTheme.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernReactivationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var showAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showAnimation = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        AnimatedVisibility(
            visible = showAnimation,
            enter = fadeIn(tween(400)) + scaleIn(
                initialScale = 0.7f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ModernLoginTheme.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 20.dp
                )
            ) {
                Box {
                    // Decorative top gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        ModernLoginTheme.DeepBlue,
                                        ModernLoginTheme.PrimaryBlue,
                                        ModernLoginTheme.LightBlue,
                                        ModernLoginTheme.AccentBlue
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icon with pulsing animation
                        val infiniteTransition = rememberInfiniteTransition(label = "")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = ""
                        )

                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            ModernLoginTheme.LightBlue.copy(alpha = 0.3f),
                                            ModernLoginTheme.PrimaryBlue.copy(alpha = 0.1f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_visibility_24),
                                contentDescription = null,
                                tint = ModernLoginTheme.PrimaryBlue,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = "Account Deactivated",
                            style = TextStyle(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        ModernLoginTheme.DeepBlue,
                                        ModernLoginTheme.PrimaryBlue
                                    )
                                )
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Your account has been deactivated. Would you like to reactivate it and continue?",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = ModernLoginTheme.TextSecondary,
                                lineHeight = 24.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(36.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(2.dp, ModernLoginTheme.SkyBlue),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ModernLoginTheme.TextSecondary
                                )
                            ) {
                                Text(
                                    text = "Cancel",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Button(
                                onClick = onConfirm,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(0.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 10.dp,
                                    pressedElevation = 4.dp
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    ModernLoginTheme.DeepBlue,
                                                    ModernLoginTheme.PrimaryBlue,
                                                    ModernLoginTheme.AccentBlue
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Reactivate",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ModernLoginTheme.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to find user type by email
fun findUserTypeByEmail(
    email: String,
    onUserTypeFound: (String) -> Unit,
    onError: (String) -> Unit
) {
    val database = FirebaseDatabase.getInstance()
    val normalizedEmail = email.lowercase().trim()

    // Check JobSeekers first
    database.getReference("JobSeekers")
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (jobSeekerSnapshot in snapshot.children) {
                        val jobSeekerEmail = jobSeekerSnapshot.child("email")
                            .getValue(String::class.java)?.lowercase()?.trim()
                        if (jobSeekerEmail == normalizedEmail) {
                            onUserTypeFound("JOBSEEKER")
                            return
                        }
                    }
                }

                // If not found in JobSeekers, check Companies
                database.getReference("Companys")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(companySnapshot: DataSnapshot) {
                            if (companySnapshot.exists()) {
                                for (company in companySnapshot.children) {
                                    val companyEmail = company.child("companyEmail")
                                        .getValue(String::class.java)?.lowercase()?.trim()
                                    if (companyEmail == normalizedEmail) {
                                        onUserTypeFound("COMPANY")
                                        return
                                    }
                                }
                            }
                            onUserTypeFound("NOT_FOUND")
                        }

                        override fun onCancelled(error: DatabaseError) {
                            onError(error.message)
                        }
                    })
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        })
}
