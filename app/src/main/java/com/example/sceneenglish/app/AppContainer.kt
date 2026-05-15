package com.example.sceneenglish.app

import android.content.Context
import com.example.sceneenglish.data.ai.AiClient
import com.example.sceneenglish.data.ai.OpenAiClient
import com.example.sceneenglish.data.local.AppSettingsStore
import com.example.sceneenglish.data.local.LocalFileStore
import com.example.sceneenglish.data.local.SecureSettingsStore
import com.example.sceneenglish.data.repository.LearningPackRepository
import com.example.sceneenglish.data.repository.AudioRepository
import com.example.sceneenglish.data.repository.AudioPlayer
import com.example.sceneenglish.data.repository.ImageRepository
import com.example.sceneenglish.data.repository.PracticeRepository
import kotlinx.serialization.json.Json

class AppContainer(context: Context) {
    val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val secureSettingsStore = SecureSettingsStore(context)
    val appSettingsStore = AppSettingsStore(context, json)
    val localFileStore = LocalFileStore(context, json)
    val aiClient: AiClient = OpenAiClient(secureSettingsStore, json)
    val learningPackRepository = LearningPackRepository(aiClient, localFileStore)
    val audioRepository = AudioRepository(aiClient, localFileStore)
    val audioPlayer = AudioPlayer(context)
    val imageRepository = ImageRepository(aiClient, localFileStore)
    val practiceRepository = PracticeRepository(aiClient)
}
