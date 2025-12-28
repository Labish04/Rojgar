package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rojgar.model.PortfolioModel
import com.example.rojgar.model.ReferenceModel
import com.example.rojgar.repository.ReferenceRepo

class ReferenceViewModel(private val referenceRepo: ReferenceRepo) : ViewModel() {

    private val _reference = MutableLiveData< ReferenceModel?>()
    val reference: LiveData<ReferenceModel?> = _reference

    private val _allReferences = MutableLiveData<List<ReferenceModel>?>()
    val allReferences : MutableLiveData<List<ReferenceModel>?> get() = _allReferences

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

    fun fetchReferencesByJobSeekerId(jobSeekerId: String) {
        referenceRepo.getReferencesByJobSeekerId(jobSeekerId) { success, message, experiences ->
            if (success) {
                _allReferences.postValue(experiences ?: emptyList())
            } else {
                _allReferences.postValue(emptyList())
            }
        }
    }
}