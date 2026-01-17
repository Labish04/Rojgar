package com.example.rojgar.model

import java.util.UUID

data class SearchHistoryModel(
    val searchId: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val userType: String = "",
    val query: String = "",
    val searchType: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val filters: Map<String, Any> = emptyMap(),
    val resultCount: Int = 0,
    val filterState: FilterStateData = FilterStateData()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "searchId" to searchId,
            "userId" to userId,
            "userType" to userType,
            "query" to query,
            "searchType" to searchType,
            "timestamp" to timestamp,
            "filters" to filters,
            "resultCount" to resultCount,
            "filterState" to filterState.toMap()
        )
    }
}

data class FilterStateData(
    val selectedCategories: List<String> = emptyList(),
    val selectedJobTypes: List<String> = emptyList(),
    val selectedExperience: String = "",
    val selectedEducation: List<String> = emptyList(),
    val minSalary: String = "",
    val maxSalary: String = "",
    val location: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "selectedCategories" to selectedCategories,
            "selectedJobTypes" to selectedJobTypes,
            "selectedExperience" to selectedExperience,
            "selectedEducation" to selectedEducation,
            "minSalary" to minSalary,
            "maxSalary" to maxSalary,
            "location" to location
        )
    }

    companion object {
        fun fromMap(map: Map<*, *>?): FilterStateData {
            if (map == null) return FilterStateData()
            return FilterStateData(
                selectedCategories = (map["selectedCategories"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                selectedJobTypes = (map["selectedJobTypes"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                selectedExperience = map["selectedExperience"] as? String ?: "",
                selectedEducation = (map["selectedEducation"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                minSalary = map["minSalary"] as? String ?: "",
                maxSalary = map["maxSalary"] as? String ?: "",
                location = map["location"] as? String ?: ""
            )
        }
    }
}