package com.example.rojgar.viewmodel

import com.example.rojgar.model.ExperienceModel
import com.example.rojgar.repository.ExperienceRepo

class ExperienceViewModel(private val repo: ExperienceRepo) {

    fun addExperience(
        experience: ExperienceModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addExperience(experience, callback)
    }

    fun getExperiencesByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<ExperienceModel>?) -> Unit
    ) {
        repo.getExperiencesByJobSeekerId(jobSeekerId, callback)
    }

    fun updateExperience(
        experienceId: String,
        updatedExperience: ExperienceModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateExperience(experienceId, updatedExperience, callback)
    }

    fun deleteExperience(
        experienceId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteExperience(experienceId, callback)
    }

    fun getExperienceById(
        experienceId: String,
        callback: (Boolean, String, ExperienceModel?) -> Unit
    ) {
        repo.getExperienceById(experienceId, callback)
    }
}