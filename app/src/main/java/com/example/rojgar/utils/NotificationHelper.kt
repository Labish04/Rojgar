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
    const val CHANNEL_MESSAGE_ID = "message_channel"
    const val CHANNEL_EVENTS_ID = "events_channel"

    const val CHANNEL_GENERAL_ID = "general_channel"
    const val CHANNEL_VERIFICATION_ID = "verification_channel"

    // Notification Types
    const val TYPE_FOLLOW = "PROFILE_UPDATE"
    const val TYPE_JOB = "JOB_ALERT"
    const val TYPE_MESSAGE = "MESSAGE"
    const val TYPE_GROUP_MESSAGE = "MESSAGE"
    const val TYPE_JOB_APPLICATION = "JOB_APPLICATION"
    const val TYPE_APPLICATION_STATUS = "APPLICATION_STATUS"
    const val TYPE_EVENTS = "EVENTS"
    const val TYPE_GENERAL = "GENERAL"
    const val TYPE_VERIFICATION = "SYSTEM"

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

            // Message Channel
            val messageChannel = NotificationChannel(
                CHANNEL_MESSAGE_ID,
                "Message Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new messages"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            val eventChannel = NotificationChannel(
                CHANNEL_EVENTS_ID,
                "Event Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for calendar events"
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
            notificationManager.createNotificationChannel(messageChannel)
            notificationManager.createNotificationChannel(eventChannel)
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
            TYPE_MESSAGE, TYPE_GROUP_MESSAGE -> showMessageNotification(context, title, message, data)
            TYPE_JOB_APPLICATION -> showJobApplicationNotification(context, title, message, data)
            TYPE_EVENTS -> showEventReminderNotification(context, title, message, data)
            TYPE_APPLICATION_STATUS -> showApplicationStatusNotification(context, title, message, data)
            else -> showGeneralNotification(context, title, message)
        }
    }

    fun sendJobApplicationNotification(
        context: Context,
        companyId: String,
        jobSeekerName: String,
        jobTitle: String,
        applicationId: String,
        jobId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val title = "New Job Application 📄"
                val message = "$jobSeekerName applied for $jobTitle"

                // Show local notification
                showNotification(
                    context,
                    title,
                    message,
                    TYPE_JOB_APPLICATION,
                    mapOf(
                        "companyId" to companyId,
                        "jobSeekerName" to jobSeekerName,
                        "jobTitle" to jobTitle,
                        "applicationId" to applicationId,
                        "jobId" to jobId
                    )
                )

                // Save notification to database
                saveNotificationToDatabase(
                    userId = companyId,
                    title = title,
                    message = message,
                    type = TYPE_JOB_APPLICATION,
                    userType = "COMPANY",
                    mapOf(
                        "jobSeekerName" to jobSeekerName,
                        "jobTitle" to jobTitle,
                        "applicationId" to applicationId,
                        "jobId" to jobId,
                        "companyId" to companyId
                    )
                )

                // Send FCM push notification
                val token = getUserFcmToken(companyId)
                if (token != null && token.isNotBlank()) {
                    val fcmData = mapOf(
                        "title" to title,
                        "message" to message,
                        "type" to TYPE_JOB_APPLICATION,
                        "applicationId" to applicationId,
                        "jobId" to jobId,
                        "jobTitle" to jobTitle,
                        "jobSeekerName" to jobSeekerName,
                        "click_action" to "FLUTTER_NOTIFICATION_CLICK"
                    )
                    sendFcmPushNotification(token, title, message, fcmData)
                }

                Log.d(TAG, "Job application notification sent to company: $companyId")

            } catch (e: Exception) {
                Log.e(TAG, "Error sending job application notification: ${e.message}")
            }
        }
    }

    fun sendEventReminderNotification(
        context: Context,
        userId: String,
        eventId: String,
        eventTitle: String,
        eventDescription: String,
        eventLocation: String,
        eventStartTime: Long,
        eventEndTime: Long,
        reminderMessage: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val title = "📅 $reminderMessage"
                val message = buildString {
                    append(eventTitle)
                    if (eventLocation.isNotEmpty()) {
                        append(" • ")
                        append(eventLocation)
                    }
                }

                // Show local notification
                showNotification(
                    context,
                    title,
                    message,
                    TYPE_EVENTS,
                    mapOf(
                        "userId" to userId,
                        "eventId" to eventId,
                        "eventTitle" to eventTitle,
                        "eventDescription" to eventDescription,
                        "eventLocation" to eventLocation,
                        "eventStartTime" to eventStartTime.toString(),
                        "eventEndTime" to eventEndTime.toString()
                    )
                )

                // Save notification to database
                saveNotificationToDatabase(
                    userId = userId,
                    title = title,
                    message = message,
                    type = TYPE_EVENTS,
                    userType = getUserType(userId),
                    mapOf(
                        "eventId" to eventId,
                        "eventTitle" to eventTitle,
                        "eventDescription" to eventDescription,
                        "eventLocation" to eventLocation,
                        "eventStartTime" to eventStartTime.toString(),
                        "eventEndTime" to eventEndTime.toString(),
                        "reminderMessage" to reminderMessage
                    )
                )

                // Send FCM push notification
                val token = getUserFcmToken(userId)
                if (token != null && token.isNotBlank()) {
                    val fcmData = mapOf(
                        "title" to title,
                        "message" to message,
                        "type" to TYPE_EVENTS,
                        "eventId" to eventId,
                        "eventTitle" to eventTitle,
                        "eventDescription" to eventDescription,
                        "eventLocation" to eventLocation,
                        "eventStartTime" to eventStartTime.toString(),
                        "eventEndTime" to eventEndTime.toString(),
                        "click_action" to "FLUTTER_NOTIFICATION_CLICK"
                    )
                    sendFcmPushNotification(token, title, message, fcmData)
                }

                Log.d(TAG, "Event reminder notification sent to user: $userId for event: $eventId")

            } catch (e: Exception) {
                Log.e(TAG, "Error sending event reminder notification: ${e.message}")
            }
        }
    }

    /**
     * Show event reminder notification in system tray
     */
    private fun showEventReminderNotification(
        context: Context,
        title: String?,
        message: String?,
        data: Map<String, String>
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", TYPE_EVENTS)
            putExtra("event_id", data["eventId"])
            putExtra("event_title", data["eventTitle"])
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_GENERAL_ID)
            .setSmallIcon(R.drawable.calendaricon) // Make sure you have this icon
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)

        // Add event details in expanded view
        val eventLocation = data["eventLocation"] ?: ""
        val eventDescription = data["eventDescription"] ?: ""

        if (eventLocation.isNotEmpty() || eventDescription.isNotEmpty()) {
            val bigTextStyle = NotificationCompat.BigTextStyle()
            val expandedText = buildString {
                if (eventDescription.isNotEmpty()) {
                    append(eventDescription)
                }
                if (eventLocation.isNotEmpty()) {
                    if (isNotEmpty()) append("\n")
                    append("📍 ")
                    append(eventLocation)
                }
            }
            bigTextStyle.bigText(expandedText)
            notificationBuilder.setStyle(bigTextStyle)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    /**
     * Send notification to job seeker when application status is updated
     */
    fun sendApplicationStatusNotification(
        context: Context,
        jobSeekerId: String,
        companyName: String,
        jobTitle: String,
        status: String,
        message: String,
        feedback: String? = null,
        applicationId: String,
        jobId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val title = when (status) {
                    "Accepted" -> "Application Accepted! 🎉"
                    "Rejected" -> "Application Update"
                    "Shortlisted" -> "You've been Shortlisted! ⭐"
                    else -> "Application Status Updated"
                }

                // Show local notification
                showNotification(
                    context,
                    title,
                    message,
                    TYPE_APPLICATION_STATUS,
                    mapOf(
                        "jobSeekerId" to jobSeekerId,
                        "companyName" to companyName,
                        "jobTitle" to jobTitle,
                        "status" to status,
                        "message" to message,
                        "applicationId" to applicationId,
                        "jobId" to jobId,
                        "feedback" to (feedback ?: "")
                    )
                )

                // Save notification to database
                saveNotificationToDatabase(
                    userId = jobSeekerId,
                    title = title,
                    message = message,
                    type = TYPE_APPLICATION_STATUS,
                    userType = "JOBSEEKER",
                    mapOf(
                        "companyName" to companyName,
                        "jobTitle" to jobTitle,
                        "status" to status,
                        "applicationId" to applicationId,
                        "jobId" to jobId,
                        "feedback" to (feedback ?: "")
                    )
                )

                // Send FCM push notification
                val token = getUserFcmToken(jobSeekerId)
                if (token != null && token.isNotBlank()) {
                    val fcmData = mapOf(
                        "title" to title,
                        "message" to message,
                        "type" to TYPE_APPLICATION_STATUS,
                        "applicationId" to applicationId,
                        "jobId" to jobId,
                        "jobTitle" to jobTitle,
                        "companyName" to companyName,
                        "status" to status,
                        "click_action" to "FLUTTER_NOTIFICATION_CLICK"
                    )
                    sendFcmPushNotification(token, title, message, fcmData)
                }

                Log.d(TAG, "Application status notification sent to job seeker: $jobSeekerId")

            } catch (e: Exception) {
                Log.e(TAG, "Error sending application status notification: ${e.message}")
            }
        }
    }

    private fun showJobApplicationNotification(
        context: Context,
        title: String?,
        message: String?,
        data: Map<String, String>
    ) {
        val applicationId = data["applicationId"] ?: ""
        val jobId = data["jobId"] ?: ""

        // Intent for opening application details
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("applicationId", applicationId)
            putExtra("jobId", jobId)
            putExtra("openApplicationDetails", true)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            applicationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_JOB_ID)
            .setSmallIcon(R.drawable.jobpost)
            .setContentTitle(title ?: "New Job Application")
            .setContentText(message ?: "You have a new job application")
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        // Add action to view application
        val viewApplicationIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("applicationId", applicationId)
            putExtra("openApplicationDetails", true)
        }
        val viewApplicationPendingIntent = PendingIntent.getActivity(
            context,
            applicationId.hashCode() + 1,
            viewApplicationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        notificationBuilder.addAction(
            R.drawable.visibility,
            "View Application",
            viewApplicationPendingIntent
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(applicationId.hashCode(), notificationBuilder.build())
    }

    private fun showApplicationStatusNotification(
        context: Context,
        title: String?,
        message: String?,
        data: Map<String, String>
    ) {
        val applicationId = data["applicationId"] ?: ""
        val status = data["status"] ?: ""

        // Intent for opening application details
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("applicationId", applicationId)
            putExtra("openMyApplications", true)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            applicationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val icon = when (status) {
            "Accepted" -> R.drawable.verified_badge
            "Rejected" -> R.drawable.warning
            "Shortlisted" -> R.drawable.following_icon
            else -> R.drawable.notification
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_JOB_ID)
            .setSmallIcon(icon)
            .setContentTitle(title ?: "Application Status Updated")
            .setContentText(message ?: "Your application status has been updated")
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        // Add feedback if available for rejected applications
        val feedback = data["feedback"]
        if (status == "Rejected" && !feedback.isNullOrEmpty()) {
            notificationBuilder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message\n\nFeedback: $feedback")
            )
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(applicationId.hashCode(), notificationBuilder.build())
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

    private fun showMessageNotification(
        context: Context,
        title: String?,
        message: String?,
        data: Map<String, String>
    ) {
        val senderId = data["senderId"] ?: ""
        val messageId = data["messageId"] ?: System.currentTimeMillis().toString()
        val chatType = data["chatType"] ?: "direct"
        val groupId = data["groupId"] ?: ""

        // Intent for opening chat
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("openMessages", true)
            if (chatType == "group") {
                putExtra("groupId", groupId)
                putExtra("openGroupChat", true)
            } else {
                putExtra("senderId", senderId)
                putExtra("openDirectChat", true)
            }
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            messageId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification based on chat type
        val channelId = if (chatType == "group") CHANNEL_MESSAGE_ID else CHANNEL_MESSAGE_ID
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.chat_filled) // Add message icon to your drawable resources
            .setContentTitle(title ?: if (chatType == "group") "New Group Message" else "New Message")
            .setContentText(message ?: "You have a new message")
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setGroup("messages") // Group all message notifications together
            .setGroupSummary(false)

        // Add reply action (for direct messages)
        if (chatType == "direct") {
            val replyIntent = Intent(context, MessageBroadcastReceiver::class.java).apply {
                action = "REPLY_ACTION"
                putExtra("senderId", senderId)
                putExtra("messageId", messageId)
            }

            val replyPendingIntent = PendingIntent.getBroadcast(
                context,
                messageId.hashCode() + 1,
                replyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            // Add reply action (Android 7.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val remoteInput = androidx.core.app.RemoteInput.Builder("reply_text")
                    .setLabel("Reply")
                    .build()

                val action = NotificationCompat.Action.Builder(
                    R.drawable.shareicon,
                    "Reply",
                    replyPendingIntent
                ).addRemoteInput(remoteInput).build()

                notificationBuilder.addAction(action)
            } else {
                // For older versions, simple action
                notificationBuilder.addAction(
                    R.drawable.shareicon,
                    "Reply",
                    replyPendingIntent
                )
            }
        }

        // Mark as read action
        val markAsReadIntent = Intent(context, MessageBroadcastReceiver::class.java).apply {
            action = "MARK_AS_READ_ACTION"
            putExtra("messageId", messageId)
            putExtra("senderId", senderId)
            if (chatType == "group") {
                putExtra("groupId", groupId)
            }
        }

        val markAsReadPendingIntent = PendingIntent.getBroadcast(
            context,
            messageId.hashCode() + 2,
            markAsReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        notificationBuilder.addAction(
            R.drawable.visibility,
            "Mark as Read",
            markAsReadPendingIntent
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Use different IDs for different chats to group notifications properly
        val notificationId = if (chatType == "group") {
            groupId.hashCode()
        } else {
            senderId.hashCode()
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    fun sendMessageNotification(
        context: Context,
        receiverId: String,
        senderId: String,
        senderName: String,
        messageText: String,
        chatType: String = "direct",
        groupId: String = "",
        groupName: String = ""
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if receiver has notifications enabled
                val notificationEnabled = isNotificationEnabled(receiverId)
                if (!notificationEnabled) {
                    return@launch
                }

                // Check if receiver is in DND mode or has blocked sender
                if (isUserInDNDMode(receiverId) || isSenderBlocked(receiverId, senderId)) {
                    return@launch
                }

                val title = when (chatType) {
                    "group" -> "$groupName"
                    else -> senderName
                }

                val body = when {
                    messageText.length > 100 -> "${messageText.substring(0, 100)}..."
                    else -> messageText
                }

                val userType = getUserType(receiverId)

                // Save notification to database
                saveNotificationToDatabase(
                    userId = receiverId,
                    title = title,
                    message = body,
                    type = if (chatType == "group") TYPE_GROUP_MESSAGE else TYPE_MESSAGE,
                    userType = userType,
                    mapOf(
                        "senderId" to senderId,
                        "senderName" to senderName,
                        "messageText" to groupId,
                        "groupName" to groupName
                    )
                )

                // Show local notification
                val notificationData = mutableMapOf(
                    "senderId" to senderId,
                    "senderName" to senderName,
                    "messageText" to messageText,
                    "chatType" to chatType,
                    "messageId" to System.currentTimeMillis().toString()
                )

                if (chatType == "group") {
                    notificationData["groupId"] = groupId
                    notificationData["groupName"] = groupName
                }

                showNotification(
                    context,
                    title,
                    body,
                    if (chatType == "group") TYPE_GROUP_MESSAGE else TYPE_MESSAGE,
                    notificationData
                )

                // Send FCM push notification
                val token = getUserFcmToken(receiverId)
                if (token != null && token.isNotBlank()) {
                    val fcmData = mutableMapOf(
                        "title" to title,
                        "message" to body,
                        "type" to if (chatType == "group") TYPE_GROUP_MESSAGE else TYPE_MESSAGE,
                        "senderId" to senderId,
                        "senderName" to senderName,
                        "receiverId" to receiverId,
                        "messageText" to messageText,
                        "chatType" to chatType,
                        "click_action" to "FLUTTER_NOTIFICATION_CLICK"
                    )

                    if (chatType == "group") {
                        fcmData["groupId"] = groupId
                        fcmData["groupName"] = groupName
                    }

                    sendFcmPushNotification(token, title, body, fcmData)
                }

                Log.d(TAG, "Message notification sent to $receiverId")

            } catch (e: Exception) {
                Log.e(TAG, "Error sending message notification: ${e.message}")
            }
        }
    }

    private suspend fun saveMessageNotificationToDatabase(
        receiverId: String,
        senderId: String,
        senderName: String,
        messageText: String,
        chatType: String,
        groupId: String = "",
        groupName: String = ""
    ) {
        try {
            val notificationId = UUID.randomUUID().toString()
            val notificationData = hashMapOf<String, Any>(
                "id" to notificationId,
                "receiverId" to receiverId,
                "senderId" to senderId,
                "senderName" to senderName,
                "message" to messageText,
                "type" to if (chatType == "group") TYPE_GROUP_MESSAGE else TYPE_MESSAGE,
                "chatType" to chatType,
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false,
                "isArchived" to false
            )

            if (chatType == "group") {
                notificationData["groupId"] = groupId
                notificationData["groupName"] = groupName
            }

            FirebaseDatabase.getInstance()
                .getReference("MessageNotifications")
                .child(receiverId)
                .child(notificationId)
                .setValue(notificationData)
                .await()

            Log.d(TAG, "Message notification saved for user: $receiverId")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving message notification: ${e.message}")
        }
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

    // In NotificationHelper.kt, enhance sendJobPostNotificationToAll function:

    /**
     * ⭐ SEND JOB POST NOTIFICATION TO ALL JOB SEEKERS ⭐
     * Now with FCM push notifications
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

                // FCM Payload
                val fcmData = mapOf(
                    "title" to title,
                    "message" to message,
                    "type" to TYPE_JOB,
                    "jobId" to jobId,
                    "jobTitle" to jobTitle,
                    "companyName" to companyName,
                    "position" to position,
                    "click_action" to "FLUTTER_NOTIFICATION_CLICK" // For Flutter apps, if applicable
                )

                // Iterate through all job seekers
                for (jobSeekerSnapshot in jobSeekersSnapshot.children) {
                    val jobSeekerId = jobSeekerSnapshot.key ?: continue

                    // Check if job seeker is active
                    val isActive = jobSeekerSnapshot.child("isActive").getValue(Boolean::class.java) ?: true
                    if (!isActive) {
                        Log.d(TAG, "Skipping inactive user: $jobSeekerId")
                        continue
                    }

                    // 1. Save notification to database for this job seeker
                    saveNotificationToDatabase(
                        userId = jobSeekerId,
                        title = title,
                        message = message,
                        type = TYPE_APPLICATION_STATUS,
                        userType = "JOBSEEKER",
                        mapOf(
                            "jobId" to jobId,
                            "jobTitle" to jobTitle,
                            "companyName" to companyName,
                            "position" to position
                        )
                    )

                    // 2. Send FCM push notification
                    try {
                        val token = getUserFcmToken(jobSeekerId)
                        if (token != null && token.isNotBlank()) {
                            sendFcmPushNotification(token, title, message, fcmData)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send FCM to $jobSeekerId: ${e.message}")
                    }

                    notificationCount++
                }

                Log.d(TAG, "✅ Job notification sent to $notificationCount job seekers")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending job post notifications: ${e.message}", e)
            }
        }
    }

    /**
     * Send FCM push notification using Firebase Cloud Messaging
     */
    private suspend fun sendFcmPushNotification(
        token: String,
        title: String,
        message: String,
        data: Map<String, String>
    ) {
        try {
            // This would typically be done on a server
            // For client-side, you'd need to:
            // 1. Create a Cloud Function that sends notifications
            // 2. Or use a backend service

            // For demonstration, here's what the FCM payload looks like:
            val fcmPayload = mapOf(
                "to" to token,
                "notification" to mapOf(
                    "title" to title,
                    "body" to message,
                    "sound" to "default",
                    "click_action" to "FLUTTER_NOTIFICATION_CLICK"
                ),
                "data" to data,
                "android" to mapOf(
                    "priority" to "high"
                ),
                "apns" to mapOf(
                    "payload" to mapOf(
                        "aps" to mapOf(
                            "content-available" to 1,
                            "sound" to "default"
                        )
                    )
                )
            )

            Log.d(TAG, "FCM Payload for $token: $fcmPayload")

        } catch (e: Exception) {
            Log.e(TAG, "Error preparing FCM payload: ${e.message}")
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
        userType: String = "ALL",
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

    private suspend fun isNotificationEnabled(userId: String): Boolean {
        return try {
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("NotificationSettings")
                .child(userId)
                .child("messageNotifications")
                .get()
                .await()

            snapshot.getValue(Boolean::class.java) ?: true // Default to true if not set
        } catch (e: Exception) {
            Log.e(TAG, "Error checking notification settings: ${e.message}")
            true
        }
    }

    private suspend fun isUserInDNDMode(userId: String): Boolean {
        return try {
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("NotificationSettings")
                .child(userId)
                .child("dndMode")
                .get()
                .await()

            val isDND = snapshot.getValue(Boolean::class.java) ?: false

            if (isDND) {
                // Check DND schedule
                val dndStart = FirebaseDatabase.getInstance()
                    .getReference("NotificationSettings")
                    .child(userId)
                    .child("dndStartTime")
                    .get()
                    .await()
                    .getValue(String::class.java)

                val dndEnd = FirebaseDatabase.getInstance()
                    .getReference("NotificationSettings")
                    .child(userId)
                    .child("dndEndTime")
                    .get()
                    .await()
                    .getValue(String::class.java)

                // Implement time check logic here
                // For now, return true if DND is enabled
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun isSenderBlocked(receiverId: String, senderId: String): Boolean {
        return try {
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("BlockedUsers")
                .child(receiverId)
                .child(senderId)
                .get()
                .await()

            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun getUserType(userId: String): String {
        return try {
            // Check if user is in Companys collection
            val companySnapshot = FirebaseDatabase.getInstance()
                .getReference("Companys")
                .child(userId)
                .get()
                .await()

            if (companySnapshot.exists()) {
                "COMPANY"
            } else {
                // Check if user is in JobSeekers collection
                val jobSeekerSnapshot = FirebaseDatabase.getInstance()
                    .getReference("JobSeekers")
                    .child(userId)
                    .get()
                    .await()

                if (jobSeekerSnapshot.exists()) {
                    "JOBSEEKER"
                } else {
                    "ALL"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user type: ${e.message}")
            "ALL"
        }
    }

}