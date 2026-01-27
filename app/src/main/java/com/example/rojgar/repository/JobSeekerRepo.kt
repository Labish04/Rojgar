package com.example.rojgar.repository

import android.content.Context
import android.net.Uri
import com.example.rojgar.model.JobSeekerModel
import com.google.firebase.auth.FirebaseUser

interface JobSeekerRepo {
    fun saveUserRole(
        uid: String,
        role: String
    )
    fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    )

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    )

    fun addJobSeekerToDatabase(
        jobSeekerId: String,
        model: JobSeekerModel,
        callback: (Boolean, String) -> Unit
    )

    fun signInWithGoogle(
        idToken: String,
        fullName: String,
        email: String,
        photoUrl: String,
        callback: (Boolean, String, String?) -> Unit // success, message, jobSeekerId
    )

    fun getJobSeekerByEmail(
        email: String,
        callback: (Boolean, String, JobSeekerModel?) -> Unit
    )

    fun getCurrentJobSeeker(): FirebaseUser?

    fun getJobSeekerById(
        jobSeekerId: String,
        callback: (Boolean, String, JobSeekerModel?) -> Unit
    )

    fun getAllJobSeeker(
        callback: (Boolean, String, List<JobSeekerModel>?) -> Unit
    )

    fun logout(
        jobSeekerId: String,
        callback: (Boolean, String) -> Unit
    )

    fun deactivateAccount(
        jobseekerId: String,
        callback: (Boolean, String) -> Unit
    )

    fun reactivateAccount(
        jobseekerId: String,
        callback: (Boolean, String) -> Unit
    )
    fun deleteAccount(
        jobseekerId: String,
        callback: (Boolean, String) -> Unit
    )


    fun checkAccountStatus(
        jobseekerId: String,
        callback: (Boolean, String) -> Unit
    )

    fun checkAccountStatusByEmail(
        email: String,
        callback: (Boolean, String?, String) -> Unit
    )

    fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    )

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
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

    fun uploadProfileImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )

    fun getFileNameFromUri(context: Context, imageUri: Uri): String?

    fun updateJobSeekerProfile(
        model: JobSeekerModel,
        callback: (Boolean, String) -> Unit
    )

    fun getJobSeekerDetails(
        jobSeekerId: String,
        callback: (Boolean, String, JobSeekerModel?) -> Unit
    )

    fun incrementProfileView(
        jobSeekerId: String,
        callback: (Boolean, String) -> Unit
    )
}