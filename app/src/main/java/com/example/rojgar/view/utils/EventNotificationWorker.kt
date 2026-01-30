package com.example.rojgar.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rojgar.model.CalendarEventModel
import com.example.rojgar.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Worker that periodically checks for upcoming calendar events and sends notifications
 */
class EventNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = FirebaseDatabase.getInstance()
    private val eventsRef = database.getReference("CalendarEvents")

    companion object {
        private const val TAG = "EventNotificationWorker"
        const val WORK_NAME = "event_notification_worker"

        // Notification reminder times (in minutes before event)
        const val REMINDER_15_MIN = 15L * 60 * 1000 // 15 minutes
        const val REMINDER_30_MIN = 30L * 60 * 1000 // 30 minutes
        const val REMINDER_1_HOUR = 60L * 60 * 1000 // 1 hour
        const val REMINDER_1_DAY = 24L * 60 * 60 * 1000 // 1 day
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting event notification check")

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Log.d(TAG, "No user logged in, skipping notification check")
                return Result.success()
            }

            checkAndSendEventNotifications(userId)

            Log.d(TAG, "Event notification check completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking event notifications: ${e.message}", e)
            Result.retry()
        }
    }

    private suspend fun checkAndSendEventNotifications(userId: String) {
        try {
            val currentTime = System.currentTimeMillis()

            // Get all events for the user
            val snapshot = eventsRef
                .orderByChild("userId")
                .equalTo(userId)
                .get()
                .await()

            if (!snapshot.exists()) {
                Log.d(TAG, "No events found for user: $userId")
                return
            }

            val events = mutableListOf<CalendarEventModel>()
            for (data in snapshot.children) {
                try {
                    val event = data.getValue(CalendarEventModel::class.java)
                    if (event != null && event.startTimeMillis > currentTime) {
                        events.add(event)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing event: ${e.message}")
                }
            }

            Log.d(TAG, "Found ${events.size} upcoming events")

            // Check each event for notification triggers
            for (event in events) {
                checkEventForNotification(event, currentTime, userId)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in checkAndSendEventNotifications: ${e.message}", e)
        }
    }

    private suspend fun checkEventForNotification(
        event: CalendarEventModel,
        currentTime: Long,
        userId: String
    ) {
        val timeUntilEvent = event.startTimeMillis - currentTime

        // Check if we should send a notification
        val shouldNotify = when {
            // Event starting in next 2 minutes (at event time)
            timeUntilEvent in 0..2 * 60 * 1000 -> {
                if (!hasNotificationBeenSent(event.eventId, "at_event")) {
                    sendEventNotification(event, userId, "Event Starting Now!")
                    markNotificationAsSent(event.eventId, "at_event")
                    true
                } else false
            }

            // 15 minutes before
            timeUntilEvent in REMINDER_15_MIN..(REMINDER_15_MIN + 2 * 60 * 1000) -> {
                if (!hasNotificationBeenSent(event.eventId, "15_min")) {
                    sendEventNotification(event, userId, "Event in 15 minutes")
                    markNotificationAsSent(event.eventId, "15_min")
                    true
                } else false
            }

            // 30 minutes before
            timeUntilEvent in REMINDER_30_MIN..(REMINDER_30_MIN + 2 * 60 * 1000) -> {
                if (!hasNotificationBeenSent(event.eventId, "30_min")) {
                    sendEventNotification(event, userId, "Event in 30 minutes")
                    markNotificationAsSent(event.eventId, "30_min")
                    true
                } else false
            }

            // 1 hour before
            timeUntilEvent in REMINDER_1_HOUR..(REMINDER_1_HOUR + 2 * 60 * 1000) -> {
                if (!hasNotificationBeenSent(event.eventId, "1_hour")) {
                    sendEventNotification(event, userId, "Event in 1 hour")
                    markNotificationAsSent(event.eventId, "1_hour")
                    true
                } else false
            }

            // 1 day before
            timeUntilEvent in REMINDER_1_DAY..(REMINDER_1_DAY + 2 * 60 * 1000) -> {
                if (!hasNotificationBeenSent(event.eventId, "1_day")) {
                    sendEventNotification(event, userId, "Event tomorrow")
                    markNotificationAsSent(event.eventId, "1_day")
                    true
                } else false
            }

            else -> false
        }

        if (shouldNotify) {
            Log.d(TAG, "Notification sent for event: ${event.title}")
        }
    }

    private suspend fun hasNotificationBeenSent(eventId: String, reminderType: String): Boolean {
        return try {
            val snapshot = database.getReference("EventNotificationsSent")
                .child(eventId)
                .child(reminderType)
                .get()
                .await()

            snapshot.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking notification status: ${e.message}")
            false
        }
    }

    private suspend fun markNotificationAsSent(eventId: String, reminderType: String) {
        try {
            database.getReference("EventNotificationsSent")
                .child(eventId)
                .child(reminderType)
                .setValue(System.currentTimeMillis())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as sent: ${e.message}")
        }
    }

    private fun sendEventNotification(event: CalendarEventModel, userId: String, timePrefix: String) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val eventTime = dateFormat.format(Date(event.startTimeMillis))

        val title = "📅 $timePrefix"
        val message = buildString {
            append(event.title)
            if (event.location.isNotEmpty()) {
                append(" • ")
                append(event.location)
            }
        }

        NotificationHelper.sendEventReminderNotification(
            context = applicationContext,
            userId = userId,
            eventId = event.eventId,
            eventTitle = event.title,
            eventDescription = event.description,
            eventLocation = event.location,
            eventStartTime = event.startTimeMillis,
            eventEndTime = event.endTimeMillis,
            reminderMessage = timePrefix
        )
    }
}