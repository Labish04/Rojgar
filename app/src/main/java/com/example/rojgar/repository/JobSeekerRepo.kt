package com.example.rojgar.repository
import android.net.Uri
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

    fun uploadVideo(
        jobSeekerId: String,
        videoUri: Uri,
        callback: (Boolean, String, String?) -> Unit
    )

    fun deleteVideo(
        jobSeekerId: String,
        videoUrl: String,
        callback: (Boolean, String) -> Unit
    )

    fun updateVideo(
        jobSeekerId: String,
        videoUri: Uri,
        oldVideoUrl: String,
        callback: (Boolean, String, String?) -> Unit
    )
}