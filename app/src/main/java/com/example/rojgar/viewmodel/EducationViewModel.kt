package com.example.rojgar.viewmodel

import com.example.rojgar.model.EducationModel
import com.example.rojgar.repository.EducationRepo

class EducationViewModel(private val repo: EducationRepo) {

    fun addEducation(
        education: EducationModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addEducation(education, callback)
    }

    fun getEducationsByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<EducationModel>?) -> Unit
    ) {
        repo.getEducationsByJobSeekerId(jobSeekerId, callback)
    }

    fun updateEducation(
        educationId: String,
        updatedEducation: EducationModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateEducation(educationId, updatedEducation, callback)
    }

    fun deleteEducation(
        educationId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteEducation(educationId, callback)
    }

    fun getEducationById(
        educationId: String,
        callback: (Boolean, String, EducationModel?) -> Unit
    ) {
        repo.getEducationById(educationId, callback)
    }
}