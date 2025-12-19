package com.example.rojgar.model

data class PreferenceModel(
    val preferenceId : String = "",
    val category : String = "",
    val industry : String = "",
    val title : String = "",
    val availability : String = "",
    val location: String = "",
    val jobSeekerId : String = ""
){
    fun toMap() : Map<String,Any?>{
        return mapOf(
            "preferenceId" to preferenceId,
            "category" to category,
            "industry" to industry,
            "title" to title,
            "availability" to availability,
            "location" to location,
            "jobSeekerId" to jobSeekerId,
        )
    }
}
