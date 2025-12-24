package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobModel
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


    fun updateProfile(
        model: JobSeekerModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateProfile(model) { success, msg ->
            _loading.value = false
            callback(success, msg)
        }
    }

}