package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rojgar.R
import com.example.rojgar.model.ChatRoom
import com.example.rojgar.utils.MutualFollowHelper
import com.example.rojgar.viewmodel.ChatViewModel
import com.example.rojgar.viewmodel.ChatViewModelFactory
import com.example.rojgar.repository.ChatRepositoryImpl
import com.example.rojgar.repository.FollowRepoImpl
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.repository.JobSeekerRepoImpl
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.tooling.preview.Preview
import com.example.rojgar.repository.UserRepo

// Premium Light Blue Color Scheme
private val LightBlue50 = Color(0xFFE3F2FD)
private val LightBlue100 = Color(0xFFBBDEFB)
private val LightBlue200 = Color(0xFF90CAF9)
private val LightBlue300 = Color(0xFF64B5F6)
private val LightBlue400 = Color(0xFF42A5F5)
private val LightBlue500 = Color(0xFF2196F3)
private val LightBlue600 = Color(0xFF1E88E5)
private val LightBlue700 = Color(0xFF1976D2)
private val AccentCyan = Color(0xFF00BCD4)
private val SoftWhite = Color(0xFFFAFBFF)

// Additional colors for GlowingChatbotFAB
private val PurpleGradientStart = Color(0xFF667EEA)
private val PurpleGradientEnd = Color(0xFF764BA2)
private val CyanGradient = Color(0xFF00D4FF)
private val PinkGradient = Color(0xFFFF00FF)

class MessageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessageBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MessageBody(
    chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(ChatRepositoryImpl()))
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showMutualFollows by remember { mutableStateOf(false) }
    var mutualFollows by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var loadingMutualFollows by remember { mutableStateOf(false) }

    val chatRooms by chatViewModel.chatRooms.observeAsState(emptyList())
    val loading by chatViewModel.loading.observeAsState(false)

    val userRepo = remember { UserRepo() }
    val currentUserId = userRepo.getCurrentUserId()
    var currentUserName by remember { mutableStateOf("") }
    var currentUserType by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        userRepo.getUserType { userType ->
            currentUserType = userType
            if (userType == "Company") {
                val companyRepo = CompanyRepoImpl()
                companyRepo.getCompanyById(currentUserId) { success, message, company ->
                    if (success && company != null) {
                        currentUserName = company.companyName
                    }
                }
            } else if (userType == "JobSeeker") {
                val jobSeekerRepo = JobSeekerRepoImpl()
                jobSeekerRepo.getJobSeekerById(currentUserId) { success, message, jobSeeker ->
                    if (success && jobSeeker != null) {
                        currentUserName = jobSeeker.fullName
                    }
                }
            }
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            chatViewModel.loadChatRooms(currentUserId)
        }
    }

    LaunchedEffect(currentUserId, currentUserType) {
        if (currentUserId.isNotEmpty() && currentUserType != null) {
            loadingMutualFollows = true
            scope.launch {
                try {
                    val helper = MutualFollowHelper(FollowRepoImpl())
                    val mutualFollowIds = helper.getMutualFollowers(currentUserId)

                    val companyRepo = CompanyRepoImpl()
                    val jobSeekerRepo = JobSeekerRepoImpl()
                    val mutualFollowsWithDetails = mutableListOf<Pair<String, String>>()

                    val usersInChats = chatRooms.flatMap { listOf(it.participant1Id, it.participant2Id) }

                    for (userId in mutualFollowIds) {
                        if (usersInChats.contains(userId)) {
                            continue
                        }

                        companyRepo.getCompanyDetails(userId) { success, message, company ->
                            if (success && company != null) {
                                mutualFollowsWithDetails.add(Pair(userId, company.companyName))
                            } else {
                                jobSeekerRepo.getJobSeekerDetails(userId) { success2, message2, jobSeeker ->
                                    if (success2 && jobSeeker != null) {
                                        mutualFollowsWithDetails.add(Pair(userId, jobSeeker.fullName))
                                    }
                                }
                            }
                        }
                    }

                    kotlinx.coroutines.delay(1000)
                    mutualFollows = mutualFollowsWithDetails
                    showMutualFollows = mutualFollowsWithDetails.isNotEmpty()
                    loadingMutualFollows = false
                } catch (e: Exception) {
                    e.printStackTrace()
                    loadingMutualFollows = false
                }
            }
        }
    }

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LightBlue50,
                        SoftWhite,
                        LightBlue100.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                GlowingChatbotFAB(
                    onClick = {
                        val intent = Intent(context, ChatbotActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Premium Top Bar with Glassmorphism Effect
                AnimatedTopBar(
                    onBackClick = { (context as? ComponentActivity)?.finish() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Premium Search Bar with Animation
                PremiumSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Mutual Follows Section
                AnimatedVisibility(
                    visible = loadingMutualFollows,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = LightBlue500,
                            strokeWidth = 3.dp
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showMutualFollows && mutualFollows.isNotEmpty() && !loadingMutualFollows,
                    enter = fadeIn() + expandVertically() + slideInVertically(),
                    exit = fadeOut() + shrinkVertically() + slideOutVertically()
                ) {
                    MutualFollowsSection(
                        mutualFollows = mutualFollows,
                        currentUserId = currentUserId,
                        currentUserName = currentUserName,
                        chatViewModel = chatViewModel
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chat List
                if (loading && chatRooms.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = LightBlue500,
                            strokeWidth = 3.dp
                        )
                    }
                } else if (chatRooms.isEmpty()) {
                    EmptyStateView(showMutualFollows = showMutualFollows && mutualFollows.isNotEmpty())
                } else {
                    val filteredChatRooms = if (searchQuery.isNotEmpty()) {
                        chatRooms.filter { chatRoom ->
                            val otherParticipantName = if (chatRoom.participant1Id == currentUserId) {
                                chatRoom.participant2Name
                            } else {
                                chatRoom.participant1Name
                            }
                            otherParticipantName.contains(searchQuery, ignoreCase = true) ||
                                    chatRoom.lastMessage.contains(searchQuery, ignoreCase = true)
                        }
                    } else {
                        chatRooms
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredChatRooms,
                            key = { it.chatId }
                        ) { chatRoom ->
                            val otherParticipantId = if (chatRoom.participant1Id == currentUserId) {
                                chatRoom.participant2Id
                            } else {
                                chatRoom.participant1Id
                            }

                            val otherParticipantName = if (chatRoom.participant1Id == currentUserId) {
                                chatRoom.participant2Name
                            } else {
                                chatRoom.participant1Name
                            }

                            AnimatedChatUserItem(
                                chatRoom = chatRoom,
                                currentUserId = currentUserId,
                                onClick = {
                                    val intent = Intent(context, ChatActivity::class.java).apply {
                                        putExtra("receiverId", otherParticipantId)
                                        putExtra("receiverName", otherParticipantName)
                                        putExtra("currentUserId", currentUserId)
                                        putExtra("currentUserName", currentUserName)
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlowingChatbotFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    // Breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .size(80.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Multiple glow layers for ethereal effect
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(64.dp + (index * 8).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                // Using the app's light blue theme colors
                                LightBlue400.copy(alpha = glowAlpha / (index + 1)),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Main button with gradient
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(16.dp, CircleShape, spotColor = LightBlue400)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            LightBlue400,
                            LightBlue600,
                            AccentCyan.copy(alpha = 0.7f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable {
                    isPressed = true
                    onClick()
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(100)
                        isPressed = false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.chatboticon),
                contentDescription = "AI Assistant",
                modifier = Modifier.size(26.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun AnimatedTopBar(onBackClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.85f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(LightBlue400, LightBlue600)
                                )
                            )
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Messages",
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlue700,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AccentCyan, LightBlue500)
                            )
                        )
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.editmessage),
                        contentDescription = "New Message",
                        modifier = Modifier.size(22.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "search_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .scale(scale)
            .shadow(
                elevation = if (isFocused) 12.dp else 6.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = LightBlue300
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_search),
                contentDescription = "Search",
                modifier = Modifier.size(24.dp),
                tint = LightBlue400
            )

            Spacer(modifier = Modifier.width(12.dp))

            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search messages...",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Gray.copy(alpha = 0.6f)
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = LightBlue700,
                    unfocusedTextColor = Color.Black
                ),
                textStyle = TextStyle(fontSize = 16.sp),
                singleLine = true
            )
        }
    }
}

@Composable
fun MutualFollowsSection(
    mutualFollows: List<Pair<String, String>>,
    currentUserId: String,
    currentUserName: String,
    chatViewModel: ChatViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentCyan)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "People you can message",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightBlue700
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            mutualFollows.forEachIndexed { index, (userId, userName) ->
                AnimatedMutualFollowCard(
                    userId = userId,
                    userName = userName,
                    currentUserId = currentUserId,
                    currentUserName = currentUserName,
                    chatViewModel = chatViewModel,
                    index = index
                )

                if (index < mutualFollows.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun AnimatedMutualFollowCard(
    userId: String,
    userName: String,
    currentUserId: String,
    currentUserName: String,
    chatViewModel: ChatViewModel,
    index: Int
) {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 100L)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "mutual_card_scale"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 })
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isPressed = true
                    chatViewModel.getOrCreateChatRoom(
                        participant1Id = currentUserId,
                        participant2Id = userId,
                        participant1Name = currentUserName,
                        participant2Name = userName
                    ) { chatRoom ->
                        val intent = Intent(context, ChatActivity::class.java).apply {
                            putExtra("chatId", chatRoom.chatId)
                            putExtra("receiverId", userId)
                            putExtra("receiverName", userName)
                            putExtra("currentUserId", currentUserId)
                            putExtra("currentUserName", currentUserName)
                        }
                        context.startActivity(intent)
                        isPressed = false
                    }
                },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = LightBlue50
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(LightBlue300, LightBlue500)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(2).uppercase(),
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = userName,
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LightBlue700
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentCyan)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Mutual connection",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = AccentCyan,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        chatViewModel.getOrCreateChatRoom(
                            participant1Id = currentUserId,
                            participant2Id = userId,
                            participant1Name = currentUserName,
                            participant2Name = userName
                        ) { chatRoom ->
                            val intent = Intent(context, ChatActivity::class.java).apply {
                                putExtra("chatId", chatRoom.chatId)
                                putExtra("receiverId", userId)
                                putExtra("receiverName", userName)
                                putExtra("currentUserId", currentUserId)
                                putExtra("currentUserName", currentUserName)
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightBlue500
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = "Message",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedChatUserItem(
    chatRoom: ChatRoom,
    currentUserId: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chat_item_scale"
    )

    val otherParticipantName = if (chatRoom.participant1Id == currentUserId) {
        chatRoom.participant2Name
    } else {
        chatRoom.participant1Name
    }

    val lastMessageTime = formatRelativeTime(chatRoom.lastMessageTime)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isPressed = true
                    onClick()
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(100)
                        isPressed = false
                    }
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Image(
                        painter = painterResource(R.drawable.sampleimage),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .shadow(4.dp, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    // Online indicator
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = otherParticipantName,
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightBlue700
                            ),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = lastMessageTime,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = LightBlue300,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = chatRoom.lastMessage,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontWeight = if (chatRoom.unreadCount > 0)
                                    FontWeight.SemiBold else FontWeight.Normal
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (chatRoom.unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFFF5252),
                                                Color(0xFFFF1744)
                                            )
                                        )
                                    )
                                    .shadow(4.dp, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (chatRoom.unreadCount > 99) "99+"
                                    else chatRoom.unreadCount.toString(),
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
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

@Composable
fun EmptyStateView(showMutualFollows: Boolean) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Icon
                val infiniteTransition = rememberInfiniteTransition(label = "empty_icon")
                val iconScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "icon_scale"
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(iconScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    LightBlue100.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.chat),
                        contentDescription = "No messages",
                        modifier = Modifier.size(80.dp),
                        tint = LightBlue300
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "No messages yet",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightBlue700
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (showMutualFollows)
                        "Start a conversation above"
                    else
                        "Connect with people to start chatting",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> {
            val date = Date(timestamp)
            val format = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
            format.format(date)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessagePreview() {
    MessageBody()
}