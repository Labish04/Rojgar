package com.example.rojgar.model

data class GroupChat(
    val groupId: String = "",
    val groupName: String = "",
    val groupImage: String = "",
    val createdBy: String = "", // userId of creator
    val createdByName: String = "",
    val members: List<String> = emptyList(), // list of user IDs
    val memberNames: List<String> = emptyList(), // list of user names
    val memberPhotos: List<String> = emptyList(), // list of user profile photos
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "groupId" to groupId,
            "groupName" to groupName,
            "groupImage" to groupImage,
            "createdBy" to createdBy,
            "createdByName" to createdByName,
            "members" to members,
            "memberNames" to memberNames,
            "memberPhotos" to memberPhotos,
            "lastMessage" to lastMessage,
            "lastMessageTime" to lastMessageTime,
            "unreadCount" to unreadCount,
            "createdAt" to createdAt,
            "isActive" to isActive
        )
    }
}

data class GroupMessage(
    val messageId: String = "",
    val groupId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val messageText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val readBy: List<String> = emptyList(), // list of user IDs who read the message
    val messageType: String = "text", // text, image, video, document, voice
    val mediaUrl: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "messageId" to messageId,
            "groupId" to groupId,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderPhoto" to senderPhoto,
            "messageText" to messageText,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "readBy" to readBy,
            "messageType" to messageType,
            "mediaUrl" to mediaUrl
        )
    }
}

data class MutualContact(
    val userId: String,
    val userType: String, // "JobSeeker" or "Company"
    val userName: String,
    val userPhoto: String = "",
    val isSelected: Boolean = false
)