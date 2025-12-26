package com.example.rojgar.utils

import com.example.rojgar.model.JobModel
import com.example.rojgar.model.PreferenceModel

object JobRecommendationHelper {

    /**
     * Get recommended jobs based on job seeker preferences
     */
    fun getRecommendedJobs(
        allJobs: List<JobModel>,
        preference: PreferenceModel?,
        limit: Int = 10
    ): List<JobModel> {
        if (preference == null) return allJobs.take(limit)

        val scoredJobs = allJobs.map { job ->
            JobWithScore(job, calculateMatchScore(job, preference))
        }

        // Filter out jobs with zero score and sort by match score (highest first)
        return scoredJobs
            .filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(limit)
            .map { it.job }
    }

    /**
     * Calculate match score between a job and preference (0-100)
     */
    private fun calculateMatchScore(job: JobModel, preference: PreferenceModel): Int {
        var score = 0
        val maxScorePerCategory = 25 // Total 100 points across 4 categories

        // 1. Category/Title match (25 points)
        if (preference.category.isNotBlank()) {
            val categoryMatch = job.categories.any { category ->
                category.contains(preference.category, ignoreCase = true)
            }
            val titleMatch = job.title.contains(preference.title, ignoreCase = true) ||
                    job.position.contains(preference.title, ignoreCase = true)

            if (categoryMatch || titleMatch) score += maxScorePerCategory
        }

        // 2. Industry/Position match (25 points)
        if (preference.industry.isNotBlank()) {
            val industryMatch = job.position.contains(preference.industry, ignoreCase = true) ||
                    job.title.contains(preference.industry, ignoreCase = true)

            if (industryMatch) score += maxScorePerCategory
        }

        // 3. Job Type match (25 points)
        if (preference.availability.isNotBlank()) {
            val jobTypeMatch = job.jobType.contains(preference.availability, ignoreCase = true)
            if (jobTypeMatch) score += maxScorePerCategory
        }

        // 4. Location match (25 points)
        if (preference.location.isNotBlank()) {
            // If job has location in description or skills
            val locationInJob = job.jobDescription.contains(preference.location, ignoreCase = true) ||
                    job.skills.contains(preference.location, ignoreCase = true)
            if (locationInJob) score += maxScorePerCategory / 2 // Partial score
        }

        return score
    }

    /**
     * Get job matches by specific category
     */
    fun getJobsByCategory(
        allJobs: List<JobModel>,
        category: String,
        limit: Int = 5
    ): List<JobModel> {
        return allJobs.filter { job ->
            job.categories.any { it.contains(category, ignoreCase = true) }
        }.take(limit)
    }

    /**
     * Get job matches by job type
     */
    fun getJobsByType(
        allJobs: List<JobModel>,
        jobType: String,
        limit: Int = 5
    ): List<JobModel> {
        return allJobs.filter { job ->
            job.jobType.contains(jobType, ignoreCase = true)
        }.take(limit)
    }

    /**
     * Filter jobs by multiple criteria
     */
    fun filterJobs(
        allJobs: List<JobModel>,
        categories: List<String> = emptyList(),
        jobTypes: List<String> = emptyList(),
        experience: String = "",
        education: String = ""
    ): List<JobModel> {
        return allJobs.filter { job ->
            val matchesCategory = categories.isEmpty() ||
                    job.categories.any { category ->
                        categories.any { it.contains(category, ignoreCase = true) }
                    }
            val matchesJobType = jobTypes.isEmpty() ||
                    jobTypes.any { job.jobType.contains(it, ignoreCase = true) }
            val matchesExperience = experience.isBlank() ||
                    job.experience.contains(experience, ignoreCase = true)
            val matchesEducation = education.isBlank() ||
                    job.education.contains(education, ignoreCase = true)

            matchesCategory && matchesJobType && matchesExperience && matchesEducation
        }
    }

    /**
     * Data class to hold job with match score
     */
    private data class JobWithScore(
        val job: JobModel,
        val score: Int
    )
}