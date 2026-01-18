package com.example.rojgar.model

data class HelpAndSupportModel(
    val id: String = "",
    val companyName: String = "",
    val companyEmail: String = "",
    val subject: String = "",
    val category: SupportCategory = SupportCategory.GENERAL,
    val priority: Priority = Priority.MEDIUM,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: RequestStatus = RequestStatus.PENDING,
    val attachments: List<String> = emptyList()
)

enum class SupportCategory(val displayName: String) {
    GENERAL("General Inquiry"),
    TECHNICAL("Technical Issue"),
    BILLING("Billing & Payments"),
    ACCOUNT("Account Management"),
    PARTNERSHIP("Partnership Request"),
    FEEDBACK("Feedback & Suggestions"),
    OTHER("Other")
}

enum class Priority(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent")
}

enum class RequestStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

// Extension function to convert model to Map for Firebase updates
fun HelpAndSupportModel.toMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "companyName" to companyName,
        "companyEmail" to companyEmail,
        "subject" to subject,
        "category" to category.name,
        "priority" to priority.name,
        "description" to description,
        "timestamp" to timestamp,
        "status" to status.name,
        "attachments" to attachments
    )
}
