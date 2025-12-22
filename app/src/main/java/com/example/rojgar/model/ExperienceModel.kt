package com.example.rojgar.model

import java.text.SimpleDateFormat
import java.util.*

data class ExperienceModel(
    val experienceId: String = "",
    val companyName: String = "",
    val title: String = "",
    val jobCategory: String = "",
    val level: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val currentlyWorkingStatus: String = "",
    val experienceLetter: String = "",
    val jobSeekerId: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "experienceId" to experienceId,
            "companyName" to companyName,
            "title" to title,
            "jobCategory" to jobCategory,
            "level" to level,
            "startDate" to startDate,
            "endDate" to endDate,
            "currentlyWorkingStatus" to currentlyWorkingStatus,
            "experienceLetter" to experienceLetter,
            "jobSeekerId" to jobSeekerId
        )
    }

    // Helper function to calculate years of experience
    fun calculateYearsOfExperience(): String {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val start = dateFormat.parse(startDate)
            val end = if (currentlyWorkingStatus == "Yes" || endDate.isEmpty()) {
                Date() // Current date if currently working
            } else {
                dateFormat.parse(endDate)
            }

            if (start != null) {
                val diffInMillies = end.time - start.time
                val years = (diffInMillies / (1000L * 60 * 60 * 24 * 365)).toInt()
                val months = ((diffInMillies % (1000L * 60 * 60 * 24 * 365)) / (1000L * 60 * 60 * 24 * 30)).toInt()

                if (years > 0) {
                    if (months > 0) {
                        "$years years $months months"
                    } else {
                        "$years years"
                    }
                } else {
                    "$months months"
                }
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            "N/A"
        }
    }

    // Helper function to get formatted date range
    fun getFormattedDateRange(): String {
        return if (currentlyWorkingStatus == "Yes") {
            "$startDate - Present"
        } else {
            "$startDate - $endDate"
        }
    }
}