package com.example.rojgar.repository

import com.example.rojgar.model.NotificationModel
import com.example.rojgar.model.UserType
import kotlinx.coroutines.flow.Flow

interface NotificationRepo {
    fun getAllNotifications(userType: UserType): Flow<List<NotificationModel>>
    suspend fun markAsRead(notificationId: String)
    suspend fun markAsUnread(notificationId: String)
    suspend fun deleteNotification(notificationId: String)
    suspend fun clearAllNotifications(userType: UserType)
    fun getUnreadCount(userType: UserType): Flow<Int>
}