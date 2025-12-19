package com.example.rojgar.model

data class ReferenceModel(
    val referenceId : String = "",
    val name : String = "",
    val jobTitle : String = "",
    val companyName : String = "",
    val email : String = "",
    val contactType : String = "",
    val contactNumber : String = "",
    val jobSeekerId : String = "",
){
    fun toMap() : Map<String,Any?>{
        return mapOf(
            "referenceId" to referenceId,
            "name" to name,
            "jobTitle" to jobTitle,
            "companyName" to companyName,
            "email" to email,
            "contactType" to contactType,
            "contactNumber" to contactNumber,
            "jobSeekerId" to jobSeekerId,
        )}
}
