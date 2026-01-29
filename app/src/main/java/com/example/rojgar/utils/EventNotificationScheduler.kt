package com.example.rojgar.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.rojgar.utils.EventNotificationWorker
import java.util.concurrent.TimeUnit

/**
 * Scheduler for event notification worker
 */
object EventNotificationScheduler {

    private const val TAG = "EventNotificationScheduler"

    /**
     * Schedule periodic event notification checks
     * Runs every 15 minutes to check for upcoming events
     */
    fun scheduleEventNotifications(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<EventNotificationWorker>(
            15, // Repeat interval
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EventNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            workRequest
        )
    }

    /**
     * Cancel event notification checks
     */
    fun cancelEventNotifications(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(EventNotificationWorker.WORK_NAME)
    }

    /**
     * Check if event notifications are scheduled
     */
    fun isEventNotificationScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(EventNotificationWorker.WORK_NAME)
            .get()
        return workInfos.isNotEmpty()
    }
}