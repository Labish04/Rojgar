package com.example.rojgar.model

data class BlockModel(
    val blockerId: String = "",
    val blockedId: String = "",
    val blockedAt: Long = System.currentTimeMillis(),
    val blockerType: String = "JobSeeker",
    val blockedType: String = "JobSeeker"
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "blockerId" to blockerId,
            "blockedId" to blockedId,
            "blockedAt" to blockedAt,
            "blockerType" to blockerType,
            "blockedType" to blockedType
        )
    }
}