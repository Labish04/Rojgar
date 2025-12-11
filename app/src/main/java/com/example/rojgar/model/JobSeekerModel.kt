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
    val pernamentAddress : String = "",
    val bio : String = "",
    val profilePhoto : String = "",
    val coverPhoto : String = "",
    val introVideo : String = "",
    val objecitve : String = "",

//    Experience
    val expCompanyName : String = "",
    val expJobTite : String = "",
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

//    Tranning
    val tranningInstituteName : String = "",
    val tranningDuration : String = "",
    val tranningCompletionDate : String = "",
    val tranningCertificate : String = "",

//    Pefrence
    val pefJobCategory : String = "",
    val pefIndustry : String = "",
    val pefJobTitle : String = "",
    val pefAviability : String = "",
    val pefLocation: String = "",

//    Portfolio
    val portfiloAccontName : String = "",
    val portfiloAccontLink : String = "",

//    Language
    val langaugeName : String = "",
    val langaugeReadingLevel : String = "",
    val langaugeSpeakingLevel : String = "",
    val langaugeWritingLevel : String = "",
    val langaugeListeningLevel : String = "",

//    Reference
    val refName : String = "",
    val refJobTitle : String = "",
    val refCompanyName : String = "",
    val refEmail : String = "",
    val refContactType : String = "",
    val refContactNumber : String = ""
)
