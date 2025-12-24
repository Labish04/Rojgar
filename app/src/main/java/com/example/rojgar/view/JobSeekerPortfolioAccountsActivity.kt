package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.graphics.Color
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

class JobSeekerPortfolioAccountsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerPortfolioAccountsBody()
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
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
        if (jobSeekerId.isNotEmpty()) {
            portfolioViewModel.getPortfoliosByJobSeekerId(jobSeekerId)
        }
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
    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = DarkBlue2,
                modifier = Modifier.size(50.dp)
            )
        }
    }

    // Delete Dialog
    if (showDeleteAlert && portfolioToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteAlert = false
                portfolioToDelete = null
            },
            title = {
                Text(
                    text = "Delete Portfolio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${portfolioToDelete?.accountName}?",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Button
                    TextButton(
                        onClick = {
                            showDeleteAlert = false
                            portfolioToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Delete Button
                    Button(
                        onClick = {
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Delete", color = Color.White)
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .height(140.dp)
                    .padding(top = 55.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(5.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBlue2),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val intent = Intent(context, JobSeekerProfileDetailsActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(80.dp))
                    Text(
                        "Portfolio Accounts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Blue)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Which project or task highlights your professional strengths the most?",
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (portfolios.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.noexperience),
                        contentDescription = "no links",
                        tint = Color.Gray,
                        modifier = Modifier.size(110.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Your Portfolio Section is currently empty. Tap the + button to add your portfolio links.",
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { openAddForm() },
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier
                                .width(170.dp)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkBlue2,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.addexperience),
                                contentDescription = "Add",
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Add", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(portfolios) { portfolio ->
                        PortfolioCard(
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { openAddForm() },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .width(170.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkBlue2,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.addexperience),
                            contentDescription = "Add",
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Add", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // Portfolio Detail Dialog
    if (showDetailDialog && selectedPortfolio != null) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = {
                Text(
                    text = "Portfolio Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = DarkBlue2
                )
            },
            text = {
                Column {
                    selectedPortfolio?.let { portfolio ->
                        Details(title = "Account Name", value = portfolio.accountName)
                        Details(title = "URL", value = portfolio.accountLink)
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            showDetailDialog = false
                            selectedPortfolio?.let { openEditForm(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue2),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            selectedPortfolio?.let {
                                portfolioToDelete = it
                                showDeleteAlert = true
                                showDetailDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        )
    }

    // Portfolio Form Dialog
    if (showPortfolioSheet) {
        Dialog(
            onDismissRequest = {
                showPortfolioSheet = false
                resetForm()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .clickable {
                        showPortfolioSheet = false
                        resetForm()
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.35f),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (isEditing) "Edit Portfolio" else "Add Portfolio",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(16.dp))

                        // Account Name
                        OutlinedTextField(
                            value = accountName,
                            onValueChange = { accountName = it },
                            label = { Text("Account Name *") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.document),
                                    contentDescription = "Account Name",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            placeholder = { Text("e.g., LinkedIn, GitHub") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // URL
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("URL *") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.linkicon),
                                    contentDescription = "URL",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            placeholder = { Text("https://example.com") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedIndicatorColor = DarkBlue2,
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )

                        Spacer(Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Back Button
                            OutlinedButton(
                                onClick = {
                                    showPortfolioSheet = false
                                    resetForm()
                                },
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier
                                    .weight(0.4f)
                                    .height(50.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = DarkBlue2
                                )
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_arrow_back_ios_24),
                                    contentDescription = "Back",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Save Button
                            Button(
                                onClick = { savePortfolio() },
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier
                                    .weight(0.6f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkBlue2,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = if (isEditing) "Update" else "Save",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PortfolioCard(
    portfolio: PortfolioModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = portfolio.accountName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // Delete Icon
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // URL
                    Text(
                        text = portfolio.accountLink,
                        fontSize = 14.sp,
                        color = DarkBlue2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun Details(title: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview
@Composable
fun JobSeekerPortfolioAccountsPreview() {
    JobSeekerPortfolioAccountsBody()
}