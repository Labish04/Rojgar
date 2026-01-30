package com.example.rojgar.repository

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.rojgar.model.ChatMessage
import com.example.rojgar.model.ChatRoom
import com.example.rojgar.utils.NotificationHelper // Add this import
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap

class ChatRepositoryImpl(private val context: Context) : ChatRepository { // Added Context parameter

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val chatRoomsRef: DatabaseReference = database.getReference("ChatRooms")
    private val messagesRef: DatabaseReference = database.getReference("ChatMessages")
    private val typingStatusRef: DatabaseReference = database.getReference("TypingStatus")
    private val notificationSettingsRef: DatabaseReference = database.getReference("NotificationSettings")

    // Cloudinary configuration
    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dtmprduic",
            "api_key" to "883843915169633",
            "api_secret" to "DhiLcks25VLVZCBhWgGvObdGGyE"
        )
    )

    override fun createChatRoom(
        participant1Id: String,
        participant2Id: String,
        participant1Name: String,
        participant2Name: String,
        participant1Photo: String,
        participant2Photo: String,
        callback: (Boolean, String, String?) -> Unit
    ) {
        val chatId = generateChatId(participant1Id, participant2Id)

        val chatRoom = ChatRoom(
            chatId = chatId,
            participant1Id = participant1Id,
            participant2Id = participant2Id,
            participant1Name = participant1Name,
            participant2Name = participant2Name,
            participant1Photo = participant1Photo,
            participant2Photo = participant2Photo,
            lastMessage = "",
            lastMessageTime = System.currentTimeMillis(),
            unreadCount = 0
        )

        chatRoomsRef.child(chatId).setValue(chatRoom)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Chat room created successfully", chatId)
                } else {
                    callback(false, task.exception?.message ?: "Failed to create chat room", null)
                }
            }
    }

    override fun getChatRooms(
        userId: String,
        callback: (Boolean, String, List<ChatRoom>?) -> Unit
    ) {
        chatRoomsRef.orderByChild("participant1Id").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatRooms1 = mutableListOf<ChatRoom>()
                    for (data in snapshot.children) {
                        val chatRoom = data.getValue(ChatRoom::class.java)
                        chatRoom?.let { chatRooms1.add(it) }
                    }

                    chatRoomsRef.orderByChild("participant2Id").equalTo(userId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot2: DataSnapshot) {
                                val chatRooms2 = mutableListOf<ChatRoom>()
                                for (data in snapshot2.children) {
                                    val chatRoom = data.getValue(ChatRoom::class.java)
                                    chatRoom?.let { chatRooms2.add(it) }
                                }

                                val allChatRooms = chatRooms1 + chatRooms2
                                val sortedChatRooms = allChatRooms.sortedByDescending { it.lastMessageTime }
                                callback(true, "Chat rooms fetched", sortedChatRooms)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                callback(false, error.message, null)
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getOrCreateChatRoom(
        participant1Id: String,
        participant2Id: String,
        participant1Name: String,
        participant2Name: String,
        participant1Photo: String,
        participant2Photo: String,
        callback: (Boolean, String, ChatRoom?) -> Unit
    ) {
        val chatId = generateChatId(participant1Id, participant2Id)

        chatRoomsRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val chatRoom = snapshot.getValue(ChatRoom::class.java)
                    callback(true, "Chat room found", chatRoom)
                } else {
                    val newChatRoom = ChatRoom(
                        chatId = chatId,
                        participant1Id = participant1Id,
                        participant2Id = participant2Id,
                        participant1Name = participant1Name,
                        participant2Name = participant2Name,
                        participant1Photo = participant1Photo,
                        participant2Photo = participant2Photo,
                        lastMessage = "",
                        lastMessageTime = System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis()
                    )

                    chatRoomsRef.child(chatId).setValue(newChatRoom)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                callback(true, "Chat room created", newChatRoom)
                            } else {
                                callback(false, task.exception?.message ?: "Failed to create chat room", null)
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun sendMessage(
        chatId: String,
        message: ChatMessage,
        callback: (Boolean, String) -> Unit
    ) {
        val messageRef = messagesRef.child(chatId).push()
        val messageWithId = message.copy(messageId = messageRef.key ?: UUID.randomUUID().toString())

        messageRef.setValue(messageWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateChatRoomLastMessage(chatId, messageWithId)

                    // Send notification to receiver if notifications are enabled
                    sendMessageNotification(messageWithId, chatId)

                    callback(true, "Message sent successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to send message")
                }
            }
    }

    // New function: Send message with notification
    fun sendMessageWithNotification(
        chatId: String,
        message: ChatMessage,
        callback: (Boolean, String) -> Unit
    ) {
        val messageRef = messagesRef.child(chatId).push()
        val messageWithId = message.copy(messageId = messageRef.key ?: UUID.randomUUID().toString())

        messageRef.setValue(messageWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateChatRoomLastMessage(chatId, messageWithId)

                    // Check if receiver has notifications enabled for this chat
                    checkAndSendNotification(messageWithId, chatId) { shouldSend ->
                        if (shouldSend) {
                            sendMessageNotification(messageWithId, chatId)
                        }
                    }

                    callback(true, "Message sent successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to send message")
                }
            }
    }

    private fun sendMessageNotification(message: ChatMessage, chatId: String) {
        val receiverId = message.receiverId
        val senderId = message.senderId

        // Get sender's name
        getSenderName(senderId) { senderName ->
            val notificationText = when (message.messageType) {
                "voice" -> "🎤 Voice message"
                "image" -> "📷 Photo"
                "video" -> "🎥 Video"
                "document" -> "📄 Document"
                else -> message.messageText
            }

            // Send notification using NotificationHelper
            NotificationHelper.sendMessageNotification(
                context,
                receiverId = receiverId,
                senderId = senderId,
                senderName = senderName,
                messageText = notificationText,
                chatType = "direct"
            )

            Log.d("ChatRepository", "Message notification sent to $receiverId")
        }
    }

    private fun checkAndSendNotification(
        message: ChatMessage,
        chatId: String,
        callback: (Boolean) -> Unit
    ) {
        val receiverId = message.receiverId

        // Check global notification settings
        notificationSettingsRef.child(receiverId).child("messageNotifications")
            .get()
            .addOnSuccessListener { snapshot ->
                val globalEnabled = snapshot.getValue(Boolean::class.java) ?: true

                if (!globalEnabled) {
                    callback(false)
                    return@addOnSuccessListener
                }

                // Check DND mode
                notificationSettingsRef.child(receiverId).child("dndMode")
                    .get()
                    .addOnSuccessListener { dndSnapshot ->
                        val isDND = dndSnapshot.getValue(Boolean::class.java) ?: false

                        if (isDND) {
                            callback(false)
                            return@addOnSuccessListener
                        }

                        // Check if sender is blocked
                        checkIfBlocked(receiverId, message.senderId) { isBlocked ->
                            callback(!isBlocked && globalEnabled)
                        }
                    }
                    .addOnFailureListener {
                        callback(globalEnabled)
                    }
            }
            .addOnFailureListener {
                callback(true) // Default to sending if settings not found
            }
    }

    private fun checkIfBlocked(receiverId: String, senderId: String, callback: (Boolean) -> Unit) {
        database.getReference("BlockedUsers").child(receiverId).child(senderId)
            .get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    private fun getSenderName(senderId: String, callback: (String) -> Unit) {
        // Try JobSeeker first
        database.getReference("JobSeekers").child(senderId).child("fullName")
            .get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.getValue(String::class.java)
                if (name != null) {
                    callback(name)
                } else {
                    // Try Company
                    database.getReference("Companys").child(senderId).child("companyName")
                        .get()
                        .addOnSuccessListener { companySnapshot ->
                            val companyName = companySnapshot.getValue(String::class.java) ?: "Someone"
                            callback(companyName)
                        }
                        .addOnFailureListener {
                            callback("Someone")
                        }
                }
            }
            .addOnFailureListener {
                // Try Company directly if JobSeeker fails
                database.getReference("Companys").child(senderId).child("companyName")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val companyName = snapshot.getValue(String::class.java) ?: "Someone"
                        callback(companyName)
                    }
                    .addOnFailureListener {
                        callback("Someone")
                    }
            }
    }

    override fun getMessages(
        chatId: String,
        callback: (Boolean, String, List<ChatMessage>?) -> Unit
    ) {
        messagesRef.child(chatId)
            .orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<ChatMessage>()
                    for (data in snapshot.children) {
                        val message = data.getValue(ChatMessage::class.java)
                        message?.let { messages.add(it) }
                    }
                    callback(true, "Messages fetched", messages.sortedBy { it.timestamp })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun listenForNewMessages(
        chatId: String,
        onNewMessage: (ChatMessage) -> Unit
    ) {
        messagesRef.child(chatId)
            .orderByChild("timestamp")
            .limitToLast(1)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(ChatMessage::class.java)
                    message?.let { onNewMessage(it) }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // New function to listen for new messages with notification support
    fun listenFor3t24NpUrJMNunMMASmhAM953bFGeLXzN7(
        chatId: String,
        currentUserId: String,
        onNewMessage: (ChatMessage) -> Unit
    ) {
        messagesRef.child(chatId)
            .orderByChild("timestamp")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(ChatMessage::class.java)
                    message?.let {
                        // Only process if message is for current user
                        if (it.receiverId == currentUserId && !it.isRead) {
                            onNewMessage(it)
                            // Optionally mark as read automatically
                            markMessageAsRead(it.messageId, chatId) { success, _ ->
                                if (success) {
                                    Log.d("ChatRepository", "Message marked as read automatically")
                                }
                            }
                        } else if (it.senderId == currentUserId) {
                            onNewMessage(it)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(ChatMessage::class.java)
                    message?.let { onNewMessage(it) }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun markMessagesAsRead(
        chatId: String,
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        messagesRef.child(chatId)
            .orderByChild("receiverId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val updates = mutableMapOf<String, Any>()
                    for (data in snapshot.children) {
                        updates["${data.key}/isRead"] = true
                    }

                    if (updates.isNotEmpty()) {
                        messagesRef.child(chatId).updateChildren(updates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    updateChatRoomUnreadCount(chatId, userId, 0)
                                    callback(true, "Messages marked as read")
                                } else {
                                    callback(false, task.exception?.message ?: "Failed to mark messages as read")
                                }
                            }
                    } else {
                        callback(true, "No unread messages")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }

    // New function to mark single message as read
    fun markMessageAsRead(
        messageId: String,
        chatId: String,
        callback: (Boolean, String) -> Unit
    ) {
        messagesRef.child(chatId).child(messageId).child("isRead")
            .setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Also decrement unread count in chat room
                    decrementChatRoomUnreadCount(chatId)
                    callback(true, "Message marked as read")
                } else {
                    callback(false, task.exception?.message ?: "Failed to mark message as read")
                }
            }
    }

    override fun deleteMessage(
        messageId: String,
        chatId: String,
        callback: (Boolean, String) -> Unit
    ) {
        messagesRef.child(chatId).child(messageId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Message deleted")
                } else {
                    callback(false, task.exception?.message ?: "Failed to delete message")
                }
            }
    }

    override fun deleteChatRoom(
        chatId: String,
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        chatRoomsRef.child(chatId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    messagesRef.child(chatId).removeValue()
                    callback(true, "Chat deleted")
                } else {
                    callback(false, task.exception?.message ?: "Failed to delete chat")
                }
            }
    }

    override fun updateTypingStatus(
        chatId: String,
        userId: String,
        isTyping: Boolean
    ) {
        typingStatusRef.child(chatId).child(userId).setValue(isTyping)
    }

    override fun listenForTypingStatus(
        chatId: String,
        onTypingStatusChanged: (String, Boolean) -> Unit
    ) {
        typingStatusRef.child(chatId).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val userId = snapshot.key
                val isTyping = snapshot.getValue(Boolean::class.java) ?: false
                userId?.let { onTypingStatusChanged(it, isTyping) }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val userId = snapshot.key
                val isTyping = snapshot.getValue(Boolean::class.java) ?: false
                userId?.let { onTypingStatusChanged(it, isTyping) }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val userId = snapshot.key
                userId?.let { onTypingStatusChanged(it, false) }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Voice message upload function
    override fun uploadVoiceMessage(
        audioFile: File,
        onProgress: (Double) -> Unit,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("VoiceUpload", "=== Starting Voice Upload ===")
        Log.d("VoiceUpload", "File: ${audioFile.absolutePath}")
        Log.d("VoiceUpload", "Size: ${audioFile.length()} bytes")

        if (!audioFile.exists()) {
            Log.e("VoiceUpload", "❌ File doesn't exist")
            onFailure("Audio file does not exist")
            return
        }

        if (audioFile.length() < 2048) {
            Log.e("VoiceUpload", "❌ File too small (likely invalid)")
            onFailure("Audio file is too small or corrupted")
            return
        }

        if (!audioFile.canRead()) {
            Log.e("VoiceUpload", "❌ Cannot read file")
            onFailure("Cannot read audio file")
            return
        }

        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            var inputStream: InputStream? = null

            try {
                Handler(Looper.getMainLooper()).post { onProgress(10.0) }

                val timestamp = System.currentTimeMillis()
                val uniqueId = UUID.randomUUID().toString().substring(0, 8)
                val publicId = "voice_${timestamp}_${uniqueId}"

                Log.d("VoiceUpload", "Public ID: $publicId")
                Log.d("VoiceUpload", "Extension: ${audioFile.extension}")

                inputStream = FileInputStream(audioFile)
                Handler(Looper.getMainLooper()).post { onProgress(30.0) }

                val uploadParams = hashMapOf<String, Any>(
                    "public_id" to publicId,
                    "resource_type" to "video",
                    "folder" to "voice_messages",
                    "format" to "m4a",
                    "tags" to arrayOf("voice_message", "android_app"),
                    "quality" to "auto",
                    "audio_codec" to "aac"
                )

                Log.d("VoiceUpload", "Upload params: $uploadParams")
                Log.d("VoiceUpload", "Starting upload...")

                val uploadResult = cloudinary.uploader().upload(inputStream, uploadParams)

                Handler(Looper.getMainLooper()).post { onProgress(70.0) }

                Log.d("VoiceUpload", "Upload completed")
                Log.d("VoiceUpload", "Result keys: ${uploadResult.keys}")

                var audioUrl = uploadResult["secure_url"] as? String

                if (audioUrl == null) {
                    val cloudName = "dtmprduic"
                    val version = uploadResult["version"] ?: timestamp
                    audioUrl = "https://res.cloudinary.com/$cloudName/video/upload/v$version/voice_messages/$publicId.m4a"
                    Log.d("VoiceUpload", "Constructed URL: $audioUrl")
                }

                audioUrl = audioUrl?.replace("http://", "https://")

                if (audioUrl.isNullOrBlank()) {
                    Log.e("VoiceUpload", "❌ No URL from upload result")
                    Handler(Looper.getMainLooper()).post {
                        onFailure("Failed to get audio URL")
                    }
                    return@execute
                }

                Log.d("VoiceUpload", "Audio URL: $audioUrl")

                Handler(Looper.getMainLooper()).post {
                    onProgress(100.0)
                    onSuccess(audioUrl)
                }

            } catch (e: Exception) {
                Log.e("VoiceUpload", "❌ Upload failed", e)
                Handler(Looper.getMainLooper()).post {
                    onFailure("Upload failed: ${e.message}")
                }
            } finally {
                try {
                    inputStream?.close()
                } catch (e: Exception) {
                    Log.e("VoiceUpload", "Error closing stream", e)
                }
            }
        }
    }

    override fun getVoiceMessageDuration(audioFile: File): Long {
        return try {
            if (!audioFile.exists()) {
                return 0L
            }

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(audioFile.absolutePath)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration.toLong()
            mediaPlayer.release()
            duration
        } catch (e: Exception) {
            15000L
        }
    }

    override fun uploadMediaFile(
        context: Context,
        mediaUri: Uri,
        mediaType: String,
        onProgress: (Double) -> Unit,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                Log.d("MediaUpload", "Starting $mediaType upload to Cloudinary")
                Log.d("MediaUpload", "URI: $mediaUri")

                val inputStream: InputStream? = context.contentResolver.openInputStream(mediaUri)

                if (inputStream == null) {
                    Handler(Looper.getMainLooper()).post {
                        onFailure("Failed to open file")
                    }
                    return@execute
                }

                val fileName = getFileNameFromUri(context, mediaUri)
                val timestamp = System.currentTimeMillis()
                val publicId = "chat_${mediaType}_${timestamp}_${fileName?.substringBeforeLast(".") ?: "media"}"

                Handler(Looper.getMainLooper()).post {
                    onProgress(25.0)
                }

                val resourceType = when (mediaType) {
                    "image" -> "image"
                    "video" -> "video"
                    "document" -> "raw"
                    else -> "auto"
                }

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", resourceType
                    )
                )

                Handler(Looper.getMainLooper()).post {
                    onProgress(75.0)
                }

                var mediaUrl = response["secure_url"] as String? ?: (response["url"] as String?)
                mediaUrl = mediaUrl?.replace("http://", "https://")

                Log.d("MediaUpload", "Upload successful: $mediaUrl")

                inputStream.close()

                Handler(Looper.getMainLooper()).post {
                    onProgress(100.0)
                    if (mediaUrl != null) {
                        onSuccess(mediaUrl)
                    } else {
                        onFailure("Failed to get media URL")
                    }
                }

            } catch (e: Exception) {
                Log.e("MediaUpload", "Upload failed", e)
                Handler(Looper.getMainLooper()).post {
                    onFailure("Upload failed: ${e.message}")
                }
            }
        }
    }

    private fun generateChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }

    private fun updateChatRoomLastMessage(chatId: String, message: ChatMessage) {
        chatRoomsRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val chatRoom = snapshot.getValue(ChatRoom::class.java)
                    chatRoom?.let { room ->
                        val isSenderParticipant1 = message.senderId == room.participant1Id

                        val updates = HashMap<String, Any>()

                        val displayText = when (message.messageType) {
                            "voice" -> "🎤 Voice message"
                            "image" -> "📷 Photo"
                            "video" -> "🎥 Video"
                            "document" -> "📄 Document"
                            else -> message.messageText
                        }

                        updates["lastMessage"] = displayText
                        updates["lastMessageTime"] = message.timestamp

                        // Increment unread count for receiver
                        val newUnreadCount = room.unreadCount + 1
                        updates["unreadCount"] = newUnreadCount

                        chatRoomsRef.child(chatId).updateChildren(updates)
                            .addOnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.e("ChatRepository", "Failed to update chat room: ${task.exception}")
                                }
                            }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepository", "Failed to get chat room: ${error.message}")
            }
        })
    }

    private fun updateChatRoomUnreadCount(chatId: String, userId: String, count: Int) {
        chatRoomsRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val chatRoom = snapshot.getValue(ChatRoom::class.java)
                    chatRoom?.let {
                        val updates = HashMap<String, Any>()
                        updates["unreadCount"] = count
                        chatRoomsRef.child(chatId).updateChildren(updates)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepository", "Failed to update unread count: ${error.message}")
            }
        })
    }

    private fun decrementChatRoomUnreadCount(chatId: String) {
        chatRoomsRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val chatRoom = snapshot.getValue(ChatRoom::class.java)
                    chatRoom?.let {
                        val currentCount = it.unreadCount
                        if (currentCount > 0) {
                            val updates = HashMap<String, Any>()
                            updates["unreadCount"] = currentCount - 1
                            chatRoomsRef.child(chatId).updateChildren(updates)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepository", "Failed to decrement unread count: ${error.message}")
            }
        })
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return uri.lastPathSegment ?: "file_${System.currentTimeMillis()}"
    }

    // New function to get notification settings for a user
    fun getNotificationSettings(
        userId: String,
        callback: (Map<String, Any>?) -> Unit
    ) {
        notificationSettingsRef.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val settings = snapshot.value as? Map<String, Any>
                        callback(settings)
                    } else {
                        callback(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    // New function to update notification settings
    fun updateNotificationSettings(
        userId: String,
        settings: Map<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        notificationSettingsRef.child(userId)
            .updateChildren(settings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Notification settings updated")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update settings")
                }
            }
    }

    // New function to mute a specific chat
    fun muteChat(
        userId: String,
        chatId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val updates = hashMapOf<String, Any>(
            "mutedChats/$chatId" to true,
            "mutedChats/$chatId/mutedAt" to System.currentTimeMillis()
        )

        notificationSettingsRef.child(userId)
            .updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Chat muted")
                } else {
                    callback(false, task.exception?.message ?: "Failed to mute chat")
                }
            }
    }

    // New function to unmute a chat
    fun unmuteChat(
        userId: String,
        chatId: String,
        callback: (Boolean, String) -> Unit
    ) {
        notificationSettingsRef.child(userId).child("mutedChats").child(chatId)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Chat unmuted")
                } else {
                    callback(false, task.exception?.message ?: "Failed to unmute chat")
                }
            }
    }

    // New function to check if chat is muted
    fun isChatMuted(
        userId: String,
        chatId: String,
        callback: (Boolean) -> Unit
    ) {
        notificationSettingsRef.child(userId).child("mutedChats").child(chatId)
            .get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}