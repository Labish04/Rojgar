package com.example.rojgar.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rojgar.model.ReferenceModel
import com.example.rojgar.repository.ReferenceRepo

class ReferenceViewModel(private val referenceRepo: ReferenceRepo) : ViewModel() {

    fun addReference(reference: ReferenceModel, callback: (Boolean, String) -> Unit) {
        referenceRepo.addReference(reference, callback)
    }

    fun getReferencesByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<ReferenceModel>?) -> Unit
    ) {
        referenceRepo.getReferencesByJobSeekerId(jobSeekerId, callback)
    }

    fun updateReference(
        referenceId: String,
        updatedReference: ReferenceModel,
        callback: (Boolean, String) -> Unit
    ) {
        referenceRepo.updateReference(referenceId, updatedReference, callback)
    }

    fun deleteReference(referenceId: String, callback: (Boolean, String) -> Unit) {
        referenceRepo.deleteReference(referenceId, callback)
    }

    fun getReferenceById(
        referenceId: String,
        callback: (Boolean, String, ReferenceModel?) -> Unit
    ) {
        referenceRepo.getReferenceById(referenceId, callback)
    }
}