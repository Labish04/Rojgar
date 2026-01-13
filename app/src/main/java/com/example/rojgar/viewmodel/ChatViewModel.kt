package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.ChatMessage
import com.example.rojgar.model.ChatRoom
import com.example.rojgar.repository.ChatRepository
import kotlinx.coroutines.launch
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

    // Track active listeners to prevent duplicates
    private var currentChatId: String? = null
    private var typingListenerActive = false

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
                    // Reset after handling
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

            // Check if message already exists (prevents duplicates)
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
                    // Remove from local list
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
            // Find which participant the current user is
            val currentUser = getCurrentUserId() // You need to implement this
            currentUser?.let { userId ->
                loadChatRooms(userId)
            }
        }
    }

    // Helper method to get current user ID (implement based on your auth system)
    private fun getCurrentUserId(): String? {
        // This should return the current logged-in user's ID
        // You might need to inject an auth repository or view model
        return null
    }
}