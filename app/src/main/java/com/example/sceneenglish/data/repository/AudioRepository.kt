package com.example.sceneenglish.data.repository

import com.example.sceneenglish.data.ai.AiClient
import com.example.sceneenglish.data.local.LocalFileStore
import com.example.sceneenglish.domain.model.SpeechSpeed
import com.example.sceneenglish.util.HashUtils
import java.io.File

class AudioRepository(
    private val aiClient: AiClient,
    private val fileStore: LocalFileStore
) {
    suspend fun getOrCreateAudio(
        packId: String,
        text: String,
        speed: SpeechSpeed,
        voice: String = "american_female_1",
        accent: String = "american"
    ): File {
        val hash = HashUtils.sha256("${text.trim()}|$voice|${speed.name}|$accent")
        fileStore.getAudioFile(packId, hash)?.let { return it }
        val bytes = aiClient.generateSpeech(text, speed)
        return fileStore.saveAudio(packId, hash, bytes)
    }
}
