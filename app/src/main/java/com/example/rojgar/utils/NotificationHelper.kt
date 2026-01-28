package com.example.rojgar.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.rojgar.R
import com.example.rojgar.view.MainActivity
import com.example.rojgar.view.CompanyProfileActivity
import com.example.rojgar.view.JobSeekerProfileActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

object NotificationHelper {

    private const val TAG = "NotificationHelper"

    // Notification Channels
    const val CHANNEL_FOLLOW_ID = "follow_channel"
    const val CHANNEL_JOB_ID = "job_channel"
    const val CHANNEL_GENERAL_ID = "general_channel"
    const val CHANNEL_VERIFICATION_ID = "verification_channel"

    // Notification Types
    const val TYPE_FOLLOW = "follow"
    const val TYPE_JOB = "job"
    const val TYPE_GENERAL = "general"
    const val TYPE_VERIFICATION = "verification"

    /**
     * Create notification channels for Android 8.0+
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Follow Channel
            val followChannel = NotificationChannel(
                CHANNEL_FOLLOW_ID,
                "Follow Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new followers"
                enableVibration(true)
                enableLights(true)
            }

            // Job Channel
            val jobChannel = NotificationChannel(
                CHANNEL_JOB_ID,
                "Job Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new job postings"
                enableVibration(true)
                enableLights(true)
            }

            // General Channel
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL_ID,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            // Verification Channel
            val verificationChannel = NotificationChannel(
                CHANNEL_VERIFICATION_ID,
                "Verification Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for verification status"
                enableVibration(true)
                enableLights(true)
            }

            notificationManager.createNotificationChannel(followChannel)
            notificationManager.createNotificationChannel(jobChannel)
            notificationManager.createNotificationChannel(generalChannel)
            notificationManager.createNotificationChannel(verificationChannel)
        }
    }

    /**
     * Show notification based on type
     */
    fun showNotification(
        context: Context,
        title: String?,
        message: String?,
        type: String?,
        data: Map<String, String> = emptyMap()
    ) {
        val notificationType = type ?: TYPE_GENERAL

        when (notificationType) {
            TYPE_FOLLOW -> showFollowNotification(context, title, message, data)
            TYPE_VERIFICATION -> showVerificationNotification(context, title, message, data)
            TYPE_JOB -> showJobNotification(context, title, message, data)
            else -> showGeneralNotification(context, title, message)
        }
    }

    /**
     * Show follow notification when someone follows
     */
    private fun showFollowNotification(
        context: Context,
        title: String?,
        message: String?,
        data: Map<String, String>
    ) {
        val followerId = data["followerId"] ?: ""
        val followerType = data["followerType"]

        // Create intent based on follower type
        val intent = if (followerType == "Company") {
            Intent(context, CompanyProfileActivity::class.java).apply {
                putExtra("COMPANY_ID", followerId)
                putExtra("fromNotification", true)
            }
        } else {
            Intent(context, JobSeekerProfileActivity::class.java).apply {
                putExtra("JOB_SEEKER_ID", followerId)
                putExtra("fromNotification", true)
            }
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntent = PendingIntent.getActivity(
            context,
            followerId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_FOLLOW_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title ?: "New Follower")
            .setContentText(message ?: "Someone started following you")
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setContentIntent(pendingIntent)

        // Add view profile action
        val viewProfileIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("openFollowers", true)
        }
        val viewProfilePendingIntent = PendingIntent.getActivity(
            context,
            (followerId.hashCode() + 1),
            viewProfileIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        notificationBuilder.addAction(
            R.drawable.profile,
            "View Profile",
            viewProfilePendingIntent
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(followerId.hashCode(), notificationBuilder.build())
    }

    /**
     * Show verification notification
     */
    private fun showVerificationNotification(
        context: Context,
        title: String?,
        message: String?,
        data: Map<String, String>
    ) {
        val companyId = data["companyId"] ?: ""

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("openVerification", true)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            companyId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_VERIFICATION_ID)
            .setSmallIcon(R.drawable.verified_badge)
            .setContentTitle(title ?: "Verification Update")
            .setContentText(message ?: "Your verification status has been updated")
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(companyId.hashCode(), notificationBuilder.build())
    }

    /**
     * Show job notification
     */
    private fun showJobNotification(
        context: Context,
        title: String?,
        message: String?,
        data: Map<String, String>
    ) {
        val jobId = data["jobId"] ?: ""
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("jobId", jobId)
            putExtra("openJobDetails", true)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            jobId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_JOB_ID)
            .setSmallIcon(R.drawable.jobpost)
            .setContentTitle(title ?: "New Job Posted")
            .setContentText(message ?: "A new job opportunity has been posted")
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        // Add action to view job details
        val viewJobIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("jobId", jobId)
            putExtra("openJobDetails", true)
        }
        val viewJobPendingIntent = PendingIntent.getActivity(
            context,
            (jobId.hashCode() + 1),
            viewJobIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        notificationBuilder.addAction(
            R.drawable.jobpost,
            "View Job",
            viewJobPendingIntent
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(jobId.hashCode(), notificationBuilder.build())
    }

    /**
     * Show general notification
     */
    private fun showGeneralNotification(
        context: Context,
        title: String?,
        message: String?
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_GENERAL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title ?: "Rojgar")
            .setContentText(message ?: "You have a new notification")
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    /**
     * Send follow notification when someone follows a user
     */
    fun sendFollowNotification(
        context: Context,
        receiverUserId: String,
        followerName: String,
        followerId: String,
        followerType: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getUserFcmToken(receiverUserId) ?: return@launch

                val title = "New Follower"
                val body = "$followerName started following you"

                Log.d(TAG, "Attempting to send follow notification to token: $token")

                // Show local notification
                showNotification(
                    context,
                    title,
                    body,
                    TYPE_FOLLOW,
                    mapOf(
                        "followerId" to followerId,
                        "followerName" to followerName,
                        "followerType" to followerType
                    )
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error sending follow notification: ${e.message}")
            }
        }
    }

    /**
     * Send verification notification
     */
    fun sendVerificationNotification(
        context: Context,
        companyId: String,
        status: String,
        message: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getUserFcmToken(companyId) ?: return@launch

                val title = when (status) {
                    "approved" -> "Verification Approved ✅"
                    "rejected" -> "Verification Rejected ❌"
                    else -> "Verification Update"
                }

                // Show local notification
                showNotification(
                    context,
                    title,
                    message,
                    TYPE_VERIFICATION,
                    mapOf(
                        "companyId" to companyId,
                        "verificationStatus" to status
                    )
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error sending verification notification: ${e.message}")
            }
        }
    }

    /**
     * ⭐ OPTION 1: Send job post notification to ALL JOB SEEKERS ⭐
     * This is the broadcast approach - sends to everyone
     */
    fun sendJobPostNotificationToAll(
        context: Context,
        jobId: String,
        jobTitle: String,
        companyName: String,
        position: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "📢 Broadcasting job notification: $jobTitle")

                // Get all job seekers
                val jobSeekersSnapshot = FirebaseDatabase.getInstance()
                    .getReference("JobSeekers")
                    .get()
                    .await()

                if (!jobSeekersSnapshot.exists()) {
                    Log.w(TAG, "No job seekers found in database")
                    return@launch
                }

                var notificationCount = 0
                val title = "New Job Posted! 💼"
                val message = "$companyName is hiring for $position"

                // Iterate through all job seekers
                for (jobSeekerSnapshot in jobSeekersSnapshot.children) {
                    val jobSeekerId = jobSeekerSnapshot.key ?: continue

                    // Check if job seeker is active
                    val isActive = jobSeekerSnapshot.child("isActive").getValue(Boolean::class.java) ?: true
                    if (!isActive) {
                        Log.d(TAG, "Skipping inactive user: $jobSeekerId")
                        continue
                    }

                    // Save notification to database for this job seeker
                    saveNotificationToDatabase(
                        jobSeekerId,
                        title,
                        message,
                        TYPE_JOB,
                        mapOf(
                            "jobId" to jobId,
                            "jobTitle" to jobTitle,
                            "companyName" to companyName,
                            "position" to position
                        )
                    )
                    notificationCount++
                }

                Log.d(TAG, "✅ Job notification sent to $notificationCount job seekers")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending job post notifications: ${e.message}", e)
            }
        }
    }

    /**
     * Save notification to database for later retrieval
     */
    private suspend fun saveNotificationToDatabase(
        userId: String,
        title: String,
        message: String,
        type: String,
        data: Map<String, String>
    ) {
        try {
            val notificationId = UUID.randomUUID().toString()
            val notificationData = hashMapOf(
                "id" to notificationId,
                "title" to title,
                "message" to message,
                "type" to type,
                "data" to data,
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false
            )

            FirebaseDatabase.getInstance()
                .getReference("Notifications")
                .child(userId)
                .child(notificationId)
                .setValue(notificationData)
                .await()

            Log.d(TAG, "Notification saved for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving notification to database: ${e.message}")
        }
    }

    /**
     * Send general notification
     */
    fun sendGeneralNotification(
        context: Context,
        receiverUserId: String,
        title: String,
        message: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getUserFcmToken(receiverUserId) ?: return@launch

                // Show local notification
                showNotification(context, title, message, TYPE_GENERAL)

            } catch (e: Exception) {
                Log.e(TAG, "Error sending general notification: ${e.message}")
            }
        }
    }

    /**
     * Get user's FCM token from database
     */
    private suspend fun getUserFcmToken(userId: String): String? {
        return try {
            // Try to get from general fcmTokens node first
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("fcmTokens")
                .child(userId)
                .child("token")
                .get()
                .await()

            if (snapshot.exists()) {
                return snapshot.getValue(String::class.java)
            }

            // Fallback: check Company and JobSeeker nodes
            val companySnapshot = FirebaseDatabase.getInstance()
                .getReference("Companys")
                .child(userId)
                .child("fcmToken")
                .get()
                .await()

            if (companySnapshot.exists()) {
                return companySnapshot.getValue(String::class.java)
            }

            val jobSeekerSnapshot = FirebaseDatabase.getInstance()
                .getReference("JobSeekers")
                .child(userId)
                .child("fcmToken")
                .get()
                .await()

            jobSeekerSnapshot.getValue(String::class.java)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting FCM token: ${e.message}")
            null
        }
    }

    /**
     * Mark notification as read
     */
    fun markNotificationAsRead(userId: String, notificationId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseDatabase.getInstance()
                    .getReference("Notifications")
                    .child(userId)
                    .child(notificationId)
                    .child("isRead")
                    .setValue(true)
                    .await()

                Log.d(TAG, "Notification marked as read: $notificationId")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking notification as read: ${e.message}")
            }
        }
    }

    /**
     * Get unread notification count
     */
    suspend fun getUnreadNotificationCount(userId: String): Int {
        return try {
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("Notifications")
                .child(userId)
                .get()
                .await()

            var unreadCount = 0
            for (notificationSnapshot in snapshot.children) {
                val isRead = notificationSnapshot.child("isRead").getValue(Boolean::class.java) ?: false
                if (!isRead) {
                    unreadCount++
                }
            }

            Log.d(TAG, "Unread notifications for $userId: $unreadCount")
            unreadCount
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread notification count: ${e.message}")
            0
        }
    }

    /**
     * Delete all notifications for a user
     */
    fun clearAllNotifications(userId: String, callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseDatabase.getInstance()
                    .getReference("Notifications")
                    .child(userId)
                    .removeValue()
                    .await()

                Log.d(TAG, "All notifications cleared for user: $userId")
                callback(true, "All notifications cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing notifications: ${e.message}")
                callback(false, "Error: ${e.message}")
            }
        }
    }
}