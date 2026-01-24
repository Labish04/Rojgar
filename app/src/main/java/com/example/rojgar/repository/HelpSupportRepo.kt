// file name: HelpSupportRepo.kt
package com.example.rojgar.repository

import android.content.Context
import android.net.Uri
import com.example.rojgar.model.HelpSupportModel

interface HelpSupportRepo {

    fun submitHelpRequest(
        model: HelpSupportModel,
        callback: (Boolean, String) -> Unit
    )

    fun uploadScreenshot(
        context: Context,
        imageUri: Uri,
        userId: String,
        callback: (String?) -> Unit
    )

    fun getUserHelpRequests(
        userId: String,
        callback: (Boolean, String, List<HelpSupportModel>?) -> Unit
    )

    fun getHelpRequestById(
        requestId: String,
        callback: (Boolean, String, HelpSupportModel?) -> Unit
    )

    fun getAllHelpRequests(
        callback: (Boolean, String, List<HelpSupportModel>?) -> Unit
    )

    fun updateRequestStatus(
        requestId: String,
        status: String,
        adminNotes: String,
        resolvedBy: String,
        callback: (Boolean, String) -> Unit
    )
}