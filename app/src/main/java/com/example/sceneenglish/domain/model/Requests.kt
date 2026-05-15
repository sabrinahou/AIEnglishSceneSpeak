package com.example.sceneenglish.domain.model

data class GeneratePackRequest(
    val sourceInput: String,
    val level: String
)

data class EvaluateRequest(
    val scenarioTitle: String,
    val promptZh: String,
    val expectedAnswer: String,
    val userAnswer: String
)

data class RoleplayRequest(
    val scenarioTitle: String,
    val userRole: String,
    val assistantRole: String,
    val level: String,
    val history: List<DialogueTurn>,
    val userAnswer: String
)

enum class SpeechSpeed {
    Normal,
    Slow
}
