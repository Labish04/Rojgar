package com.example.rojgar.viewmodel

import android.content.Context
import android.net.Uri
import com.example.rojgar.repository.CommonRepo

class CommonViewModel(val repo: CommonRepo) {

    fun updateProfilePhoto(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        repo.updateProfilePhoto(context, imageUri, callback)
    }

    fun updateCoverPhoto(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        repo.updateCoverPhoto(context, imageUri, callback)
    }
}