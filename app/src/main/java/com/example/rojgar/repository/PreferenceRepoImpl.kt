package com.example.rojgar.repository

import com.example.rojgar.model.PreferenceModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class PreferenceRepoImpl : PreferenceRepo {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Preferences")

    override fun savePreference(
        preference: PreferenceModel,
        callback: (Boolean, String) -> Unit
    ) {
        val preferenceId = ref.push().key ?: UUID.randomUUID().toString()
        val preferenceWithId = preference.copy(preferenceId = preferenceId)

        ref.child(preferenceId).setValue(preferenceWithId.toMap())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Preference saved successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to save preference")
                }
            }
    }

    override fun updatePreference(
        preference: PreferenceModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(preference.preferenceId).setValue(preference.toMap())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Preference updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update preference")
                }
            }
    }

    override fun getPreferenceByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, PreferenceModel?) -> Unit
    ) {
        ref.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val preference = child.getValue(PreferenceModel::class.java)
                            if (preference != null) {
                                callback(true, "Preference found", preference.copy(preferenceId = child.key ?: preference.preferenceId))
                                return
                            }
                        }
                        callback(false, "No preference found", null)
                    } else {
                        callback(false, "No preference found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun deletePreference(
        preferenceId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(preferenceId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Preference deleted successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to delete preference")
                }
            }
    }
}