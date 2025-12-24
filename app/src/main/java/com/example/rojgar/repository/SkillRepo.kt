package com.example.rojgar.repository

import com.example.rojgar.model.SkillModel

interface SkillRepo {
    fun addSkill(
        skill: SkillModel,
        callback: (Boolean, String) -> Unit
    )

    fun getSkillsByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<SkillModel>?) -> Unit
    )

    fun updateSkill(
        skillId: String,
        updatedSkill: SkillModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteSkill(
        skillId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getSkillById(
        skillId: String,
        callback: (Boolean, String, SkillModel?) -> Unit
    )
}