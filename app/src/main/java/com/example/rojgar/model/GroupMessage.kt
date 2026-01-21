package com.example.rojgar.model

data class GroupMessage(
    val messageId: String = "",
    val groupId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val messageText: String = "",
    val messageType: String = "text", // text, image, voice, video, document
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val readBy: List<String> = emptyList(),
    val isDelivered: Boolean = false,
    val deliveredTo: List<String> = emptyList(),
    val replyTo: String? = null, // messageId of the message being replied to
    val isDeleted: Boolean = false,
    val deletedAt: Long = 0,
    val editedAt: Long = 0,
    val mediaUrl: String? = null,
    val mediaThumbnail: String? = null,
    val mediaSize: Long = 0,
    val mediaDuration: Int = 0, // For voice/video in seconds
    val reactions: Map<String, String> = emptyMap() // userId to emoji
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "messageId" to messageId,
            "groupId" to groupId,
            "senderId" to senderId,
            "senderName" to senderName,
            "messageText" to messageText,
            "messageType" to messageType,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "readBy" to readBy,
            "isDelivered" to isDelivered,
            "deliveredTo" to deliveredTo,
            "replyTo" to replyTo,
            "isDeleted" to isDeleted,
            "deletedAt" to deletedAt,
            "editedAt" to editedAt,
            "mediaUrl" to mediaUrl,
            "mediaThumbnail" to mediaThumbnail,
            "mediaSize" to mediaSize,
            "mediaDuration" to mediaDuration,
            "reactions" to reactions
        )
    }
}