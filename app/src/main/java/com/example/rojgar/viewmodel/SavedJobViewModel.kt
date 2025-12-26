package com.example.rojgar.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rojgar.model.SavedJobModel
import com.example.rojgar.repository.JobRepo
import com.example.rojgar.repository.SavedJobRepo
import com.google.firebase.auth.FirebaseAuth

class SavedJobViewModel(private val repo: SavedJobRepo): ViewModel(){

    private val _loading = MutableLiveData(false)
    val loading: MutableLiveData<Boolean> get() = _loading

    private val _savedJobs = MutableLiveData<List<SavedJobModel>>(emptyList())
    val savedJobs: MutableLiveData<List<SavedJobModel>> get() = _savedJobs

    private val _savedJobIds = MutableLiveData<Set<String>>(emptySet())
    val savedJobIds: MutableLiveData<Set<String>> get() = _savedJobIds

    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun saveJob(jobId: String, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        val userId = getCurrentUserId()
        if (userId == null) {
            _loading.postValue(false)
            callback(false, "User not logged in")
            return
        }

        val savedJob = SavedJobModel(
            jobSeekerId = userId,
            jobId = jobId
        )

        repo.saveJob(savedJob) { success, message ->
            _loading.postValue(false)
            if (success) {
                // Update local state
                val currentIds = _savedJobIds.value ?: emptySet()
                _savedJobIds.postValue(currentIds + jobId)
            }
            callback(success, message)
        }
    }

    fun unsaveJob(jobId: String, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        val userId = getCurrentUserId()
        if (userId == null) {
            _loading.postValue(false)
            callback(false, "User not logged in")
            return
        }

        repo.checkIfJobSaved(userId, jobId) { success, message, isSaved, savedId ->
            if (success && isSaved && savedId != null) {
                repo.unsaveJob(savedId) { unsaveSuccess, unsaveMessage ->
                    _loading.postValue(false)
                    if (unsaveSuccess) {
                        // Update local state
                        val currentIds = _savedJobIds.value ?: emptySet()
                        _savedJobIds.postValue(currentIds - jobId)
                    }
                    callback(unsaveSuccess, unsaveMessage)
                }
            } else {
                _loading.postValue(false)
                callback(false, "Job not found in saved list")
            }
        }
    }

    fun loadSavedJobs(callback: (Boolean, String) -> Unit = { _, _ -> }) {
        _loading.postValue(true)
        val userId = getCurrentUserId()
        if (userId == null) {
            _loading.postValue(false)
            callback(false, "User not logged in")
            return
        }

        repo.getSavedJobsByJobSeeker(userId) { success, message, savedJobs ->
            _loading.postValue(false)
            if (success) {
                _savedJobs.postValue(savedJobs ?: emptyList())
                // Extract job IDs for quick lookup
                val jobIds = savedJobs?.map { it.jobId }?.toSet() ?: emptySet()
                _savedJobIds.postValue(jobIds)
                callback(true, message)
            } else {
                _savedJobs.postValue(emptyList())
                _savedJobIds.postValue(emptySet())
                callback(false, message)
            }
        }
    }

    fun toggleSaveJob(jobId: String, callback: (Boolean, String, Boolean) -> Unit) {
        val userId = getCurrentUserId()
        if (userId == null) {
            callback(false, "User not logged in", false)
            return
        }

        repo.checkIfJobSaved(userId, jobId) { success, message, isSaved, savedId ->
            if (success) {
                if (isSaved) {
                    // Unsave the job
                    unsaveJob(jobId) { unsaveSuccess, unsaveMessage ->
                        callback(unsaveSuccess, unsaveMessage, false)
                    }
                } else {
                    // Save the job
                    saveJob(jobId) { saveSuccess, saveMessage ->
                        callback(saveSuccess, saveMessage, true)
                    }
                }
            } else {
                callback(false, message, false)
            }
        }
    }

    fun isJobSaved(jobId: String): Boolean {
        return _savedJobIds.value?.contains(jobId) ?: false
    }
}

class SavedJobViewModelFactory(private val repo: SavedJobRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavedJobViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SavedJobViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class JobViewModelFactory(private val repo: JobRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JobViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}