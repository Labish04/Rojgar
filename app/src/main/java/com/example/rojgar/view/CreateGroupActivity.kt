package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class CreateGroupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CreateGroupBody()
        }
    }
}

data class Contact(val id: Int, val name: String, val phone: String, val status: String = "Available")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupBody() {
    var groupName by remember { mutableStateOf("") }
    var selectedContacts by remember { mutableStateOf(setOf<Int>()) }

    // Sample contacts
    val contacts = remember {
        listOf(
            Contact(1, "Alice Johnson", "+1 234 567 8901", "Hey there! 👋"),
            Contact(2, "Bob Smith", "+1 234 567 8902", "Busy"),
            Contact(3, "Charlie Brown", "+1 234 567 8903", "Available"),
            Contact(4, "Diana Prince", "+1 234 567 8904", "At work 💼"),
            Contact(5, "Eve Wilson", "+1 234 567 8905", "Sleeping 😴"),
            Contact(6, "Frank Miller", "+1 234 567 8906", "In a meeting"),
            Contact(7, "Grace Lee", "+1 234 567 8907", "Coding..."),
        )
    }

    val lightBlue = Color(0xFF42A5F5)
    val deepBlue = Color(0xFF1976D2)
    val accentBlue = Color(0xFF64B5F6)
    val lightBlueBackground = Color(0xFFE3F2FD)
    val gradientStart = Color(0xFF42A5F5)
    val gradientEnd = Color(0xFF1E88E5)

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(gradientStart, gradientEnd)
                            )
                        )
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { /* Handle back */ },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                "Create Group",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.White
                            )
                            Text(
                                "Add members and set group info",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedContacts.isNotEmpty() && groupName.isNotBlank(),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { /* Create group */ },
                    containerColor = deepBlue,
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(12.dp, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Check,
                        "Create Group",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            lightBlueBackground.copy(alpha = 0.3f),
                            Color.White
                        )
                    )
                )
                .padding(padding)
        ) {
            // Group Info Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Group Photo with gradient border
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .shadow(8.dp, CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(gradientStart, gradientEnd)
                                    ),
                                    CircleShape
                                )
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { /* Select photo */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(lightBlueBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.AddCircle,
                                        contentDescription = "Add Photo",
                                        modifier = Modifier.size(36.dp),
                                        tint = lightBlue
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Add",
                                        fontSize = 11.sp,
                                        color = lightBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Group Name Input with modern styling
                        OutlinedTextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            label = {
                                Text(
                                    "Group Name",
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            placeholder = { Text("e.g., Family, Work Team...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = lightBlue,
                                focusedLabelColor = lightBlue,
                                cursorColor = lightBlue,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Face,
                                    contentDescription = null,
                                    tint = if (groupName.isNotEmpty()) lightBlue else Color.Gray
                                )
                            }
                        )
                    }
                }
            }

            // Selected Members Preview
            item {
                AnimatedVisibility(
                    visible = selectedContacts.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = deepBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${selectedContacts.size} member${if (selectedContacts.size > 1) "s" else ""} selected",
                                fontWeight = FontWeight.Bold,
                                color = deepBlue,
                                fontSize = 15.sp
                            )
                        }

                        LazyRow(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(contacts.filter { selectedContacts.contains(it.id) }) { contact ->
                                SelectedMemberChip(contact, lightBlue, accentBlue)
                            }
                        }
                    }
                }
            }

            // Section Header with icon
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        tint = deepBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Select Contacts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = lightBlueBackground
                    ) {
                        Text(
                            "${contacts.size} contacts",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = deepBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Contact List
            items(contacts) { contact ->
                ModernContactItem(
                    contact = contact,
                    isSelected = selectedContacts.contains(contact.id),
                    onToggle = {
                        selectedContacts = if (selectedContacts.contains(contact.id)) {
                            selectedContacts - contact.id
                        } else {
                            selectedContacts + contact.id
                        }
                    },
                    lightBlue = lightBlue,
                    deepBlue = deepBlue,
                    lightBlueBackground = lightBlueBackground
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SelectedMemberChip(contact: Contact, lightBlue: Color, accentBlue: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accentBlue.copy(alpha = 0.2f),
        modifier = Modifier.shadow(2.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(lightBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    contact.name.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                contact.name.split(" ").first(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ModernContactItem(
    contact: Contact,
    isSelected: Boolean,
    onToggle: () -> Unit,
    lightBlue: Color,
    deepBlue: Color,
    lightBlueBackground: Color
) {
    var scale by remember { mutableStateOf(1f) }

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
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scale)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) lightBlueBackground else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 2.dp),
        shape = RoundedCornerShape(20.dp),
        border = if (isSelected) BorderStroke(2.dp, lightBlue) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with status indicator
            Box {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (isSelected)
                                    listOf(lightBlue, deepBlue)
                                else
                                    listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
                            )
                        )
                        .border(
                            2.dp,
                            if (isSelected) Color.White else Color.Transparent,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        contact.name.first().toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }

                // Online indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .border(2.dp, Color.White, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Contact Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isSelected) deepBlue else Color.DarkGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    contact.status,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Normal
                )
            }

            // Custom Checkbox
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) lightBlue else Color.Transparent
                    )
                    .border(
                        2.dp,
                        if (isSelected) lightBlue else Color.Gray,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                    exit = scaleOut(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
                ) {
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

@Preview(showBackground = true)
@Composable
fun CreateGroupBodyPreview() {
    CreateGroupBody()
}