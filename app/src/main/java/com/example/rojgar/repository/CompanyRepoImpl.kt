package com.example.rojgar.repository

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.util.UUID

class CompanyRepoImpl : CompanyRepo {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("Company")
    val jobPostRef: DatabaseReference = database.getReference("JobPosts")


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

    override fun addCompanyToDatabase(
        companyId: String,
        model: CompanyModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(companyId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Registration Successful")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getCurrentCompany(): FirebaseUser? {
        return auth.currentUser
    }

    override fun getCompanyById(
        companyId: String,
        callback: (Boolean, String, CompanyModel?) -> Unit
    ) {
        ref.child(companyId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val company = snapshot.getValue(CompanyModel::class.java)
                    if (company != null) {
                        callback(true, "Profile fetched", company)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getAllCompany(callback: (Boolean, String, List<CompanyModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var allCompanys = mutableStateListOf<CompanyModel>()
                    for (data in snapshot.children) {
                        var company = data.getValue(CompanyModel::class.java)
                        if (company != null) {
                            allCompanys.add(company)
                        }
                    }
                    callback(true, "Company Fetched", allCompanys)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun logout(
        companyId: String,
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

    // Job Post Implementation
    override fun createJobPost(
        jobPost: JobModel,
        callback: (Boolean, String) -> Unit
    ) {
        val postId = jobPostRef.push().key ?: UUID.randomUUID().toString()
        val postWithId = jobPost.copy(postId = postId)

        jobPostRef.child(postId).setValue(postWithId).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Job post created successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun updateJobPost(
        jobPost: JobModel,
        callback: (Boolean, String) -> Unit
    ) {
        jobPostRef.child(jobPost.postId).setValue(jobPost).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Job post updated successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deleteJobPost(
        postId: String,
        callback: (Boolean, String) -> Unit
    ) {
        jobPostRef.child(postId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Job post deleted successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getJobPostsByCompanyId(
        companyId: String,
        callback: (Boolean, String, List<JobModel>?) -> Unit
    ) {
        jobPostRef.orderByChild("companyId").equalTo(companyId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val jobPosts = mutableListOf<JobModel>()
                        for (data in snapshot.children) {
                            val post = data.getValue(JobModel::class.java)
                            if (post != null) {
                                jobPosts.add(post)
                            }
                        }
                        // Sort by timestamp (newest first)
                        jobPosts.sortByDescending { it.timestamp }
                        callback(true, "Job posts fetched", jobPosts)
                    } else {
                        callback(true, "No job posts found", emptyList())
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getJobPostById(
        postId: String,
        callback: (Boolean, String, JobModel?) -> Unit
    ) {
        jobPostRef.child(postId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val jobPost = snapshot.getValue(JobModel::class.java)
                    callback(true, "Job post fetched", jobPost)
                } else {
                    callback(false, "Job post not found", null)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }


}



//    override fun uploadImage(
//        imageUri: Uri,
//        callback: (Boolean, String, String) -> Unit
//    ) {
//        val fileName = "job_images/${UUID.randomUUID()}.jpg"
//        val imageRef = storageRef.child(fileName)
//
//        imageRef.putFile(imageUri)
//            .addOnSuccessListener {
//                imageRef.downloadUrl.addOnSuccessListener { uri ->
//                    callback(true, "Image uploaded successfully", uri.toString())
//                }.addOnFailureListener { e ->
//                    callback(false, "Failed to get download URL: ${e.message}", "")
//                }
//            }
//            .addOnFailureListener { e ->
//                callback(false, "Failed to upload image: ${e.message}", "")
//            }
//    }
