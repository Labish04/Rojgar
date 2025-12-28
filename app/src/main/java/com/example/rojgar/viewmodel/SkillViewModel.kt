package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rojgar.model.ObjectiveModel
import com.example.rojgar.model.SkillModel
import com.example.rojgar.repository.SkillRepo

class SkillViewModel(private val repo: SkillRepo) {

    private val _skill = MutableLiveData< SkillModel?>()
    val skill: LiveData<SkillModel?> = _skill

    private val _allSkills = MutableLiveData<List<SkillModel>?>()
    val allSkills : MutableLiveData<List<SkillModel>?> get() = _allSkills

    fun addSkill(
        skill: SkillModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addSkill(skill, callback)
    }

    fun getSkillsByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<SkillModel>?) -> Unit
    ) {
        repo.getSkillsByJobSeekerId(jobSeekerId, callback)
    }

    fun updateSkill(
        skillId: String,
        updatedSkill: SkillModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateSkill(skillId, updatedSkill, callback)
    }

    fun deleteSkill(
        skillId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteSkill(skillId, callback)
    }

    fun getSkillById(
        skillId: String,
        callback: (Boolean, String, SkillModel?) -> Unit
    ) {
        repo.getSkillById(skillId, callback)
    }
}