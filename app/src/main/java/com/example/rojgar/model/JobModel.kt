package com.example.rojgar.model

data class JobModel(
    val postId: String = "",
    val companyId: String = "",
    val title: String = "",
    val position: String = "",
    val categories: List<String> = emptyList(),
    val jobType: String = "",
    val experience: String = "",
    val education: String = "",
    val skills: String = "",
    val salary: String = "",
    val deadline: String = "",
    val responsibilities: String = "",
    val jobDescription: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "postId" to postId,
            "companyId" to companyId,
            "title" to title,
            "position" to position,
            "categories" to categories,
            "jobType" to jobType,
            "experience" to experience,
            "education" to education,
            "skills" to skills,
            "salary" to salary,
            "deadline" to deadline,
            "responsibilities" to responsibilities,
            "jobDescription" to jobDescription,
            "imageUrl" to imageUrl,
            "timestamp" to timestamp
        )
    }
}

data class JobCategory(
    val name: String,
    val isSelected: Boolean = false
)