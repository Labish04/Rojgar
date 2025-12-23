package com.example.rojgar.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobModel
import com.example.rojgar.model.JobSeekerModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JobSeekerRepoImpl : JobSeekerRepo {

    val auth : FirebaseAuth = FirebaseAuth.getInstance()

    val database : FirebaseDatabase = FirebaseDatabase.getInstance()

    val ref : DatabaseReference = database.getReference("JobSeekers")

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(
                        true, "Registration Successful",
                        "${auth.currentUser?.uid}"
                    )
                }
                else {
                    callback(false, "${it.exception?.message}","")
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
                if (it.isSuccessful){
                    callback(true, "Login Successful")
                }
                else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun addJobSeekerToDatabase(
        jobSeekerId: String,
        model: JobSeekerModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(jobSeekerId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true,"Registration Successful")
            }
            else{
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
        ref.child(jobSeekerId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val jobSeeker = snapshot.getValue(JobSeekerModel::class.java)
                    if (jobSeeker != null) {
                        callback(true, "Profile fetched", jobSeeker)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }

        })
    }

    override fun getAllJobSeeker(callback: (Boolean, String, List<JobSeekerModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var allJobSeekers = mutableStateListOf<JobSeekerModel>()
                    for (data in snapshot.children){
                        var jobSeeker = data.getValue(JobSeekerModel::class.java)
                        if (jobSeeker != null){
                            allJobSeekers.add(jobSeeker)
                        }
                    }
                    callback(true, "JobSeeker Fetched", allJobSeekers)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false,error.message,emptyList())
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

    override fun updateProfile(
        model: JobSeekerModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(model.jobSeekerId).updateChildren(model.toMap()).addOnCompleteListener {
            if(it.isSuccessful){
                callback(true,"Profile updated successfully")
            }else{
                callback(false,"${it.exception?.message}")
            }
        }
    }

    // ============================================
    // NEW FOLLOW FUNCTIONALITY METHODS
    // ============================================

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
            })
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
}
