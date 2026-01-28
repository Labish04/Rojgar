package com.example.rojgar

import android.app.Application
import android.util.Log
import com.example.rojgar.utils.ZegoCloudConstants
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService

class RojgarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize ZEGO UIKit Call Service
        try {
            // TODO: Initialize ZEGO UIKit Prebuilt Call Service with correct API
            // Check ZEGO documentation for the correct initialization method
            // com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService.init(...)
            Log.d("RojgarApplication", "ZEGO UIKit Call Service initialization placeholder - check ZEGO docs")
        } catch (e: Exception) {
            Log.e("RojgarApplication", "Failed to initialize ZEGO UIKit Call Service", e)
        }

        // Initialize CallInvitationManager
        com.example.rojgar.utils.CallInvitationManager.initialize(this)
    }
}