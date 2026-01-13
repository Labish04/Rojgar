package com.example.rojgar.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.ChatMessage
import com.example.rojgar.model.ChatRoom
import com.example.rojgar.repository.ChatRepository
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private val _chatRooms = MutableLiveData<List<ChatRoom>>()
    val chatRooms: LiveData<List<ChatRoom>> = _chatRooms

    private val _messages = MutableLiveData<List<ChatMessage>?>()
    val messages: LiveData<List<ChatMessage>> = _messages as LiveData<List<ChatMessage>>

    private val _currentChatRoom = MutableLiveData<ChatRoom?>(null)
    val currentChatRoom: LiveData<ChatRoom?> = _currentChatRoom

    private val _isTyping = MutableLiveData<Pair<String, Boolean>?>(null)
    val isTyping: LiveData<Pair<String, Boolean>?> = _isTyping

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _messageSent = MutableLiveData<Boolean>(false)
    val messageSent: LiveData<Boolean> = _messageSent

    private var currentChatId: String? = null
    private var typingListenerActive = false

    private val _uploadProgress = MutableLiveData<Double>(0.0)
    val uploadProgress: LiveData<Double> = _uploadProgress

    private val _isUploading = MutableLiveData<Boolean>(false)
    val isUploading: LiveData<Boolean> = _isUploading

    fun loadChatRooms(userId: String) {
        _loading.value = true
        viewModelScope.launch {
            chatRepository.getChatRooms(userId) { success, message, chatRooms ->
                _loading.value = false
                if (success && chatRooms != null) {
                    _chatRooms.value = chatRooms
                } else {
                    _error.value = message
                }
            }
        }
    }

    fun getOrCreateChatRoom(
        participant1Id: String,
        participant2Id: String,
        participant1Name: String,
        participant2Name: String,
        onSuccess: (ChatRoom) -> Unit = {}
    ) {
        _loading.value = true
        viewModelScope.launch {
            chatRepository.getOrCreateChatRoom(
                participant1Id = participant1Id,
                participant2Id = participant2Id,
                participant1Name = participant1Name,
                participant2Name = participant2Name
            ) { success, message, chatRoom ->
                _loading.value = false
                if (success && chatRoom != null) {
                    _currentChatRoom.value = chatRoom
                    currentChatId = chatRoom.chatId
                    onSuccess(chatRoom)
                } else {
                    _error.value = message
                }
            }
        }
    }

    fun loadMessages(chatId: String) {
        _loading.value = true
        currentChatId = chatId
        viewModelScope.launch {
            chatRepository.getMessages(chatId) { success, message, messages ->
                _loading.value = false
                if (success && messages != null) {
                    _messages.value = messages.sortedBy { it.timestamp }
                } else {
                    _error.value = message
                }
            }
        }
    }

    fun sendMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        senderName: String,
        receiverName: String,
        messageText: String,
        messageType: String = "text",
        mediaUrl: String = ""
    ) {
        if (messageText.trim().isEmpty() && mediaUrl.isEmpty()) return

        val message = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = senderId,
            receiverId = receiverId,
            senderName = senderName,
            receiverName = receiverName,
            messageText = messageText,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            messageType = messageType,
            mediaUrl = mediaUrl
        )

        viewModelScope.launch {
            chatRepository.sendMessage(chatId, message) { success, errorMessage ->
                if (success) {
                    _messageSent.value = true
                    _messageSent.value = false
                } else {
                    _error.value = errorMessage
                }
            }
        }
    }

    fun listenForNewMessages(chatId: String) {
        chatRepository.listenForNewMessages(chatId) { newMessage ->
            val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()

            if (!currentMessages.any { it.messageId == newMessage.messageId }) {
                currentMessages.add(newMessage)
                _messages.value = currentMessages.sortedBy { it.timestamp }
            }
        }
    }

    fun markMessagesAsRead(chatId: String, userId: String) {
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(chatId, userId) { success, message ->
                if (!success) {
                    _error.value = message
                }
            }
        }
    }

    fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        chatRepository.updateTypingStatus(chatId, userId, isTyping)
    }

    fun listenForTypingStatus(chatId: String) {
        if (!typingListenerActive) {
            chatRepository.listenForTypingStatus(chatId) { userId, isTyping ->
                _isTyping.value = Pair(userId, isTyping)
            }
            typingListenerActive = true
        }
    }

    fun deleteMessage(messageId: String, chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId, chatId) { success, message ->
                if (success) {
                    val updatedMessages = _messages.value?.filterNot { it.messageId == messageId }
                    _messages.value = updatedMessages
                } else {
                    _error.value = message
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearCurrentChat() {
        _currentChatRoom.value = null
        _messages.value = emptyList()
        _isTyping.value = null
        currentChatId = null
        typingListenerActive = false
    }

    fun refreshMessages() {
        currentChatId?.let { loadMessages(it) }
    }

    fun updateChatRooms() {
        _currentChatRoom.value?.let { room ->
            val currentUser = getCurrentUserId()
            currentUser?.let { userId ->
                loadChatRooms(userId)
            }
        }
    }

    private fun getCurrentUserId(): String? {
        return null
    }

    fun uploadAndSendVoiceMessage(
        audioFile: File,
        chatId: String,
        senderId: String,
        receiverId: String,
        senderName: String,
        receiverName: String
    ) {
        if (!audioFile.exists()) {
            _error.value = "Audio file does not exist"
            return
        }

        if (audioFile.length() == 0L) {
            _error.value = "Audio file is empty"
            return
        }

        Log.d("VoiceUpload", "ViewModel: Starting upload")
        Log.d("VoiceUpload", "ViewModel: File path: ${audioFile.absolutePath}")
        Log.d("VoiceUpload", "ViewModel: File size: ${audioFile.length()} bytes")

        _isUploading.value = true
        _uploadProgress.value = 0.0

        viewModelScope.launch {
            chatRepository.uploadVoiceMessage(
                audioFile = audioFile,
                onProgress = { progress ->
                    Log.d("VoiceUpload", "ViewModel: Progress: $progress%")
                    _uploadProgress.value = progress
                },
                onSuccess = { downloadUrl ->
                    Log.d("VoiceUpload", "ViewModel: Upload successful")
                    Log.d("VoiceUpload", "ViewModel: Download URL: $downloadUrl")

                    val duration = chatRepository.getVoiceMessageDuration(audioFile)
                    val durationText = formatDuration(duration)

                    Log.d("VoiceUpload", "ViewModel: Duration: $durationText")

                    sendMessage(
                        chatId = chatId,
                        senderId = senderId,
                        receiverId = receiverId,
                        senderName = senderName,
                        receiverName = receiverName,
                        messageText = durationText,
                        messageType = "voice",
                        mediaUrl = downloadUrl
                    )

                    _isUploading.value = false
                    _uploadProgress.value = 0.0

                    try {
                        if (audioFile.exists()) {
                            val deleted = audioFile.delete()
                            Log.d("VoiceUpload", "ViewModel: Temp file deleted: $deleted")
                        }
                    } catch (e: Exception) {
                        Log.e("VoiceUpload", "ViewModel: Failed to delete temp file: ${e.message}")
                    }
                },
                onFailure = { errorMessage ->
                    Log.e("VoiceUpload", "ViewModel: Upload failed: $errorMessage")
                    _error.value = "Upload failed: $errorMessage"
                    _isUploading.value = false
                    _uploadProgress.value = 0.0

                    try {
                        if (audioFile.exists()) {
                            val deleted = audioFile.delete()
                            Log.d("VoiceUpload", "ViewModel: Temp file deleted after failure: $deleted")
                        }
                    } catch (e: Exception) {
                        Log.e("VoiceUpload", "ViewModel: Failed to delete temp file: ${e.message}")
                    }
                }
            )
        }
    }

    // New method for media upload
    fun uploadAndSendMediaMessage(
        context: Context,
        mediaUri: Uri,
        mediaType: String, // "image", "video", "document"
        chatId: String,
        senderId: String,
        receiverId: String,
        senderName: String,
        receiverName: String
    ) {
        Log.d("MediaUpload", "ViewModel: Starting $mediaType upload")

        _isUploading.value = true
        _uploadProgress.value = 0.0

        viewModelScope.launch {
            chatRepository.uploadMediaFile(
                context = context,
                mediaUri = mediaUri,
                mediaType = mediaType,
                onProgress = { progress ->
                    Log.d("MediaUpload", "ViewModel: Progress: $progress%")
                    _uploadProgress.value = progress
                },
                onSuccess = { downloadUrl ->
                    Log.d("MediaUpload", "ViewModel: Upload successful")
                    Log.d("MediaUpload", "ViewModel: Download URL: $downloadUrl")

                    val displayText = when (mediaType) {
                        "image" -> "Photo"
                        "video" -> "Video"
                        "document" -> "Document"
                        else -> "Media file"
                    }

                    sendMessage(
                        chatId = chatId,
                        senderId = senderId,
                        receiverId = receiverId,
                        senderName = senderName,
                        receiverName = receiverName,
                        messageText = displayText,
                        messageType = mediaType,
                        mediaUrl = downloadUrl
                    )

                    _isUploading.value = false
                    _uploadProgress.value = 0.0
                },
                onFailure = { errorMessage ->
                    Log.e("MediaUpload", "ViewModel: Upload failed: $errorMessage")
                    _error.value = "Upload failed: $errorMessage"
                    _isUploading.value = false
                    _uploadProgress.value = 0.0
                }
            )
        }
    }

    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}