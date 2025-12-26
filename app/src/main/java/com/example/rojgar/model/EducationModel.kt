package com.example.rojgar.model

data class EducationModel(
    val educationId : String = "",
    val educationDegree : String = "",
    val instituteName : String = "",
    val board : String = "",
    val field : String = "",
    val startYear : String = "",
    val endYear : String = "",
    val gradeType : String = "",
    val score : String = "",
    val currentlyStudying : Boolean = false,
    val jobSeekerId : String = "",
){
    fun toMap() : Map<String,Any?>{
        return mapOf(
            "educationId" to educationId,
            "educationDegree" to educationDegree,
            "instituteName" to instituteName,
            "board" to board,
            "field" to field,
            "startYear" to startYear,
            "endYear" to endYear,
            "gradeType" to gradeType,
            "score" to score,
            "currentlyStudying" to currentlyStudying,
            "jobSeekerId" to jobSeekerId,
        )
    }
}
