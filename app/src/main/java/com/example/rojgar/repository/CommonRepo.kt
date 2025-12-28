package com.example.rojgar.repository

import android.content.Context
import android.net.Uri

interface CommonRepo {
    fun updateProfilePhoto(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )

    fun updateCoverPhoto(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )

    fun getFileNameFromUri(context: Context, imageUri: Uri): String?
}