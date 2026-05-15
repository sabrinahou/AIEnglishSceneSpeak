package com.example.sceneenglish.data.ai

import com.example.sceneenglish.domain.model.EvaluateRequest
import com.example.sceneenglish.domain.model.EvaluationResult
import com.example.sceneenglish.domain.model.GeneratePackRequest
import com.example.sceneenglish.domain.model.Dialogue
import com.example.sceneenglish.domain.model.LearningPack
import com.example.sceneenglish.domain.model.PhraseItem
import com.example.sceneenglish.domain.model.ReviewQuizItem
import com.example.sceneenglish.domain.model.RoleplayTask
import com.example.sceneenglish.domain.model.RoleplayRequest
import com.example.sceneenglish.domain.model.RoleplayResult
import com.example.sceneenglish.domain.model.SentenceItem
import com.example.sceneenglish.domain.model.SpeechSpeed

interface AiClient {
    suspend fun generateLearningPack(request: GeneratePackRequest): LearningPack
    suspend fun generateInitialLearningPack(request: GeneratePackRequest): LearningPack
    suspend fun generatePhrases(pack: LearningPack): List<PhraseItem>
    suspend fun generateSentences(pack: LearningPack): List<SentenceItem>
    suspend fun generateDialogues(pack: LearningPack): List<Dialogue>
    suspend fun generateRoleplayTasks(pack: LearningPack): List<RoleplayTask>
    suspend fun generateReviewQuiz(pack: LearningPack): List<ReviewQuizItem>
    suspend fun generateSpeech(text: String, speed: SpeechSpeed): ByteArray
    suspend fun generateImage(prompt: String): ByteArray
    suspend fun transcribeAudio(audioBytes: ByteArray): String
    suspend fun evaluateTranslation(request: EvaluateRequest): EvaluationResult
    suspend fun nextRoleplayTurn(request: RoleplayRequest): RoleplayResult
}
