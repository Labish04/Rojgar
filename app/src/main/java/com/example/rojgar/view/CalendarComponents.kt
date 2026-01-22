package com.example.rojgar.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.model.CalendarEventModel
import com.example.rojgar.util.CalendarDateUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MiniCalendar(modifier: Modifier = Modifier, events: List<CalendarEventModel> = emptyList()) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

    val monthNames = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Get days of week (Sun, Mon, Tue, etc.)
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    // Generate calendar grid similar to CalendarActivity
    val calendarGrid = generateCalendarDays(currentYear, currentMonth)
    val todayCalendar = Calendar.getInstance()
    val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)
    val todayMonth = todayCalendar.get(Calendar.MONTH)
    val todayYear = todayCalendar.get(Calendar.YEAR)

    // Helper function to get event colors for a specific day
    fun getEventColorsForDay(day: Int): List<Color> {
        val (dayStart, dayEnd) = CalendarDateUtils.dayRangeMillis(currentYear, currentMonth, day)
        return events
            .filter { event -> event.startTimeMillis < dayEnd && event.endTimeMillis > dayStart }
            .map { event -> Color(android.graphics.Color.parseColor(event.colorHex)) }
            .distinct()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Month and Year
        Text(
            text = "${monthNames[currentMonth]} $currentYear",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Days of week header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(textAlign = TextAlign.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid (showing only 4 weeks for mini view)
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Show weeks 2-5 (skipping first and last week if incomplete)
            for (week in 1..4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (dayIndex in 0 until 7) {
                        val dayNumber = calendarGrid[week * 7 + dayIndex]
                        val isToday = dayNumber == todayDay && currentMonth == todayMonth && currentYear == todayYear && dayNumber > 0
                        val eventColors = if (dayNumber > 0) getEventColorsForDay(dayNumber) else emptyList()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(
                                    if (isToday) Color(0xFF3B82F6) else Color.Transparent
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
                                        fontSize = 10.sp,
                                        color = if (isToday) Color.White else Color.Black,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    )

                                    if (eventColors.isNotEmpty() && !isToday) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val displayColors = eventColors.take(2) // Show max 2 dots for mini calendar
                                            displayColors.forEachIndexed { index, color ->
                                                androidx.compose.foundation.Canvas(
                                                    modifier = Modifier.size(3.dp)
                                                ) {
                                                    drawCircle(color)
                                                }
                                                if (index < displayColors.size - 1) {
                                                    Spacer(modifier = Modifier.width(1.dp))
                                                }
                                            }
                                            if (eventColors.size > 2) {
                                                Spacer(modifier = Modifier.width(1.dp))
                                                Text(
                                                    text = "+",
                                                    fontSize = 6.sp,
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

        Spacer(modifier = Modifier.height(8.dp))

        // Current date display
        val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.time)
        Text(
            text = currentDate,
            style = TextStyle(
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        )
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