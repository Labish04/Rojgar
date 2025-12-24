package com.example.rojgar.model

data class PortfolioModel(
    val portfolioId : String = "",
    val accountName : String = "",
    val accountLink : String = "",
    val jobSeekerId : String = ""
){
    fun toMap() : Map<String,Any?>{
        return mapOf(
            "portfolioId" to portfolioId,
            "accountName" to accountName,
            "accountLink" to accountLink,
            "jobSeekerId" to jobSeekerId,
        )
    }
}
