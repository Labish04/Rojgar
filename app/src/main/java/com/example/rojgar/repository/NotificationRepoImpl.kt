package com.example.rojgar.repository

import com.example.rojgar.model.NotificationModel
import com.example.rojgar.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class NotificationRepoImpl : NotificationRepo {

    private val notifications = MutableStateFlow(generateMockNotifications())

    override fun getAllNotifications(): Flow<List<NotificationModel>> {
        return notifications
    }

    override suspend fun markAsRead(notificationId: String) {
        notifications.value = notifications.value.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }
    }

    override suspend fun markAsUnread(notificationId: String) {
        notifications.value = notifications.value.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(isRead = false)
            } else {
                notification
            }
        }
    }

    override suspend fun deleteNotification(notificationId: String) {
        notifications.value = notifications.value.filter { it.id != notificationId }
    }

    override suspend fun clearAllNotifications() {
        notifications.value = emptyList()
    }

    override fun getUnreadCount(): Flow<Int> {
        return notifications.map { list -> list.count { !it.isRead } }
    }

    private fun generateMockNotifications(): List<NotificationModel> {
        val currentTime = System.currentTimeMillis()
        return listOf(
            NotificationModel(
                id = "1",
                title = "New Job Alert: Android Developer",
                message = "Google is hiring for Senior Android Developer position in your area. Apply now!",
                timestamp = currentTime - 5 * 60 * 1000, // 5 minutes ago
                isRead = false,
                type = NotificationType.JOB_ALERT
            ),
            NotificationModel(
                id = "2",
                title = "Application Update",
                message = "Your application for Frontend Developer at Meta has been viewed by the recruiter.",
                timestamp = currentTime - 2 * 60 * 60 * 1000, // 2 hours ago
                isRead = false,
                type = NotificationType.MESSAGE
            ),
            NotificationModel(
                id = "3",
                title = "Profile Boost",
                message = "Complete your profile to increase visibility by 60%. Add your work experience now!",
                timestamp = currentTime - 5 * 60 * 60 * 1000, // 5 hours ago
                isRead = true,
                type = NotificationType.SYSTEM
            ),
            NotificationModel(
                id = "4",
                title = "New Message from Recruiter",
                message = "Sarah from Amazon wants to discuss an opportunity with you. Check your messages.",
                timestamp = currentTime - 24 * 60 * 60 * 1000, // 1 day ago
                isRead = false,
                type = NotificationType.MESSAGE
            ),
            NotificationModel(
                id = "5",
                title = "Job Match: 95% Compatible",
                message = "UI/UX Designer role at Adobe matches your skills perfectly. Don't miss out!",
                timestamp = currentTime - 2 * 24 * 60 * 60 * 1000, // 2 days ago
                isRead = true,
                type = NotificationType.JOB_ALERT
            ),
            NotificationModel(
                id = "6",
                title = "Interview Scheduled",
                message = "Your interview with Microsoft is scheduled for tomorrow at 2:00 PM. Prepare well!",
                timestamp = currentTime - 3 * 24 * 60 * 60 * 1000, // 3 days ago
                isRead = true,
                type = NotificationType.GENERAL
            )
        )
    }
}