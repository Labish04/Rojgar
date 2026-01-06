package com.example.rojgar.repository

import com.example.rojgar.model.FeedbackModel
import kotlinx.coroutines.flow.Flow

interface FeedbackRepo {
    suspend fun sendFeedback(feedback: FeedbackModel): Result<String>
    fun getAllFeedbacks(): Flow<List<FeedbackModel>>
    fun getFeedbacksByCompany(companyId: String): Flow<List<FeedbackModel>>
}