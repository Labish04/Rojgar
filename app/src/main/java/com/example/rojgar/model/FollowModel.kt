package com.example.rojgar.model

data class FollowModel(
    val followId: String = "",
    val followerId: String = "",       // Who is following
    val followerType: String = "",     // "JobSeeker" or "Company"
    val followingId: String = "",      // Who is being followed
    val followingType: String = "",    // "JobSeeker" or "Company"
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "followId" to followId,
            "followerId" to followerId,
            "followerType" to followerType,
            "followingId" to followingId,
            "followingType" to followingType,
            "timestamp" to timestamp
        )
    }
}

data class BlockModel(
    val blockId: String = "",
    val blockerId: String = "",        // Who is blocking
    val blockerType: String = "",      // "JobSeeker" or "Company"
    val blockedId: String = "",        // Who is being blocked
    val blockedType: String = "",      // "JobSeeker" or "Company"
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "blockId" to blockId,
            "blockerId" to blockerId,
            "blockerType" to blockerType,
            "blockedId" to blockedId,
            "blockedType" to blockedType,
            "timestamp" to timestamp
        )
    }
}