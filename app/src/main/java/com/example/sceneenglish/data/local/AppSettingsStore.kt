package com.example.sceneenglish.data.local

import android.content.Context
import com.example.sceneenglish.domain.model.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class AppSettingsStore(
    context: Context,
    private val json: Json
) {
    private val settingsFile = File(context.filesDir, "settings/app_settings.json")

    suspend fun load(): AppSettings = withContext(Dispatchers.IO) {
        if (!settingsFile.exists()) return@withContext AppSettings()
        runCatching {
            json.decodeFromString<AppSettings>(settingsFile.readText())
        }.getOrDefault(AppSettings())
    }

    suspend fun save(settings: AppSettings) = withContext(Dispatchers.IO) {
        settingsFile.parentFile?.mkdirs()
        settingsFile.writeText(json.encodeToString(settings))
    }
}
