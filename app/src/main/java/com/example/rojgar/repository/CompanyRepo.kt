package com.example.rojgar.repository

import com.example.rojgar.model.JobSeekerModel
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
}