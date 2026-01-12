package com.example.rojgar.repository

import com.example.rojgar.model.HelpAndSupportModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

class HelpAndSupportRepoImpl : HelpAndSupportRepo {

    // Simulated database - replace with actual Firebase/Room implementation
    private val supportRequests = mutableListOf<HelpAndSupportModel>()

    override suspend fun submitSupportRequest(request: HelpAndSupportModel): Result<String> {
        return try {
            delay(1000) // Simulate network delay
            val requestWithId = request.copy(id = generateRequestId())
            supportRequests.add(requestWithId)
            Result.success(requestWithId.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSupportRequests(companyEmail: String): Flow<List<HelpAndSupportModel>> {
        return flow {
            delay(500)
            emit(supportRequests.filter { it.companyEmail == companyEmail })
        }
    }

    override suspend fun updateRequestStatus(requestId: String, status: String): Result<Boolean> {
        return try {
            delay(500)
            val index = supportRequests.indexOfFirst { it.id == requestId }
            if (index != -1) {
                // Update status logic here
                Result.success(true)
            } else {
                Result.failure(Exception("Request not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSupportRequest(requestId: String): Result<Boolean> {
        return try {
            delay(500)
            val removed = supportRequests.removeIf { it.id == requestId }
            Result.success(removed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateRequestId(): String {
        return "REQ${System.currentTimeMillis()}"
    }
}