package com.example.rojgar.repository

import com.example.rojgar.model.ReferenceModel

interface ReferenceRepo {
    fun addReference(
        reference: ReferenceModel,
        callback: (Boolean, String) -> Unit
    )

    fun getReferencesByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<ReferenceModel>?) -> Unit
    )

    fun updateReference(
        referenceId: String,
        updatedReference: ReferenceModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteReference(
        referenceId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getReferenceById(
        referenceId: String,
        callback: (Boolean, String, ReferenceModel?) -> Unit
    )
}