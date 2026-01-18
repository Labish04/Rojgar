package com.example.rojgar.repository

import com.example.rojgar.model.MutualContact

interface MutualContactsRepository {
    fun getMutualContacts(
        currentUserId: String,
        callback: (Result<List<MutualContact>>) -> Unit
    )

    fun getUserDetails(
        userId: String,
        userType: String,
        callback: (Result<MutualContact>) -> Unit
    )
}