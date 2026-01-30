package com.example.rojgar.model

data class HelpSupportModel(
    val requestId: String = "",
    val userId: String = "",
    val userType: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val problemType: String = "",
    val priority: String = "",
    val description: String = "",
    val screenshotUrl: String = "",
    val status: String = "Pending",
    val createdAt: String = "",
    val updatedAt: String = "",
    val adminNotes: String = "",
    val resolvedBy: String = "",
    val resolvedAt: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "requestId" to requestId,
            "userId" to userId,
            "userType" to userType,
            "userEmail" to userEmail,
            "userName" to userName,
            "problemType" to problemType,
            "priority" to priority,
            "description" to description,
            "screenshotUrl" to screenshotUrl,
            "status" to status,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "adminNotes" to adminNotes,
            "resolvedBy" to resolvedBy,
            "resolvedAt" to resolvedAt
        )
    }
}