package com.example.rojgar.viewmodel

import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.repository.JobSeekerRepo
import com.google.firebase.auth.FirebaseUser

class JobSeekerViewModel (val repo: JobSeekerRepo){
    fun register(
        email : String,
        password : String,
        callback : (Boolean, String, String) ->Unit
    )
    {
        repo.register(email,password,callback)
    }

    fun login(
        email : String,
        password : String,
        callback : (Boolean, String) ->Unit
    ){
        repo.login(email,password,callback)
    }

    fun addJobSeekerToDatabase(
        jobSeekerId : String,
        model : JobSeekerModel,
        callback : (Boolean, String) ->Unit
    ){
        repo.addJobSeekerToDatabase(jobSeekerId,model,callback)
    }

    fun getCurrentJobSeeker() : FirebaseUser?{
        return repo.getCurrentJobSeeker()
    }

    fun getJobSeekerById(
        jobSeekerId : String,
        callback : (Boolean, String, JobSeekerModel?) ->Unit
    ){
        repo.getJobSeekerById(jobSeekerId,callback)
    }

    fun getAllJobSeeker(
        callback : (Boolean, String, List<JobSeekerModel>?) ->Unit
    ){
        repo.getAllJobSeeker(callback)
    }

    fun logout(
        jobSeekerId : String,
        callback : (Boolean, String) ->Unit
    ){
        repo.logout(jobSeekerId,callback)
    }

    fun forgetPassword(
        email : String,
        callback : (Boolean, String) ->Unit
    ){
        repo.forgetPassword(email,callback)
    }
    fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit){
        repo.deleteAccount(userId,callback)
    }
    fun  deactivateAccount(userId: String,callback: (Boolean, String) -> Unit){
        repo.deactivateAccount(userId,callback)
    }
}