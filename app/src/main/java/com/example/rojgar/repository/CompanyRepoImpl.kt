package com.example.rojgar.repository

import android.content.Context
import android.database.Cursor
import android.location.Geocoder
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateListOf
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.rojgar.model.CompanyModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.Executors

class CompanyRepoImpl : CompanyRepo {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("Companys")

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dtmprduic",
            "api_key" to "883843915169633",
            "api_secret" to "DhiLcks25VLVZCBhWgGvObdGGyE"
        )
    )

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(
                        true, "Registration Successful",
                        "${auth.currentUser?.uid}"
                    )
                } else {
                    callback(false, "${it.exception?.message}", "")
                }
            }
    }

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Login Successful")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun addCompanyToDatabase(
        companyId: String,
        model: CompanyModel,
        callback: (Boolean, String) -> Unit
    ) {
        // Ensure isActive is set to true for new accounts
        val updatedModel = model.copy(isActive = true)
        ref.child(companyId).setValue(updatedModel).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Registration Successful")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getCurrentCompany(): FirebaseUser? {
        return auth.currentUser
    }

    override fun getCompanyById(
        companyId: String,
        callback: (Boolean, String, CompanyModel?) -> Unit
    ) {
        ref.child(companyId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val company = snapshot.getValue(CompanyModel::class.java)
                    if (company != null) {
                        callback(true, "Profile fetched", company)
                    } else {
                        callback(false, "Company data is null", null)
                    }
                } else {
                    callback(false, "Company not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getAllCompany(callback: (Boolean, String, List<CompanyModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val allCompanys = mutableStateListOf<CompanyModel>()
                    for (data in snapshot.children) {
                        val company = data.getValue(CompanyModel::class.java)
                        if (company != null) {
                            // Set the companyId to the key of the Firebase node
                            val companyWithId = company.copy(companyId = data.key ?: "")
                            allCompanys.add(companyWithId)
                        }
                    }
                    callback(true, "Company Fetched", allCompanys)
                } else {
                    callback(false, "No companies found", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun logout(
        companyId: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            auth.signOut()
            callback(true, "Logout successfully")
        } catch (e: Exception) {
            callback(false, e.message.toString())
        }
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Link sent to $email")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getCompanyDetails(
        companyId: String,
        callback: (Boolean, String, CompanyModel?) -> Unit
    ) {
        if (companyId.isEmpty()) {
            callback(false, "Invalid companyId", null)
            return
        }
        ref.child(companyId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val company = snapshot.getValue(CompanyModel::class.java)
                        callback(true, "Company fetched", company)
                    } else {
                        callback(false, "Company not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun uploadRegistrationDocument(
        companyId: String,
        imageUri: Uri,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(companyId).child("companyRegistrationDocument")
            .setValue(imageUri.toString())
            .addOnSuccessListener {
                callback(true, "Document uploaded successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to update database: ${e.message}")
            }
    }

    override fun uploadCompanyProfileImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

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

    override fun uploadCompanyCoverPhoto(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

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
        return fileName
    }

    override fun deactivateAccount(
        companyId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(companyId).child("isActive").setValue(false)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.signOut()
                    callback(true, "Account deactivated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to deactivate account")
                }
            }
    }

    override fun reactivateAccount(
        companyId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(companyId).child("isActive").setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Account reactivated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to reactivate account")
                }
            }
    }

    override fun checkAccountStatus(
        companyId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(companyId).child("isActive").get()
            .addOnSuccessListener { snapshot ->
                val isActive = snapshot.getValue(Boolean::class.java) ?: true
                if (isActive) {
                    callback(true, "Account is active")
                } else {
                    callback(false, "Account is deactivated")
                }
            }
            .addOnFailureListener { exception ->
                callback(false, "Error checking account status: ${exception.message}")
            }
    }

    override fun checkAccountStatusByEmail(
        email: String,
        callback: (Boolean, String?, String) -> Unit
    ) {
        val normalizedEmail = email.lowercase().trim()

        // Query all companies and filter by email
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var foundCompany: CompanyModel? = null
                    var foundCompanyId: String? = null

                    // Iterate through all companies to find matching email
                    for (companySnapshot in snapshot.children) {
                        val company = companySnapshot.getValue(CompanyModel::class.java)
                        if (company != null) {
                            val companyEmailNormalized = company.companyEmail.lowercase().trim()
                            if (companyEmailNormalized == normalizedEmail) {
                                foundCompany = company
                                foundCompanyId = companySnapshot.key
                                break
                            }
                        }
                    }

                    if (foundCompany != null && foundCompanyId != null) {
                        val isActive = foundCompany.isActive
                        if (isActive) {
                            callback(true, foundCompanyId, "Account is active")
                        } else {
                            callback(false, foundCompanyId, "Account is deactivated")
                        }
                    } else {
                        callback(false, null, "No company found with this email")
                    }
                } else {
                    callback(false, null, "Email not found in companies")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, null, "Error: ${error.message}")
            }
        })
    }

    // NEW METHOD: Update company profile
    override fun updateCompanyProfile(
        model: CompanyModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(model.companyId)
            .setValue(model)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Company profile updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Profile update failed")
                }
            }
    }

    override fun getLatLngFromAddress(context: Context, address: String): LatLng? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())

            // Try different address formats for better accuracy in Nepal
            val addressVariations = listOf(
                "$address, Kathmandu, Nepal",
                "$address, Nepal",
                address
            )

            for (addressVariant in addressVariations) {
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(addressVariant, 1)

                    if (!addresses.isNullOrEmpty()) {
                        return LatLng(addresses[0].latitude, addresses[0].longitude)
                    }
                } catch (e: Exception) {
                    // Try next variation
                    continue
                }
            }

            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}