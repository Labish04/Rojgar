//package com.example.rojgar.repository
//
//import com.example.rojgar.model.HelpAndSupportModel
//import com.example.rojgar.model.RequestStatus
//import kotlinx.coroutines.flow.Flow
//
//interface HelpAndSupportRepo {
//    // Core operations
//    suspend fun submitSupportRequest(request: HelpAndSupportModel): Result<String>
//    suspend fun getSupportRequests(companyEmail: String): Flow<List<HelpAndSupportModel>>
//    suspend fun updateRequestStatus(requestId: String, status: String): Result<Boolean>
//    suspend fun deleteSupportRequest(requestId: String): Result<Boolean>
//
//    // Additional operations with callbacks
//    fun getSupportRequestById(
//        requestId: String,
//        callback: (Boolean, String, HelpAndSupportModel?) -> Unit
//    )
//
//    fun getAllSupportRequests(
//        callback: (Boolean, String, List<HelpAndSupportModel>?) -> Unit
//    )
//
//    fun getSupportRequestsByStatus(
//        status: RequestStatus,
//        callback: (Boolean, String, List<HelpAndSupportModel>?) -> Unit
//    )
//
//    fun getSupportRequestsByCategory(
//        category: String,
//        callback: (Boolean, String, List<HelpAndSupportModel>?) -> Unit
//    )
//
//    fun updateSupportRequest(
//        requestId: String,
//        updatedRequest: HelpAndSupportModel,
//        callback: (Boolean, String) -> Unit
//    )
//}
