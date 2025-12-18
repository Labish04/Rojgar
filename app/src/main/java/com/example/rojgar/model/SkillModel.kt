package com.example.rojgar.model

data class SkillModel(
    val skillId : String = "",
    val skill : String = "",
    val level : String = "",
    val jobSeekerId : String = "",
){
    fun toMap() : Map<String,Any?>{
        return mapOf(
            "skillId" to skillId,
            "skill" to skill,
            "Level" to level,
            "jobSeekerId" to jobSeekerId,
        )
    }
}
