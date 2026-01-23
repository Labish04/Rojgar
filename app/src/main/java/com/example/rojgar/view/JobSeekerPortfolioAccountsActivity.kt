package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rojgar.R
import com.example.rojgar.model.PortfolioModel
import com.example.rojgar.repository.PortfolioRepoImpl
import com.example.rojgar.ui.theme.*
import com.example.rojgar.viewmodel.PortfolioViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay

class JobSeekerPortfolioAccountsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerPortfolioAccountsBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun JobSeekerPortfolioAccountsBody() {
    val context = LocalContext.current

    // Initialize ViewModel
    val portfolioViewModel = remember { PortfolioViewModel(PortfolioRepoImpl()) }

    // Get current user
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val jobSeekerId = currentUser?.uid ?: ""

    // Observe LiveData from ViewModel
    val portfolios by portfolioViewModel.portfolios.observeAsState(emptyList())
    val loading by portfolioViewModel.loading.observeAsState(false)
    val error by portfolioViewModel.error.observeAsState(null)
    val successMessage by portfolioViewModel.successMessage.observeAsState(null)

    // State variables
    var showPortfolioSheet by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var selectedPortfolio by remember { mutableStateOf<PortfolioModel?>(null) }

    var accountName by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    var currentPortfolioId by remember { mutableStateOf("") }
    var showDeleteAlert by remember { mutableStateOf(false) }
    var portfolioToDelete by remember { mutableStateOf<PortfolioModel?>(null) }

    var topBarVisible by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Show Toast messages
    LaunchedEffect(error) {
        error?.let {
            if (it.isNotBlank()) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                portfolioViewModel.clearMessages()
            }
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            if (it.isNotBlank()) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                portfolioViewModel.clearMessages()
            }
        }
    }

    // Load portfolios on initial composition
    LaunchedEffect(Unit) {
        delay(100)
        topBarVisible = true
        if (jobSeekerId.isNotEmpty()) {
            portfolioViewModel.getPortfoliosByJobSeekerId(jobSeekerId)
            delay(500)
        }
        isLoading = false
        showContent = true
    }

    // Function to reset form
    fun resetForm() {
        accountName = ""
        url = ""
        currentPortfolioId = ""
        isEditing = false
    }

    // Function to open add form
    fun openAddForm() {
        resetForm()
        showPortfolioSheet = true
    }

    // Function to open edit form
    fun openEditForm(portfolio: PortfolioModel) {
        accountName = portfolio.accountName
        url = portfolio.accountLink
        currentPortfolioId = portfolio.portfolioId
        isEditing = true
        showPortfolioSheet = true
    }

    // Function to save portfolio
    fun savePortfolio() {
        if (accountName.isEmpty() || url.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Basic URL validation
        val processedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }

        val portfolioModel = PortfolioModel(
            portfolioId = if (isEditing) currentPortfolioId else "",
            accountName = accountName,
            accountLink = processedUrl,
            jobSeekerId = jobSeekerId
        )

        if (isEditing) {
            portfolioViewModel.updatePortfolio(currentPortfolioId, portfolioModel) { success, message ->
                if (success) {
                    showPortfolioSheet = false
                    resetForm()
                }
            }
        } else {
            portfolioViewModel.addPortfolio(portfolioModel) { success, message ->
                if (success) {
                    showPortfolioSheet = false
                    resetForm()
                }
            }
        }
    }

    fun deletePortfolio(portfolio: PortfolioModel) {
        portfolioToDelete = portfolio
        showDeleteAlert = true
    }

    // Loading indicator
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF2196F3),
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
        }
    }

    // Delete Dialog
    if (showDeleteAlert && portfolioToDelete != null) {
        ModernPortfolioDeleteDialog(
            onDismiss = {
                showDeleteAlert = false
                portfolioToDelete = null
            },
            onConfirm = {
                portfolioToDelete?.let { portfolio ->
                    portfolioViewModel.deletePortfolio(
                        portfolio.portfolioId,
                        jobSeekerId
                    ) { success, message ->
                        if (success) {
                            showDetailDialog = false
                        }
                    }
                }
                showDeleteAlert = false
                portfolioToDelete = null
            },
            portfolioName = portfolioToDelete?.accountName ?: ""
        )
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
                                val intent = Intent(context, JobSeekerProfileDetailsActivity::class.java)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.graphicsLayer {
                                scaleX = backScale
                                scaleY = backScale
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                                contentDescription = null,
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
                                    text = "Portfolio Accounts",
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
                visible = showContent,
                enter = fadeIn(animationSpec = tween(600))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    var headerVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(200)
                        headerVisible = true
                    }

                    AnimatedVisibility(
                        visible = headerVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -it / 2 },
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(500))
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
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
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF2196F3).copy(alpha = 0.12f)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.linkicon),
                                        contentDescription = "Portfolio",
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Portfolio & Links",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF263238)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Showcase your online presence",
                                        fontSize = 13.sp,
                                        color = Color(0xFF78909C)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (portfolios.isEmpty()) {
                        var emptyStateVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(400)
                            emptyStateVisible = true
                        }

                        AnimatedVisibility(
                            visible = emptyStateVisible,
                            enter = fadeIn(animationSpec = tween(600)) +
                                    scaleIn(
                                        initialScale = 0.8f,
                                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                                    )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    modifier = Modifier.size(120.dp),
                                    shape = RoundedCornerShape(60.dp),
                                    color = Color.White.copy(alpha = 0.5f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.noexperience),
                                            contentDescription = "no portfolio",
                                            tint = Color(0xFF78909C),
                                            modifier = Modifier.size(70.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    "No Portfolio Added Yet",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF263238)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Add your portfolio links to showcase your work",
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF78909C),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(portfolios) { portfolio ->
                                ModernPortfolioCard(
                                    portfolio = portfolio,
                                    onClick = {
                                        selectedPortfolio = portfolio
                                        showDetailDialog = true
                                    },
                                    onEditClick = { openEditForm(portfolio) },
                                    onDeleteClick = { deletePortfolio(portfolio) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    var buttonVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(600)
                        buttonVisible = true
                    }

                    AnimatedVisibility(
                        visible = buttonVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(500))
                    ) {
                        ModernAddButton(
                            text = if (portfolios.isEmpty()) "Add Portfolio" else "Add Another",
                            onClick = { openAddForm() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Portfolio Form Dialog
    if (showPortfolioSheet) {
        ModernPortfolioFormDialog(
            isEditing = isEditing,
            accountName = accountName,
            url = url,
            onAccountNameChange = { accountName = it },
            onUrlChange = { url = it },
            onDismiss = {
                showPortfolioSheet = false
                resetForm()
            },
            onSave = { savePortfolio() }
        )
    }

    // Detail Dialog
    if (showDetailDialog && selectedPortfolio != null) {
        ModernPortfolioDetailDialog(
            portfolio = selectedPortfolio!!,
            onDismiss = {
                showDetailDialog = false
                selectedPortfolio = null
            },
            onEdit = {
                showDetailDialog = false
                openEditForm(selectedPortfolio!!)
                selectedPortfolio = null
            },
            onDelete = {
                showDetailDialog = false
                deletePortfolio(selectedPortfolio!!)
                selectedPortfolio = null
            }
        )
    }
}

@Composable
fun ModernPortfolioCard(
    portfolio: PortfolioModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2196F3).copy(alpha = 0.12f)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.linkicon),
                            contentDescription = "Portfolio",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = portfolio.accountName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF263238)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = portfolio.accountLink,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF78909C),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row {
                    var editPressed by remember { mutableStateOf(false) }
                    val editScale by animateFloatAsState(
                        targetValue = if (editPressed) 0.85f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    IconButton(
                        onClick = {
                            editPressed = true
                            onEditClick()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = editScale
                                scaleY = editScale
                            }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2196F3).copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    LaunchedEffect(editPressed) {
                        if (editPressed) {
                            delay(150)
                            editPressed = false
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    var deletePressed by remember { mutableStateOf(false) }
                    val deleteScale by animateFloatAsState(
                        targetValue = if (deletePressed) 0.85f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    IconButton(
                        onClick = {
                            deletePressed = true
                            onDeleteClick()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer {
                                scaleX = deleteScale
                                scaleY = deleteScale
                            }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF44336).copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    LaunchedEffect(deletePressed) {
                        if (deletePressed) {
                            delay(150)
                            deletePressed = false
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.3f)),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.linkicon),
                        contentDescription = "Link",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = portfolio.accountLink,
                        fontSize = 13.sp,
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(R.drawable.openicon),
                        contentDescription = "Open",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernPortfolioFormDialog(
    isEditing: Boolean,
    accountName: String,
    url: String,
    onAccountNameChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.39f)
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit Portfolio" else "Add Portfolio",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Account Name Field
                    ModernPortfolioTextField(
                        value = accountName,
                        onValueChange = onAccountNameChange,
                        label = "Account Name *",
                        icon = R.drawable.document,
                        placeholder = "e.g., LinkedIn, GitHub, Behance"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // URL Field
                    ModernPortfolioTextField(
                        value = url,
                        onValueChange = onUrlChange,
                        label = "URL *",
                        icon = R.drawable.linkicon,
                        placeholder = "https://example.com"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.3f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF78909C)
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(0.7f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Text(
                            text = if (isEditing) "Update" else "Save",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernPortfolioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: Int,
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(24.dp)
            )
        },
        placeholder = { Text(placeholder) },
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2196F3),
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedLabelColor = Color(0xFF2196F3),
            unfocusedLabelColor = Color(0xFF78909C)
        )
    )
}

//@Composable
//fun ModernAddButton(
//    text: String,
//    onClick: () -> Unit
//) {
//    var isPressed by remember { mutableStateOf(false) }
//    val scale by animateFloatAsState(
//        targetValue = if (isPressed) 0.95f else 1f,
//        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioMediumBouncy,
//            stiffness = Spring.StiffnessMedium
//        )
//    )
//
//    Button(
//        onClick = {
//            isPressed = true
//            onClick()
//        },
//        modifier = Modifier
//            .padding(horizontal = 24.dp)
//            .fillMaxWidth()
//            .height(56.dp)
//            .graphicsLayer {
//                scaleX = scale
//                scaleY = scale
//            },
//        shape = RoundedCornerShape(16.dp),
//        colors = ButtonDefaults.buttonColors(
//            containerColor = Color(0xFF2196F3)
//        ),
//        elevation = ButtonDefaults.buttonElevation(
//            defaultElevation = 6.dp,
//            pressedElevation = 2.dp
//        )
//    ) {
//        Icon(
//            painter = painterResource(id = R.drawable.addexperience),
//            contentDescription = "Add",
//            modifier = Modifier.size(24.dp)
//        )
//        Spacer(modifier = Modifier.width(12.dp))
//        Text(
//            text = text,
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold
//        )
//    }
//}

@Composable
fun ModernPortfolioDetailDialog(
    portfolio: PortfolioModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Portfolio Details",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238)
            )
        },
        text = {
            Column {
                DetailItem(title = "Account Name", value = portfolio.accountName)
                Spacer(modifier = Modifier.height(8.dp))
                DetailItem(title = "URL", value = portfolio.accountLink)
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun ModernPortfolioDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    portfolioName: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF44336).copy(alpha = 0.1f)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        title = {
            Text(
                text = "Delete Portfolio?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF263238),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$portfolioName\"? This action cannot be undone.",
                fontSize = 15.sp,
                color = Color(0xFF78909C),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF78909C)
                    )
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

//@Composable
//fun DetailItem(title: String, value: String) {
//    Column(modifier = Modifier.padding(vertical = 4.dp)) {
//        Text(
//            text = title,
//            fontWeight = FontWeight.Medium,
//            fontSize = 14.sp,
//            color = Color(0xFF78909C)
//        )
//        Spacer(modifier = Modifier.height(2.dp))
//        Text(
//            text = value,
//            fontSize = 16.sp,
//            color = Color(0xFF263238),
//            modifier = Modifier.padding(top = 2.dp)
//        )
//    }
//}

@Preview
@Composable
fun JobSeekerPortfolioAccountsPreview() {
    JobSeekerPortfolioAccountsBody()
}