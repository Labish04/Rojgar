package com.example.rojgar.model

data class JobSeekerModel (
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
    val video : String = "",


//    Experience
    val expCompanyName : String = "",
    val expJobTitle : String = "",
    val expJobLevel : String = "",
    val expJobStartDate : String = "",
    val expJobEndDate : String = "",
    val expJobWorkingStatus : String = "",
    val expLetter : String = "",

//    Skill
    val skillName : String = "",
    val skillLevel : String = "",

//    Education
    val eduInstituteName : String = "",
    val eduBoard : String = "",
    val eduField : String = "",
    val eduStartYear : String = "",
    val eduEndYear : String = "",
    val eduGradeType : String = "",
    val eduScore : String = "",
    val eduStatus : String = "",

//    Training
    val trainingInstituteName : String = "",
    val trainingDuration : String = "",
    val trainingCompletionDate : String = "",
    val trainingCertificate : String = "",

//    Pefrence
    val pefJobCategory : String = "",
    val pefIndustry : String = "",
    val pefJobTitle : String = "",
    val pefAvailability : String = "",
    val pefLocation: String = "",

//    Portfolio
    val portfolioAccountName : String = "",
    val portfolioAccountLink : String = "",

//    Language
    val languageName : String = "",
    val languageReadingLevel : String = "",
    val languageSpeakingLevel : String = "",
    val languageWritingLevel : String = "",
    val languageListeningLevel : String = "",

//    Reference
    val refName : String = "",
    val refJobTitle : String = "",
    val refCompanyName : String = "",
    val refEmail : String = "",
    val refContactType : String = "",
    val refContactNumber : String = ""
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
            "video" to video,
            "expCompanyName" to expCompanyName,
            "expJobTitle" to expJobTitle,
            "expJobLevel" to expJobLevel,
            "expJobStartDate" to expJobStartDate,
            "expJobEndDate" to expJobEndDate,
            "expJobWorkingStatus" to expJobWorkingStatus,
            "expLetter" to expLetter,
            "skillName" to skillName,
            "skillLevel" to skillLevel,
            "eduInstituteName" to eduInstituteName,
            "eduBoard" to eduBoard,
            "eduField" to eduField,
            "eduStartYear" to eduStartYear,
            "eduEndYear" to eduEndYear,
            "eduGradeType" to eduGradeType,
            "eduStatus" to eduStatus,
            "trainingInstituteName" to trainingInstituteName,
            "trainingDuration" to trainingDuration,
            "trainingCompletionDate" to trainingCompletionDate,
            "trainingCertificate" to trainingCertificate,
            "pefJobCategory" to pefJobCategory,
            "pefIndustry" to pefIndustry,
            "pefJobTitle" to pefJobTitle,
            "pefAvailability" to pefAvailability,
            "pefLocation" to pefLocation,
            "portfolioAccountName" to portfolioAccountName,
            "portfolioAccountLink" to portfolioAccountLink,
            "languageName" to languageName,
            "languageReadingLevel" to languageReadingLevel,
            "languageSpeakingLevel" to languageSpeakingLevel,
            "languageWritingLevel" to languageWritingLevel,
            "languageListeningLevel" to languageListeningLevel,
            "refName" to refName,
            "refJobTitle" to refJobTitle,
            "refCompanyName" to refCompanyName,
            "refEmail" to refEmail,
            "refEmail" to refEmail,
            "refContactType" to refContactType,
            "refContactNumber" to refContactNumber,
        )
    }
}
