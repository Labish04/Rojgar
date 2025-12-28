
package com.example.rojgar.repository

import com.example.rojgar.model.LanguageModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class LanguageRepoImpl : LanguageRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Languages")

    override fun addLanguage(
        languageModel: LanguageModel,
        callback: (Boolean, String) -> Unit
    ) {
        val languageId = UUID.randomUUID().toString()
        val languageWithId = languageModel.copy(languageId = languageId)

        ref.child(languageId)
            .setValue(languageWithId.toMap())
            .addOnSuccessListener {
                callback(true, "Language added successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to add language: ${e.message}")
            }
    }

    override fun updateLanguage(
        languageId: String,
        languageModel: LanguageModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(languageId)
            .setValue(languageModel.toMap())
            .addOnSuccessListener {
                callback(true, "Language updated successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to update language: ${e.message}")
            }
    }

    override fun deleteLanguage(
        languageId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(languageId)
            .removeValue()
            .addOnSuccessListener {
                callback(true, "Language deleted successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to delete language: ${e.message}")
            }
    }

    override fun getLanguagesByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<LanguageModel>?) -> Unit
    ) {
        ref.orderByChild("jobSeekerId")
            .equalTo(jobSeekerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val languages = mutableListOf<LanguageModel>()

                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val language = data.getValue(LanguageModel::class.java)
                            language?.let {
                                languages.add(it)
                            }
                        }
                    }

                    callback(true, "Languages fetched successfully", languages)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getLanguageById(
        languageId: String,
        callback: (Boolean, String, LanguageModel?) -> Unit
    ) {
        ref.child(languageId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val language = snapshot.getValue(LanguageModel::class.java)
                        callback(true, "Language fetched", language)
                    } else {
                        callback(false, "Language not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }
}
