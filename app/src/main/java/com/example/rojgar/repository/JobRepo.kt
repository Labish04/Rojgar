package com.example.rojgar.repository

import android.content.Context
import android.net.Uri
import com.example.rojgar.model.JobModel
import com.example.rojgar.model.PreferenceModel

interface JobRepo {

    fun createJobPost(
        jobPost: JobModel,
        callback: (Boolean, String) -> Unit
    )

    fun updateJobPost(
        jobPost: JobModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteJobPost(
        postId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getJobPostsByCompanyId(
        companyId: String,
        callback: (Boolean, String, List<JobModel>?) -> Unit
    )

    fun getJobPostById(
        postId: String,
        callback: (Boolean, String, JobModel?) -> Unit
    )

    fun getAllJobPosts(
        callback: (Boolean, String, List<JobModel>?) -> Unit)

    fun getRecommendedJobs(
        preference: PreferenceModel,
        callback: (Boolean, String, List<JobModel>?) -> Unit
    )

    fun uploadBannerImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )

    fun getFileNameFromUri(context: Context, imageUri: Uri): String?


}