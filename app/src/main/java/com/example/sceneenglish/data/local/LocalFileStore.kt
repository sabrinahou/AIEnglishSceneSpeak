package com.example.sceneenglish.data.local

import android.content.Context
import com.example.sceneenglish.domain.model.LearningPack
import com.example.sceneenglish.domain.model.LearningPackIndex
import com.example.sceneenglish.domain.model.LearningPackSummary
import com.example.sceneenglish.domain.model.LocalPackState
import com.example.sceneenglish.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class LocalFileStore(
    context: Context,
    private val json: Json
) {
    private val rootDir = File(context.filesDir, "learning_packs")
    private val indexFile = File(rootDir, "index.json")

    suspend fun saveLearningPack(pack: LearningPack): Unit = withContext(Dispatchers.IO) {
        val folder = packFolder(pack.id)
        folder.mkdirs()
        File(folder, "learning_pack.json").writeText(json.encodeToString(pack))
        if (!File(folder, "local_state.json").exists()) {
            saveLocalStateInternal(pack.id, LocalPackState(packId = pack.id))
        }
        upsertSummary(pack)
    }

    suspend fun loadLearningPack(packId: String): LearningPack = withContext(Dispatchers.IO) {
        json.decodeFromString<LearningPack>(File(packFolder(packId), "learning_pack.json").readText())
    }

    suspend fun listLearningPacks(): List<LearningPackSummary> = withContext(Dispatchers.IO) {
        readIndex().packs.sortedByDescending { it.createdAt }
    }

    suspend fun deleteLearningPack(packId: String): Unit = withContext(Dispatchers.IO) {
        packFolder(packId).deleteRecursively()
        writeIndex(LearningPackIndex(readIndex().packs.filterNot { it.id == packId }))
    }

    suspend fun saveLocalState(packId: String, state: LocalPackState): Unit = withContext(Dispatchers.IO) {
        saveLocalStateInternal(packId, state)
    }

    suspend fun loadLocalState(packId: String): LocalPackState = withContext(Dispatchers.IO) {
        val file = File(packFolder(packId), "local_state.json")
        if (!file.exists()) return@withContext LocalPackState(packId = packId)
        json.decodeFromString<LocalPackState>(file.readText())
    }

    suspend fun saveAudio(packId: String, audioHash: String, bytes: ByteArray): File = withContext(Dispatchers.IO) {
        val audioDir = File(packFolder(packId), "audio").apply { mkdirs() }
        File(audioDir, "$audioHash.mp3").also { it.writeBytes(bytes) }
    }

    suspend fun getAudioFile(packId: String, audioHash: String): File? = withContext(Dispatchers.IO) {
        File(packFolder(packId), "audio/$audioHash.mp3").takeIf { it.exists() }
    }

    suspend fun saveImage(packId: String, bytes: ByteArray): File = withContext(Dispatchers.IO) {
        saveImageInternal(packId, "main", bytes)
    }

    suspend fun saveImage(packId: String, sceneId: String, bytes: ByteArray): File = withContext(Dispatchers.IO) {
        saveImageInternal(packId, sceneId, bytes)
    }

    private fun saveImageInternal(packId: String, sceneId: String, bytes: ByteArray): File {
        val imageDir = File(packFolder(packId), "images").apply { mkdirs() }
        return File(imageDir, "scene_${sceneId}.png").also { it.writeBytes(bytes) }
    }

    suspend fun saveVocabularyImage(packId: String, wordId: String, bytes: ByteArray): File = withContext(Dispatchers.IO) {
        val imageDir = File(packFolder(packId), "images/words").apply { mkdirs() }
        File(imageDir, "$wordId.png").also { it.writeBytes(bytes) }
    }

    suspend fun saveModuleImage(packId: String, moduleId: String, bytes: ByteArray): File = withContext(Dispatchers.IO) {
        val imageDir = File(packFolder(packId), "images/modules").apply { mkdirs() }
        File(imageDir, "$moduleId.png").also { it.writeBytes(bytes) }
    }

    fun imageFile(packId: String): File = File(packFolder(packId), "images/scene_main.png")

    fun imageFile(packId: String, sceneId: String): File = File(packFolder(packId), "images/scene_${sceneId}.png")

    fun vocabularyImageFile(packId: String, wordId: String): File = File(packFolder(packId), "images/words/$wordId.png")

    fun moduleImageFile(packId: String, moduleId: String): File = File(packFolder(packId), "images/modules/$moduleId.png")

    private fun packFolder(packId: String): File = File(rootDir, packId)

    private fun saveLocalStateInternal(packId: String, state: LocalPackState) {
        val folder = packFolder(packId).apply { mkdirs() }
        File(folder, "local_state.json").writeText(json.encodeToString(state))
    }

    private fun upsertSummary(pack: LearningPack) {
        rootDir.mkdirs()
        val index = readIndex()
        val now = DateTimeUtils.nowIso()
        val summary = LearningPackSummary(
            id = pack.id,
            title = pack.scenarioTitle,
            description = pack.scenarioDescription,
            level = pack.level,
            createdAt = pack.createdAt,
            updatedAt = now,
            folderName = pack.id,
            wordCount = pack.vocabulary.size,
            sentenceCount = pack.sentences.size,
            hasImage = imageFile(pack.id).exists()
        )
        writeIndex(LearningPackIndex(index.packs.filterNot { it.id == pack.id } + summary))
    }

    private fun readIndex(): LearningPackIndex {
        rootDir.mkdirs()
        if (!indexFile.exists()) return LearningPackIndex()
        return runCatching { json.decodeFromString<LearningPackIndex>(indexFile.readText()) }
            .getOrDefault(LearningPackIndex())
    }

    private fun writeIndex(index: LearningPackIndex) {
        rootDir.mkdirs()
        indexFile.writeText(json.encodeToString(index))
    }
}
