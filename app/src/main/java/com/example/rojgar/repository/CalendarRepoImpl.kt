package com.example.rojgar.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.rojgar.model.CalendarEventModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CalendarRepoImpl : CalendarRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("CalendarEvents")

    override fun observeAllEventsForUser(
        userId: String,
        callback: (Boolean, String, List<CalendarEventModel>?) -> Unit
    ) {
        ref.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val events = mutableListOf<CalendarEventModel>()
                        for (data in snapshot.children) {
                            try {
                                val event = data.getValue(CalendarEventModel::class.java)
                                if (event != null) {
                                    events.add(event)
                                }
                            } catch (e: Exception) {
                                continue
                            }
                        }
                        // Sort by start time
                        events.sortBy { it.startTimeMillis }
                        callback(true, "Events fetched successfully", events)
                    } else {
                        callback(true, "No events found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun observeEventsForUserInRange(
        userId: String,
        startMillis: Long,
        endMillis: Long,
        callback: (Boolean, String, List<CalendarEventModel>?) -> Unit
    ) {
        ref.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val events = mutableListOf<CalendarEventModel>()
                        for (data in snapshot.children) {
                            try {
                                val event = data.getValue(CalendarEventModel::class.java)
                                if (event != null) {
                                    // Check if event overlaps with the date range
                                    // Event overlaps if: event.start < rangeEnd AND event.end > rangeStart
                                    if (event.startTimeMillis < endMillis && event.endTimeMillis > startMillis) {
                                        events.add(event)
                                    }
                                }
                            } catch (e: Exception) {
                                continue
                            }
                        }
                        // Sort by start time
                        events.sortBy { it.startTimeMillis }
                        callback(true, "Events fetched successfully", events)
                    } else {
                        callback(true, "No events found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getEventById(
        eventId: String,
        callback: (Boolean, String, CalendarEventModel?) -> Unit
    ) {
        ref.child(eventId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    try {
                        val event = snapshot.getValue(CalendarEventModel::class.java)
                        if (event != null) {
                            callback(true, "Event found", event)
                        } else {
                            callback(false, "Event not found", null)
                        }
                    } catch (e: Exception) {
                        callback(false, "Error parsing event data", null)
                    }
                } else {
                    callback(false, "Event not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun addEvent(
        event: CalendarEventModel,
        callback: (Boolean, String, String?) -> Unit
    ) {
        val eventId = ref.push().key
        if (eventId != null) {
            val eventWithId = event.copy(
                eventId = eventId,
                updatedAtMillis = System.currentTimeMillis()
            )
            ref.child(eventId).setValue(eventWithId)
                .addOnSuccessListener {
                    callback(true, "Event added successfully", eventId)
                }
                .addOnFailureListener { exception ->
                    callback(false, exception.message ?: "Failed to add event", null)
                }
        } else {
            callback(false, "Failed to generate event ID", null)
        }
    }

    override fun updateEvent(
        event: CalendarEventModel,
        callback: (Boolean, String) -> Unit
    ) {
        val updatedEvent = event.copy(updatedAtMillis = System.currentTimeMillis())
        ref.child(event.eventId).setValue(updatedEvent)
            .addOnSuccessListener {
                callback(true, "Event updated successfully")
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message ?: "Failed to update event")
            }
    }

    override fun deleteEvent(
        eventId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(eventId).removeValue()
            .addOnSuccessListener {
                callback(true, "Event deleted successfully")
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message ?: "Failed to delete event")
            }
    }
}
