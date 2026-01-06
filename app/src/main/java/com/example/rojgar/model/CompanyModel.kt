package com.example.rojgar.model

data class CompanyModel(
    val companyId: String = "",
    val companyName: String = "",
    val companyContactNumber: String = "",
    val companyEmail: String = "",
    val companyRegistrationDocument: String = "",
    val companyLocation: String = "",
    val companyEstablishedDate: String = "",
    val companyProfileImage: String = "",
    val companyCoverPhoto: String = "",
    val isActive: Boolean = true, // Added isActive field with default true
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "companyId" to companyId,
            "companyName" to companyName,
            "companyContactNumber" to companyContactNumber,
            "companyEmail" to companyEmail,
            "companyRegistrationDocument" to companyRegistrationDocument,
            "companyLocation" to companyLocation,
            "companyEstablishedDate" to companyEstablishedDate,
            "companyProfileImage" to companyProfileImage,
            "companyCoverPhoto" to companyCoverPhoto,
            "isActive" to isActive // Added to map
        )
    }
}