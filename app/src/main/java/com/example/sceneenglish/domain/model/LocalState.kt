package com.example.sceneenglish.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LocalPackState(
    val packId: String,
    val favorites: List<FavoriteItem> = emptyList(),
    val progress: LearningProgress = LearningProgress(),
    val audioCache: Map<String, AudioCacheItem> = emptyMap(),
    val image: ImageState? = null
)

@Serializable
data class FavoriteItem(
    val itemType: String,
    val itemId: String,
    val createdAt: String
)

@Serializable
data class LearningProgress(
    val learnedItemIds: List<String> = emptyList(),
    val needsReviewItemIds: List<String> = emptyList(),
    val practiceCount: Int = 0,
    val lastPracticedAt: String? = null
)

@Serializable
data class AudioCacheItem(
    val text: String,
    val speed: String,
    val filePath: String,
    val createdAt: String
)

@Serializable
data class ImageState(
    val status: String,
    val filePath: String? = null
)

@Serializable
data class AppSettings(
    val defaultLevel: String = "A1-A2",
    val targetAccent: String = "american",
    val defaultVoice: String = "american_female_1",
    val pronunciationMode: String = "system",
    val defaultSpeed: String = "normal",
    val showChineseByDefault: Boolean = true,
    val autoGenerateAudio: Boolean = false,
    val autoGenerateImage: Boolean = false
)
