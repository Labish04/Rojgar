package com.example.rojgar

import android.app.Application
import com.example.rojgar.utils.ZegoKey.APP_ID
import com.example.rojgar.utils.ZegoKey.APP_SIGN
import com.zegocloud.zimkit.services.ZIMKit

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        ZIMKit.initWith(this, APP_ID, APP_SIGN)
    }
}