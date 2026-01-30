package com.example.rojgar.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.rojgar.model.GroupChat
import com.example.rojgar.model.GroupMember
import com.example.rojgar.model.GroupMessage
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GroupChatRepositoryImpl : GroupChatRepository {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val groupsRef: DatabaseReference = database.getReference("GroupChats")
    private val groupMessagesRef: DatabaseReference = database.getReference("GroupMessages")
    private val userGroupsRef: DatabaseReference = database.getReference("UserGroups")

    private val storageRef: StorageReference = storage.reference

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dtmprduic",
            "api_key" to "883843915169633",
            "api_secret" to "DhiLcks25VLVZCBhWgGvObdGGyE"
        )
    )

    override fun createGroup(
        groupName: String,
        groupImage: String,
        createdBy: String,
        createdByName: String,
        members: List<String>,
        memberNames: List<String>,
        memberPhotos: List<String>,
        callback: (Result<String>) -> Unit
    ) {
        val groupId = groupsRef.push().key ?: UUID.randomUUID().toString()

        val group = GroupChat(
            groupId = groupId,
            groupName = groupName,
            groupImage = groupImage,
            createdBy = createdBy,
            createdByName = createdByName,
            members = members,
            memberNames = memberNames,
            memberPhotos = memberPhotos
        )

        val updates = hashMapOf<String, Any?>()
        updates["GroupChats/$groupId"] = group.toMap()

        // Add group reference to each user's groups
        members.forEach { memberId ->
            updates["UserGroups/$memberId/$groupId"] = true
        }

        database.reference.updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(Result.success(groupId))
                } else {
                    callback(Result.failure(task.exception ?: Exception("Failed to create group")))
                }
            }
    }

    override fun getGroupsForUser(
        userId: String,
        callback: (Result<List<GroupChat>>) -> Unit
    ) {
        userGroupsRef.child(userId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        callback(Result.success(emptyList()))
                        return
                    }

                    val groupIds = snapshot.children.mapNotNull { it.key }
                    val groups = mutableListOf<GroupChat>()
                    var completed = 0

                    if (groupIds.isEmpty()) {
                        callback(Result.success(emptyList()))
                        return
                    }

                    groupIds.forEach { groupId ->
                        groupsRef.child(groupId).addListenerForSingleValueEvent(
                            object : ValueEventListener {
                                override fun onDataChange(groupSnapshot: DataSnapshot) {
                                    if (groupSnapshot.exists()) {
                                        val group = groupSnapshot.getValue(GroupChat::class.java)
                                        group?.let { groups.add(it) }
                                    }

                                    completed++
                                    if (completed == groupIds.size) {
                                        // Sort by last message time (most recent first)
                                        val sortedGroups = groups.sortedByDescending { it.lastMessageTime }
                                        callback(Result.success(sortedGroups))
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    completed++
                                    if (completed == groupIds.size) {
                                        callback(Result.failure(Exception(error.message)))
                                    }
                                }
                            }
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(Result.failure(Exception(error.message)))
                }
            }
        )
    }

    override fun getGroupById(
        groupId: String,
        callback: (Result<GroupChat>) -> Unit
    ) {
        groupsRef.child(groupId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val group = snapshot.getValue(GroupChat::class.java)
                        group?.let {
                            callback(Result.success(it))
                        } ?: callback(Result.failure(Exception("Group not found")))
                    } else {
                        callback(Result.failure(Exception("Group not found")))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(Result.failure(Exception(error.message)))
                }
            }
        )
    }

    override fun updateGroup(
        groupId: String,
        updates: Map<String, Any>,
        callback: (Result<Unit>) -> Unit
    ) {
        groupsRef.child(groupId).updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(Result.success(Unit))
                } else {
                    callback(Result.failure(task.exception ?: Exception("Failed to update group")))
                }
            }
    }

    override fun deleteGroup(
        groupId: String,
        callback: (Result<Unit>) -> Unit
    ) {
        // First get group to remove references from users
        getGroupById(groupId) { result ->
            result.onSuccess { group ->
                val updates = hashMapOf<String, Any?>()
                updates["GroupChats/$groupId"] = null

                // Remove group reference from all members
                group.members.forEach { memberId ->
                    updates["UserGroups/$memberId/$groupId"] = null
                }

                // Delete all messages
                updates["GroupMessages/$groupId"] = null

                database.reference.updateChildren(updates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(Result.success(Unit))
                        } else {
                            callback(Result.failure(task.exception ?: Exception("Failed to delete group")))
                        }
                    }
            }.onFailure { error ->
                callback(Result.failure(error))
            }
        }
    }

    override fun addGroupMembers(
        groupId: String,
        newMembers: List<String>,
        newMemberNames: List<String>,
        newMemberPhotos: List<String>,
        callback: (Result<Unit>) -> Unit
    ) {
        getGroupById(groupId) { result ->
            result.onSuccess { group ->
                val updatedMembers = group.members + newMembers
                val updatedMemberNames = group.memberNames + newMemberNames
                val updatedMemberPhotos = group.memberPhotos + newMemberPhotos

                val updates = hashMapOf<String, Any>()
                updates["members"] = updatedMembers
                updates["memberNames"] = updatedMemberNames
                updates["memberPhotos"] = updatedMemberPhotos

                // Add group reference to new users
                newMembers.forEach { memberId ->
                    database.reference.child("UserGroups/$memberId/$groupId").setValue(true)
                }

                groupsRef.child(groupId).updateChildren(updates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(Result.success(Unit))
                        } else {
                            callback(Result.failure(task.exception ?: Exception("Failed to add members")))
                        }
                    }
            }.onFailure { error ->
                callback(Result.failure(error))
            }
        }
    }

    override fun removeGroupMember(
        groupId: String,
        memberId: String,
        callback: (Result<Unit>) -> Unit
    ) {
        getGroupById(groupId) { result ->
            result.onSuccess { group ->
                val memberIndex = group.members.indexOf(memberId)
                if (memberIndex == -1) {
                    callback(Result.failure(Exception("Member not found in group")))
                    return@onSuccess
                }

                val updatedMembers = group.members.toMutableList().apply { removeAt(memberIndex) }
                val updatedMemberNames = group.memberNames.toMutableList().apply { removeAt(memberIndex) }
                val updatedMemberPhotos = group.memberPhotos.toMutableList().apply { removeAt(memberIndex) }

                val updates = hashMapOf<String, Any>()
                updates["members"] = updatedMembers
                updates["memberNames"] = updatedMemberNames
                updates["memberPhotos"] = updatedMemberPhotos

                // Remove group reference from user
                database.reference.child("UserGroups/$memberId/$groupId").removeValue()

                groupsRef.child(groupId).updateChildren(updates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(Result.success(Unit))
                        } else {
                            callback(Result.failure(task.exception ?: Exception("Failed to remove member")))
                        }
                    }
            }.onFailure { error ->
                callback(Result.failure(error))
            }
        }
    }

    override fun sendGroupMessage(
        groupId: String,
        message: GroupMessage,
        callback: (Result<String>) -> Unit
    ) {
        val messageId = groupMessagesRef.child(groupId).push().key
            ?: UUID.randomUUID().toString()
        val messageWithId = message.copy(messageId = messageId)

        val updates = hashMapOf<String, Any>()
        updates["GroupMessages/$groupId/$messageId"] = messageWithId.toMap()

        // Better last message display
        val lastMessageText = when (message.messageType) {
            "voice" -> "🎤 Voice message"
            "image" -> "📷 Photo"
            "video" -> "🎥 Video"
            "document" -> "📄 Document"
            else -> message.messageText
        }

        updates["GroupChats/$groupId/lastMessage"] = lastMessageText
        updates["GroupChats/$groupId/lastMessageTime"] = message.timestamp

        database.reference.updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(Result.success(messageId))
                } else {
                    callback(Result.failure(task.exception ?: Exception("Failed to send message")))
                }
            }
    }

    override fun getGroupMessages(
        groupId: String,
        limit: Int,
        callback: (Result<List<GroupMessage>>) -> Unit
    ) {
        groupMessagesRef.child(groupId)
            .orderByChild("timestamp")
            .limitToLast(limit)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val messages = mutableListOf<GroupMessage>()
                        for (data in snapshot.children) {
                            val message = data.getValue(GroupMessage::class.java)
                            message?.let { messages.add(it) }
                        }
                        callback(Result.success(messages.sortedBy { it.timestamp }))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(Result.failure(Exception(error.message)))
                    }
                }
            )
    }

    override fun listenForGroupMessages(
        groupId: String,
        onNewMessage: (GroupMessage) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // FIXED: Listen to all new messages, not just limitToLast(1)
        groupMessagesRef.child(groupId)
            .orderByChild("timestamp")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(GroupMessage::class.java)
                    message?.let { onNewMessage(it) }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(GroupMessage::class.java)
                    message?.let { onNewMessage(it) }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    onError(Exception(error.message))
                }
            })
    }

    override fun markGroupMessageAsRead(
        groupId: String,
        messageId: String,
        userId: String,
        callback: (Result<Unit>) -> Unit
    ) {
        groupMessagesRef.child(groupId).child(messageId)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val message = snapshot.getValue(GroupMessage::class.java)
                            if (message != null && !message.readBy.contains(userId)) {
                                val updatedReadBy = message.readBy + userId
                                val updates = hashMapOf<String, Any>()
                                updates["readBy"] = updatedReadBy
                                updates["isRead"] = updatedReadBy.isNotEmpty()

                                snapshot.ref.updateChildren(updates)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            callback(Result.success(Unit))
                                        } else {
                                            callback(Result.failure(task.exception ?: Exception("Failed to mark as read")))
                                        }
                                    }
                            } else {
                                callback(Result.success(Unit))
                            }
                        } else {
                            callback(Result.failure(Exception("Message not found")))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(Result.failure(Exception(error.message)))
                    }
                }
            )
    }

    override fun deleteGroupMessage(
        groupId: String,
        messageId: String,
        callback: (Result<Unit>) -> Unit
    ) {
        groupMessagesRef.child(groupId).child(messageId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(Result.success(Unit))
                } else {
                    callback(Result.failure(task.exception ?: Exception("Failed to delete message")))
                }
            }
    }

    override fun uploadGroupImage(
        context: Context,
        imageUri: Uri,
        onProgress: (Double) -> Unit,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                if (inputStream == null) {
                    onFailure("Failed to open image")
                    return@launch
                }

                val timestamp = System.currentTimeMillis()
                val uniqueId = UUID.randomUUID().toString().substring(0, 8)
                val publicId = "group_images/group_${timestamp}_${uniqueId}"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["secure_url"] as String? ?: (response["url"] as String?)
                imageUrl = imageUrl?.replace("http://", "https://")

                if (imageUrl != null) {
                    withContext(Dispatchers.Main) {
                        onSuccess(imageUrl)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to get image URL")
                    }
                }
            } catch (e: Exception) {
                Log.e("GroupChatRepository", "Upload failed", e)
                withContext(Dispatchers.Main) {
                    onFailure("Upload failed: ${e.message}")
                }
            }
        }
    }

    override fun uploadGroupMedia(
        context: Context,
        mediaUri: Uri,
        mediaType: String,
        onProgress: (Double) -> Unit,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(mediaUri)
                if (inputStream == null) {
                    onFailure("Failed to open file")
                    return@launch
                }

                val timestamp = System.currentTimeMillis()
                val uniqueId = UUID.randomUUID().toString().substring(0, 8)
                val fileName = getFileNameFromUri(context, mediaUri)
                val publicId = "group_${mediaType}_${timestamp}_${fileName?.substringBeforeLast(".") ?: "media"}"

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

                var mediaUrl = response["secure_url"] as String? ?: (response["url"] as String?)
                mediaUrl = mediaUrl?.replace("http://", "https://")

                if (mediaUrl != null) {
                    withContext(Dispatchers.Main) {
                        onSuccess(mediaUrl)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to get media URL")
                    }
                }
            } catch (e: Exception) {
                Log.e("GroupChatRepository", "Upload failed", e)
                withContext(Dispatchers.Main) {
                    onFailure("Upload failed: ${e.message}")
                }
            }
        }
    }

    override fun leaveGroup(
        groupId: String,
        userId: String,
        callback: (Result<Unit>) -> Unit
    ) {
        getGroupById(groupId) { result ->
            result.onSuccess { group ->
                // Check if user is creator
                if (group.createdBy == userId) {
                    // Creator leaving - need to assign new admin or delete group
                    // For now, we'll show message that creator can't leave
                    callback(Result.failure(Exception("Group creator cannot leave. Delete group instead.")))
                    return@onSuccess
                }

                // Find user index
                val memberIndex = group.members.indexOf(userId)
                if (memberIndex == -1) {
                    callback(Result.failure(Exception("User not found in group")))
                    return@onSuccess
                }

                // Remove user from all arrays
                val updatedMembers = group.members.toMutableList().apply { removeAt(memberIndex) }
                val updatedMemberNames = group.memberNames.toMutableList().apply { removeAt(memberIndex) }
                val updatedMemberPhotos = group.memberPhotos.toMutableList().apply { removeAt(memberIndex) }

                val updates = hashMapOf<String, Any>()
                updates["members"] = updatedMembers
                updates["memberNames"] = updatedMemberNames
                updates["memberPhotos"] = updatedMemberPhotos

                // Remove group from user's group list
                database.reference.child("UserGroups/$userId/$groupId").removeValue()

                // Update group
                groupsRef.child(groupId).updateChildren(updates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(Result.success(Unit))
                        } else {
                            callback(Result.failure(task.exception ?: Exception("Failed to leave group")))
                        }
                    }
            }.onFailure { error ->
                callback(Result.failure(error))
            }
        }
    }

    override fun updateGroupMemberRole(
        groupId: String,
        memberId: String,
        role: String,
        callback: (Result<Unit>) -> Unit
    ) {
        // Store roles in separate node for scalability
        database.reference.child("GroupRoles/$groupId/$memberId")
            .setValue(role)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(Result.success(Unit))
                } else {
                    callback(Result.failure(task.exception ?: Exception("Failed to update role")))
                }
            }
    }

    override fun getGroupMembersDetails(
        groupId: String,
        callback: (Result<List<GroupMember>>) -> Unit
    ) {
        getGroupById(groupId) { result ->
            result.onSuccess { group ->
                val members = mutableListOf<GroupMember>()

                // Fetch roles from separate node
                database.reference.child("GroupRoles/$groupId")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(roleSnapshot: DataSnapshot) {
                            val roles = mutableMapOf<String, String>()
                            for (roleData in roleSnapshot.children) {
                                roles[roleData.key.toString()] = roleData.getValue(String::class.java) ?: "member"
                            }

                            // Create GroupMember objects
                            for (i in group.members.indices) {
                                val member = GroupMember(
                                    userId = group.members[i],
                                    userName = group.memberNames[i],
                                    userPhoto = group.memberPhotos.getOrElse(i) { "" },
                                    role = roles[group.members[i]] ?:
                                    if (group.members[i] == group.createdBy) "admin" else "member"
                                )
                                members.add(member)
                            }

                            callback(Result.success(members))
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // If roles node doesn't exist, create members without roles
                            for (i in group.members.indices) {
                                val member = GroupMember(
                                    userId = group.members[i],
                                    userName = group.memberNames[i],
                                    userPhoto = group.memberPhotos.getOrElse(i) { "" },
                                    role = if (group.members[i] == group.createdBy) "admin" else "member"
                                )
                                members.add(member)
                            }
                            callback(Result.success(members))
                        }
                    })
            }.onFailure { error ->
                callback(Result.failure(error))
            }
        }
    }

    override fun updateGroupMember(
        groupId: String,
        oldMemberId: String,
        newMemberId: String,
        newMemberName: String,
        newMemberPhoto: String,
        callback: (Result<Unit>) -> Unit
    ) {
        getGroupById(groupId) { result ->
            result.onSuccess { group ->
                val memberIndex = group.members.indexOf(oldMemberId)
                if (memberIndex == -1) {
                    callback(Result.failure(Exception("Member not found")))
                    return@onSuccess
                }

                // Update the specific member in all arrays
                val updatedMembers = group.members.toMutableList().apply {
                    set(memberIndex, newMemberId)
                }
                val updatedMemberNames = group.memberNames.toMutableList().apply {
                    set(memberIndex, newMemberName)
                }
                val updatedMemberPhotos = group.memberPhotos.toMutableList().apply {
                    set(memberIndex, newMemberPhoto)
                }

                val updates = hashMapOf<String, Any>()
                updates["members"] = updatedMembers
                updates["memberNames"] = updatedMemberNames
                updates["memberPhotos"] = updatedMemberPhotos

                // Update user groups reference
                database.reference.child("UserGroups/$oldMemberId/$groupId").removeValue()
                database.reference.child("UserGroups/$newMemberId/$groupId").setValue(true)

                groupsRef.child(groupId).updateChildren(updates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(Result.success(Unit))
                        } else {
                            callback(Result.failure(task.exception ?: Exception("Failed to update member")))
                        }
                    }
            }.onFailure { error ->
                callback(Result.failure(error))
            }
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return uri.lastPathSegment ?: "file_${System.currentTimeMillis()}"
    }

    private var messageListener: ChildEventListener? = null

    fun stopListeningForMessages(groupId: String) {
        messageListener?.let {
            groupMessagesRef.child(groupId).removeEventListener(it)
        }
        messageListener = null
    }
}
