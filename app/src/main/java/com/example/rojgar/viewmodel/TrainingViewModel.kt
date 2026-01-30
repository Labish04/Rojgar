package com.example.rojgar.viewmodel

import android.content.Context
import android.net.Uri
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

    private val _uploading = MutableLiveData(false)
    val uploading: LiveData<Boolean> = _uploading

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

    fun uploadCertificateImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        _uploading.value = true
        trainingRepo.uploadCertificateImage(context, imageUri) { url ->
            _uploading.value = false
            callback(url)
        }
    }
    fun getFileNameFromUri(context: Context, imageUri: Uri): String? {
        return trainingRepo.getFileNameFromUri(context, imageUri)
    }

}