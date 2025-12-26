package com.example.rojgar.model

data class TrainingModel(
    val trainingId: String = "",
    val trainingName: String = "",
    val instituteName: String = "",
    val duration: String = "",
    val durationType: String = "",
    val completionDate: String = "",
    val certificate: String = "",
    val jobSeekerId: String = "",
){
    fun toMap() : Map<String,Any?>{
        return mapOf(
            "trainingId" to trainingId,
            "trainingName" to trainingName,
            "instituteName" to instituteName,
            "duration" to duration,
            "durationType" to durationType,
            "completionDate" to completionDate,
            "certificate" to certificate,
            "jobSeekerId" to jobSeekerId,
        )
    }
}
