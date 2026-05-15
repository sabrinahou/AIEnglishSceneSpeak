package com.example.sceneenglish.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EvaluationResult(
    val isCorrect: Boolean,
    val meaningScore: Int,
    val grammarScore: Int,
    val naturalnessScore: Int,
    val correctedAnswer: String,
    val naturalAnswer: String,
    val feedbackZh: String
)

@Serializable
data class RoleplayResult(
    val feedback: RoleplayFeedback,
    val nextMessage: DialogueTurn
)

@Serializable
data class RoleplayFeedback(
    val correctedAnswer: String,
    val naturalAnswer: String,
    val feedbackZh: String
)
