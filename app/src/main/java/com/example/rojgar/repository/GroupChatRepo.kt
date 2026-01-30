package com.example.rojgar.repository

import android.content.Context
import android.net.Uri
import com.example.rojgar.model.GroupChat
import com.example.rojgar.model.GroupMember
import com.example.rojgar.model.GroupMessage
import java.io.File

interface GroupChatRepository {
    // Group Operations
    fun createGroup(
        groupName: String,
        groupImage: String,
        createdBy: String,
        createdByName: String,
        members: List<String>,
        memberNames: List<String>,
        memberPhotos: List<String>,
        callback: (Result<String>) -> Unit // returns groupId
    )

    fun getGroupsForUser(
        userId: String,
        callback: (Result<List<GroupChat>>) -> Unit
    )

    fun getGroupById(
        groupId: String,
        callback: (Result<GroupChat>) -> Unit
    )

    fun updateGroup(
        groupId: String,
        updates: Map<String, Any>,
        callback: (Result<Unit>) -> Unit
    )

    fun deleteGroup(
        groupId: String,
        callback: (Result<Unit>) -> Unit
    )

    fun addGroupMembers(
        groupId: String,
        newMembers: List<String>,
        newMemberNames: List<String>,
        newMemberPhotos: List<String>,
        callback: (Result<Unit>) -> Unit
    )

    fun removeGroupMember(
        groupId: String,
        memberId: String,
        callback: (Result<Unit>) -> Unit
    )

    // Message Operations
    fun sendGroupMessage(
        groupId: String,
        message: GroupMessage,
        callback: (Result<String>) -> Unit // returns messageId
    )

    fun getGroupMessages(
        groupId: String,
        limit: Int = 50,
        callback: (Result<List<GroupMessage>>) -> Unit
    )

    fun listenForGroupMessages(
        groupId: String,
        onNewMessage: (GroupMessage) -> Unit,
        onError: (Exception) -> Unit
    )

    fun markGroupMessageAsRead(
        groupId: String,
        messageId: String,
        userId: String,
        callback: (Result<Unit>) -> Unit
    )

    fun deleteGroupMessage(
        groupId: String,
        messageId: String,
        callback: (Result<Unit>) -> Unit
    )

    // Media Operations
    fun uploadGroupImage(
        context: Context,
        imageUri: Uri,
        onProgress: (Double) -> Unit,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    )

    fun uploadGroupMedia(
        context: Context,
        mediaUri: Uri,
        mediaType: String,
        onProgress: (Double) -> Unit,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    )

    fun leaveGroup(
        groupId: String,
        userId: String,
        callback: (Result<Unit>) -> Unit
    )

    fun updateGroupMemberRole(
        groupId: String,
        memberId: String,
        role: String, // "admin", "member"
        callback: (Result<Unit>) -> Unit
    )

    fun getGroupMembersDetails(
        groupId: String,
        callback: (Result<List<GroupMember>>) -> Unit
    )

    fun updateGroupMember(
        groupId: String,
        oldMemberId: String,
        newMemberId: String,
        newMemberName: String,
        newMemberPhoto: String,
        callback: (Result<Unit>) -> Unit
    )
}