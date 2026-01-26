package com.example.rojgar.model

/**
 * Notification types for different events in the app
 */
enum class NotificationType {
    JOB_ALERT,              // New job posted
    MESSAGE,                // New message received
    SYSTEM,                 // System notifications (verification, etc.)
    GENERAL,                // General notifications
    APPLICATION_UPDATE,     // Application status changed
    CANDIDATE_ALERT,        // New candidate applied (for companies)
    EVENTS,                 // Events and activities
    PROFILE_UPDATE          // Profile related (follows, etc.)
}

/**
 * User type for filtering notifications
 */
enum class UserType {
    JOBSEEKER,  // Job seeker specific notifications
    COMPANY,    // Company specific notifications
    ALL         // Notifications for all users
}

/**
 * Notification data model
 */
data class NotificationModel(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.GENERAL,
    val userType: UserType = UserType.ALL,
    val data: Map<String, String> = emptyMap()  // Additional data (jobId, userId, etc.)
) {
    /**
     * Convert to Map for Firebase storage
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "message" to message,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "type" to type.name,
            "userType" to userType.name,
            "data" to data
        )
    }

    companion object {
        /**
         * Create NotificationModel from Firebase snapshot data
         */
        fun fromMap(map: Map<String, Any>): NotificationModel {
            return NotificationModel(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                message = map["message"] as? String ?: "",
                timestamp = map["timestamp"] as? Long ?: System.currentTimeMillis(),
                isRead = map["isRead"] as? Boolean ?: false,
                type = try {
                    NotificationType.valueOf(map["type"] as? String ?: "GENERAL")
                } catch (e: Exception) {
                    NotificationType.GENERAL
                },
                userType = try {
                    UserType.valueOf(map["userType"] as? String ?: "ALL")
                } catch (e: Exception) {
                    UserType.ALL
                },
                data = (map["data"] as? Map<String, String>) ?: emptyMap()
            )
        }
    }
}

/**
 * Preference model for job recommendations
 * (This might already exist in your project - if so, you can skip this)
 */
data class PreferencesModel(
    val userId: String = "",
    val categories: List<String> = emptyList(),
    val industries: List<String> = emptyList(),
    val titles: List<String> = emptyList(),
    val availabilities: List<String> = emptyList(),
    val location: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "categories" to categories,
            "industries" to industries,
            "titles" to titles,
            "availabilities" to availabilities,
            "location" to location,
            "timestamp" to timestamp
        )
    }
}