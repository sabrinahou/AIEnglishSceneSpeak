package com.example.sceneenglish.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LearningPack(
    val id: String,
    val scenarioTitle: String,
    val scenarioDescription: String,
    val sourceInput: String,
    val level: String,
    val targetAccent: String,
    val createdAt: String,
    val vocabulary: List<VocabularyItem>,
    val phrases: List<PhraseItem>,
    val sentences: List<SentenceItem>,
    val dialogues: List<Dialogue>,
    val imagePrompt: String,
    val imageLabels: List<ImageLabel>,
    val roleplayTasks: List<RoleplayTask>,
    val reviewQuiz: List<ReviewQuizItem>
)

@Serializable
data class VocabularyItem(
    val id: String,
    val english: String,
    val chinese: String,
    val partOfSpeech: String,
    val usageNoteZh: String,
    val exampleEn: String,
    val exampleZh: String,
    val phonetic: String = "",
    val categoryZh: String = "核心词汇",
    val priority: Int = 2
)

@Serializable
data class PhraseItem(
    val id: String,
    val english: String,
    val chinese: String,
    val usageNoteZh: String,
    val exampleEn: String,
    val exampleZh: String,
    val categoryZh: String = "常用短语",
    val priority: Int = 2
)

@Serializable
data class SentenceItem(
    val id: String,
    val english: String,
    val chinese: String,
    val wordByWord: List<WordMeaning>,
    val usageContextZh: String,
    val categoryZh: String = "常用句子",
    val priority: Int = 2
)

@Serializable
data class WordMeaning(
    val text: String,
    val meaningZh: String
)

@Serializable
data class Dialogue(
    val id: String,
    val title: String,
    val descriptionZh: String,
    val turns: List<DialogueTurn>
)

@Serializable
data class DialogueTurn(
    val id: String,
    val speaker: String,
    val english: String,
    val chinese: String
)

@Serializable
data class ImageLabel(
    val id: String,
    val english: String,
    val chinese: String,
    val labelType: String,
    val descriptionZh: String,
    val relatedItemId: String? = null,
    val x: Float? = null,
    val y: Float? = null
)

@Serializable
data class RoleplayTask(
    val id: String,
    val userRole: String,
    val assistantRole: String,
    val starterEnglish: String,
    val starterChinese: String
)

@Serializable
data class ReviewQuizItem(
    val id: String,
    val promptZh: String,
    val expectedAnswer: String
)

@Serializable
data class LearningPackSummary(
    val id: String,
    val title: String,
    val description: String,
    val level: String,
    val createdAt: String,
    val updatedAt: String,
    val folderName: String,
    val wordCount: Int,
    val sentenceCount: Int,
    val hasImage: Boolean
)

@Serializable
data class LearningPackIndex(
    val packs: List<LearningPackSummary> = emptyList()
)
