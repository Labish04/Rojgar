package com.example.rojgar.model

data class EventModel(
    val eventId: String = "",
    val userId: String = "", // JobSeeker or Company ID
    val userType: String = "", // "JobSeeker" or "Company"
    val title: String = "",
    val description: String = "",
    val date: String = "", // Format: "dd/MM/yyyy"
    val time: String = "", // Format: "HH:mm"
    val color: String = "#3B82F6", // Hex color code
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "eventId" to eventId,
            "userId" to userId,
            "userType" to userType,
            "title" to title,
            "description" to description,
            "date" to date,
            "time" to time,
            "color" to color,
            "timestamp" to timestamp
        )
    }
}

