// File: ChatRepositoryImpl.kt
package com.example.rojgar.repository

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.cloudinary.Cloudinary
import com.cloudinary.Url
import com.cloudinary.utils.ObjectUtils
import com.example.rojgar.model.ChatMessage
import com.example.rojgar.model.ChatRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap

class ChatRepositoryImpl : ChatRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val chatRoomsRef: DatabaseReference = database.getReference("ChatRooms")
    private val messagesRef: DatabaseReference = database.getReference("ChatMessages")
    private val typingStatusRef: DatabaseReference = database.getReference("TypingStatus")

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
                    callback(true, "Message sent successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to send message")
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

    // Replace the uploadVoiceMessage method in ChatRepositoryImpl.kt

    override fun uploadVoiceMessage(
        audioFile: File,
        onProgress: (Double) -> Unit,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("VoiceUpload", "=== Starting Voice Upload ===")
        Log.d("VoiceUpload", "File: ${audioFile.absolutePath}")
        Log.d("VoiceUpload", "Size: ${audioFile.length()} bytes")

        // CRITICAL: Validate file
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

                // CRITICAL FIX: Explicitly set resource_type to "video" for audio files
                // Cloudinary treats audio files (m4a, mp3, etc.) as "video" resource type
                val uploadParams = hashMapOf<String, Any>(
                    "public_id" to publicId,
                    "resource_type" to "video",  // CRITICAL: Use "video" not "auto" for audio
                    "folder" to "voice_messages",
                    "format" to "m4a",  // Preserve original format
                    "tags" to arrayOf("voice_message", "android_app"),
                    "quality" to "auto",  // Let Cloudinary optimize
                    "audio_codec" to "aac"  // Ensure AAC codec
                )

                Log.d("VoiceUpload", "Upload params: $uploadParams")
                Log.d("VoiceUpload", "Starting upload...")

                val uploadResult = cloudinary.uploader().upload(inputStream, uploadParams)

                Handler(Looper.getMainLooper()).post { onProgress(70.0) }

                Log.d("VoiceUpload", "Upload completed")
                Log.d("VoiceUpload", "Result keys: ${uploadResult.keys}")

                // CRITICAL: Get the URL correctly
                var audioUrl = uploadResult["secure_url"] as? String

                if (audioUrl == null) {
                    // Fallback: construct URL manually
                    val cloudName = "dtmprduic"
                    val version = uploadResult["version"] ?: timestamp
                    audioUrl = "https://res.cloudinary.com/$cloudName/video/upload/v$version/voice_messages/$publicId.m4a"
                    Log.d("VoiceUpload", "Constructed URL: $audioUrl")
                }

                // CRITICAL: Ensure HTTPS
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

                        val displayText = if (message.messageType == "voice") {
                            "🎤 Voice message"
                        } else {
                            message.messageText
                        }

                        updates["lastMessage"] = displayText
                        updates["lastMessageTime"] = message.timestamp

                        if (isSenderParticipant1) {
                            val newUnreadCount = room.unreadCount + 1
                            updates["unreadCount"] = newUnreadCount
                        } else {
                            val newUnreadCount = room.unreadCount + 1
                            updates["unreadCount"] = newUnreadCount
                        }

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

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return uri.lastPathSegment ?: "file_${System.currentTimeMillis()}"
    }
}