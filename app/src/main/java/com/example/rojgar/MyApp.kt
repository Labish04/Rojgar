package com.example.rojgar

import android.app.Application
import android.util.Log
import com.example.rojgar.utils.ZegoKey.APP_ID
import com.example.rojgar.utils.ZegoKey.APP_SIGN
import com.zegocloud.zimkit.services.ZIMKit

class MyApp: Application() {

    companion object {
        private const val TAG = "MyApp"
    }

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize ZIMKit
            ZIMKit.initWith(this, APP_ID, APP_SIGN)

            // Optional: Set ZIMKit configurations
            // ZIMKit.setAdvancedConfig("token", yourTokenValue)

            Log.d(TAG, "ZIMKit initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ZIMKit: ${e.message}", e)
        }
    }
}