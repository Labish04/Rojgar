package com.example.rojgar.model

data class CompanyModel(
    val companyId: String = "",
    val companyName: String = "",
    val companyTagline: String = "",
    val companyInformation: String = "",
    val companyIndustry: String = "",
    val companyContactNumber: String = "",
    val companyEmail: String = "",
    val companyRegistrationDocument: String = "",
    val companyLocation: String = "",
    val companyEstablishedDate: String = "",
    val companyWebsite: String = "",
    val companySpecialties: List<String> = emptyList(),
    val companyProfileImage: String = "",
    val companyCoverPhoto: String = "",
    val isActive: Boolean = true,
    val isVerified: Boolean = false,
    val verificationDocument: String = "",
    val verificationRequestDate: String = "",
    val verificationStatus: String = "", // pending, approved, rejected
    val verificationReviewedDate: String = "",
    val verificationRejectionReason: String = "", // Add this field
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
            "isActive" to isActive,
            "companyTagline" to companyTagline,
            "companyInformation" to companyInformation,
            "companyIndustry" to companyIndustry,
            "companyWebsite" to companyWebsite,
            "companySpecialties" to companySpecialties,
            "isVerified" to isVerified,
            "verificationDocument" to verificationDocument,
            "verificationRequestDate" to verificationRequestDate,
            "verificationStatus" to verificationStatus,
            "verificationReviewedDate" to verificationReviewedDate,
            "verificationRejectionReason" to verificationRejectionReason, // Add this
            "latitude" to latitude,
            "longitude" to longitude
        )
    }
}