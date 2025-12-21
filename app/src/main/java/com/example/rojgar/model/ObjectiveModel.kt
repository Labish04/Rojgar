package com.example.rojgar.model

data class ObjectiveModel(
    val objectiveId: String = "",
    val jobSeekerId: String = "",
    val objectiveText: String = "",
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "objectiveId" to objectiveId,
            "jobSeekerId" to jobSeekerId,
            "objectiveText" to objectiveText,
        )
    }
}