package com.example.rojgar.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import com.example.rojgar.R
import com.example.rojgar.model.CalendarEventModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MiniEventList(
    modifier: Modifier = Modifier,
    events: List<CalendarEventModel> = emptyList(),
    maxItems: Int = 3,
    showAllEvents: Boolean = false
) {
    val context = LocalContext.current
    val currentTime = System.currentTimeMillis()

    // Get events based on filter preference
    val displayEvents = if (showAllEvents) {
        // Show all events
        events.sortedBy { it.startTimeMillis }.take(maxItems)
    } else {
        // Get upcoming events (today and future)
        events
            .filter { event ->
                // Show events that are today or in the future, or events that are currently ongoing
                val eventEnd = event.endTimeMillis
                eventEnd > currentTime
            }
            .sortedBy { it.startTimeMillis }
            .take(maxItems)
    }

    Column(modifier = modifier) {
        Text(
            text = if (showAllEvents) "All Events" else "Upcoming Events",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (displayEvents.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color(0xFFF8FAFC)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.calendaricon),
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No upcoming events",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayEvents) { event ->
                    MiniEventCard(event = event)
                }

                // Show "View All" if there are more events
                if (events.size > maxItems) {
                    item {
                        Text(
                            text = "View all events",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF3B82F6)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    context.startActivity(
                                        Intent(context, CalendarActivity::class.java)
                                    )
                                }
                                .padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniEventCard(event: CalendarEventModel) {
    val context = LocalContext.current
    val startTime = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(event.startTimeMillis))
    val eventColor = Color(android.graphics.Color.parseColor(event.colorHex))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                context.startActivity(
                    Intent(context, CalendarActivity::class.java)
                )
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(eventColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = startTime,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1
                )
            }
        }
    }
}