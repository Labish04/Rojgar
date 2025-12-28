package com.example.rojgar.repository

import com.example.rojgar.model.ReferenceModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ReferenceRepoImpl : ReferenceRepo {

    private val database = FirebaseDatabase.getInstance()
    private val referencesRef: DatabaseReference = database.getReference("References")

    override fun addReference(
        reference: ReferenceModel,
        callback: (Boolean, String) -> Unit
    ) {
        val referenceId = referencesRef.push().key ?: System.currentTimeMillis().toString()
        val referenceWithId = reference.copy(referenceId = referenceId)

        referencesRef.child(referenceId).setValue(referenceWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Reference added successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to add reference")
                }
            }
    }

    override fun getReferencesByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<ReferenceModel>?) -> Unit
    ) {
        referencesRef.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val referenceList = mutableListOf<ReferenceModel>()

                    if (snapshot.exists()) {
                        for (referenceSnapshot in snapshot.children) {
                            val reference = referenceSnapshot.getValue(ReferenceModel::class.java)
                            reference?.let {
                                referenceList.add(it)
                            }
                        }
                        callback(true, "References fetched", referenceList)
                    } else {
                        callback(true, "No references found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun updateReference(
        referenceId: String,
        updatedReference: ReferenceModel,
        callback: (Boolean, String) -> Unit
    ) {
        referencesRef.child(referenceId).updateChildren(updatedReference.toMap())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Reference updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update reference")
                }
            }
    }

    override fun deleteReference(
        referenceId: String,
        callback: (Boolean, String) -> Unit
    ) {
        referencesRef.child(referenceId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Reference deleted successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to delete reference")
                }
            }
    }

    override fun getReferenceById(
        referenceId: String,
        callback: (Boolean, String, ReferenceModel?) -> Unit
    ) {
        referencesRef.child(referenceId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val reference = snapshot.getValue(ReferenceModel::class.java)
                        callback(true, "Reference fetched", reference)
                    } else {
                        callback(false, "Reference not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            }
        )
    }
}