package com.example.rojgar.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rojgar.model.JobModel
import com.example.rojgar.model.PreferenceModel
import com.example.rojgar.repository.JobRepo
import com.example.rojgar.utils.NotificationHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JobViewModel(private val repo: JobRepo) : ViewModel() {

    private val TAG = "JobViewModel"

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

    fun createJobPost(
        context: Context,
        jobPost: JobModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)

        repo.createJobPost(jobPost) { success, message ->
            _loading.postValue(false)

            if (success) {
                Log.d(TAG, "Job post created successfully: ${jobPost.title}")

                // Send notifications to ALL job seekers
                sendJobPostNotificationToAll(context, jobPost)

                callback(true, "Job posted successfully! Notifying all job seekers...")
            } else {
                Log.e(TAG, "Failed to create job post: $message")
                callback(false, message)
            }
        }
    }

    /**
     * Send job post notifications to ALL active job seekers
     */
    private fun sendJobPostNotificationToAll(context: Context, jobPost: JobModel) {
        // Get company details first
        FirebaseDatabase.getInstance()
            .getReference("Companys")
            .child(jobPost.companyId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val companyName = snapshot.child("companyName").getValue(String::class.java)
                        ?: "A Company"

                    Log.d(TAG, "📢 Sending broadcast notification for: ${jobPost.title} by $companyName")

                    // Send to ALL active job seekers
                    NotificationHelper.sendJobPostNotificationToAll(
                        context,
                        jobPost.postId,
                        jobPost.title,
                        companyName,
                        jobPost.position
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error fetching company details: ${error.message}")
                }
            })
    }

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