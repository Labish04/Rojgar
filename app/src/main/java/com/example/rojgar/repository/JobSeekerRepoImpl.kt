package com.example.rojgar.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateListOf
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.rojgar.model.JobSeekerModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.concurrent.Executors

class JobSeekerRepoImpl : JobSeekerRepo {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("JobSeekers")

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dtmprduic",
            "api_key" to "883843915169633",
            "api_secret" to "DhiLcks25VLVZCBhWgGvObdGGyE"
        )
    )

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(
                        true, "Registration Successful",
                        "${auth.currentUser?.uid}"
                    )
                } else {
                    callback(false, "${it.exception?.message}", "")
                }
            }
    }

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Login Successful")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun addJobSeekerToDatabase(
        jobSeekerId: String,
        model: JobSeekerModel,
        callback: (Boolean, String) -> Unit
    ) {
        // Ensure isActive is set to true for new accounts
        val updatedModel = model.copy(isActive = true)
        ref.child(jobSeekerId).setValue(updatedModel).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Registration Successful")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getCurrentJobSeeker(): FirebaseUser? {
        return auth.currentUser
    }

    override fun getJobSeekerById(
        jobSeekerId: String,
        callback: (Boolean, String, JobSeekerModel?) -> Unit
    ) {
        ref.child(jobSeekerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val jobSeeker = snapshot.getValue(JobSeekerModel::class.java)
                    if (jobSeeker != null) {
                        // Ensure the jobSeekerId is set from the Firebase key
                        val jobSeekerWithId = jobSeeker.copy(jobSeekerId = snapshot.key ?: jobSeekerId)
                        callback(true, "Profile fetched", jobSeekerWithId)
                    } else {
                        callback(false, "Job seeker data is null", null)
                    }
                } else {
                    callback(false, "Job seeker not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getAllJobSeeker(callback: (Boolean, String, List<JobSeekerModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val allJobSeekers = mutableStateListOf<JobSeekerModel>()
                    for (data in snapshot.children) {
                        try {
                            val jobSeeker = data.getValue(JobSeekerModel::class.java)
                            if (jobSeeker != null) {
                                // Set the jobSeekerId to the key of the Firebase node
                                val firebaseKey = data.key
                                if (firebaseKey != null && firebaseKey.isNotEmpty()) {
                                    val jobSeekerWithId = jobSeeker.copy(jobSeekerId = firebaseKey)
                                    allJobSeekers.add(jobSeekerWithId)
                                }
                            }
                        } catch (e: Exception) {
                            continue
                        }
                    }
                    callback(true, "JobSeeker Fetched", allJobSeekers)
                } else {
                    callback(false, "No job seekers found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun logout(
        jobSeekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            auth.signOut()
            callback(true, "Logout successfully")
        } catch (e: Exception) {
            callback(false, e.message.toString())
        }
    }

    override fun deactivateAccount(
        jobseekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(jobseekerId).child("isActive").setValue(false)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Also sign out the user
                    auth.signOut()
                    callback(true, "Account deactivated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to deactivate account")
                }
            }
    }

    override fun deleteAccount(
        jobseekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        // First, delete the user data from the database
        ref.child(jobseekerId).removeValue()
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    // Then delete the Firebase Auth user
                    val user = auth.currentUser
                    if (user != null) {
                        user.delete()
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    callback(true, "Account deleted permanently")
                                } else {
                                    callback(false, authTask.exception?.message ?: "Failed to delete authentication")
                                }
                            }
                    } else {
                        callback(false, "User not authenticated")
                    }
                } else {
                    callback(false, dbTask.exception?.message ?: "Failed to delete account data")
                }
            }
    }

    override fun reactivateAccount(
        jobseekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(jobseekerId).child("isActive").setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Account reactivated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to reactivate account")
                }
            }
    }


    override fun checkAccountStatus(
        jobseekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(jobseekerId).child("isActive").get()
            .addOnSuccessListener { snapshot ->
                val isActive = snapshot.getValue(Boolean::class.java) ?: true
                if (isActive) {
                    callback(true, "Account is active")
                } else {
                    callback(false, "Account is deactivated")
                }
            }
            .addOnFailureListener { exception ->
                callback(false, "Error checking account status: ${exception.message}")
            }
    }

    // NEW METHOD: Check account status by email
    override fun checkAccountStatusByEmail(
        email: String,
        callback: (Boolean, String?, String) -> Unit
    ) {
        // Query by email to find the job seeker
        ref.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Get the first matching job seeker (email should be unique)
                        val children = snapshot.children
                        if (children.iterator().hasNext()) {
                            val jobSeekerSnapshot = children.iterator().next()
                            val jobSeekerId = jobSeekerSnapshot.key
                            val jobSeeker = jobSeekerSnapshot.getValue(JobSeekerModel::class.java)

                            if (jobSeeker != null && jobSeekerId != null) {
                                val isActive = jobSeeker.isActive
                                if (isActive) {
                                    callback(true, jobSeekerId, "Account is active")
                                } else {
                                    callback(false, jobSeekerId, "Account is deactivated")
                                }
                            } else {
                                callback(false, null, "Job seeker data not found")
                            }
                        } else {
                            callback(false, null, "No job seeker found with this email")
                        }
                    } else {
                        callback(false, null, "Email not found in job seekers")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, null, "Error: ${error.message}")
                }
            })
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Link sent to $email")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun changePassword(
        currentPassword: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null) {
            callback(false, "User not authenticated")
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Current password is correct, now update to new password
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        callback(true, "Password changed successfully!")
                    }
                    .addOnFailureListener { e ->
                        callback(false, "Failed to update password: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                callback(false, "Current password is incorrect")
            }
    }

    override fun followJobSeeker(
        currentUserId: String,
        targetJobSeekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Get the target job seeker's current followers list
        ref.child(targetJobSeekerId).child("followers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentFollowers = mutableListOf<String>()

                    // Get existing followers
                    if (snapshot.exists()) {
                        for (followerSnapshot in snapshot.children) {
                            val followerId = followerSnapshot.getValue(String::class.java)
                            if (followerId != null) {
                                currentFollowers.add(followerId)
                            }
                        }
                    }

                    // Check if already following
                    if (currentFollowers.contains(currentUserId)) {
                        callback(false, "Already following this user")
                        return
                    }

                    // Add current user to followers list
                    currentFollowers.add(currentUserId)

                    // Update in database
                    ref.child(targetJobSeekerId).child("followers").setValue(currentFollowers)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                callback(true, "Following successfully")
                            } else {
                                callback(false, task.exception?.message ?: "Failed to follow")
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            }
        )
    }

    override fun unfollowJobSeeker(
        currentUserId: String,
        targetJobSeekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Get the target job seeker's current followers list
        ref.child(targetJobSeekerId).child("followers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentFollowers = mutableListOf<String>()

                    // Get existing followers
                    if (snapshot.exists()) {
                        for (followerSnapshot in snapshot.children) {
                            val followerId = followerSnapshot.getValue(String::class.java)
                            if (followerId != null) {
                                currentFollowers.add(followerId)
                            }
                        }
                    }

                    // Remove current user from followers list
                    currentFollowers.remove(currentUserId)

                    // Update in database
                    ref.child(targetJobSeekerId).child("followers").setValue(currentFollowers)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                callback(true, "Unfollowed successfully")
                            } else {
                                callback(false, task.exception?.message ?: "Failed to unfollow")
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }

    override fun isFollowing(
        currentUserId: String,
        targetJobSeekerId: String,
        callback: (Boolean) -> Unit
    ) {
        ref.child(targetJobSeekerId).child("followers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var following = false

                    if (snapshot.exists()) {
                        for (followerSnapshot in snapshot.children) {
                            val followerId = followerSnapshot.getValue(String::class.java)
                            if (followerId == currentUserId) {
                                following = true
                                break
                            }
                        }
                    }

                    callback(following)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }

    override fun updateJobSeekerProfile(
        model: JobSeekerModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(model.jobSeekerId)
            .setValue(model)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Profile updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Profile update failed")
                }
            }
    }

    override fun getJobSeekerDetails(
        jobSeekerId: String,
        callback: (Boolean, String, JobSeekerModel?) -> Unit
    ) {
        ref.child(jobSeekerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val jobSeeker = snapshot.getValue(JobSeekerModel::class.java)
                    if (jobSeeker != null) {
                        val jobSeekerWithId = jobSeeker.copy(jobSeekerId = snapshot.key ?: jobSeekerId)
                        callback(true, "Job seeker details fetched", jobSeekerWithId)
                    } else {
                        callback(false, "Job seeker data is null", null)
                    }
                } else {
                    callback(false, "Job seeker not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    // NEW METHOD: Get job seeker by email
    override fun getJobSeekerByEmail(
        email: String,
        callback: (Boolean, String, JobSeekerModel?) -> Unit
    ) {
        ref.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val children = snapshot.children
                        if (children.iterator().hasNext()) {
                            val jobSeekerSnapshot = children.iterator().next()
                            val jobSeeker = jobSeekerSnapshot.getValue(JobSeekerModel::class.java)
                            if (jobSeeker != null) {
                                val jobSeekerId = jobSeekerSnapshot.key ?: ""
                                val jobSeekerWithId = jobSeeker.copy(jobSeekerId = jobSeekerId)
                                callback(true, "Job seeker found", jobSeekerWithId)
                            } else {
                                callback(false, "Job seeker data is null", null)
                            }
                        } else {
                            callback(false, "No job seeker found with this email", null)
                        }
                    } else {
                        callback(false, "Email not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun uploadProfileImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?

                imageUrl = imageUrl?.replace("http://", "https://")

                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

    override fun getFileNameFromUri(
        context: Context,
        imageUri: Uri
    ): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(imageUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}