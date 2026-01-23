package com.example.rojgar.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.GroupChat
import com.example.rojgar.model.GroupMessage
import com.example.rojgar.repository.GroupChatRepository
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GroupChatRoomViewModel(private val repository: GroupChatRepository) : ViewModel() {

    private val _messages = MutableLiveData<List<GroupMessage>>(emptyList())
    val messages: LiveData<List<GroupMessage>> = _messages

    private val _groupInfo = MutableLiveData<GroupChat?>(null)
    val groupInfo: LiveData<GroupChat?> = _groupInfo

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _uploadProgress = MutableLiveData(0.0)
    val uploadProgress: LiveData<Double> = _uploadProgress

    private val _isUploading = MutableLiveData(false)
    val isUploading: LiveData<Boolean> = _isUploading

    private var messageListener: (() -> Unit)? = null

    /**
     * Listen for new messages in real-time
     */
    fun listenForMessages(groupId: String) {
        repository.listenForGroupMessages(
            groupId = groupId,
            onNewMessage = { newMessage ->
                val currentList = _messages.value ?: emptyList()

                // Check if message already exists (prevent duplicates)
                val exists = currentList.any { it.messageId == newMessage.messageId }

                if (!exists) {
                    // Add new message and sort by timestamp
                    val updatedList = (currentList + newMessage).sortedBy { it.timestamp }
                    _messages.value = updatedList
                }
            },
            onError = { e ->
                _error.value = e.message
            }
        )
    }


            /**
     * Load group information
     */
    fun loadGroupInfo(groupId: String) {
        repository.getGroupById(groupId) { result ->
            result.onSuccess { group ->
                _groupInfo.value = group
            }.onFailure { error ->
                _error.value = error.message
            }
        }
    }

    /**
     * Send text message
     */
    fun sendGroupMessage(
        groupId: String,
        senderId: String,
        senderName: String,
        text: String
    ) {
        if (text.isBlank()) return

        val message = GroupMessage(
            groupId = groupId,
            senderId = senderId,
            senderName = senderName,
            messageText = text,
            timestamp = System.currentTimeMillis(),
            messageType = "text"
        )

        repository.sendGroupMessage(groupId, message) { result ->
            result.onFailure {
                _error.value = "Failed to send message"
            }
        }
    }

    /**
     * Send image message
     */
    fun sendGroupImage(
        context: Context,
        groupId: String,
        senderId: String,
        senderName: String,
        imageUri: Uri
    ) {
        _isUploading.value = true
        _uploadProgress.value = 0.0

        viewModelScope.launch {
            repository.uploadGroupMedia(
                context = context,
                mediaUri = imageUri,
                mediaType = "image",
                onProgress = { progress ->
                    _uploadProgress.value = progress
                },
                onSuccess = { imageUrl ->
                    _isUploading.value = false
                    _uploadProgress.value = 0.0

                    val message = GroupMessage(
                        groupId = groupId,
                        senderId = senderId,
                        senderName = senderName,
                        messageText = imageUrl,
                        timestamp = System.currentTimeMillis(),
                        messageType = "image"
                    )

                    repository.sendGroupMessage(groupId, message) { result ->
                        result.onFailure {
                            _error.value = "Failed to send image message"
                        }
                    }
                },
                onFailure = { error ->
                    _isUploading.value = false
                    _uploadProgress.value = 0.0
                    _error.value = "Image upload failed: $error"
                }
            )
        }
    }

    /**
     * Send voice message
     */
    fun sendGroupVoiceMessage(
        context: Context,
        groupId: String,
        senderId: String,
        senderName: String,
        voiceUri: Uri
    ) {
        _isUploading.value = true
        _uploadProgress.value = 0.0

        viewModelScope.launch {
            repository.uploadGroupMedia(
                context = context,
                mediaUri = voiceUri,
                mediaType = "voice",
                onProgress = { progress ->
                    _uploadProgress.value = progress
                },
                onSuccess = { voiceUrl ->
                    _isUploading.value = false
                    _uploadProgress.value = 0.0

                    val message = GroupMessage(
                        groupId = groupId,
                        senderId = senderId,
                        senderName = senderName,
                        messageText = voiceUrl,
                        timestamp = System.currentTimeMillis(),
                        messageType = "voice"
                    )

                    repository.sendGroupMessage(groupId, message) { result ->
                        result.onFailure {
                            _error.value = "Failed to send voice message"
                        }
                    }
                },
                onFailure = { error ->
                    _isUploading.value = false
                    _uploadProgress.value = 0.0
                    _error.value = "Voice upload failed: $error"
                }
            )
        }
    }

    /**
     * Mark message as read
     */
    fun markMessageAsRead(groupId: String, messageId: String, userId: String) {
        repository.markGroupMessageAsRead(groupId, messageId, userId) { result ->
            result.onFailure { error ->
                // Silent fail - read receipts are not critical
            }
        }
    }

    /**
     * Delete message
     */
    fun deleteMessage(groupId: String, messageId: String) {
        repository.deleteGroupMessage(groupId, messageId) { result ->
            result.onSuccess {
                // Remove from local list
                _messages.value = _messages.value?.filter { it.messageId != messageId }
            }.onFailure { error ->
                _error.value = "Failed to delete message"
            }
        }
    }

    /**
     * Load initial messages
     */
    fun loadMessages(groupId: String, limit: Int = 100) {
        repository.getGroupMessages(groupId, limit) { result ->
            result.onSuccess { messages ->
                // Only set if messages list is currently empty (initial load)
                if (_messages.value.isNullOrEmpty()) {
                    _messages.value = messages.sortedBy { it.timestamp }
                }
            }.onFailure { error ->
                _error.value = error.message
            }
        }
    }

    /**
     * Create image URI for camera
     */
    fun createImageUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "GROUP_IMG_${timeStamp}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: Uri.EMPTY
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Get unread message count
     */
    fun getUnreadCount(userId: String): Int {
        return _messages.value?.count { message ->
            message.senderId != userId && !message.readBy.contains(userId)
        } ?: 0
    }

    /**
     * Search messages
     */
    fun searchMessages(query: String): List<GroupMessage> {
        return _messages.value?.filter { message ->
            message.messageText.contains(query, ignoreCase = true) ||
                    message.senderName.contains(query, ignoreCase = true)
        } ?: emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        // Stop listening to prevent memory leaks
        // You'll need to store groupId as a property
    }


}