package com.example.rojgar.repository

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.Executors

class CommonRepoImpl : CommonRepo {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dtmprduic",
            "api_key" to "883843915169633",
            "api_secret" to "DhiLcks25VLVZCBhWgGvObdGGyE"
        )
    )

    override fun updateProfilePhoto(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        uploadImage(context, imageUri, "profile_", callback)
    }

    override fun updateCoverPhoto(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        uploadImage(context, imageUri, "cover_", callback)
    }

    private fun uploadImage(
        context: Context,
        imageUri: Uri,
        prefix: String,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                // Generate unique filename with prefix and UUID
                val fileName = "${prefix}${UUID.randomUUID()}"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id" to fileName,
                        "resource_type" to "image",
                        "folder" to "jobseeker_photos"
                    )
                )

                var imageUrl = response["secure_url"] as String? ?: (response["url"] as String?)

                // Ensure HTTPS URL
                imageUrl = imageUrl?.replace("http://", "https://")

                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

    override fun getFileNameFromUri(
        context: Context,
        imageUri: Uri
    ): String? {
        return imageUri.lastPathSegment ?: "image_${System.currentTimeMillis()}"
    }
}