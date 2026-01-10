package com.example.rojgar.model

data class CalendarEventModel(
    val eventId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
    val colorHex: String = "#3B82F6",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "eventId" to eventId,
            "userId" to userId,
            "title" to title,
            "description" to description,
            "location" to location,
            "startTimeMillis" to startTimeMillis,
            "endTimeMillis" to endTimeMillis,
            "colorHex" to colorHex,
            "createdAtMillis" to createdAtMillis,
            "updatedAtMillis" to updatedAtMillis
        )
    }
}
