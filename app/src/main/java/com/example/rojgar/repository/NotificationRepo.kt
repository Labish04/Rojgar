package com.example.rojgar.repository

import com.example.rojgar.model.NotificationModel
import kotlinx.coroutines.flow.Flow

interface NotificationRepo {
    fun getAllNotifications(): Flow<List<NotificationModel>>
    suspend fun markAsRead(notificationId: String)
    suspend fun markAsUnread(notificationId: String)
    suspend fun deleteNotification(notificationId: String)
    suspend fun clearAllNotifications()
    fun getUnreadCount(): Flow<Int>
}