package com.example.rojgar.repository

import com.example.rojgar.model.NotificationModel
import com.example.rojgar.model.NotificationType
import com.example.rojgar.model.UserType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class NotificationRepoImpl : NotificationRepo {

    private val notifications = MutableStateFlow(generateMockNotifications())

    override fun getAllNotifications(userType: UserType): Flow<List<NotificationModel>> {
        return notifications.map { list ->
            list.filter { notification ->
                notification.userType == userType || notification.userType == UserType.ALL
            }
        }
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

    override suspend fun clearAllNotifications(userType: UserType) {
        notifications.value = notifications.value.filter {
            it.userType != userType && it.userType != UserType.ALL
        }
    }

    override fun getUnreadCount(userType: UserType): Flow<Int> {
        return notifications.map { list ->
            list.count {
                !it.isRead && (it.userType == userType || it.userType == UserType.ALL)
            }
        }
    }

    private fun generateMockNotifications(): List<NotificationModel> {
        val currentTime = System.currentTimeMillis()
        return listOf(
            // Jobseeker Notifications
            NotificationModel(
                id = "js1",
                title = "New Job Alert: Android Developer",
                message = "Google is hiring for Senior Android Developer position in your area. Apply now!",
                timestamp = currentTime - 5 * 60 * 1000,
                isRead = false,
                type = NotificationType.JOB_ALERT,
                userType = UserType.JOBSEEKER
            ),
            NotificationModel(
                id = "js2",
                title = "Application Update",
                message = "Your application for Frontend Developer at Meta has been viewed by the recruiter.",
                timestamp = currentTime - 2 * 60 * 60 * 1000,
                isRead = false,
                type = NotificationType.APPLICATION_UPDATE,
                userType = UserType.JOBSEEKER
            ),
            NotificationModel(
                id = "js3",
                title = "Profile Boost",
                message = "Complete your profile to increase visibility by 60%. Add your work experience now!",
                timestamp = currentTime - 5 * 60 * 60 * 1000,
                isRead = true,
                type = NotificationType.SYSTEM,
                userType = UserType.JOBSEEKER
            ),
            NotificationModel(
                id = "js4",
                title = "New Message from Recruiter",
                message = "Sarah from Amazon wants to discuss an opportunity with you. Check your messages.",
                timestamp = currentTime - 24 * 60 * 60 * 1000,
                isRead = false,
                type = NotificationType.MESSAGE,
                userType = UserType.JOBSEEKER
            ),
            NotificationModel(
                id = "js5",
                title = "Job Match: 95% Compatible",
                message = "UI/UX Designer role at Adobe matches your skills perfectly. Don't miss out!",
                timestamp = currentTime - 2 * 24 * 60 * 60 * 1000,
                isRead = true,
                type = NotificationType.JOB_ALERT,
                userType = UserType.JOBSEEKER
            ),
            NotificationModel(
                id = "js6",
                title = "Interview Scheduled",
                message = "Your interview with Microsoft is scheduled for tomorrow at 2:00 PM. Prepare well!",
                timestamp = currentTime - 3 * 24 * 60 * 60 * 1000,
                isRead = true,
                type = NotificationType.INTERVIEW_SCHEDULED,
                userType = UserType.JOBSEEKER
            ),

            // Company Notifications
            NotificationModel(
                id = "c1",
                title = "New Application Received",
                message = "John Doe has applied for Senior Android Developer position. Review application now.",
                timestamp = currentTime - 10 * 60 * 1000,
                isRead = false,
                type = NotificationType.CANDIDATE_ALERT,
                userType = UserType.COMPANY
            ),
            NotificationModel(
                id = "c2",
                title = "Candidate Accepted Interview",
                message = "Sarah Johnson has accepted your interview invitation for Frontend Developer role.",
                timestamp = currentTime - 3 * 60 * 60 * 1000,
                isRead = false,
                type = NotificationType.INTERVIEW_SCHEDULED,
                userType = UserType.COMPANY
            ),
            NotificationModel(
                id = "c3",
                title = "Job Post Expiring Soon",
                message = "Your job posting 'UI/UX Designer' will expire in 3 days. Renew to keep receiving applications.",
                timestamp = currentTime - 6 * 60 * 60 * 1000,
                isRead = true,
                type = NotificationType.SYSTEM,
                userType = UserType.COMPANY
            ),
            NotificationModel(
                id = "c4",
                title = "New Message from Candidate",
                message = "Michael Brown sent you a message regarding the Backend Developer position.",
                timestamp = currentTime - 12 * 60 * 60 * 1000,
                isRead = false,
                type = NotificationType.MESSAGE,
                userType = UserType.COMPANY
            ),
            NotificationModel(
                id = "c5",
                title = "Premium Features Activated",
                message = "Your company account has been upgraded to Premium. Enjoy unlimited job posts!",
                timestamp = currentTime - 2 * 24 * 60 * 60 * 1000,
                isRead = true,
                type = NotificationType.SYSTEM,
                userType = UserType.COMPANY
            ),
            NotificationModel(
                id = "c6",
                title = "Matching Candidates Available",
                message = "5 highly qualified candidates match your Data Scientist position. View profiles now.",
                timestamp = currentTime - 4 * 24 * 60 * 60 * 1000,
                isRead = false,
                type = NotificationType.CANDIDATE_ALERT,
                userType = UserType.COMPANY
            ),

            // Notifications visible to both
            NotificationModel(
                id = "all1",
                title = "Platform Maintenance Scheduled",
                message = "The platform will undergo maintenance on Saturday, 2 AM - 4 AM. Services may be limited.",
                timestamp = currentTime - 8 * 60 * 60 * 1000,
                isRead = false,
                type = NotificationType.SYSTEM,
                userType = UserType.ALL
            )
        )
    }
}