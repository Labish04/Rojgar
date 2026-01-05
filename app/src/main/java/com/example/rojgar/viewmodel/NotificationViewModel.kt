package com.example.rojgar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.NotificationModel
import com.example.rojgar.repository.NotificationRepo
import com.example.rojgar.repository.NotificationRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepo = NotificationRepoImpl()
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNotifications()
        observeUnreadCount()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            repository.getAllNotifications().collect { notificationList ->
                _notifications.value = notificationList.sortedByDescending { it.timestamp }
                _isLoading.value = false
            }
        }
    }

    private fun observeUnreadCount() {
        viewModelScope.launch {
            repository.getUnreadCount().collect { count ->
                _unreadCount.value = count
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
        }
    }

    fun markAsUnread(notificationId: String) {
        viewModelScope.launch {
            repository.markAsUnread(notificationId)
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            repository.deleteNotification(notificationId)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _notifications.value.filter { !it.isRead }.forEach { notification ->
                repository.markAsRead(notification.id)
            }
        }
    }
}