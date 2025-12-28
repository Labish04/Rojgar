package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rojgar.model.EducationModel
import com.example.rojgar.model.TrainingModel
import com.example.rojgar.repository.TrainingRepo

class TrainingViewModel(private val trainingRepo: TrainingRepo) : ViewModel() {
    private val _training = MutableLiveData< TrainingModel?>()
    val training: LiveData<TrainingModel?> = _training

    private val _allTrainings = MutableLiveData<List<TrainingModel>?>()
    val allTrainings : MutableLiveData<List<TrainingModel>?> get() = _allTrainings

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

    fun fetchTrainingsByJobSeekerId(jobSeekerId: String) {
        trainingRepo.getTrainingsByJobSeekerId(jobSeekerId) { success, message, experiences ->
            if (success) {
                _allTrainings.postValue(experiences ?: emptyList())
            } else {
                _allTrainings.postValue(emptyList())
            }
        }
    }
}