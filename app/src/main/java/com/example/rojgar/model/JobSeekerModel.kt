package com.example.rojgar.model

data class JobSeekerModel(
    val jobSeekerId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",

    // Personal Information
    val gender: String = "",
    val dob: String = "",
    val currentAddress: String = "",
    val permanentAddress: String = "",
    val religion: String = "",
    val nationality: String = "",
    val maritalStatus: String = "",
    val bio: String = "",
    val profession: String = "",
    val profilePhoto: String = "",
    val isActive: Boolean = true,
    val video: String = "",
    val fcmToken: String = "",
    val profileViews: Long = 0L
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "jobSeekerId" to jobSeekerId,
            "fullName" to fullName,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "gender" to gender,
            "dob" to dob,
            "currentAddress" to currentAddress,
            "permanentAddress" to permanentAddress,
            "religion" to religion,
            "nationality" to nationality,
            "maritalStatus" to maritalStatus,
            "profession" to profession,
            "bio" to bio,
            "profilePhoto" to profilePhoto,
            "video" to video,
            "isActive" to isActive,
            "fcmToken" to fcmToken,
            "profileViews" to profileViews

        )
    }
}