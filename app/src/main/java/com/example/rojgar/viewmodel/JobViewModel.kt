package com.example.rojgar.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rojgar.model.JobModel
import com.example.rojgar.model.PreferenceModel
import com.example.rojgar.repository.JobRepo

class JobViewModel(private val repo: JobRepo) : ViewModel() {

    // Loading State
    private val _loading = MutableLiveData(false)
    val loading: MutableLiveData<Boolean> get() = _loading

    // Single JobPost
    private val _jobPost = MutableLiveData<JobModel?>()
    val job: MutableLiveData<JobModel?> get() = _jobPost

    // All JobPosts
    private val _allJobPost = MutableLiveData<List<JobModel>>(emptyList())
    val allJobs: MutableLiveData<List<JobModel>> get() = _allJobPost

    // Company Jobs
    private val _companyJobs = MutableLiveData<List<JobModel>>(emptyList())
    val company: MutableLiveData<List<JobModel>> get() = _companyJobs

    fun createJobPost(jobPost: JobModel, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.createJobPost(jobPost) { success, message ->
            _loading.postValue(false)
            callback(success, message)
        }
    }

    fun updateJobPost(jobPost: JobModel, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.updateJobPost(jobPost) { success, message ->
            _loading.postValue(false)
            callback(success, message)
        }
    }

    fun deleteJobPost(postId: String, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.deleteJobPost(postId) { success, message ->
            _loading.postValue(false)
            callback(success, message)
        }
    }

    fun getAllJobPosts(
        callback: (Boolean, String, List<JobModel>?) -> Unit
    ) {
        _loading.postValue(true)
        repo.getAllJobPosts { success, message, data ->
            _loading.postValue(false)
            _allJobPost.postValue(data ?: emptyList())
            callback(success, message, data)
        }
    }

    fun getJobPostById(postId: String) {
        _loading.postValue(true)
        repo.getJobPostById(postId) { success, _, data ->
            _loading.postValue(false)
            _jobPost.postValue(if (success) data else null)
        }
    }

    fun getJobPostsByCompanyId(companyId: String) {
        _loading.postValue(true)
        repo.getJobPostsByCompanyId(companyId) { success, _, data ->
            _loading.postValue(false)
            // FIX: Post the actual data, not empty list
            _companyJobs.postValue(data ?: emptyList())
        }
    }

    private val _recommendedJobs = MutableLiveData<List<JobModel>>()
    val recommendedJobs: LiveData<List<JobModel>> = _recommendedJobs

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun loadRecommendations(preference: PreferenceModel) {
        repo.getRecommendedJobs(preference) { success, msg, jobs ->
            if (success && jobs != null) {
                _recommendedJobs.value = jobs
            }
            _message.value = msg
        }
    }

    fun uploadBannerImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ){
        repo.uploadHiringBanner(context, imageUri, callback)
    }
}