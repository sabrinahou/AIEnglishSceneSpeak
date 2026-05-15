package com.example.sceneenglish.data.repository

import com.example.sceneenglish.data.ai.AiClient
import com.example.sceneenglish.data.local.LocalFileStore
import com.example.sceneenglish.domain.model.ImageLabel
import com.example.sceneenglish.domain.model.LearningPack
import com.example.sceneenglish.domain.model.VocabularyItem
import java.io.File

data class ImageSceneSpec(
    val id: String,
    val title: String,
    val labels: List<ImageLabel>
)

data class ModuleImageSpec(
    val id: String,
    val title: String,
    val description: String,
    val visualFocus: String
)

class ImageRepository(
    private val aiClient: AiClient,
    private val fileStore: LocalFileStore
) {
    fun getSceneImageFile(packId: String): File? {
        return fileStore.imageFile(packId, "scene_1").takeIf { it.exists() }
            ?: fileStore.imageFile(packId).takeIf { it.exists() }
    }

    fun getSceneImageFile(packId: String, sceneId: String): File? {
        return fileStore.imageFile(packId, sceneId).takeIf { it.exists() }
    }

    fun getVocabularyImageFile(packId: String, wordId: String): File? {
        return fileStore.vocabularyImageFile(packId, wordId).takeIf { it.exists() }
    }

    fun getModuleImageFile(packId: String, moduleId: String): File? {
        return fileStore.moduleImageFile(packId, moduleId).takeIf { it.exists() }
    }

    suspend fun getOrCreateSceneImage(pack: LearningPack): File {
        val spec = sceneSpecs(pack).first()
        return getOrCreateSceneImage(pack, spec, force = false)
    }

    suspend fun getOrCreateSceneImage(pack: LearningPack, spec: ImageSceneSpec, force: Boolean): File {
        if (!force) {
            getSceneImageFile(pack.id, spec.id)?.let { return it }
        }
        val labels = spec.labels.joinToString(", ") { "${it.english} (${it.chinese})" }
        val prompt = """
            Create a polished animated cartoon educational scene illustration for language learning.

            Scenario: ${pack.scenarioTitle}
            Description: ${pack.scenarioDescription}
            Scene focus: ${spec.title}

            Style:
            - Premium 2D animated cartoon style, like a modern educational animation still.
            - Friendly, clear, expressive characters, but not childish or toy-like.
            - Clean shapes, crisp outlines, warm colors, professional learning-app quality.
            - Accurate tennis court, correct racket shapes, visible tennis balls, net, court lines, and believable coach/student body poses.
            - Character actions must be easy to understand at a glance.

            Layout requirements:
            - Include the important visible elements listed below for this scene, but keep the scene uncluttered.
            - Place people and objects with generous empty space around them so the app can overlay clickable English labels.
            - Keep label-friendly empty zones near the top-left, top-right, lower-left, and lower-right.
            - Avoid placing all important objects in the center; spread them across the image.
            - Show only the elements that belong in this scene group; do not cram every tennis concept into one image.
            - Use a square composition.

            Absolute text ban:
            - The image must contain NO text of any kind.
            - No English words, no Chinese words, no phonetics, no numbers, no letters, no captions, no labels, no signs, no logo, no watermark.
            - Do not draw blank signboards, posters, speech bubbles, scoreboards, or any surface that would naturally contain writing.
            - The app will overlay all clickable English labels after image generation.

            Important visible elements: $labels

            Base image prompt:
            ${pack.imagePrompt}
        """.trimIndent()
        val bytes = aiClient.generateImage(prompt)
        return fileStore.saveImage(pack.id, spec.id, bytes)
    }

    suspend fun getOrCreateVocabularyImage(pack: LearningPack, item: VocabularyItem, force: Boolean): File {
        if (!force) {
            getVocabularyImageFile(pack.id, item.id)?.let { return it }
        }
        val prompt = """
            Create one precise educational flashcard image for this English vocabulary item.

            Word: ${item.english}
            Chinese meaning: ${item.chinese}
            Part of speech: ${item.partOfSpeech}
            Scenario: ${pack.scenarioTitle}
            Usage note: ${item.usageNoteZh}
            Example sentence: ${item.exampleEn}

            Requirements:
            - Show the actual object, person, place, action, or concept represented by the word.
            - Make this image visually distinct from other vocabulary cards in the same scenario.
            - Use a polished semi-realistic educational illustration style for adult learners.
            - If the word is an action, show a person doing the action clearly.
            - If the word is abstract, show a concrete real-life scene that makes the meaning obvious.
            - Center the subject with a clean background and strong silhouette.
            - Square composition, no border frame, no UI elements.
            - No text of any kind: no English, no Chinese, no letters, no phonetics, no captions, no labels, no numbers, no logos, no watermarks.
            - Avoid childish clipart, generic icons, random colored shapes, and repeated abstract graphics.
        """.trimIndent()
        val bytes = aiClient.generateImage(prompt)
        return fileStore.saveVocabularyImage(pack.id, item.id, bytes)
    }

    fun moduleSpecs(pack: LearningPack): List<ModuleImageSpec> = listOf(
        ModuleImageSpec(
            id = "vocabulary",
            title = "单词背诵",
            description = "图片联想、发音、认识和复习队列",
            visualFocus = "a focused learner reviewing illustrated vocabulary flashcards with small object images and audio waves"
        ),
        ModuleImageSpec(
            id = "phrases",
            title = "常用短语",
            description = "短语搭配、例句发音、场景用法",
            visualFocus = "short phrase cards connected to a real-life scene, showing useful spoken chunks without readable text"
        ),
        ModuleImageSpec(
            id = "sentences",
            title = "实用句子",
            description = "先听后看、逐词解释、整句跟读",
            visualFocus = "a learner listening to a complete spoken sentence and following structured meaning blocks"
        ),
        ModuleImageSpec(
            id = "dialogues",
            title = "真实对话",
            description = "角色分句听、隐藏中文练理解",
            visualFocus = "two people practicing a realistic conversation with turn-taking speech bubbles that contain no text"
        ),
        ModuleImageSpec(
            id = "image",
            title = "场景图片",
            description = "看图点词，建立物体和英文连接",
            visualFocus = "an interactive scene image with clear objects and invisible tappable learning points, no labels"
        ),
        ModuleImageSpec(
            id = "translate",
            title = "中译英练习",
            description = "主动回忆、AI 纠错、更自然表达",
            visualFocus = "a learner writing an English answer from a Chinese prompt with correction marks and learning feedback, no readable text"
        ),
        ModuleImageSpec(
            id = "roleplay",
            title = "角色扮演",
            description = "模拟真实交流，边说边纠错",
            visualFocus = "a learner roleplaying with a coach or service person in the scenario, practicing spoken English"
        )
    )

    suspend fun getOrCreateModuleImage(pack: LearningPack, spec: ModuleImageSpec, force: Boolean): File {
        if (!force) {
            getModuleImageFile(pack.id, spec.id)?.let { return it }
        }
        val prompt = """
            Create one premium educational illustration for a feature module in an English learning app.

            App scenario: ${pack.scenarioTitle}
            Scenario description: ${pack.scenarioDescription}
            Feature module: ${spec.title}
            Feature purpose: ${spec.description}
            Visual focus: ${spec.visualFocus}

            Requirements:
            - Make the picture clearly communicate this specific feature, not a generic app icon.
            - Use a polished semi-realistic editorial illustration style for adult learners.
            - Make it visually distinct from the other feature modules.
            - Square composition, clear subject, clean background, warm but professional.
            - Include visual hints of the scenario when useful, such as tennis court, coach, racket, learner, or practice cards.
            - Do not include UI screenshots, phones, app buttons, or readable interface text.
            - No text of any kind: no English, no Chinese, no letters, no numbers, no captions, no labels, no logos, no watermarks.
            - Avoid childish clipart, abstract colored shapes, and repeated generic icons.
        """.trimIndent()
        val bytes = aiClient.generateImage(prompt)
        return fileStore.saveModuleImage(pack.id, spec.id, bytes)
    }

    fun sceneSpecs(pack: LearningPack): List<ImageSceneSpec> {
        val labels = buildOverlayLabels(pack)
        val chunks = labels.chunked(6)
        return chunks.mapIndexed { index, chunk ->
            ImageSceneSpec(
                id = "scene_${index + 1}",
                title = if (chunks.size == 1) "完整场景" else "场景 ${index + 1}",
                labels = chunk
            )
        }
    }

    private fun buildOverlayLabels(pack: LearningPack): List<ImageLabel> {
        val imageByEnglish = pack.imageLabels.associateBy { it.english.lowercase() }
        val vocabLabels = pack.vocabulary.map { item ->
            imageByEnglish[item.english.lowercase()] ?: ImageLabel(
                id = "vocab_label_${item.id}",
                english = item.english,
                chinese = item.chinese,
                labelType = "vocabulary",
                descriptionZh = item.usageNoteZh,
                relatedItemId = item.id,
                x = null,
                y = null
            )
        }
        return (pack.imageLabels + vocabLabels)
            .distinctBy { it.english.lowercase() }
            .take(32)
    }
}
