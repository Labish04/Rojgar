package com.example.rojgar.repository

import com.example.rojgar.model.AppliedJobModel
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobModel
import com.example.rojgar.model.JobSeekerModel
import com.google.firebase.auth.FirebaseUser

interface JobSeekerRepo {
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

    fun addJobSeekerToDatabase(
        jobSeekerId : String,
        model : JobSeekerModel,
        callback : (Boolean, String) ->Unit
    )

    fun getCurrentJobSeeker() : FirebaseUser?

    fun getJobSeekerById(
        jobSeekerId : String,
        callback : (Boolean, String, JobSeekerModel?) ->Unit
    )

    fun getAllJobSeeker(
        callback : (Boolean, String, List<JobSeekerModel>?) ->Unit
    )

    fun logout(
        jobSeekerId : String,
        callback : (Boolean, String) ->Unit
    )

    fun forgetPassword(
        email : String,
        callback : (Boolean, String) ->Unit
    )

    fun followJobSeeker(
        currentUserId: String,
        targetJobSeekerId: String,
        callback: (Boolean, String) -> Unit
    )

    fun unfollowJobSeeker(
        currentUserId: String,
        targetJobSeekerId: String,
        callback: (Boolean, String) -> Unit
    )

    fun isFollowing(
        currentUserId: String,
        targetJobSeekerId: String,
        callback: (Boolean) -> Unit
    )

    fun updateProfile(
        model: JobSeekerModel,
        callback: (Boolean, String) -> Unit
    )

    // Applied Jobs Methods
    fun applyForJob(
        appliedJobModel: AppliedJobModel,
        callback: (Boolean, String) -> Unit
    )

    fun getAppliedJobsByJobSeeker(
        jobSeekerId: String,
        callback: (Boolean, String, List<AppliedJobModel>?) -> Unit
    )

    fun getAppliedJobsByStatus(
        jobSeekerId: String,
        status: String,
        callback: (Boolean, String, List<AppliedJobModel>?) -> Unit
    )

    fun updateApplicationStatus(
        applicationId: String,
        newStatus: String,
        callback: (Boolean, String) -> Unit
    )

    fun withdrawApplication(
        applicationId: String,
        jobSeekerId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getApplicationById(
        applicationId: String,
        callback: (Boolean, String, AppliedJobModel?) -> Unit
    )
}