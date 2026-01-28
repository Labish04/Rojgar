package com.example.rojgar.repository

import android.content.Context
import android.util.Log
import com.example.rojgar.model.CalendarEventModel
import com.example.rojgar.utils.EventNotificationScheduler
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CalendarRepoImpl(private val context: Context) : CalendarRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("CalendarEvents")

    init {
        // Schedule event notifications when repository is initialized
        EventNotificationScheduler.scheduleEventNotifications(context)
    }

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
                    // Schedule notification for this event
                    scheduleEventNotification(eventWithId)
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
                // Clear old notification flags and reschedule
                clearEventNotificationFlags(event.eventId)
                scheduleEventNotification(updatedEvent)
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
                // Clear notification flags when event is deleted
                clearEventNotificationFlags(eventId)
                callback(true, "Event deleted successfully")
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message ?: "Failed to delete event")
            }
    }

    /**
     * Schedule notification for the event
     * The WorkManager will handle checking and sending notifications
     */
    private fun scheduleEventNotification(event: CalendarEventModel) {
        try {
            // Ensure the event notification worker is scheduled
            EventNotificationScheduler.scheduleEventNotifications(context)
            Log.d("CalendarRepo", "Event notification scheduled for: ${event.title}")
        } catch (e: Exception) {
            Log.e("CalendarRepo", "Error scheduling event notification: ${e.message}")
        }
    }

    /**
     * Clear notification sent flags when event is updated or deleted
     */
    private fun clearEventNotificationFlags(eventId: String) {
        try {
            database.getReference("EventNotificationsSent")
                .child(eventId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d("CalendarRepo", "Notification flags cleared for event: $eventId")
                }
                .addOnFailureListener { e ->
                    Log.e("CalendarRepo", "Error clearing notification flags: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("CalendarRepo", "Error in clearEventNotificationFlags: ${e.message}")
        }
    }
}