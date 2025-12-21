package com.example.rojgar.repository

import com.example.rojgar.model.EducationModel

interface EducationRepo {
    fun addEducation(
        education: EducationModel,
        callback: (Boolean, String) -> Unit
    )

    fun getEducationsByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<EducationModel>?) -> Unit
    )

    fun updateEducation(
        educationId: String,
        updatedEducation: EducationModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteEducation(
        educationId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getEducationById(
        educationId: String,
        callback: (Boolean, String, EducationModel?) -> Unit
    )
}