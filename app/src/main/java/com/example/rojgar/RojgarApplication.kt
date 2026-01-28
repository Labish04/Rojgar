package com.example.rojgar

import android.app.Application
import android.util.Log
import com.example.rojgar.utils.CallInvitationManager
import com.example.rojgar.utils.ZegoCloudConstants
import com.google.firebase.FirebaseApp
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig

class RojgarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize Firebase
            if (!FirebaseApp.getApps(this).isEmpty()) {
                Log.d("RojgarApplication", "Firebase already initialized")
            }
            
            // Initialize CallInvitationManager for handling call invitations
            CallInvitationManager.initialize(this)
            Log.d("RojgarApplication", "CallInvitationManager initialized successfully")

            // Initialize ZEGO UIKit Prebuilt Call Service
            try {
                // Initialize ZEGO service with app configuration
                val invitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
                ZegoUIKitPrebuiltCallService.init(
                    application = this,
                    appID = ZegoCloudConstants.APP_ID,
                    appSign = ZegoCloudConstants.APP_SIGN,
                    userID = "", // Will be set per call
                    userName = "", // Will be set per call
                    invitationConfig = invitationConfig
                )
                Log.d("RojgarApplication", "ZEGO UIKit Prebuilt Call Service initialized successfully")
            } catch (e: Exception) {
                Log.e("RojgarApplication", "Error initializing ZEGO service: ${e.message}", e)
            }

        } catch (e: Exception) {
            Log.e("RojgarApplication", "Error during application initialization", e)
        }

        Log.d("RojgarApplication", "Application initialized successfully")
    }
}