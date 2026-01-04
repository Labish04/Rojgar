package com.example.rojgar.repository

import com.example.rojgar.model.EventModel

interface CalendarRepo {
    fun createEvent(
        event: EventModel,
        callback: (Boolean, String) -> Unit
    )

    fun updateEvent(
        event: EventModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteEvent(
        eventId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getEventById(
        eventId: String,
        callback: (Boolean, String, EventModel?) -> Unit
    )

    fun getEventsByUserId(
        userId: String,
        callback: (Boolean, String, List<EventModel>?) -> Unit
    )

    fun getEventsByDate(
        userId: String,
        date: String, // Format: "dd/MM/yyyy"
        callback: (Boolean, String, List<EventModel>?) -> Unit
    )

    fun getEventsByMonth(
        userId: String,
        month: Int, // 1-12
        year: Int,
        callback: (Boolean, String, List<EventModel>?) -> Unit
    )
}

