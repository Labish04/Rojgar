package com.example.rojgar.model

data class EducationModel(
    val educationId : String = "",
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
            "eduInstituteName" to instituteName,
            "eduBoard" to board,
            "eduField" to field,
            "eduStartYear" to startYear,
            "eduEndYear" to endYear,
            "eduGradeType" to gradeType,
            "eduStatus" to currentlyStudying,
            "jobSeekerId" to jobSeekerId,
        )
    }
}
