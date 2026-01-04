package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rojgar.R
import com.example.rojgar.ui.theme.*
import com.example.rojgar.view.ui.theme.RojgarTheme
import com.example.rojgar.repository.CalendarRepoImpl
import com.example.rojgar.viewmodel.CalendarViewModel
import com.example.rojgar.util.CalendarDateUtils
import com.example.rojgar.model.CalendarEventModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.LaunchedEffect
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.text.BasicTextField

class CalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RojgarTheme {
                CalendarBody()
            }
        }
    }
}

@Composable
fun CalendarBody() {
    val context = LocalContext.current
    val calendarViewModel = remember { CalendarViewModel(CalendarRepoImpl()) }
    val events by calendarViewModel.events.observeAsState(emptyList())
    val selectedDayEvents by calendarViewModel.selectedDayEvents.observeAsState(emptyList())
    val loading by calendarViewModel.loading.observeAsState(false)
    val message by calendarViewModel.message.observeAsState("")

    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    // Dialog states
    var showAddEventDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<CalendarEventModel?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }
    var showAllEventsSheet by remember { mutableStateOf(false) }
    var allEventsSearchQuery by remember { mutableStateOf("") }
    var allEventsSortMode by remember { mutableStateOf("upcoming") } // "upcoming" or "newest"

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Observe all events for the current month
    LaunchedEffect(userId, currentMonth, currentYear) {
        if (userId.isNotEmpty()) {
            calendarViewModel.observeAllEventsForUser(userId)
        }
    }

    // Observe events for selected day
    LaunchedEffect(userId, selectedDay, currentMonth, currentYear) {
        if (userId.isNotEmpty()) {
            val (dayStart, dayEnd) = CalendarDateUtils.dayRangeMillis(currentYear, currentMonth, selectedDay)
            calendarViewModel.observeEventsForUserInRange(userId, dayStart, dayEnd)
        }
    }

    // Show message as snackbar
    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            // Could add snackbar here, for now just reset message
            calendarViewModel.message
        }
    }

    // Event management functions
    val addEvent: (CalendarEventModel) -> Unit = { event ->
        val eventWithUserId = event.copy(userId = userId)
        calendarViewModel.addEvent(eventWithUserId) { success, message, eventId ->
            if (success) {
                showAddEventDialog = false
            }
        }
    }

    val updateEvent: (CalendarEventModel) -> Unit = { event ->
        calendarViewModel.updateEvent(event) { success, message ->
            if (success) {
                editingEvent = null
            }
        }
    }

    val deleteEvent: (String) -> Unit = { eventId ->
        calendarViewModel.deleteEvent(eventId) { success, message ->
            if (success) {
                showDeleteConfirm = null
            }
        }
    }

    // Helper function to get event colors for a specific day
    fun getEventColorsForDay(day: Int): List<Color> {
        if (userId.isEmpty()) return emptyList()
        val (dayStart, dayEnd) = CalendarDateUtils.dayRangeMillis(currentYear, currentMonth, day)
        return events
            .filter { event -> event.startTimeMillis < dayEnd && event.endTimeMillis > dayStart }
            .map { event -> Color(android.graphics.Color.parseColor(event.colorHex)) }
            .distinct()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                modifier = Modifier.size(52.dp),
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ) {
                Icon(
                    painter = painterResource(R.drawable.addicon),
                    contentDescription = "Add Event",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFAFCEFC),
                            Color(0xFF5594FA)
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { (context as? ComponentActivity)?.finish() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        "Calendar",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Calendar Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color.Black.copy(alpha = 0.25f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Month Navigation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentMonth == 0) {
                                        currentMonth = 11
                                        currentYear--
                                    } else {
                                        currentMonth--
                                    }
                                    selectedDay = 1 // Reset selection
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                                    contentDescription = "Previous Month",
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val monthNames = arrayOf(
                                    "January", "February", "March", "April", "May", "June",
                                    "July", "August", "September", "October", "November", "December"
                                )
                                Text(
                                    text = "${monthNames[currentMonth]} $currentYear",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = 0.5.sp
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (currentMonth == 11) {
                                        currentMonth = 0
                                        currentYear++
                                    } else {
                                        currentMonth++
                                    }
                                    selectedDay = 1 // Reset selection
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_keyboard_arrow_right_24),
                                    contentDescription = "Next Month",
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Subtle divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFE2E8F0).copy(alpha = 0.5f))
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Days of Week
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
                            daysOfWeek.forEach { day ->
                                Text(
                                    text = day,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Calendar Grid
                        val calendarDays = generateCalendarDays(currentYear, currentMonth)
                        val todayCalendar = Calendar.getInstance()
                        val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)
                        val todayMonth = todayCalendar.get(Calendar.MONTH)
                        val todayYear = todayCalendar.get(Calendar.YEAR)

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (week in 0 until 6) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    for (dayIndex in 0 until 7) {
                                        val dayNumber = calendarDays[week * 7 + dayIndex]
                                        val isSelected = dayNumber == selectedDay && dayNumber > 0
                                        val isToday = dayNumber == todayDay && currentMonth == todayMonth && currentYear == todayYear && dayNumber > 0
                                        val eventColors = if (dayNumber > 0) getEventColorsForDay(dayNumber) else emptyList()

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(1.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable(
                                                    enabled = dayNumber > 0,
                                                    indication = null,
                                                    interactionSource = remember { MutableInteractionSource() }
                                                ) {
                                                    if (dayNumber > 0) selectedDay = dayNumber
                                                }
                                                .background(
                                                    when {
                                                        isSelected -> Brush.linearGradient(
                                                            colors = listOf(
                                                                Color(0xFF3B82F6),
                                                                Color(0xFF2563EB)
                                                            )
                                                        )
                                                        isToday -> Brush.linearGradient(
                                                            colors = listOf(
                                                                Color(0xFFE0F2FE),
                                                                Color(0xFFB3E5FC)
                                                            )
                                                        )
                                                        else -> Brush.linearGradient(
                                                            colors = listOf(
                                                                Color.Transparent,
                                                                Color.Transparent
                                                            )
                                                        )
                                                    }
                                                )
                                                .border(
                                                    width = if (isToday && !isSelected) 1.dp else 0.dp,
                                                    color = if (isToday && !isSelected) Color(0xFF2196F3) else Color.Transparent,
                                                    shape = RoundedCornerShape(14.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (dayNumber > 0) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = dayNumber.toString(),
                                                        fontSize = if (isSelected) 18.sp else 16.sp,
                                                        color = when {
                                                            isSelected -> Color.White
                                                            isToday -> Color(0xFF1976D2)
                                                            else -> Color(0xFF0F172A)
                                                        },
                                                        fontWeight = when {
                                                            isSelected -> FontWeight.Bold
                                                            isToday -> FontWeight.SemiBold
                                                            else -> FontWeight.Medium
                                                        }
                                                    )

                                                    if (eventColors.isNotEmpty() && !isSelected) {
                                                        Spacer(modifier = Modifier.height(3.dp))
                                                        Row(
                                                            horizontalArrangement = Arrangement.Center,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            val displayColors = eventColors.take(3)
                                                            displayColors.forEachIndexed { index, color ->
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(5.dp)
                                                                        .clip(CircleShape)
                                                                        .background(color)
                                                                )
                                                                if (index < displayColors.size - 1) {
                                                                    Spacer(modifier = Modifier.width(1.5.dp))
                                                                }
                                                            }
                                                            if (eventColors.size > 3) {
                                                                Spacer(modifier = Modifier.width(2.dp))
                                                                Text(
                                                                    text = "+${eventColors.size - 3}",
                                                                    fontSize = 7.sp,
                                                                    color = Color(0xFF64748B),
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
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Events Section
                val selectedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(
                    Calendar.getInstance().apply {
                        set(currentYear, currentMonth, selectedDay)
                    }.time
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Events for $selectedDate",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (events.isNotEmpty()) {
                        AssistChip(
                            onClick = { showAllEventsSheet = true },
                            label = {
                                Text(
                                    text = "See all",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Event Items
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    )
                ) {
                    if (loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF2196F3),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else if (selectedDayEvents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    modifier = Modifier.size(80.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFF8FAFC),
                                    shadowElevation = 8.dp
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.calendaricon),
                                        contentDescription = null,
                                        tint = Color(0xFFCBD5E1),
                                        modifier = Modifier
                                            .padding(20.dp)
                                            .size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "No events scheduled",
                                    color = Color(0xFF475569),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap the + button to add your first event for this day",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(selectedDayEvents.sortedBy { it.startTimeMillis }) { event ->
                                EventCard(
                                    event = event,
                                    onEdit = { editingEvent = event },
                                    onDelete = { showDeleteConfirm = event.eventId }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add/Edit Event Dialog
        if (showAddEventDialog || editingEvent != null) {
            EventDialog(
                event = editingEvent,
                selectedDate = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, selectedDay)
                }.time,
                onDismiss = {
                    showAddEventDialog = false
                    editingEvent = null
                },
                onSave = { event ->
                    if (editingEvent != null) {
                        updateEvent(event)
                    } else {
                        addEvent(event)
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        showDeleteConfirm?.let { eventId ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = null },
                title = { Text("Delete Event") },
                text = { Text("Are you sure you want to delete this event?") },
                confirmButton = {
                    TextButton(
                        onClick = { deleteEvent(eventId) }
                    ) {
                        Text("Delete", color = Color(0xFFEF4444))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirm = null }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // All Events Modal Bottom Sheet
        if (showAllEventsSheet) {
            AllEventsBottomSheet(
                events = events,
                searchQuery = allEventsSearchQuery,
                sortMode = allEventsSortMode,
                onSearchQueryChange = { allEventsSearchQuery = it },
                onSortModeChange = { allEventsSortMode = it },
                onEventEdit = { event ->
                    editingEvent = event
                    showAllEventsSheet = false
                },
                onEventDelete = { eventId ->
                    showDeleteConfirm = eventId
                },
                onDismiss = { showAllEventsSheet = false }
            )
        }
    }
}

// Helper function to group events by date
fun groupEventsByDate(events: List<CalendarEventModel>): Map<String, List<CalendarEventModel>> {
    return events.groupBy { event ->
        val calendar = Calendar.getInstance().apply {
            timeInMillis = event.startTimeMillis
        }
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.time)
    }
}

// Helper function to sort events
fun sortEvents(events: List<CalendarEventModel>, mode: String): List<CalendarEventModel> {
    return when (mode) {
        "upcoming" -> events.sortedBy { it.startTimeMillis }
        "newest" -> events.sortedByDescending { it.createdAtMillis }
        else -> events.sortedBy { it.startTimeMillis }
    }
}

// Helper function to filter events by search query
fun filterEventsByQuery(events: List<CalendarEventModel>, query: String): List<CalendarEventModel> {
    if (query.isBlank()) return events

    val lowerQuery = query.lowercase()
    return events.filter { event ->
        event.title.lowercase().contains(lowerQuery) ||
        event.description.lowercase().contains(lowerQuery) ||
        event.location.lowercase().contains(lowerQuery)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllEventsBottomSheet(
    events: List<CalendarEventModel>,
    searchQuery: String,
    sortMode: String,
    onSearchQueryChange: (String) -> Unit,
    onSortModeChange: (String) -> Unit,
    onEventEdit: (CalendarEventModel) -> Unit,
    onEventDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Events",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(R.drawable.outline_keyboard_arrow_right_24),
                        contentDescription = "Close",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search and Sort Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search events...") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.searchicon),
                            contentDescription = "Search",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Sort Toggle
                AssistChip(
                    onClick = {
                        onSortModeChange(if (sortMode == "upcoming") "newest" else "upcoming")
                    },
                    label = {
                        Text(
                            text = if (sortMode == "upcoming") "Upcoming" else "Newest",
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(
                                if (sortMode == "upcoming") R.drawable.outline_keyboard_arrow_right_24
                                else R.drawable.outline_keyboard_arrow_right_24
                            ),
                            contentDescription = "Sort",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Events List
            val filteredEvents = filterEventsByQuery(events, searchQuery)
            val sortedEvents = sortEvents(filteredEvents, sortMode)
            val groupedEvents = groupEventsByDate(sortedEvents)

            if (groupedEvents.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.calendaricon),
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No events match your search"
                                   else "No events yet",
                            color = Color(0xFF64748B),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        if (searchQuery.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the + button to create your first event",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Grouped Events
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedEvents.forEach { (date, dayEvents) ->
                        item {
                            // Date Header
                            Text(
                                text = date,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF374151),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(dayEvents) { event ->
                            AllEventsEventCard(
                                event = event,
                                onEdit = { onEventEdit(event) },
                                onDelete = { onEventDelete(event.eventId) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AllEventsEventCard(
    event: CalendarEventModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val startTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(event.startTimeMillis))
    val endTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(event.endTimeMillis))
    val eventColor = Color(android.graphics.Color.parseColor(event.colorHex))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(eventColor)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$startTime - $endTime",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )

                if (event.location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.location,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_edit_24),
                        contentDescription = "Edit Event",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = "Delete Event",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: CalendarEventModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val startTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(event.startTimeMillis))
    val endTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(event.endTimeMillis))
    val eventColor = Color(android.graphics.Color.parseColor(event.colorHex))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = eventColor.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced Color indicator
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(eventColor)
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A),
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF64748B)
                        )
                        Text(
                            text = "$startTime - $endTime",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (event.location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF94A3B8)
                        )
                        Text(
                            text = event.location,
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (event.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.description,
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 2,
                        lineHeight = 18.sp
                    )
                }
            }

            // Enhanced action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onEdit() },
                    shape = CircleShape,
                    color = Color(0xFFF1F5F9)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_edit_24),
                        contentDescription = "Edit Event",
                        tint = Color(0xFF64748B),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onDelete() },
                    shape = CircleShape,
                    color = Color(0xFFFEF2F2)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = "Delete Event",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
            }
        }
    }
}

private fun generateCalendarDays(year: Int, month: Int): List<Int> {
    val calendar = Calendar.getInstance().apply {
        set(year, month, 1)
    }

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday, 1 = Monday, etc.
    val totalCells = 42

    return List(totalCells) { index ->
        if (index >= startDayOfWeek && index < startDayOfWeek + daysInMonth) {
            index - startDayOfWeek + 1
        } else {
            0
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EventDialog(
    event: CalendarEventModel?,
    selectedDate: Date,
    onDismiss: () -> Unit,
    onSave: (CalendarEventModel) -> Unit
) {
    var title by remember { mutableStateOf(event?.title ?: "") }
    var description by remember { mutableStateOf(event?.description ?: "") }
    var location by remember { mutableStateOf(event?.location ?: "") }
    var colorHex by remember { mutableStateOf(event?.colorHex ?: "#3B82F6") }

    val isEditing = event != null

    // Initialize dates and times properly
    val initialStartCalendar = remember {
        Calendar.getInstance().apply {
            if (isEditing && event != null) {
                timeInMillis = event.startTimeMillis
            } else {
                time = selectedDate
            }
        }
    }

    val initialEndCalendar = remember {
        Calendar.getInstance().apply {
            if (isEditing && event != null) {
                timeInMillis = event.endTimeMillis
            } else {
                time = selectedDate
                add(Calendar.HOUR_OF_DAY, 1) // +1 hour from selected date
            }
        }
    }

    // Separate state for each date/time field
    var startDateMillis by remember { mutableStateOf(initialStartCalendar.timeInMillis) }
    var startTimeMillis by remember { mutableStateOf(initialStartCalendar.timeInMillis) }
    var endDateMillis by remember { mutableStateOf(initialEndCalendar.timeInMillis) }
    var endTimeMillis by remember { mutableStateOf(initialEndCalendar.timeInMillis) }

    // Picker dialog states
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var activeDateTimeField by remember { mutableStateOf("") }

    val colorOptions = listOf(
        "#3B82F6", "#EF4444", "#10B981", "#F59E0B", "#8B5CF6", "#EC4899", "#06B6D4"
    )

    // Validation: end time must be after start time
    val isValidTimeRange = remember(startTimeMillis, startDateMillis, endTimeMillis, endDateMillis) {
        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = startDateMillis
            val timeCal = Calendar.getInstance().apply { timeInMillis = startTimeMillis }
            set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
        }

        val endCalendar = Calendar.getInstance().apply {
            timeInMillis = endDateMillis
            val timeCal = Calendar.getInstance().apply { timeInMillis = endTimeMillis }
            set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
        }

        endCalendar.timeInMillis > startCalendar.timeInMillis
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit Event" else "Add Event",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = title.isEmpty()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Date & Time Pickers
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Start", fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                showDatePicker = true
                                activeDateTimeField = "startDate"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(startDateMillis)))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                showTimePicker = true
                                activeDateTimeField = "startTime"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(startTimeMillis)))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("End", fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                showDatePicker = true
                                activeDateTimeField = "endDate"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(endDateMillis)))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                showTimePicker = true
                                activeDateTimeField = "endTime"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(endTimeMillis)))
                        }
                    }
                }

                // Show validation error if times are invalid
                if (!isValidTimeRange) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "End time must be after start time",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Color Picker
                Text("Color", fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { color ->
                        val isSelected = colorHex == color
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) Color.Black else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_keyboard_arrow_right_24),
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isNotEmpty() && isValidTimeRange) {
                                // Combine date and time for start
                                val startCalendar = Calendar.getInstance().apply {
                                    timeInMillis = startDateMillis
                                    val timeCal = Calendar.getInstance().apply { timeInMillis = startTimeMillis }
                                    set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                                    set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                                }

                                // Combine date and time for end
                                val endCalendar = Calendar.getInstance().apply {
                                    timeInMillis = endDateMillis
                                    val timeCal = Calendar.getInstance().apply { timeInMillis = endTimeMillis }
                                    set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                                    set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                                }

                                val newEvent = CalendarEventModel(
                                    eventId = event?.eventId ?: "",
                                    userId = event?.userId ?: "",
                                    title = title,
                                    description = description,
                                    location = location,
                                    startTimeMillis = startCalendar.timeInMillis,
                                    endTimeMillis = endCalendar.timeInMillis,
                                    colorHex = colorHex
                                )
                                onSave(newEvent)
                            }
                        },
                        enabled = title.isNotEmpty() && isValidTimeRange
                    ) {
                        Text(if (isEditing) "Update" else "Save")
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val initialDateMillis = when (activeDateTimeField) {
            "startDate" -> startDateMillis
            "endDate" -> endDateMillis
            else -> System.currentTimeMillis()
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedMillis ->
                        when (activeDateTimeField) {
                            "startDate" -> startDateMillis = selectedMillis
                            "endDate" -> endDateMillis = selectedMillis
                        }
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = when (activeDateTimeField) {
                            "startDate" -> "Select Start Date"
                            "endDate" -> "Select End Date"
                            else -> "Select Date"
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val initialTimeMillis = when (activeDateTimeField) {
            "startTime" -> startTimeMillis
            "endTime" -> endTimeMillis
            else -> System.currentTimeMillis()
        }

        val initialCalendar = Calendar.getInstance().apply { timeInMillis = initialTimeMillis }

        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }

                when (activeDateTimeField) {
                    "startTime" -> startTimeMillis = calendar.timeInMillis
                    "endTime" -> endTimeMillis = calendar.timeInMillis
                }
                showTimePicker = false
            },
            initialHour = initialCalendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = initialCalendar.get(Calendar.MINUTE)
        )
    }
}

// Material3 TimePicker Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    initialHour: Int = 12,
    initialMinute: Int = 0
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(timePickerState.hour, timePickerState.minute)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview
@Composable
fun CalendarBodyPreview() {
    CalendarBody()
}
