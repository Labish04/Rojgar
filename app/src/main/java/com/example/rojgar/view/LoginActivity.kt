package com.example.rojgar.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rojgar.R
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.NormalBlue
import com.example.rojgar.ui.theme.Purple
import com.example.rojgar.ui.theme.White
import com.example.rojgar.viewmodel.CompanyViewModel
import com.example.rojgar.viewmodel.JobSeekerViewModel
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.animateColorAsState

class LoginActivity : ComponentActivity() {

    private var selectedUserType: String = "JOBSEEKER"

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
                        handleGoogleSignIn(token, fullName, email, photoUrl)
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleGoogleSignIn(idToken: String, fullName: String, email: String, photoUrl: String) {
        val auth = FirebaseAuth.getInstance()
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        checkIfUserExistsAndProceed(
                            uid = user.uid,
                            email = email,
                            fullName = fullName,
                            photoUrl = photoUrl
                        )
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkIfUserExistsAndProceed(uid: String, email: String, fullName: String, photoUrl: String) {
        val database = FirebaseDatabase.getInstance()

        if (selectedUserType == "JOBSEEKER") {
            val jobSeekerRef = database.getReference("JobSeekers").child(uid)

            jobSeekerRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val isActive = snapshot.child("isActive").getValue(Boolean::class.java) ?: true

                    if (isActive) {
                        Toast.makeText(this, "Login Successful as JobSeeker", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, JobSeekerDashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Account is deactivated. Please contact support.", Toast.LENGTH_LONG).show()
                        FirebaseAuth.getInstance().signOut()
                    }
                } else {
                    createNewJobSeekerAccount(uid, email, fullName, photoUrl)
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error checking user: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            val companyRef = database.getReference("Companies").child(uid)

            companyRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val isActive = snapshot.child("isActive").getValue(Boolean::class.java) ?: true

                    if (isActive) {
                        Toast.makeText(this, "Login Successful as Company", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, CompanyDashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Account is deactivated. Please contact support.", Toast.LENGTH_LONG).show()
                        FirebaseAuth.getInstance().signOut()
                    }
                } else {
                    createNewCompanyAccount(uid, email, fullName, photoUrl)
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error checking company: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createNewJobSeekerAccount(uid: String, email: String, fullName: String, photoUrl: String) {
        val database = FirebaseDatabase.getInstance()
        val jobSeekerRef = database.getReference("JobSeekers").child(uid)

        val userName = if (fullName.isNotEmpty()) fullName else email.substringBefore("@")
        val jobSeekerData = hashMapOf(
            "uid" to uid,
            "userName" to userName,
            "fullName" to fullName,
            "email" to email,
            "photoUrl" to photoUrl,
            "userType" to "JOBSEEKER",
            "isActive" to true,
            "createdAt" to System.currentTimeMillis(),
            "authProvider" to "google"
        )

        jobSeekerRef.setValue(jobSeekerData)
            .addOnSuccessListener {
                Toast.makeText(this, "JobSeeker account created successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, JobSeekerDashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create account: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun createNewCompanyAccount(uid: String, email: String, fullName: String, photoUrl: String) {
        val database = FirebaseDatabase.getInstance()
        val companyRef = database.getReference("Companies").child(uid)

        val companyName = if (fullName.isNotEmpty()) fullName else email.substringBefore("@")
        val companyData = hashMapOf(
            "uid" to uid,
            "companyName" to companyName,
            "fullName" to fullName,
            "email" to email,
            "photoUrl" to photoUrl,
            "userType" to "COMPANY",
            "isActive" to true,
            "createdAt" to System.currentTimeMillis(),
            "authProvider" to "google"
        )

        companyRef.setValue(companyData)
            .addOnSuccessListener {
                Toast.makeText(this, "Company account created successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CompanyDashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create company account: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    fun startGoogleSignIn(userType: String) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginBody(
                onGoogleSignInClick = { userType -> startGoogleSignIn(userType) }
            )
        }
    }
}

@Composable
fun LoginBody(
    onGoogleSignInClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val companyViewModel = remember { CompanyViewModel(CompanyRepoImpl()) }

    var showReactivationDialog by remember { mutableStateOf(false) }
    var pendingReactivationUserId by remember { mutableStateOf<String?>(null) }
    var pendingUserType by remember { mutableStateOf<String?>(null) }
    var showGoogleSignInDialog by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .background(color = Color.White)
        ) {
            Box {
                Image(
                    painter = painterResource(R.drawable.design1),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = -80.dp, y = -80.dp)
                        .rotate(10f)
                        .size(250.dp)
                )
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 220.dp, y = -60.dp)
                        .size(300.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.mandesign1),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(y = 120.dp)
                            .size(200.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column {
                    Text(
                        "Welcome to Rojgar",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        "Find your dream job or hire top talent.",
                        style = TextStyle(fontSize = 12.sp),
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.padding(horizontal = 30.dp)
            ) {
                LoginTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    leadingIcon = R.drawable.email,
                    isPassword = false
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.padding(horizontal = 30.dp)
            ) {
                LoginTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leadingIcon = R.drawable.outline_lock_24,
                    isPassword = true
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        modifier = Modifier.offset(y = -5.dp)
                    )

                    Text(
                        text = "Remember me.",
                        style = TextStyle(fontSize = 18.sp),
                        modifier = Modifier.offset(y = -5.dp)
                    )
                }
                Spacer(modifier = Modifier.width(45.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "Forget Password?",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = Purple
                        ),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val intent = Intent(context, ForgetPasswordActivity::class.java)
                            context.startActivity(intent)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        if (email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(context, "Email and password required", Toast.LENGTH_SHORT).show()
                        } else {
                            FindUserTypeByEmail(
                                email = email,
                                onUserTypeFound = { userType ->
                                    when (userType) {
                                        "JOBSEEKER" -> {
                                            jobSeekerViewModel.login(email, password) { success, message ->
                                                if (success) {
                                                    val currentUser = jobSeekerViewModel.getCurrentJobSeeker()
                                                    if (currentUser != null) {
                                                        jobSeekerViewModel.checkAccountStatus(currentUser.uid) { isActive, statusMessage ->
                                                            if (!isActive && statusMessage == "Account is deactivated") {
                                                                pendingReactivationUserId = currentUser.uid
                                                                pendingUserType = "JOBSEEKER"
                                                                showReactivationDialog = true
                                                            } else {
                                                                Toast.makeText(context, "Login Successful as JobSeeker", Toast.LENGTH_SHORT).show()
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
                                                if (success) {
                                                    val currentUser = companyViewModel.getCurrentCompany()
                                                    if (currentUser != null) {
                                                        companyViewModel.checkAccountStatus(currentUser.uid) { isActive, statusMessage ->
                                                            if (!isActive && statusMessage == "Account is deactivated") {
                                                                pendingReactivationUserId = currentUser.uid
                                                                pendingUserType = "COMPANY"
                                                                showReactivationDialog = true
                                                            } else {
                                                                Toast.makeText(context, "Login Successful as Company", Toast.LENGTH_SHORT).show()
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
                                            Toast.makeText(
                                                context,
                                                "Email not found. Please check and try again.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                onError = { errorMessage ->
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                ) {
                    Text(
                        "Login",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Don't have an account?",
                    style = TextStyle(fontSize = 18.sp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    "SignUp",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Purple
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val intent = Intent(context, RegisterAsActivity::class.java)
                        context.startActivity(intent)
                    },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("OR", modifier = Modifier.padding(horizontal = 15.dp))
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        showGoogleSignInDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.google),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(horizontal = 10.dp)
                        )
                        Text(
                            "Login with Google",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Image(
                    painter = painterResource(R.drawable.design2),
                    contentDescription = null,
                    modifier = Modifier
                        .size(500.dp)
                        .offset(x = 160.dp, y = 40.dp)
                )
            }
        }
    }

    if (showReactivationDialog) {
        ReactivationDialog(
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
                                    Toast.makeText(
                                        context,
                                        "Account reactivated successfully!",
                                        Toast.LENGTH_LONG
                                    ).show()

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
                                    Toast.makeText(
                                        context,
                                        "Company account reactivated successfully!",
                                        Toast.LENGTH_LONG
                                    ).show()

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

    if (showGoogleSignInDialog) {
        GoogleSignInTypeDialog(
            onDismiss = { showGoogleSignInDialog = false },
            onUserTypeSelected = { userType ->
                showGoogleSignInDialog = false
                (activity as? LoginActivity)?.startGoogleSignIn(userType)
            }
        )
    }
}

@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: Int,
    isPassword: Boolean = false
) {
    var visibility by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (isPassword && !visibility) PasswordVisualTransformation()
        else VisualTransformation.None,
        leadingIcon = {
            Icon(
                painter = painterResource(id = leadingIcon),
                contentDescription = null,
                tint = NormalBlue,
                modifier = Modifier.size(22.dp)
            )
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { visibility = !visibility }) {
                    Icon(
                        painter = painterResource(
                            id = if (visibility)
                                R.drawable.baseline_visibility_off_24
                            else R.drawable.baseline_visibility_24
                        ),
                        contentDescription = null,
                        tint = NormalBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            focusedIndicatorColor = NormalBlue,
            unfocusedIndicatorColor = NormalBlue
        )
    )
}
@Composable
fun ReactivationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val primaryBlue = Color(0xFF2563EB)
    val lightBlue = Color(0xFF60A5FA)
    val darkBlue = Color(0xFF1E40AF)
    val surfaceBlue = Color(0xFFEFF6FF)
    val accentBlue = Color(0xFF3B82F6)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(lightBlue, primaryBlue, darkBlue)
                            )
                        )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    lightBlue.copy(alpha = 0.2f),
                                    primaryBlue.copy(alpha = 0.15f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_visibility_24),
                        contentDescription = null,
                        tint = primaryBlue,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Account Deactivated",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkBlue
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your account has been deactivated. Would you like to reactivate it and continue?",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                        lineHeight = 24.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(surfaceBlue)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_visibility_24),
                            contentDescription = null,
                            tint = accentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Reactivating will restore full access to all your account features.",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = darkBlue,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = primaryBlue
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryBlue
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_visibility_24),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reactivate",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview
@Composable
fun GreetingPreview2() {
    LoginBody()
}
@Composable
fun GoogleSignInTypeDialog(
    onDismiss: () -> Unit,
    onUserTypeSelected: (String) -> Unit
) {
    var selectedType by remember { mutableStateOf("JOBSEEKER") }

    val primaryBlue = Color(0xFF1A73E8)
    val lightBlue = Color(0xFF4285F4)
    val darkBlue = Color(0xFF174EA6)
    val surfaceBlue = Color(0xFFE8F0FE)
    val accentBlue = Color(0xFF1967D2)
    val textGray = Color(0xFF5F6368)
    val dividerColor = Color(0xFFDADCE0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 28.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(surfaceBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.google),
                        contentDescription = "Google",
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = "Sign in with Google",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.W500,
                        color = Color(0xFF202124),
                        letterSpacing = 0.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "Choose how you want to continue",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = textGray,
                        letterSpacing = 0.2.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // User Type Selection Cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // JobSeeker Option
                    ModernUserTypeCard(
                        title = "Job Seeker",
                        description = "Looking for job opportunities",
                        icon = R.drawable.profileemptypic,
                        isSelected = selectedType == "JOBSEEKER",
                        primaryBlue = primaryBlue,
                        surfaceBlue = surfaceBlue,
                        onClick = { selectedType = "JOBSEEKER" }
                    )

                    // Company Option
                    ModernUserTypeCard(
                        title = "Company",
                        description = "Hiring top talent",
                        icon = R.drawable.profileemptypic,
                        isSelected = selectedType == "COMPANY",
                        primaryBlue = primaryBlue,
                        surfaceBlue = surfaceBlue,
                        onClick = { selectedType = "COMPANY" }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Info Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(surfaceBlue)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_visibility_24),
                        contentDescription = null,
                        tint = accentBlue,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "You'll be able to access all features immediately after sign in.",
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = Color(0xFF202124),
                            lineHeight = 18.sp,
                            letterSpacing = 0.2.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel Button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.W500,
                                color = primaryBlue,
                                letterSpacing = 0.1.sp
                            )
                        )
                    }

                    // Continue Button
                    Button(
                        onClick = { onUserTypeSelected(selectedType) },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryBlue
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 6.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.google),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continue",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.W500,
                                    color = Color.White,
                                    letterSpacing = 0.1.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernUserTypeCard(
    title: String,
    description: String,
    icon: Int,
    isSelected: Boolean,
    primaryBlue: Color,
    surfaceBlue: Color,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) primaryBlue else Color(0xFFDADCE0)
    val backgroundColor = if (isSelected) surfaceBlue else Color.White
    val titleColor = if (isSelected) primaryBlue else Color(0xFF202124)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                modifier = Modifier.size(24.dp),
                colors = RadioButtonDefaults.colors(
                    selectedColor = primaryBlue,
                    unselectedColor = Color(0xFF5F6368)
                )
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected)
                            primaryBlue.copy(alpha = 0.12f)
                        else
                            Color(0xFFF1F3F4)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500,
                        color = titleColor,
                        letterSpacing = 0.1.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF5F6368),
                        letterSpacing = 0.2.sp
                    )
                )
            }

            if (isSelected) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_visibility_24),
                    contentDescription = "Selected",
                    tint = primaryBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}