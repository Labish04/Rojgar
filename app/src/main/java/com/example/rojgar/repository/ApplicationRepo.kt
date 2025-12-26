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
        callback: (Boolean, String) -> Unit
    )
}