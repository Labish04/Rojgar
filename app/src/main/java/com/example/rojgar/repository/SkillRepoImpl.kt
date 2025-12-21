package com.example.rojgar.repository

import com.example.rojgar.model.SkillModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class SkillRepoImpl : SkillRepo {

    private val database = FirebaseDatabase.getInstance()
    private val skillsRef: DatabaseReference = database.getReference("Skills")

    override fun addSkill(
        skill: SkillModel,
        callback: (Boolean, String) -> Unit
    ) {
        val skillId = skillsRef.push().key ?: System.currentTimeMillis().toString()
        val skillWithId = skill.copy(skillId = skillId)

        skillsRef.child(skillId).setValue(skillWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Skill added successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to add skill")
                }
            }
    }

    override fun getSkillsByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<SkillModel>?) -> Unit
    ) {
        skillsRef.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val skillList = mutableListOf<SkillModel>()

                    if (snapshot.exists()) {
                        for (skillSnapshot in snapshot.children) {
                            val skill = skillSnapshot.getValue(SkillModel::class.java)
                            skill?.let {
                                skillList.add(it)
                            }
                        }
                        callback(true, "Skills fetched", skillList)
                    } else {
                        callback(true, "No skills found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun updateSkill(
        skillId: String,
        updatedSkill: SkillModel,
        callback: (Boolean, String) -> Unit
    ) {
        skillsRef.child(skillId).updateChildren(updatedSkill.toMap())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Skill updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update skill")
                }
            }
    }

    override fun deleteSkill(
        skillId: String,
        callback: (Boolean, String) -> Unit
    ) {
        skillsRef.child(skillId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Skill deleted successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to delete skill")
                }
            }
    }

    override fun getSkillById(
        skillId: String,
        callback: (Boolean, String, SkillModel?) -> Unit
    ) {
        skillsRef.child(skillId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val skill = snapshot.getValue(SkillModel::class.java)
                        callback(true, "Skill fetched", skill)
                    } else {
                        callback(false, "Skill not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            }
        )
    }
}