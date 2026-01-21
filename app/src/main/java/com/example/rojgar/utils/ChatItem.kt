package com.example.rojgar.utils

import com.example.rojgar.model.ChatRoom
import com.example.rojgar.model.GroupChat

sealed class ChatItem {
    abstract val id: String
    abstract val name: String
    abstract val image: String
    abstract val lastMessage: String
    abstract val lastMessageTime: Long
    abstract val unreadCount: Int

    data class Private(val chatRoom: ChatRoom, val otherUserId: String) : ChatItem() {
        override val id = chatRoom.chatId
        // Logic to pick the OTHER person's name/image
        override val name = if (chatRoom.participant1Id == otherUserId) chatRoom.participant1Name else chatRoom.participant2Name
        override val image = if (chatRoom.participant1Id == otherUserId) chatRoom.participant1Photo else chatRoom.participant2Photo
        override val lastMessage = chatRoom.lastMessage
        override val lastMessageTime = chatRoom.lastMessageTime
        override val unreadCount = chatRoom.unreadCount
    }

    data class Group(val groupChat: GroupChat) : ChatItem() {
        override val id = groupChat.groupId
        override val name = groupChat.groupName
        override val image = groupChat.groupImage
        override val lastMessage = groupChat.lastMessage
        override val lastMessageTime = groupChat.lastMessageTime
        override val unreadCount = groupChat.unreadCount
    }
}