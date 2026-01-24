package com.example.rojgar.repository

import android.content.Context
import android.net.Uri
import com.example.rojgar.model.TrainingModel

interface TrainingRepo {
    fun addTraining(
        training: TrainingModel,
        callback: (Boolean, String) -> Unit
    )

    fun getTrainingsByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<TrainingModel>?) -> Unit
    )

    fun updateTraining(
        trainingId: String,
        updatedTraining: TrainingModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteTraining(
        trainingId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getTrainingById(
        trainingId: String,
        callback: (Boolean, String, TrainingModel?) -> Unit
    )

    fun uploadCertificateImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )

    fun getFileNameFromUri(context: Context, imageUri: Uri): String?
}