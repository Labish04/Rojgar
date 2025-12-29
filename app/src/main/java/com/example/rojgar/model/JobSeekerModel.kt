package com.example.rojgar.model

data class JobSeekerModel(

    val jobSeekerId : String = "",
    val fullName : String = "",
    val email : String = "",
    val phoneNumber : String = "",

    // Personal Information
    val gender : String = "",
    val dob : String = "",
    val currentAddress : String = "",
    val permanentAddress : String = "",
    val religion: String = "",
    val nationality: String = "",
    val maritalStatus: String = "",
    val bio : String = "",
    val profession : String = "",
    val profilePhoto : String = "",
    val coverPhoto : String = "",
    val video : String = "",

    val followers : List<String> = emptyList(),
    val appliedJobs : List<String> = emptyList() // List of application IDs

){
    fun toMap() : Map<String,Any?>{
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
            "coverPhoto" to coverPhoto,
            "video" to video,
            "followers" to followers,
            "appliedJobs" to appliedJobs
        )
    }
}