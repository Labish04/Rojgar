package com.example.rojgar.repository

import com.example.rojgar.model.EducationModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class EducationRepoImpl : EducationRepo {

    private val database = FirebaseDatabase.getInstance()
    private val educationsRef: DatabaseReference = database.getReference("Educations")

    override fun addEducation(
        education: EducationModel,
        callback: (Boolean, String) -> Unit
    ) {
        val educationId = educationsRef.push().key ?: System.currentTimeMillis().toString()
        val educationWithId = education.copy(educationId = educationId)

        educationsRef.child(educationId).setValue(educationWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Education added successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to add education")
                }
            }
    }

    override fun getEducationsByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<EducationModel>?) -> Unit
    ) {
        educationsRef.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val educationList = mutableListOf<EducationModel>()

                    if (snapshot.exists()) {
                        for (educationSnapshot in snapshot.children) {
                            val education = educationSnapshot.getValue(EducationModel::class.java)
                            education?.let {
                                educationList.add(it)
                            }
                        }
                        callback(true, "Educations fetched", educationList)
                    } else {
                        callback(true, "No educations found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun updateEducation(
        educationId: String,
        updatedEducation: EducationModel,
        callback: (Boolean, String) -> Unit
    ) {
        educationsRef.child(educationId).updateChildren(updatedEducation.toMap())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Education updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update education")
                }
            }
    }

    override fun deleteEducation(
        educationId: String,
        callback: (Boolean, String) -> Unit
    ) {
        educationsRef.child(educationId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Education deleted successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to delete education")
                }
            }
    }

    override fun getEducationById(
        educationId: String,
        callback: (Boolean, String, EducationModel?) -> Unit
    ) {
        educationsRef.child(educationId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val education = snapshot.getValue(EducationModel::class.java)
                        callback(true, "Education fetched", education)
                    } else {
                        callback(false, "Education not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            }
        )
    }
}