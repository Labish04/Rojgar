// File: ChatRepository.kt
package com.example.rojgar.repository

import com.example.rojgar.model.ChatMessage
import com.example.rojgar.model.ChatRoom

interface ChatRepository {
    // Chat Room Operations
    fun createChatRoom(
        participant1Id: String,
        participant2Id: String,
        participant1Name: String,
        participant2Name: String,
        participant1Photo: String = "",
        participant2Photo: String = "",
        callback: (Boolean, String, String?) -> Unit
    )

    fun getChatRooms(
        userId: String,
        callback: (Boolean, String, List<ChatRoom>?) -> Unit
    )

    fun getOrCreateChatRoom(
        participant1Id: String,
        participant2Id: String,
        participant1Name: String,
        participant2Name: String,
        callback: (Boolean, String, ChatRoom?) -> Unit
    )

    // Message Operations
    fun sendMessage(
        chatId: String,
        message: ChatMessage,
        callback: (Boolean, String) -> Unit
    )

    fun getMessages(
        chatId: String,
        callback: (Boolean, String, List<ChatMessage>?) -> Unit
    )

    fun listenForNewMessages(
        chatId: String,
        onNewMessage: (ChatMessage) -> Unit
    )

    fun markMessagesAsRead(
        chatId: String,
        userId: String,
        callback: (Boolean, String) -> Unit
    )

    fun deleteMessage(
        messageId: String,
        chatId: String,
        callback: (Boolean, String) -> Unit
    )

    fun deleteChatRoom(
        chatId: String,
        userId: String,
        callback: (Boolean, String) -> Unit
    )

    fun updateTypingStatus(
        chatId: String,
        userId: String,
        isTyping: Boolean
    )

    fun listenForTypingStatus(
        chatId: String,
        onTypingStatusChanged: (String, Boolean) -> Unit
    )
}