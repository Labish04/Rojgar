package com.example.rojgar.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rojgar.model.TrainingModel
import com.example.rojgar.repository.TrainingRepo

class TrainingViewModel(private val trainingRepo: TrainingRepo) : ViewModel() {

    fun addTraining(training: TrainingModel, callback: (Boolean, String) -> Unit) {
        trainingRepo.addTraining(training, callback)
    }

    fun getTrainingsByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<TrainingModel>?) -> Unit
    ) {
        trainingRepo.getTrainingsByJobSeekerId(jobSeekerId, callback)
    }

    fun updateTraining(
        trainingId: String,
        updatedTraining: TrainingModel,
        callback: (Boolean, String) -> Unit
    ) {
        trainingRepo.updateTraining(trainingId, updatedTraining, callback)
    }

    fun deleteTraining(trainingId: String, callback: (Boolean, String) -> Unit) {
        trainingRepo.deleteTraining(trainingId, callback)
    }

    fun getTrainingById(
        trainingId: String,
        callback: (Boolean, String, TrainingModel?) -> Unit
    ) {
        trainingRepo.getTrainingById(trainingId, callback)
    }
}