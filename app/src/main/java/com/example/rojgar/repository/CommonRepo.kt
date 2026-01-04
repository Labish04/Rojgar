package com.example.rojgar.repository

import android.content.Context
import android.net.Uri

interface CommonRepo {
    fun uploadImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )


    fun getFileNameFromUri(context: Context, imageUri: Uri): String?
}