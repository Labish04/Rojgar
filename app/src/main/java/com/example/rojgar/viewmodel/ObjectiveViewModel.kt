package com.example.rojgar.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rojgar.model.ObjectiveModel
import com.example.rojgar.repository.ObjectiveRepo

class ObjectiveViewModel(private val repo: ObjectiveRepo) {

    fun saveOrUpdateObjective(
        jobSeekerId: String,
        objective: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.saveOrUpdateObjective(jobSeekerId, objective, callback)
    }

    fun getObjectiveByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, ObjectiveModel?) -> Unit
    ) {
        repo.getObjectiveByJobSeekerId(jobSeekerId, callback)
    }

    fun getObjectiveTextByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, String?) -> Unit
    ) {
        repo.getObjectiveTextByJobSeekerId(jobSeekerId, callback)
    }

    fun deleteObjective(
        jobSeekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteObjective(jobSeekerId, callback)
    }
}