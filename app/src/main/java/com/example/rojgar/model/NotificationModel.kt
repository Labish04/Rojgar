package com.example.rojgar.model

data class NotificationModel(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.GENERAL,
    val actionUrl: String? = null,
    val userType: UserType = UserType.JOBSEEKER // Target user type for this notification
)

enum class NotificationType {
    JOB_ALERT,
    MESSAGE,
    SYSTEM,
    GENERAL,
    APPLICATION_UPDATE,  // For jobseekers
    CANDIDATE_ALERT,     // For companies
    INTERVIEW_SCHEDULED,
    PROFILE_UPDATE
}

enum class UserType {
    JOBSEEKER,
    COMPANY,
    ALL  // For notifications visible to both
}