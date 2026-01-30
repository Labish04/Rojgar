package com.example.rojgar.repository

import com.example.rojgar.model.ApplicationModel

interface ApplicationRepo {
    fun applyForJob(
        application: ApplicationModel,
        callback: (Boolean, String) -> Unit
    )

    fun getApplicationsByJobSeeker(
        jobSeekerId: String,
        callback: (Boolean, String, List<ApplicationModel>?) -> Unit
    )

    fun getApplicationsByCompany(
        companyId: String,
        callback: (Boolean, String, List<ApplicationModel>?) -> Unit
    )

    fun updateApplicationStatus(
        applicationId: String,
        status: String,
        rejectionFeedback: String?,
        callback: (Boolean, String) -> Unit
    )
    fun deleteApplication(
        applicationId: String,
        callback: (Boolean, String) -> Unit
    )
    fun checkIfApplied(
        jobSeekerId: String,
        postId: String,
        callback: (Boolean) -> Unit
    )

}