package com.example.rojgar.model

data class SavedJobModel(
    val savedId: String = "",
    val jobSeekerId: String = "",
    val jobId: String = "",
    val savedAt: Long = System.currentTimeMillis()
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "savedId" to savedId,
            "jobSeekerId" to jobSeekerId,
            "jobId" to jobId,
            "savedAt" to savedAt
        )
    }
}
