package com.example.rojgar.repository

import com.example.rojgar.model.ExperienceModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ExperienceRepoImpl : ExperienceRepo {

    private val database = FirebaseDatabase.getInstance()
    private val experiencesRef: DatabaseReference = database.getReference("Experiences")

    override fun addExperience(
        experience: ExperienceModel,
        callback: (Boolean, String) -> Unit
    ) {
        val experienceId = experiencesRef.push().key ?: System.currentTimeMillis().toString()
        val experienceWithId = experience.copy(experienceId = experienceId)

        experiencesRef.child(experienceId).setValue(experienceWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Experience added successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to add experience")
                }
            }
    }

    override fun getExperiencesByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<ExperienceModel>?) -> Unit
    ) {
        experiencesRef.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val experienceList = mutableListOf<ExperienceModel>()

                    if (snapshot.exists()) {
                        for (experienceSnapshot in snapshot.children) {
                            val experience = experienceSnapshot.getValue(ExperienceModel::class.java)
                            experience?.let {
                                experienceList.add(it)
                            }
                        }
                        callback(true, "Experiences fetched", experienceList)
                    } else {
                        callback(true, "No experiences found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun updateExperience(
        experienceId: String,
        updatedExperience: ExperienceModel,
        callback: (Boolean, String) -> Unit
    ) {
        experiencesRef.child(experienceId).updateChildren(updatedExperience.toMap())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Experience updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update experience")
                }
            }
    }

    override fun deleteExperience(
        experienceId: String,
        callback: (Boolean, String) -> Unit
    ) {
        experiencesRef.child(experienceId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Experience deleted successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to delete experience")
                }
            }
    }

    override fun getExperienceById(
        experienceId: String,
        callback: (Boolean, String, ExperienceModel?) -> Unit
    ) {
        experiencesRef.child(experienceId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val experience = snapshot.getValue(ExperienceModel::class.java)
                        callback(true, "Experience fetched", experience)
                    } else {
                        callback(false, "Experience not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            }
        )
    }
}