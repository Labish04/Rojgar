package com.example.rojgar.repository

import com.example.rojgar.model.ObjectiveModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ObjectiveRepoImpl : ObjectiveRepo {

    private val database = FirebaseDatabase.getInstance()
    private val objectivesRef: DatabaseReference = database.getReference("Objectives")

    override fun saveOrUpdateObjective(
        jobSeekerId: String,
        objective: String,
        callback: (Boolean, String) -> Unit
    ) {
        objectivesRef.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (objectiveSnapshot in snapshot.children) {
                            val existingObjective = objectiveSnapshot.getValue(ObjectiveModel::class.java)
                            existingObjective?.let {
                                updateExistingObjective(it.objectiveId, objective, callback)
                                return
                            }
                        }
                    } else {
                        createNewObjective(jobSeekerId, objective, callback)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }

    override fun getObjectiveByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, ObjectiveModel?) -> Unit
    ) {
        objectivesRef.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (objectiveSnapshot in snapshot.children) {
                            val objective = objectiveSnapshot.getValue(ObjectiveModel::class.java)
                            if (objective != null) {
                                callback(true, "Objective found", objective)
                                return
                            }
                        }
                    }
                    callback(true, "No objective found", null)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getObjectiveTextByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, String?) -> Unit
    ) {
        objectivesRef.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (objectiveSnapshot in snapshot.children) {
                            val objective = objectiveSnapshot.getValue(ObjectiveModel::class.java)
                            if (objective != null) {
                                callback(true, "Objective found", objective.objectiveText)
                                return
                            }
                        }
                    }
                    callback(true, "No objective found", null)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun deleteObjective(
        jobSeekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        objectivesRef.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (objectiveSnapshot in snapshot.children) {
                            objectiveSnapshot.ref.removeValue()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        callback(true, "Objective deleted successfully")
                                    } else {
                                        callback(false, task.exception?.message ?: "Failed to delete objective")
                                    }
                                }
                            return
                        }
                    }
                    callback(true, "No objective to delete")
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }

    private fun createNewObjective(
        jobSeekerId: String,
        objective: String,
        callback: (Boolean, String) -> Unit
    ) {
        val objectiveId = objectivesRef.push().key ?: System.currentTimeMillis().toString()
        val objectiveModel = ObjectiveModel(
            objectiveId = objectiveId,
            jobSeekerId = jobSeekerId,
            objectiveText = objective,

        )

        objectivesRef.child(objectiveId).setValue(objectiveModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Objective saved successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to save objective")
                }
            }
    }

    private fun updateExistingObjective(
        objectiveId: String,
        objective: String,
        callback: (Boolean, String) -> Unit
    ) {
        val updates = hashMapOf<String, Any>(
            "objectiveText" to objective,
        )

        objectivesRef.child(objectiveId).updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Objective updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update objective")
                }
            }
    }
}