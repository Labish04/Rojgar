package com.example.rojgar.repository

import com.example.rojgar.model.JobSeekerModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
                callback(false, "$")
            }
        }
    }

    override fun getCurrentJobSeeker(): FirebaseUser? {
        TODO("Not yet implemented")
    }

    override fun getJobSeekerById(
        jobSeekerId: String,
        callback: (Boolean, String, JobSeekerModel?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getAllJobSeeker(callback: (Boolean, String, List<JobSeekerModel>?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun logout(
        jobSeekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}