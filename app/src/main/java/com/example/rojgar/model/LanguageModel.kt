package com.example.rojgar.model

data class LanguageModel(
    val languageId : String = "",
    val language : String = "",
    val readingLevel : String = "",
    val speakingLevel : String = "",
    val writingLevel : String = "",
    val listeningLevel : String = "",
    val jobSeekerId : String = ""
){
    fun toMap() : Map<String,Any?>{
        return mapOf(
            "languageId" to languageId,
            "language" to language,
            "readingLevel" to readingLevel,
            "speakingLevel" to speakingLevel,
            "writingLevel" to writingLevel,
            "listeningLevel" to listeningLevel,
            "jobSeekerId" to jobSeekerId,
            )}
}
