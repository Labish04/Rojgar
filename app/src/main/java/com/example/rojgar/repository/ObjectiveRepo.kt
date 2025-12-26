package com.example.rojgar.repository

import com.example.rojgar.model.ObjectiveModel

interface ObjectiveRepo {
    fun saveOrUpdateObjective(
        jobSeekerId: String,
        objective: String,
        callback: (Boolean, String) -> Unit
    )

    fun getObjectiveByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, ObjectiveModel?) -> Unit
    )

    fun getObjectiveTextByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, String?) -> Unit
    )

    fun deleteObjective(
        jobSeekerId: String,
        callback: (Boolean, String) -> Unit
    )
}