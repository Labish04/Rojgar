package com.example.rojgar.model

data class ReviewModel(
    val reviewId: String = "",
    val userId: String = "",
    val companyId: String = "",
    val userName: String = "",
    val userImageUrl: String = "",
    val rating: Int = 5,
    val reviewText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false,
    val editedTimestamp: Long? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "reviewId" to reviewId,
            "userId" to userId,
            "companyId" to companyId,
            "userName" to userName,
            "userImageUrl" to userImageUrl,
            "rating" to rating,
            "reviewText" to reviewText,
            "timestamp" to timestamp,
            "isEdited" to isEdited,
            "editedTimestamp" to editedTimestamp
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): ReviewModel {
            return ReviewModel(
                reviewId = map["reviewId"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                companyId = map["companyId"] as? String ?: "",
                userName = map["userName"] as? String ?: "",
                userImageUrl = map["userImageUrl"] as? String ?: "",
                rating = (map["rating"] as? Long)?.toInt() ?: 5,
                reviewText = map["reviewText"] as? String ?: "",
                timestamp = map["timestamp"] as? Long ?: System.currentTimeMillis(),
                isEdited = map["isEdited"] as? Boolean ?: false,
                editedTimestamp = map["editedTimestamp"] as? Long
            )
        }
    }
}
