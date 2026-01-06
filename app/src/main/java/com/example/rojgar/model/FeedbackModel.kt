package com.example.rojgar.model

data class FeedbackModel(
    val id: String = "",
    val companyName: String = "",
    val companyId: String = "",
    val message: String = "",
    val rating: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "" // e.g., "Interview", "Communication", "Process"
)