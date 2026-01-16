package com.example.rojgar.repository

import com.example.rojgar.model.HelpAndSupportModel
import kotlinx.coroutines.flow.Flow

interface HelpAndSupportRepo {
    suspend fun submitSupportRequest(request: HelpAndSupportModel): Result<String>
    suspend fun getSupportRequests(companyEmail: String): Flow<List<HelpAndSupportModel>>
    suspend fun updateRequestStatus(requestId: String, status: String): Result<Boolean>
    suspend fun deleteSupportRequest(requestId: String): Result<Boolean>
}