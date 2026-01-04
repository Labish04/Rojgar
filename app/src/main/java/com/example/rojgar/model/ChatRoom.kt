package com.example.rojgar.model

data class ChatRoom(
    val chatId: String = "",
    val participant1Id: String = "", // JobSeeker ID
    val participant2Id: String = "", // Company ID or another JobSeeker ID
    val participant1Name: String = "",
    val participant2Name: String = "",
    val participant1Photo: String = "",
    val participant2Photo: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "chatId" to chatId,
            "participant1Id" to participant1Id,
            "participant2Id" to participant2Id,
            "participant1Name" to participant1Name,
            "participant2Name" to participant2Name,
            "participant1Photo" to participant1Photo,
            "participant2Photo" to participant2Photo,
            "lastMessage" to lastMessage,
            "lastMessageTime" to lastMessageTime,
            "unreadCount" to unreadCount,
            "createdAt" to createdAt
        )
    }
}

data class ChatMessage(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val receiverName: String = "",
    val messageText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val messageType: String = "text", // text, image, video, document
    val mediaUrl: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "messageId" to messageId,
            "chatId" to chatId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "senderName" to senderName,
            "receiverName" to receiverName,
            "messageText" to messageText,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "messageType" to messageType,
            "mediaUrl" to mediaUrl
        )
    }
}
