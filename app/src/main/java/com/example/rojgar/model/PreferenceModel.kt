package com.example.rojgar.model

data class PreferenceModel(
    val preferenceId: String = "",
    val categories: List<String> = emptyList(),
    val industries: List<String> = emptyList(),
    val titles: List<String> = emptyList(),
    val availabilities: List<String> = emptyList(),
    val location: String = "",
    val jobSeekerId: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "preferenceId" to preferenceId,
            "categories" to categories,
            "industries" to industries,
            "titles" to titles,
            "availabilities" to availabilities,
            "location" to location,
            "jobSeekerId" to jobSeekerId
        )
    }
}