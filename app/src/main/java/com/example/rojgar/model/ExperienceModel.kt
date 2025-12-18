package com.example.rojgar.model

data class ExperienceModel(
    val experienceId : String = "",
    val companyName : String = "",
    val title : String = "",
    val level : String = "",
    val startDate : String = "",
    val endDate : String = "",
    val currentlyWorkingStatus : String = "",
    val experienceLetter : String = "",
    val jobSeekerId : String = ""
){
    fun toMap() : Map<String,Any?>{
        return mapOf(
            "experienceId" to experienceId,
            "CompanyName" to companyName,
            "JobTitle" to title,
            "JobLevel" to level,
            "JobStartDate" to startDate,
            "JobEndDate" to endDate,
            "JobWorkingStatus" to currentlyWorkingStatus,
            "Letter" to experienceLetter,
            "jobSeekerId" to jobSeekerId,
        )
    }
}
