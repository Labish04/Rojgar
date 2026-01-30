package com.example.rojgar.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rojgar.R
import com.example.rojgar.model.MutualContact
import com.example.rojgar.repository.*
import com.example.rojgar.ui.theme.SkyBlue
import com.example.rojgar.viewmodel.CreateGroupViewModel
import com.example.rojgar.viewmodel.CreateGroupViewModelFactory
import com.example.rojgar.viewmodel.CreateGroupUiState
import java.util.*

class CreateGroupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val userRepo = remember { UserRepo() }
            val followRepo = remember { FollowRepoImpl(context) }
            val companyRepo = remember { CompanyRepoImpl() }
            val jobSeekerRepo = remember { JobSeekerRepoImpl() }

            val viewModel: CreateGroupViewModel = viewModel(
                factory = CreateGroupViewModelFactory(userRepo, followRepo, companyRepo, jobSeekerRepo)
            )

            // Load current user on start
            LaunchedEffect(Unit) {
                viewModel.loadCurrentUser()
            }

            CreateGroupScreen(
                viewModel = viewModel,
                onBackClick = { finish() },
                onGroupCreated = { groupId ->
                    val intent = Intent(this, ChatActivity::class.java).apply {
                        putExtra("groupId", groupId)
                        putExtra("isGroupChat", true)
                    }
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    viewModel: CreateGroupViewModel,
    onBackClick: () -> Unit,
    onGroupCreated: (String) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val mutualContacts by viewModel.mutualContacts.collectAsState()
    val selectedContacts by viewModel.selectedContacts.collectAsState()
    val groupName by viewModel.groupName.collectAsState()
    val groupImage by viewModel.groupImage.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadGroupImage(context, it)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is CreateGroupUiState.GroupCreated) {
            Toast.makeText(context, "Group created successfully!", Toast.LENGTH_SHORT).show()
            onGroupCreated((uiState as CreateGroupUiState.GroupCreated).groupId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadMutualContacts()
    }

    Scaffold(
        bottomBar = {
            CreateGroupBottomBar(
                enabled = selectedContacts.isNotEmpty() && groupName.isNotBlank(),
                loading = uiState is CreateGroupUiState.Loading,
                onCreateClick = { viewModel.createGroup() }
            )
        }
    ) { paddingValues ->
        // Apply Scaffold padding to the main container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // This applies the padding from Scaffold
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE8F4F8),
                            Color(0xFFF0F8FF)
                        )
                    )
                )
        ) {
            Column (
                modifier = Modifier
                    .fillMaxSize()
            ){
                // Updated: Use Card-based top bar like ChatActivity
                GroupCreationTopBar(
                    selectedCount = selectedContacts.size,
                    loading = uiState is CreateGroupUiState.Loading,
                    onBackClick = onBackClick
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        GroupInfoCard(
                            groupName = groupName,
                            groupImage = groupImage,
                            onGroupNameChange = { viewModel.updateGroupName(it) },
                            onGroupImageClick = { imagePickerLauncher.launch("image/*") },
                            uploadProgress = uploadProgress,
                            isUploading = uiState is CreateGroupUiState.Uploading
                        )
                    }

                    item {
                        SelectedContactsPreview(
                            selectedContacts = mutualContacts.filter { it.isSelected },
                            onContactClick = { viewModel.toggleContactSelection(it.userId) }
                        )
                    }

                    item {
                        MutualContactsHeader(
                            contactCount = mutualContacts.size,
                            loading = uiState is CreateGroupUiState.Loading
                        )
                    }

                    if (mutualContacts.isEmpty() && uiState !is CreateGroupUiState.Loading) {
                        item {
                            EmptyContactsState()
                        }
                    } else {
                        items(mutualContacts) { contact ->
                            MutualContactItem(
                                contact = contact,
                                isSelected = selectedContacts.contains(contact.userId),
                                onToggle = { viewModel.toggleContactSelection(contact.userId) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }

                if (uiState is CreateGroupUiState.Loading && mutualContacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = SkyBlue
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Loading mutual contacts...",
                                color = Color.White
                            )
                        }
                    }
                }

                if (uiState is CreateGroupUiState.Error) {
                    AlertDialog(
                        onDismissRequest = { viewModel.clearError() },
                        title = { Text("Error") },
                        text = {
                            Text(
                                (uiState as CreateGroupUiState.Error).message,
                                textAlign = TextAlign.Center
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GroupCreationTopBar(
    selectedCount: Int,
    loading: Boolean = false,
    onBackClick: () -> Unit
) {
    // NEW: Card-based top bar similar to ChatActivity
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .shadow(4.dp),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF5F5F5), CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF1976D2)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SkyBlue, Color(0xFF42A5F5))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.addicon),
                        contentDescription = "Create Group",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Create Group",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    )
                    Text(
                        text = if (selectedCount > 0) {
                            "$selectedCount member${if (selectedCount > 1) "s" else ""} selected"
                        } else {
                            "Select members"
                        },
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = Color(0xFF1976D2)
                        )
                    )
                }
            }

            if (loading) {
                CircularProgressIndicator(
                    color = SkyBlue,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CreateGroupBottomBar(
    enabled: Boolean,
    loading: Boolean,
    onCreateClick: () -> Unit
) {
    // Updated: Card-based bottom bar like ChatActivity
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .shadow(8.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onCreateClick,
                enabled = enabled && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (enabled) {
                        Color(0xFF1976D2)
                    } else {
                        Color.Gray.copy(alpha = 0.3f)
                    },
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(26.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Creating...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Create Group",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GroupInfoCard(
    groupName: String,
    groupImage: String?,
    onGroupNameChange: (String) -> Unit,
    onGroupImageClick: () -> Unit,
    uploadProgress: Double,
    isUploading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF42A5F5).copy(alpha = 0.1f))
                    .clickable { onGroupImageClick() },
                contentAlignment = Alignment.Center
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        progress = (uploadProgress / 100).toFloat(),
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 4.dp,
                        color = SkyBlue
                    )
                    Text(
                        "${uploadProgress.toInt()}%",
                        color = SkyBlue,
                        fontWeight = FontWeight.Bold
                    )
                } else if (!groupImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(groupImage)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Group Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Add Group Photo",
                            modifier = Modifier.size(36.dp),
                            tint = SkyBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Add Photo",
                            fontSize = 11.sp,
                            color = SkyBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = groupName,
                onValueChange = onGroupNameChange,
                label = {
                    Text(
                        "Group Name",
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                },
                placeholder = { Text("Enter group name...") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkyBlue,
                    focusedLabelColor = SkyBlue,
                    cursorColor = SkyBlue,
                    unfocusedBorderColor = Color.LightGray
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = if (groupName.isNotEmpty())
                            SkyBlue
                        else Color.Gray
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose a name that describes your group",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SelectedContactsPreview(
    selectedContacts: List<MutualContact>,
    onContactClick: (MutualContact) -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = selectedContacts.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = SkyBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Selected Members",
                    fontWeight = FontWeight.Bold,
                    color = SkyBlue,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "${selectedContacts.size} selected",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            LazyRow(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(selectedContacts) { contact ->
                    SelectedContactChip(
                        contact = contact,
                        onClick = { onContactClick(contact) }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedContactChip(
    contact: MutualContact,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF64B5F6).copy(alpha = 0.2f),
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                SkyBlue,
                                Color(0xFF1976D2)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (contact.userPhoto.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(contact.userPhoto)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Contact Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        contact.userName.first().toString().uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .border(2.dp, Color.White, CircleShape)
                ) {
                    Text(
                        "×",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                contact.userName.split(" ").first(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray,
                maxLines = 1
            )

            Text(
                contact.userType,
                fontSize = 9.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun MutualContactsHeader(
    contactCount: Int,
    loading: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = SkyBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Mutual Contacts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SkyBlue
                )
                Text(
                    "People who follow you back",
                    fontSize = 12.sp,
                    color = SkyBlue.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (loading) {
                CircularProgressIndicator(
                    color = SkyBlue,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SkyBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        "$contactCount contacts",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        color = SkyBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MutualContactItem(
    contact: MutualContact,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            scale = 1.02f
            kotlinx.coroutines.delay(100)
            scale = 1f
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Color(0xFFE3F2FD)
            else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 2.dp),
        shape = RoundedCornerShape(20.dp),
        border = if (isSelected)
            BorderStroke(2.dp, SkyBlue)
        else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (isSelected)
                                    listOf(
                                        SkyBlue,
                                        Color(0xFF1976D2)
                                    )
                                else
                                    listOf(
                                        Color(0xFFE0E0E0),
                                        Color(0xFFBDBDBD)
                                    )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (contact.userPhoto.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(contact.userPhoto)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Contact Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            contact.userName.first().toString().uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(
                            when (contact.userType) {
                                "Company" -> Color(0xFF4CAF50)
                                "JobSeeker" -> Color(0xFF2196F3)
                                else -> Color.Gray
                            }
                        )
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        contact.userType.first().toString(),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    contact.userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isSelected)
                        SkyBlue
                    else Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    contact.userType,
                    fontSize = 13.sp,
                    color = if (isSelected)
                        SkyBlue
                    else Color.Gray,
                    fontWeight = FontWeight.Normal
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) SkyBlue
                        else Color.Transparent
                    )
                    .border(
                        2.dp,
                        if (isSelected) SkyBlue
                        else Color.Gray,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                    exit = scaleOut(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
                ) {
                    Box(modifier = Modifier) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyContactsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Face,
            contentDescription = "No contacts",
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFBDBDBD)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No mutual contacts found",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF757575)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "You need to follow people and have them follow you back to create groups",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}