package com.example.rojgar.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class RememberMeManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "rojgar_remember_me_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_EMAIL = "saved_email"
        private const val KEY_PASSWORD = "saved_password"
    }

    fun saveCredentials(email: String, password: String, rememberMe: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            if (rememberMe) {
                putString(KEY_EMAIL, email)
                putString(KEY_PASSWORD, password)
            } else {
                remove(KEY_EMAIL)
                remove(KEY_PASSWORD)
            }
            apply()
        }
    }

    fun getSavedEmail(): String? {
        return if (isRememberMeEnabled()) {
            sharedPreferences.getString(KEY_EMAIL, null)
        } else null
    }

    fun getSavedPassword(): String? {
        return if (isRememberMeEnabled()) {
            sharedPreferences.getString(KEY_PASSWORD, null)
        } else null
    }

    fun isRememberMeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun clearCredentials() {
        sharedPreferences.edit().apply {
            remove(KEY_REMEMBER_ME)
            remove(KEY_EMAIL)
            remove(KEY_PASSWORD)
            apply()
        }
    }
}