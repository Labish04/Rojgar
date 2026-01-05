package com.example.rojgar.model

data class FollowModel(
    val followId: String = "",
    val followerId: String = "",       // Who is following
    val followerType: String = "",     // "JobSeeker" or "Company"
    val followingId: String = "",      // Who is being followed
    val followingType: String = "",    // "JobSeeker" or "Company"
    val timestamp: Long = System.currentTimeMillis()
) {
    // Add compound key for querying
    val followerId_followingId: String
        get() = "$followerId-$followingId"

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "followId" to followId,
            "followerId" to followerId,
            "followerType" to followerType,
            "followingId" to followingId,
            "followingType" to followingType,
            "followerId_followingId" to followerId_followingId, // Add this
            "timestamp" to timestamp
        )
    }
}