package com.example.sceneenglish.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureSettingsStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        "secure_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(apiKey: String) {
        preferences.edit().putString(KEY_API_KEY, apiKey.trim()).apply()
    }

    fun getApiKey(): String? = preferences.getString(KEY_API_KEY, null)

    fun hasApiKey(): Boolean = !getApiKey().isNullOrBlank()

    fun clearApiKey() {
        preferences.edit().remove(KEY_API_KEY).apply()
    }

    private companion object {
        const val KEY_API_KEY = "api_key"
    }
}
