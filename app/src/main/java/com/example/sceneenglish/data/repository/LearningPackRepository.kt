package com.example.sceneenglish.data.repository

import com.example.sceneenglish.data.ai.AiClient
import com.example.sceneenglish.data.ai.MockLearningPack
import com.example.sceneenglish.data.local.LocalFileStore
import com.example.sceneenglish.domain.model.GeneratePackRequest
import com.example.sceneenglish.domain.model.LearningPack
import com.example.sceneenglish.domain.model.LearningPackSummary
import com.example.sceneenglish.domain.model.LocalPackState

class LearningPackRepository(
    private val aiClient: AiClient,
    private val fileStore: LocalFileStore
) {
    suspend fun createPack(sourceInput: String, level: String): LearningPack {
        val pack = MockLearningPack.tennis(sourceInput, level)
        fileStore.saveLearningPack(pack)
        return pack
    }

    suspend fun enrichPack(
        pack: LearningPack,
        onUpdated: suspend (LearningPack) -> Unit
    ): LearningPack {
        var current = pack

        if (current.phrases.isEmpty()) {
            current = current.copy(phrases = aiClient.generatePhrases(current))
            fileStore.saveLearningPack(current)
            onUpdated(current)
        }
        if (current.sentences.isEmpty()) {
            current = current.copy(sentences = aiClient.generateSentences(current))
            fileStore.saveLearningPack(current)
            onUpdated(current)
        }
        if (current.dialogues.isEmpty()) {
            current = current.copy(dialogues = aiClient.generateDialogues(current))
            fileStore.saveLearningPack(current)
            onUpdated(current)
        }
        if (current.roleplayTasks.isEmpty()) {
            current = current.copy(roleplayTasks = aiClient.generateRoleplayTasks(current))
            fileStore.saveLearningPack(current)
            onUpdated(current)
        }
        if (current.reviewQuiz.isEmpty()) {
            current = current.copy(reviewQuiz = aiClient.generateReviewQuiz(current))
            fileStore.saveLearningPack(current)
            onUpdated(current)
        }

        return current
    }

    suspend fun createMockPack(sourceInput: String, level: String): LearningPack {
        val pack = MockLearningPack.tennis(sourceInput, level)
        fileStore.saveLearningPack(pack)
        return pack
    }

    suspend fun listPacks(): List<LearningPackSummary> = fileStore.listLearningPacks()

    suspend fun getPack(packId: String): LearningPack = fileStore.loadLearningPack(packId)

    suspend fun getState(packId: String): LocalPackState = fileStore.loadLocalState(packId)

    suspend fun saveState(packId: String, state: LocalPackState) = fileStore.saveLocalState(packId, state)

    suspend fun deletePack(packId: String) = fileStore.deleteLearningPack(packId)
}
