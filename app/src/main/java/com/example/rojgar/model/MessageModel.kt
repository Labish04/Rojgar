package com.example.rojgar.model

data class MessageModel(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false,
    val messageType: String = "text", // text, image, voice, file
    val mediaUrl: String = "",
    val senderName: String = "",
    val senderProfileImage: String = "",
    val groupId: String = "" // For grouping messages between two users
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "messageId" to messageId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "text" to text,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "messageType" to messageType,
            "mediaUrl" to mediaUrl,
            "senderName" to senderName,
            "senderProfileImage" to senderProfileImage,
            "groupId" to groupId
        )
    }
}

data class GroupModel(
    val groupId: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0,
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantImages: Map<String, String> = emptyMap(),
    val isGroup: Boolean = false,
    val groupName: String = "",
    val groupPhoto: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "groupId" to groupId,
            "lastMessage" to lastMessage,
            "lastMessageTime" to lastMessageTime,
            "unreadCount" to unreadCount,
            "participants" to participants,
            "participantNames" to participantNames,
            "participantImages" to participantImages,
            "isGroup" to isGroup,
            "groupName" to groupName,
            "groupPhoto" to groupPhoto
        )
    }
}