package com.example.rojgar.repository

import android.content.Context
import android.net.Uri
import com.example.rojgar.model.MessageModel
import com.zegocloud.zimkit.services.model.ZIMKitConversation
import com.zegocloud.zimkit.services.model.ZIMKitMessage

interface ZeboMessageRepo {

    // Zebo Cloud Initialization
    fun initializeZegoCloud(
        context: Context,
        userId: String,
        userName: String,
        userAvatar: String? = null,
        callback: (Boolean, String) -> Unit
    )

    fun loginZegoCloud(
        userId: String,
        userName: String,
        userAvatar: String? = null,
        callback: (Boolean, String) -> Unit
    )

    fun logoutZegoCloud(callback: (Boolean, String) -> Unit)

    // Message Operations
    fun sendTextMessage(
        receiverId: String,
        text: String,
        callback: (Boolean, String, String?) -> Unit
    )

    fun sendImageMessage(
        context: Context,
        receiverId: String,
        imageUri: Uri,
        callback: (Boolean, String) -> Unit
    )

    fun sendFileMessage(
        context: Context,
        receiverId: String,
        fileUri: Uri,
        fileName: String,
        callback: (Boolean, String) -> Unit
    )

    fun sendVoiceMessage(
        context: Context,
        receiverId: String,
        voiceUri: Uri,
        duration: Long,
        callback: (Boolean, String) -> Unit
    )

    fun editMessage(
        message: ZIMKitMessage,
        newText: String,
        conversationId: String,
        conversationType: Int,
        callback: (Boolean, String) -> Unit
    )

    fun deleteMessage(
        message: ZIMKitMessage,
        conversationId: String,
        conversationType: Int,
        callback: (Boolean, String) -> Unit
    )

    fun recallMessage(
        message: ZIMKitMessage,
        conversationId: String,
        conversationType: Int,
        callback: (Boolean, String) -> Unit
    )

    // Conversation Operations
    fun getConversationList(
        callback: (List<ZIMKitConversation>?) -> Unit
    )

    fun getMessages(
        conversationId: String,
        conversationType: Int,
        count: Int = 50,
        callback: (List<ZIMKitMessage>?) -> Unit
    )

    fun markConversationAsRead(
        conversationId: String,
        conversationType: Int,
        callback: (Boolean) -> Unit
    )

    fun deleteConversation(
        conversationId: String,
        conversationType: Int,
        callback: (Boolean) -> Unit
    )

    // Real-time Listeners
    fun listenForNewMessages(
        conversationId: String,
        conversationType: Int,
        onNewMessage: (ZIMKitMessage) -> Unit
    )

    fun listenForMessageUpdates(
        conversationId: String,
        conversationType: Int,
        onMessageUpdated: (ZIMKitMessage) -> Unit
    )

    fun removeAllListeners()

    // Helper Functions
    fun canEditMessage(messageTimestamp: Long): Boolean
    fun getCurrentUserId(): String?
    fun getCurrentUserName(): String?
}