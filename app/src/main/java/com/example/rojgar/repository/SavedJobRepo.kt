package com.example.rojgar.repository

import com.example.rojgar.model.SavedJobModel

interface SavedJobRepo {
    fun saveJob(savedJob: SavedJobModel, callback: (Boolean, String) -> Unit)

    fun unsaveJob(savedId: String, callback: (Boolean, String) -> Unit)

    fun getSavedJobsByJobSeeker(
        jobSeekerId: String,
        callback: (Boolean, String, List<SavedJobModel>?) -> Unit
    )

    fun checkIfJobSaved(
        jobSeekerId: String,
        jobId: String,
        callback: (Boolean, String, Boolean, String?) -> Unit
    )

    fun getSavedJobDetails(
        jobSeekerId: String,
        jobId: String,
        callback: (Boolean, String, SavedJobModel?) -> Unit
    )
}