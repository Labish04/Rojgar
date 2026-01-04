package com.example.rojgar.repository

import com.example.rojgar.model.CalendarEventModel

interface CalendarRepo {
    fun observeAllEventsForUser(
        userId: String,
        callback: (Boolean, String, List<CalendarEventModel>?) -> Unit
    )

    fun observeEventsForUserInRange(
        userId: String,
        startMillis: Long,
        endMillis: Long,
        callback: (Boolean, String, List<CalendarEventModel>?) -> Unit
    )

    fun getEventById(
        eventId: String,
        callback: (Boolean, String, CalendarEventModel?) -> Unit
    )

    fun addEvent(
        event: CalendarEventModel,
        callback: (Boolean, String, String?) -> Unit
    )

    fun updateEvent(
        event: CalendarEventModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteEvent(
        eventId: String,
        callback: (Boolean, String) -> Unit
    )
}