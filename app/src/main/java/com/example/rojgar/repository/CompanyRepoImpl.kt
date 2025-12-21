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
    val ref: DatabaseReference = database.getReference("Companys")


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


    override fun getCompanyDetails(
        companyId: String,
        callback: (Boolean, String, CompanyModel?) -> Unit
    ) {
        if (companyId.isEmpty()) {
            callback(false, "Invalid companyId", null)
            return
        }

        ref.child(companyId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val company = snapshot.getValue(CompanyModel::class.java)
                        callback(true, "Company fetched", company)
                    } else {
                        callback(false, "Company not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun uploadRegistrationDocument(
        companyId: String,
        imageUri: Uri,
        callback: (Boolean, String) -> Unit
    ) {
        // For now, just save the URI as a string
        // In production, you would upload to Firebase Storage
        ref.child(companyId).child("companyRegistrationDocument")
            .setValue(imageUri.toString())
            .addOnSuccessListener {
                callback(true, "Document uploaded successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to update database: ${e.message}")
            }
    }

}