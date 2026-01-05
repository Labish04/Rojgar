package com.example.rojgar.model

data class NotificationModel(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.GENERAL,
    val actionUrl: String? = null
)

enum class NotificationType {
    JOB_ALERT,
    MESSAGE,
    SYSTEM,
    GENERAL
}