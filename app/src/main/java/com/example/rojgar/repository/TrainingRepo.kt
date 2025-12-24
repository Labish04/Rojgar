package com.example.rojgar.repository

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
}