package com.example.rojgar.model


data class NotificationModel(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.GENERAL
)

enum class NotificationType {
    JOB_ALERT,
    APPLICATION_UPDATE,
    MESSAGE,
    GENERAL
}

