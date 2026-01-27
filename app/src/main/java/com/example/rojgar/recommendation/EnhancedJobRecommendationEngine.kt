package com.example.rojgar.recommendation

import com.example.rojgar.model.JobModel
import com.example.rojgar.model.JobSeekerModel
import com.example.rojgar.model.PreferenceModel
import com.example.rojgar.model.SkillModel
import com.example.rojgar.model.ExperienceModel
import com.example.rojgar.model.EducationModel
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Advanced Job Recommendation Engine
 * Combines multiple recommendation techniques for optimal results
 */
class EnhancedJobRecommendationEngine {

    companion object {
        // Scoring weights (can be tuned based on analytics)
        private const val WEIGHT_CATEGORY = 3.5
        private const val WEIGHT_TITLE = 3.0
        private const val WEIGHT_SKILLS = 4.0
        private const val WEIGHT_EXPERIENCE = 2.5
        private const val WEIGHT_EDUCATION = 2.0
        private const val WEIGHT_JOB_TYPE = 2.5
        private const val WEIGHT_LOCATION = 1.5
        private const val WEIGHT_SALARY = 1.0
        private const val WEIGHT_RECENCY = 1.5

        // Decay factors
        private const val TIME_DECAY_DAYS = 30.0
        private const val SKILL_MATCH_THRESHOLD = 0.3
    }

    /**
     * Main recommendation function - returns scored and ranked jobs
     */
    fun getRecommendedJobs(
        allJobs: List<JobModel>,
        jobSeeker: JobSeekerModel,
        preference: PreferenceModel?,
        skills: List<SkillModel>?,
        experiences: List<ExperienceModel>?,
        education: List<EducationModel>?
    ): List<ScoredJob> {

        val scoredJobs = allJobs.map { job ->
            val score = calculateJobScore(
                job = job,
                jobSeeker = jobSeeker,
                preference = preference,
                skills = skills,
                experiences = experiences,
                education = education
            )

            ScoredJob(
                job = job,
                totalScore = score.totalScore,
                scoreBreakdown = score
            )
        }

        // Filter out jobs with zero score and sort by total score
        return scoredJobs
            .filter { it.totalScore > 0 }
            .sortedByDescending { it.totalScore }
    }

    /**
     * Calculate comprehensive job score using multiple factors
     */
    private fun calculateJobScore(
        job: JobModel,
        jobSeeker: JobSeekerModel,
        preference: PreferenceModel?,
        skills: List<SkillModel>?,
        experiences: List<ExperienceModel>?,
        education: List<EducationModel>?
    ): JobScoreBreakdown {

        var categoryScore = 0.0
        var titleScore = 0.0
        var skillScore = 0.0
        var experienceScore = 0.0
        var educationScore = 0.0
        var jobTypeScore = 0.0
        var locationScore = 0.0
        var salaryScore = 0.0
        var recencyScore = 0.0

        // 1. CATEGORY MATCHING (Preference-based)
        if (preference != null && preference.categories.isNotEmpty()) {
            categoryScore = calculateCategoryScore(job.categories, preference.categories)
        }

        // 2. TITLE/POSITION MATCHING (Fuzzy matching)
        if (preference != null && preference.titles.isNotEmpty()) {
            titleScore = calculateTitleScore(job, preference.titles)
        }

        // 3. SKILLS MATCHING (Advanced skill matching with synonyms)
        if (skills != null && skills.isNotEmpty()) {
            skillScore = calculateSkillScore(job.skills, skills)
        }

        // 4. EXPERIENCE MATCHING
        if (experiences != null && experiences.isNotEmpty()) {
            experienceScore = calculateExperienceScore(job, experiences)
        }

        // 5. EDUCATION MATCHING
        if (education != null && education.isNotEmpty()) {
            educationScore = calculateEducationScore(job.education, education)
        }

        // 6. JOB TYPE MATCHING (Full-time, Part-time, etc.)
        if (preference != null && preference.availabilities.isNotEmpty()) {
            jobTypeScore = if (preference.availabilities.any {
                    it.equals(job.jobType, ignoreCase = true)
                }) WEIGHT_JOB_TYPE else 0.0
        }

        // 7. LOCATION MATCHING (Can be enhanced with distance calculation)
        if (preference != null && preference.location.isNotBlank()) {
            locationScore = calculateLocationScore(jobSeeker.currentAddress, preference.location)
        }

        // 8. SALARY EXPECTATIONS (if available in future)
        salaryScore = 0.0 // Placeholder for salary matching

        // 9. RECENCY BOOST (Newer jobs get higher priority)
        recencyScore = calculateRecencyScore(job.timestamp)

        // Calculate total weighted score
        val totalScore =
            (categoryScore * WEIGHT_CATEGORY) +
                    (titleScore * WEIGHT_TITLE) +
                    (skillScore * WEIGHT_SKILLS) +
                    (experienceScore * WEIGHT_EXPERIENCE) +
                    (educationScore * WEIGHT_EDUCATION) +
                    (jobTypeScore * WEIGHT_JOB_TYPE) +
                    (locationScore * WEIGHT_LOCATION) +
                    (salaryScore * WEIGHT_SALARY) +
                    (recencyScore * WEIGHT_RECENCY)

        return JobScoreBreakdown(
            categoryScore = categoryScore,
            titleScore = titleScore,
            skillScore = skillScore,
            experienceScore = experienceScore,
            educationScore = educationScore,
            jobTypeScore = jobTypeScore,
            locationScore = locationScore,
            salaryScore = salaryScore,
            recencyScore = recencyScore,
            totalScore = totalScore
        )
    }

    /**
     * Calculate category matching score with partial matches
     */
    private fun calculateCategoryScore(
        jobCategories: List<String>,
        preferenceCategories: List<String>
    ): Double {
        if (jobCategories.isEmpty() || preferenceCategories.isEmpty()) return 0.0

        var matchCount = 0
        jobCategories.forEach { jobCat ->
            preferenceCategories.forEach { prefCat ->
                if (jobCat.equals(prefCat, ignoreCase = true)) {
                    matchCount++
                } else if (isSimilarCategory(jobCat, prefCat)) {
                    matchCount++ // Partial match for similar categories
                }
            }
        }

        // Normalize score (0 to 1)
        return matchCount.toDouble() / preferenceCategories.size.coerceAtLeast(1)
    }

    /**
     * Calculate title matching score using fuzzy matching
     */
    private fun calculateTitleScore(
        job: JobModel,
        preferenceTitles: List<String>
    ): Double {
        var bestMatch = 0.0

        preferenceTitles.forEach { prefTitle ->
            // Check title
            val titleSimilarity = calculateStringSimilarity(
                job.title.lowercase(),
                prefTitle.lowercase()
            )

            // Check position
            val positionSimilarity = calculateStringSimilarity(
                job.position.lowercase(),
                prefTitle.lowercase()
            )

            val maxSimilarity = maxOf(titleSimilarity, positionSimilarity)
            if (maxSimilarity > bestMatch) {
                bestMatch = maxSimilarity
            }
        }

        return bestMatch
    }

    /**
     * Advanced skill matching with weighted scoring
     */
    private fun calculateSkillScore(
        jobSkills: String,
        userSkills: List<SkillModel>
    ): Double {
        if (jobSkills.isBlank() || userSkills.isEmpty()) return 0.0

        val jobSkillsList = jobSkills
            .split(",", ";", "|")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        if (jobSkillsList.isEmpty()) return 0.0

        var totalMatch = 0.0
        var matchedSkills = 0

        userSkills.forEach { userSkill ->
            val userSkillName = userSkill.skill.lowercase()
            val proficiencyMultiplier = when (userSkill.level.lowercase()) {
                "expert", "advanced" -> 1.5
                "intermediate" -> 1.2
                "beginner", "basic" -> 0.8
                else -> 1.0
            }

            jobSkillsList.forEach { jobSkill ->
                val similarity = calculateStringSimilarity(userSkillName, jobSkill)
                if (similarity > SKILL_MATCH_THRESHOLD) {
                    totalMatch += similarity * proficiencyMultiplier
                    matchedSkills++
                }
            }
        }

        // Normalize based on job requirements
        return if (matchedSkills > 0) {
            (totalMatch / jobSkillsList.size.coerceAtLeast(1)).coerceAtMost(1.0)
        } else {
            0.0
        }
    }

    /**
     * Calculate experience score based on years and relevance
     */
    private fun calculateExperienceScore(
        job: JobModel,
        userExperiences: List<ExperienceModel>
    ): Double {
        if (userExperiences.isEmpty()) return 0.0

        // Extract required experience years from job
        val requiredYears = extractYearsFromExperience(job.experience)

        // Calculate total user experience
        val totalUserExperience = userExperiences.sumOf { exp ->
            calculateExperienceDuration(exp.startDate, exp.endDate)
        }

        // Score based on experience match
        return when {
            requiredYears <= 0 -> 0.5 // No specific requirement
            totalUserExperience >= requiredYears -> 1.0 // Meets or exceeds
            totalUserExperience >= requiredYears * 0.7 -> 0.8 // Close match
            totalUserExperience >= requiredYears * 0.5 -> 0.5 // Partial match
            else -> 0.2 // Less experience but still considered
        }
    }

    /**
     * Calculate education matching score
     */
    private fun calculateEducationScore(
        jobEducation: String,
        userEducation: List<EducationModel>
    ): Double {
        if (jobEducation.isBlank() || userEducation.isEmpty()) return 0.0

        val jobEduLower = jobEducation.lowercase()
        var bestMatch = 0.0

        userEducation.forEach { edu ->
            val eduLevel = edu.educationDegree.lowercase()
            val similarity = when {
                // Exact match
                jobEduLower.contains(eduLevel) || eduLevel.contains(jobEduLower) -> 1.0

                // Qualification hierarchy matching
                isEducationQualified(eduLevel, jobEduLower) -> 0.9

                // Partial match
                else -> calculateStringSimilarity(eduLevel, jobEduLower) * 0.7
            }

            if (similarity > bestMatch) {
                bestMatch = similarity
            }
        }

        return bestMatch
    }

    /**
     * Calculate location score (basic string matching, can be enhanced with geocoding)
     */
    private fun calculateLocationScore(
        userAddress: String,
        preferredLocation: String
    ): Double {
        if (userAddress.isBlank() || preferredLocation.isBlank()) return 0.0

        return if (userAddress.contains(preferredLocation, ignoreCase = true) ||
            preferredLocation.contains(userAddress, ignoreCase = true)) {
            1.0
        } else {
            calculateStringSimilarity(
                userAddress.lowercase(),
                preferredLocation.lowercase()
            ) * 0.7
        }
    }

    /**
     * Calculate recency score using exponential decay
     */
    private fun calculateRecencyScore(timestamp: Long): Double {
        val currentTime = System.currentTimeMillis()
        val ageInDays = (currentTime - timestamp) / (1000 * 60 * 60 * 24).toDouble()

        // Exponential decay: newer jobs get higher scores
        return exp(-ageInDays / TIME_DECAY_DAYS)
    }

    /**
     * String similarity using Levenshtein distance (normalized)
     */
    private fun calculateStringSimilarity(str1: String, str2: String): Double {
        if (str1 == str2) return 1.0
        if (str1.isEmpty() || str2.isEmpty()) return 0.0

        // Use Levenshtein distance
        val distance = levenshteinDistance(str1, str2)
        val maxLength = maxOf(str1.length, str2.length)

        return 1.0 - (distance.toDouble() / maxLength)
    }

    /**
     * Levenshtein distance calculation
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }

        for (i in 0..str1.length) dp[i][0] = i
        for (j in 0..str2.length) dp[0][j] = j

        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }

        return dp[str1.length][str2.length]
    }

    /**
     * Check if two categories are similar (can be expanded with synonyms)
     */
    private fun isSimilarCategory(cat1: String, cat2: String): Boolean {
        val synonyms = mapOf(
            "software" to listOf("it", "technology", "programming"),
            "marketing" to listOf("sales", "advertising", "promotion"),
            "finance" to listOf("accounting", "banking", "economics"),
            "healthcare" to listOf("medical", "nursing", "hospital"),
            "education" to listOf("teaching", "training", "academic")
        )

        val cat1Lower = cat1.lowercase()
        val cat2Lower = cat2.lowercase()

        synonyms.forEach { (key, values) ->
            if ((cat1Lower.contains(key) || values.any { cat1Lower.contains(it) }) &&
                (cat2Lower.contains(key) || values.any { cat2Lower.contains(it) })) {
                return true
            }
        }

        return false
    }

    /**
     * Extract years from experience requirement string
     */
    private fun extractYearsFromExperience(experience: String): Double {
        val regex = Regex("(\\d+)\\s*(?:year|yr)", RegexOption.IGNORE_CASE)
        val match = regex.find(experience)
        return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }

    /**
     * Calculate experience duration in years
     */
    private fun calculateExperienceDuration(startDate: String, endDate: String): Double {
        // Simple year calculation (can be enhanced with actual date parsing)
        return try {
            val start = startDate.substringAfterLast(" ").toIntOrNull() ?: 0
            val end = if (endDate.equals("present", ignoreCase = true)) {
                java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            } else {
                endDate.substringAfterLast(" ").toIntOrNull() ?: 0
            }
            maxOf(0.0, (end - start).toDouble())
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * Check if user education qualifies for job requirement
     */
    private fun isEducationQualified(userEdu: String, requiredEdu: String): Boolean {
        val educationHierarchy = listOf(
            listOf("phd", "doctorate", "doctoral"),
            listOf("master", "masters", "msc", "mba", "ma"),
            listOf("bachelor", "bachelors", "bsc", "ba", "be", "btech"),
            listOf("associate", "diploma"),
            listOf("high school", "secondary", "+2", "intermediate")
        )

        var userLevel = -1
        var requiredLevel = -1

        educationHierarchy.forEachIndexed { index, levels ->
            if (levels.any { userEdu.contains(it) }) userLevel = index
            if (levels.any { requiredEdu.contains(it) }) requiredLevel = index
        }

        return userLevel != -1 && requiredLevel != -1 && userLevel <= requiredLevel
    }
}

/**
 * Data class to hold job with its score
 */
data class ScoredJob(
    val job: JobModel,
    val totalScore: Double,
    val scoreBreakdown: JobScoreBreakdown
)

/**
 * Detailed score breakdown for transparency and debugging
 */
data class JobScoreBreakdown(
    val categoryScore: Double,
    val titleScore: Double,
    val skillScore: Double,
    val experienceScore: Double,
    val educationScore: Double,
    val jobTypeScore: Double,
    val locationScore: Double,
    val salaryScore: Double,
    val recencyScore: Double,
    val totalScore: Double
) {
    fun getTopFactors(limit: Int = 3): List<Pair<String, Double>> {
        val factors = listOf(
            "Category" to categoryScore,
            "Title" to titleScore,
            "Skills" to skillScore,
            "Experience" to experienceScore,
            "Education" to educationScore,
            "Job Type" to jobTypeScore,
            "Location" to locationScore,
            "Salary" to salaryScore,
            "Recency" to recencyScore
        )
        return factors.sortedByDescending { it.second }.take(limit)
    }
}