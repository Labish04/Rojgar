package com.example.rojgar.repository

import com.example.rojgar.model.CompanyModel
import com.example.rojgar.model.JobSeekerModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepo {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val companiesRef = db.getReference("Companys")
    private val jobSeekersRef = db.getReference("JobSeekers")

    // Get current Firebase user
    fun getCurrentFirebaseUser(): FirebaseUser? = auth.currentUser

    // Get current user ID
    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    // Get current user email
    fun getCurrentUserEmail(): String = auth.currentUser?.email ?: ""

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // Get user type by checking both Company and JobSeeker nodes
    fun getUserType(callback: (String?) -> Unit) {
        val uid = getCurrentUserId()
        if (uid.isEmpty()) {
            callback(null)
            return
        }

        // First check if user is a Company
        companiesRef.child(uid).get().addOnSuccessListener { companySnapshot ->
            if (companySnapshot.exists()) {
                callback("Company")
            } else {
                // If not a Company, check if user is a JobSeeker
                jobSeekersRef.child(uid).get().addOnSuccessListener { jobSeekerSnapshot ->
                    if (jobSeekerSnapshot.exists()) {
                        callback("JobSeeker")
                    } else {
                        callback(null)
                    }
                }.addOnFailureListener {
                    callback(null)
                }
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    // Get user role (legacy method - might not be used)
    fun getCurrentUserRole(callback: (String?) -> Unit) {
        // First try the new method
        getUserType { type ->
            if (type != null) {
                callback(type)
                return@getUserType
            }

            // Fallback to old method
            val uid = getCurrentUserId()
            if (uid.isEmpty()) {
                callback(null)
                return@getUserType
            }

            db.getReference("users")
                .child(uid)
                .child("role")
                .get()
                .addOnSuccessListener {
                    callback(it.getValue(String::class.java))
                }
                .addOnFailureListener {
                    callback(null)
                }
        }
    }

    // Get current user with complete data
    fun getCurrentUserData(
        companyRepo: CompanyRepo,
        jobSeekerRepo: JobSeekerRepo,
        callback: (userType: String?, userData: Any?) -> Unit
    ) {
        if (!isUserLoggedIn()) {
            callback(null, null)
            return
        }

        getUserType { userType ->
            when (userType) {
                "Company" -> {
                    val userId = getCurrentUserId()
                    companyRepo.getCompanyById(userId) { success, message, company ->
                        if (success && company != null) {
                            callback("Company", company)
                        } else {
                            callback("Company", null)
                        }
                    }
                }
                "JobSeeker" -> {
                    val userId = getCurrentUserId()
                    jobSeekerRepo.getJobSeekerById(userId) { success, message, jobSeeker ->
                        if (success && jobSeeker != null) {
                            callback("JobSeeker", jobSeeker)
                        } else {
                            callback("JobSeeker", null)
                        }
                    }
                }
                else -> {
                    callback(null, null)
                }
            }
        }
    }

    // Add to UserRepo.kt
    fun getCurrentUserName(callback: (String) -> Unit) {
        val uid = getCurrentUserId()
        if (uid.isEmpty()) {
            callback("Anonymous")
            return
        }

        getUserType { userType ->
            when (userType) {
                "Company" -> {
                    // Fetch company name
                    FirebaseDatabase.getInstance()
                        .getReference("Companys")
                        .child(uid)
                        .child("companyName")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val name = snapshot.getValue(String::class.java) ?: "Company User"
                            callback(name)
                        }
                        .addOnFailureListener {
                            callback("Company User")
                        }
                }
                "JobSeeker" -> {
                    // Fetch job seeker name
                    FirebaseDatabase.getInstance()
                        .getReference("JobSeekers")
                        .child(uid)
                        .child("fullName")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val name = snapshot.getValue(String::class.java) ?: "Job Seeker"
                            callback(name)
                        }
                        .addOnFailureListener {
                            callback("Job Seeker")
                        }
                }
                else -> callback("User")
            }
        }
    }

    // Logout all users
    fun logout() {
        auth.signOut()
    }
}