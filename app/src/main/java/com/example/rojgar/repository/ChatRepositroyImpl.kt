// File: ChatRepositoryImpl.kt
package com.example.rojgar.repository

import android.util.Log
import com.example.rojgar.model.ChatMessage
import com.example.rojgar.model.ChatRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.HashMap

class ChatRepositoryImpl : ChatRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val chatRoomsRef: DatabaseReference = database.getReference("ChatRooms")
    private val messagesRef: DatabaseReference = database.getReference("ChatMessages")
    private val typingStatusRef: DatabaseReference = database.getReference("TypingStatus")

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

                    // Get chat rooms where user is participant2
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
                    // Create new chat room directly instead of calling createChatRoom recursively
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
                    // Update chat room with last message
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

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // Handle message updates (like read receipts)
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
                                    // Update chat room unread count
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
                    // Also delete all messages in this chat
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

    private fun generateChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }

    private fun updateChatRoomLastMessage(chatId: String, message: ChatMessage) {
        // First, get the chat room to know who the receiver is
        chatRoomsRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val chatRoom = snapshot.getValue(ChatRoom::class.java)
                    chatRoom?.let { room ->
                        // Determine if sender is participant1 or participant2
                        val isSenderParticipant1 = message.senderId == room.participant1Id

                        // Update last message and time
                        val updates = HashMap<String, Any>()
                        updates["lastMessage"] = message.messageText
                        updates["lastMessageTime"] = message.timestamp

                        // Update unread count based on receiver
                        if (isSenderParticipant1) {
                            // Sender is participant1, so increment for participant2
                            val newUnreadCount = room.unreadCount + 1
                            updates["unreadCount"] = newUnreadCount
                        } else {
                            // Sender is participant2, so increment for participant1
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
                    chatRoom?.let { room ->
                        // Determine which participant's count to update
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
}