package com.example.rojgar.model

data class JobSeekerModel(
//    Basic SignUp Data
    val jobSeekerId : String = "",
    val fullName : String = "",
    val email : String = "",
    val phoneNumber : String = "",

//    Personal Information
    val gender : String = "",
    val dob : String = "",
    val currentAddress : String = "",
    val permanentAddress : String = "",
    val bio : String = "",
    val profilePhoto : String = "",
    val coverPhoto : String = "",
    val objective : String = "",
    val video : String = ""

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
            "bio" to bio,
            "profilePhoto" to profilePhoto,
            "coverPhoto" to coverPhoto,
            "objective" to objective,
            "video" to video
        )
    }
}
