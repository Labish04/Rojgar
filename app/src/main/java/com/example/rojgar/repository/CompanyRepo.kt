package com.example.rojgar.repository

import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobModel
import com.google.firebase.auth.FirebaseUser

interface CompanyRepo {
    fun register(
        email : String,
        password : String,
        callback : (Boolean, String, String) ->Unit
    )

    fun login(
        email : String,
        password : String,
        callback : (Boolean, String) ->Unit
    )

    fun addCompanyToDatabase(
        companyId : String,
        model : CompanyModel,
        callback : (Boolean, String) ->Unit
    )

    fun getCurrentCompany() : FirebaseUser?

    fun getCompanyById(
        companyId : String,
        callback : (Boolean, String, CompanyModel?) ->Unit
    )

    fun getAllCompany(
        callback : (Boolean, String, List<CompanyModel>?) ->Unit
    )

    fun logout(
        companyId : String,
        callback : (Boolean, String) ->Unit
    )

    fun forgetPassword(
        email : String,
        callback : (Boolean, String) ->Unit
    )

    // Job Post Methods
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



//    fun uploadImage(
//        imageUri: android.net.Uri,
//        callback: (Boolean, String, String) -> Unit
//    )

}