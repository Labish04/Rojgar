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
    val isCurrentlyWorking: Boolean = false,
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
            "isCurrentlyWorking" to isCurrentlyWorking,
            "experienceLetter" to experienceLetter,
            "jobSeekerId" to jobSeekerId
        )
    }

    // Calculate years & months of experience
    fun calculateYearsOfExperience(): String {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val start = dateFormat.parse(startDate)
            val end = if (isCurrentlyWorking || endDate.isEmpty()) {
                Date()
            } else {
                dateFormat.parse(endDate)
            }

            if (start != null && end != null) {
                val diffInMillis = end.time - start.time
                val years = (diffInMillis / (1000L * 60 * 60 * 24 * 365)).toInt()
                val months =
                    ((diffInMillis % (1000L * 60 * 60 * 24 * 365)) /
                            (1000L * 60 * 60 * 24 * 30)).toInt()

                when {
                    years > 0 && months > 0 -> "$years years $months months"
                    years > 0 -> "$years years"
                    else -> "$months months"
                }
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            "N/A"
        }
    }

    // Date range display
    fun getFormattedDateRange(): String {
        return if (isCurrentlyWorking) {
            "$startDate - Present"
        } else {
            "$startDate - $endDate"
        }
    }
}
