package com.example.sceneenglish.data.ai

import com.example.sceneenglish.domain.model.DialogueTurn
import com.example.sceneenglish.domain.model.Dialogue
import com.example.sceneenglish.domain.model.EvaluateRequest
import com.example.sceneenglish.domain.model.EvaluationResult
import com.example.sceneenglish.domain.model.GeneratePackRequest
import com.example.sceneenglish.domain.model.LearningPack
import com.example.sceneenglish.domain.model.PhraseItem
import com.example.sceneenglish.domain.model.ReviewQuizItem
import com.example.sceneenglish.domain.model.RoleplayFeedback
import com.example.sceneenglish.domain.model.RoleplayRequest
import com.example.sceneenglish.domain.model.RoleplayResult
import com.example.sceneenglish.domain.model.RoleplayTask
import com.example.sceneenglish.domain.model.SentenceItem
import com.example.sceneenglish.domain.model.SpeechSpeed

class MockAiClient : AiClient {
    override suspend fun generateLearningPack(request: GeneratePackRequest): LearningPack {
        return MockLearningPack.tennis(request.sourceInput, request.level)
    }

    override suspend fun generateInitialLearningPack(request: GeneratePackRequest): LearningPack {
        return MockLearningPack.tennis(request.sourceInput, request.level).copy(
            phrases = emptyList(),
            sentences = emptyList(),
            dialogues = emptyList(),
            roleplayTasks = emptyList(),
            reviewQuiz = emptyList()
        )
    }

    override suspend fun generatePhrases(pack: LearningPack): List<PhraseItem> {
        return MockLearningPack.tennis(pack.sourceInput, pack.level).phrases
    }

    override suspend fun generateSentences(pack: LearningPack): List<SentenceItem> {
        return MockLearningPack.tennis(pack.sourceInput, pack.level).sentences
    }

    override suspend fun generateDialogues(pack: LearningPack): List<Dialogue> {
        return MockLearningPack.tennis(pack.sourceInput, pack.level).dialogues
    }

    override suspend fun generateRoleplayTasks(pack: LearningPack): List<RoleplayTask> {
        return MockLearningPack.tennis(pack.sourceInput, pack.level).roleplayTasks
    }

    override suspend fun generateReviewQuiz(pack: LearningPack): List<ReviewQuizItem> {
        return MockLearningPack.tennis(pack.sourceInput, pack.level).reviewQuiz
    }

    override suspend fun generateSpeech(text: String, speed: SpeechSpeed): ByteArray {
        return ByteArray(0)
    }

    override suspend fun generateImage(prompt: String): ByteArray {
        return ByteArray(0)
    }

    override suspend fun transcribeAudio(audioBytes: ByteArray): String = ""

    override suspend fun evaluateTranslation(request: EvaluateRequest): EvaluationResult {
        return EvaluationResult(
            isCorrect = request.userAnswer.trim().equals(request.expectedAnswer, ignoreCase = true),
            meaningScore = 80,
            grammarScore = 65,
            naturalnessScore = 70,
            correctedAnswer = request.expectedAnswer,
            naturalAnswer = "I'd like to work on my forehand today.",
            feedbackZh = "意思基本能表达。注意 want 后面要接 to practice，forehand 前面通常加 my。"
        )
    }

    override suspend fun nextRoleplayTurn(request: RoleplayRequest): RoleplayResult {
        return RoleplayResult(
            feedback = RoleplayFeedback(
                correctedAnswer = "I want to practice my serve.",
                naturalAnswer = "I'd like to work on my serve today.",
                feedbackZh = "want 后面要用 to practice。说自己的发球时，用 my serve 更自然。"
            ),
            nextMessage = DialogueTurn(
                id = "roleplay_next",
                speaker = request.assistantRole,
                english = "Good. Let's start with your grip.",
                chinese = "很好。我们先从你的握拍开始。"
            )
        )
    }
}
