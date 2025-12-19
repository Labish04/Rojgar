package com.example.rojgar.viewmodel

import com.example.rojgar.model.JobModel
import com.example.rojgar.repository.JobRepo

class JobViewModel(val repo: JobRepo) {

    // Job Post Methods
    fun createJobPost(
        jobPost: JobModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.createJobPost(jobPost, callback)
    }

    fun updateJobPost(
        jobPost: JobModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateJobPost(jobPost, callback)
    }

    fun deleteJobPost(
        postId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteJobPost(postId, callback)
    }

    fun getJobPostsByCompanyId(
        companyId: String,
        callback: (Boolean, String, List<JobModel>?) -> Unit
    ) {
        repo.getJobPostsByCompanyId(companyId, callback)
    }

    fun getJobPostById(
        postId: String,
        callback: (Boolean, String, JobModel?) -> Unit
    ) {
        repo.getJobPostById(postId, callback)
    }
    fun getAllJobPosts(
        callback: (Boolean, String, List<JobModel>?) -> Unit
    ){
        repo.getAllJobPosts(callback)
    }
}