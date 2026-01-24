package com.example.rojgar.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.rojgar.model.TrainingModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.concurrent.Executors

class TrainingRepoImpl : TrainingRepo {

    private val database = FirebaseDatabase.getInstance()
    private val trainingsRef: DatabaseReference = database.getReference("Trainings")

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dtmprduic",
            "api_key" to "883843915169633",
            "api_secret" to "DhiLcks25VLVZCBhWgGvObdGGyE"
        )
    )

    override fun addTraining(
        training: TrainingModel,
        callback: (Boolean, String) -> Unit
    ) {
        val trainingId = trainingsRef.push().key ?: System.currentTimeMillis().toString()
        val trainingWithId = training.copy(trainingId = trainingId)

        trainingsRef.child(trainingId).setValue(trainingWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Training added successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to add training")
                }
            }
    }

    override fun getTrainingsByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<TrainingModel>?) -> Unit
    ) {
        trainingsRef.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val trainingList = mutableListOf<TrainingModel>()

                    if (snapshot.exists()) {
                        for (trainingSnapshot in snapshot.children) {
                            val training = trainingSnapshot.getValue(TrainingModel::class.java)
                            training?.let {
                                trainingList.add(it)
                            }
                        }
                        callback(true, "Trainings fetched", trainingList)
                    } else {
                        callback(true, "No trainings found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun updateTraining(
        trainingId: String,
        updatedTraining: TrainingModel,
        callback: (Boolean, String) -> Unit
    ) {
        trainingsRef.child(trainingId).updateChildren(updatedTraining.toMap())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Training updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update training")
                }
            }
    }

    override fun deleteTraining(
        trainingId: String,
        callback: (Boolean, String) -> Unit
    ) {
        trainingsRef.child(trainingId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Training deleted successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to delete training")
                }
            }
    }

    override fun getTrainingById(
        trainingId: String,
        callback: (Boolean, String, TrainingModel?) -> Unit
    ) {
        trainingsRef.child(trainingId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val training = snapshot.getValue(TrainingModel::class.java)
                        callback(true, "Training fetched", training)
                    } else {
                        callback(false, "Training not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            }
        )
    }

    override fun uploadCertificateImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                fileName = fileName?.substringBeforeLast(".") ?: "training_certificate"
                fileName = "training_${System.currentTimeMillis()}_$fileName"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?

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
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(imageUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName    }


}