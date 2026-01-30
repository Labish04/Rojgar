package com.example.rojgar.repository

import android.content.Context
import android.net.Uri
import com.example.rojgar.model.ExperienceModel

interface ExperienceRepo {
    fun addExperience(
        experience: ExperienceModel,
        callback: (Boolean, String) -> Unit
    )

    fun getExperiencesByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<ExperienceModel>?) -> Unit
    )

    fun updateExperience(
        experienceId: String,
        updatedExperience: ExperienceModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteExperience(
        experienceId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getExperienceById(
        experienceId: String,
        callback: (Boolean, String, ExperienceModel?) -> Unit
    )

    fun uploadExperienceLetterImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )

    fun getFileNameFromUri(context: Context, imageUri: Uri): String?
}