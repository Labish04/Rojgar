package com.example.rojgar.repository

import android.util.Log
import com.example.rojgar.model.NotificationModel
import com.example.rojgar.model.NotificationType
import com.example.rojgar.model.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepoImpl : NotificationRepo {

    private val database = FirebaseDatabase.getInstance()
    private val notificationsRef = database.getReference("Notifications")
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "NotificationRepoImpl"
    }

    override fun getAllNotifications(userType: UserType): Flow<List<NotificationModel>> = callbackFlow {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Log.w(TAG, "User not logged in")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        Log.d(TAG, "Fetching notifications for user: $userId")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifications = mutableListOf<NotificationModel>()

                if (snapshot.exists()) {
                    Log.d(TAG, "Found ${snapshot.childrenCount} notifications")

                    for (notificationSnapshot in snapshot.children) {
                        try {
                            val id = notificationSnapshot.key ?: continue
                            val title = notificationSnapshot.child("title").getValue(String::class.java) ?: "Notification"
                            val message = notificationSnapshot.child("message").getValue(String::class.java) ?: ""
                            val timestamp = notificationSnapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                            val isRead = notificationSnapshot.child("isRead").getValue(Boolean::class.java) ?: false
                            val typeString = notificationSnapshot.child("type").getValue(String::class.java) ?: "general"

                            // Map notification type from string to enum
                            val type = mapStringToNotificationType(typeString)

                            // Map notification to user type
                            val mappedUserType = when (typeString) {
                                "follow" -> UserType.ALL // Both can receive follow notifications
                                "job" -> UserType.JOBSEEKER // Job notifications go to job seekers
                                "verification" -> UserType.COMPANY // Verification notifications go to companies
                                "candidate_alert" -> UserType.COMPANY
                                "application_update" -> UserType.JOBSEEKER
                                "events", "event" -> UserType.ALL
                                "message" -> UserType.ALL
                                else -> UserType.ALL
                            }

                            // Only add notifications that match the current user type or are for all
                            if (mappedUserType == userType || mappedUserType == UserType.ALL) {
                                val notification = NotificationModel(
                                    id = id,
                                    title = title,
                                    message = message,
                                    timestamp = timestamp,
                                    isRead = isRead,
                                    type = type,
                                    userType = mappedUserType
                                )
                                notifications.add(notification)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing notification: ${e.message}")
                        }
                    }

                    // Sort by timestamp (newest first)
                    notifications.sortByDescending { it.timestamp }
                    Log.d(TAG, "Sending ${notifications.size} filtered notifications")
                } else {
                    Log.d(TAG, "No notifications found for user")
                }

                trySend(notifications)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching notifications: ${error.message}")
                trySend(emptyList())
            }
        }

        notificationsRef.child(userId).addValueEventListener(listener)

        awaitClose {
            notificationsRef.child(userId).removeEventListener(listener)
        }
    }

    override suspend fun markAsRead(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return

        try {
            notificationsRef
                .child(userId)
                .child(notificationId)
                .child("isRead")
                .setValue(true)
                .await()

            Log.d(TAG, "Marked notification as read: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read: ${e.message}")
        }
    }

    override suspend fun markAsUnread(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return

        try {
            notificationsRef
                .child(userId)
                .child(notificationId)
                .child("isRead")
                .setValue(false)
                .await()

            Log.d(TAG, "Marked notification as unread: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as unread: ${e.message}")
        }
    }

    override suspend fun deleteNotification(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return

        try {
            notificationsRef
                .child(userId)
                .child(notificationId)
                .removeValue()
                .await()

            Log.d(TAG, "Deleted notification: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification: ${e.message}")
        }
    }

    override suspend fun clearAllNotifications(userType: UserType) {
        val userId = auth.currentUser?.uid ?: return

        try {
            notificationsRef
                .child(userId)
                .removeValue()
                .await()

            Log.d(TAG, "Cleared all notifications for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing notifications: ${e.message}")
        }
    }

    override fun getUnreadCount(userType: UserType): Flow<Int> = callbackFlow {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var unreadCount = 0

                if (snapshot.exists()) {
                    for (notificationSnapshot in snapshot.children) {
                        val isRead = notificationSnapshot.child("isRead").getValue(Boolean::class.java) ?: false
                        val typeString = notificationSnapshot.child("type").getValue(String::class.java) ?: "general"

                        // Map notification to user type
                        val mappedUserType = when (typeString) {
                            "follow" -> UserType.ALL
                            "job" -> UserType.JOBSEEKER
                            "verification" -> UserType.COMPANY
                            "candidate_alert" -> UserType.COMPANY
                            "application_update" -> UserType.JOBSEEKER
                            else -> UserType.ALL
                        }

                        // Only count unread notifications that match user type
                        if (!isRead && (mappedUserType == userType || mappedUserType == UserType.ALL)) {
                            unreadCount++
                        }
                    }
                }

                Log.d(TAG, "Unread count for user $userId: $unreadCount")
                trySend(unreadCount)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching unread count: ${error.message}")
                trySend(0)
            }
        }

        notificationsRef.child(userId).addValueEventListener(listener)

        awaitClose {
            notificationsRef.child(userId).removeEventListener(listener)
        }
    }

    /**
     * Map string notification type to NotificationType enum
     */
    private fun mapStringToNotificationType(typeString: String): NotificationType {
        return when (typeString.lowercase()) {
            "job", "job_alert" -> NotificationType.JOB_ALERT
            "follow" -> NotificationType.PROFILE_UPDATE // Using PROFILE_UPDATE for follow notifications
            "verification" -> NotificationType.SYSTEM
            "message" -> NotificationType.MESSAGE
            "application_update" -> NotificationType.APPLICATION_STATUS
            "candidate_alert" -> NotificationType.CANDIDATE_ALERT
            "events", "event" -> NotificationType.EVENTS
            "system" -> NotificationType.SYSTEM
            else -> NotificationType.GENERAL
        }
    }
}