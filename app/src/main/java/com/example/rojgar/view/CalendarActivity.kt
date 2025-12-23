package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue
import com.example.rojgar.ui.theme.NormalBlue
import com.example.rojgar.ui.theme.SkyBlue
import com.example.rojgar.ui.theme.White
import com.example.rojgar.view.ui.theme.RojgarTheme
import java.time.LocalDate
import java.time.YearMonth

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
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Blue)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                    contentDescription = null
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Calendar", style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Calendar Title
            Text(
                text = "November",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                textAlign = TextAlign.Center
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "2006",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 0.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_keyboard_arrow_down_24),
                        contentDescription = null
                    )
                }
            }

            // Days of Week Header
            Card (
                colors = CardDefaults.cardColors(
                    containerColor = White
                )
            ){
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 15.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val daysOfWeek = listOf("SU", "MO", "TU", "WE", "TH", "FR", "SA")
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Grid
                val calendarDays = generateCalendarDays()
                Column {
                    for (week in 0 until 6) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (dayIndex in 0 until 7) {
                                val dayNumber = calendarDays[week * 7 + dayIndex]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dayNumber > 0) {
                                        // Highlight Nov 11 with blue background
                                        val isHighlighted = dayNumber == 11
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(
                                                    if (isHighlighted) Color(0xFF2196F3) else Color.Transparent
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayNumber.toString(),
                                                fontSize = 16.sp,
                                                color = if (isHighlighted) Color.White else Color.Black,
                                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    } else {
                                        // Empty cell for days outside the month
                                        Text(
                                            text = "",
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Events Section
            Text(
                text = "Events",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Event Item
            EventItem(
                title = "Job interview of Google",
                date = "Nov 11",
                time = "8:00AM"
            )
        }
    }
}

@Composable
fun EventItem(title: String, date: String, time: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = SkyBlue,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = date,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = time,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

private fun generateCalendarDays(): List<Int> {
    // November 2006 starts on Wednesday (index 3)
    val daysInMonth = 30
    val startDayOfWeek = 3  // Wednesday
    val totalCells = 42  // 6 weeks * 7 days

    return List(totalCells) { index ->
        if (index >= startDayOfWeek && index < startDayOfWeek + daysInMonth) {
            index - startDayOfWeek + 1
        } else {
            0
        }
    }
}

@Preview
@Composable
fun CalendarBodyPreview() {
    CalendarBody()
}

@Preview
@Composable
fun EventItemPreview() {
    EventItem(
        title = "Job interview of Google",
        date = "Nov 11",
        time = "8:00AM"
    )
}