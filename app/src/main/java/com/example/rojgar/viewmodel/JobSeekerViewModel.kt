package com.example.rojgar.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.repository.JobSeekerRepo
import com.google.firebase.auth.FirebaseUser

class JobSeekerViewModel (val repo: JobSeekerRepo) {

    private val _jobSeeker = MutableLiveData<JobSeekerModel?>()
    val jobSeeker: LiveData<JobSeekerModel?> = _jobSeeker

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        repo.register(email, password, callback)
    }

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.login(email, password, callback)
    }

    fun addJobSeekerToDatabase(
        jobSeekerId: String,
        model: JobSeekerModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addJobSeekerToDatabase(jobSeekerId, model, callback)
    }

    fun getCurrentJobSeeker(): FirebaseUser? {
        return repo.getCurrentJobSeeker()
    }

    fun getJobSeekerById(
        jobSeekerId: String,
        callback: (Boolean, String, JobSeekerModel?) -> Unit
    ) {
        repo.getJobSeekerById(jobSeekerId, callback)
    }

    fun getAllJobSeeker(
        callback: (Boolean, String, List<JobSeekerModel>?) -> Unit
    ) {
        repo.getAllJobSeeker(callback)
    }

    fun logout(
        jobSeekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.logout(jobSeekerId, callback)
    }

    fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.forgetPassword(email, callback)
    }
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    ){
        repo.changePassword(currentPassword, newPassword, callback)

    }

    fun updateProfile(
        model: JobSeekerModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addJobSeekerToDatabase(model.jobSeekerId, model, callback)
    }

    fun fetchCurrentJobSeeker() {
        _loading.value = true

        val currentUser = repo.getCurrentJobSeeker()
        if (currentUser != null) {
            repo.getJobSeekerById(currentUser.uid) { success, message, jobSeekerModel ->
                _loading.value = false
                if (success && jobSeekerModel != null) {
                    _jobSeeker.value = jobSeekerModel
                } else {
                    _jobSeeker.value = null
                }
            }
        } else {
            _loading.value = false
            _jobSeeker.value = null
        }
    }

    fun fetchJobSeekerById(jobSeekerId: String) {
        _loading.value = true
        repo.getJobSeekerById(jobSeekerId) { success, message, jobSeekerModel ->
            _loading.value = false
            if (success && jobSeekerModel != null) {
                _jobSeeker.value = jobSeekerModel
            } else {
                _jobSeeker.value = null
            }
        }
    }

    fun updateJobSeekerProfile(
        model: JobSeekerModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        repo.updateJobSeekerProfile(model) { success, message ->
            _loading.value = false
            callback(success, message)
        }
    }

    fun uploadProfileImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ){
        repo.uploadProfileImage(context, imageUri, callback)
    }



}