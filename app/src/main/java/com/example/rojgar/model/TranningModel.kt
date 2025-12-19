package com.example.rojgar.model

data class TranningModel(
    val trainingId : String = "",
    val instituteName : String = "",
    val duration : String = "",
    val completionDate : String = "",
    val certificate : String = "",
    val jobSeekerId : String = "",
){
    fun toMap() : Map<String,Any?>{
        return mapOf(
            "trainingId" to trainingId,
            "instituteName" to instituteName,
            "duration" to duration,
            "completionDate" to completionDate,
            "certificate" to certificate,
            "jobSeekerId" to jobSeekerId,
        )
    }
}
