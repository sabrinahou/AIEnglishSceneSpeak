package com.example.sceneenglish.app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sceneenglish.data.local.AppSettingsStore
import com.example.sceneenglish.data.local.SecureSettingsStore
import com.example.sceneenglish.data.repository.AudioPlayer
import com.example.sceneenglish.data.repository.AudioRepository
import com.example.sceneenglish.data.repository.ImageSceneSpec
import com.example.sceneenglish.data.repository.ImageRepository
import com.example.sceneenglish.data.repository.LearningPackRepository
import com.example.sceneenglish.data.repository.ModuleImageSpec
import com.example.sceneenglish.data.repository.PracticeRepository
import com.example.sceneenglish.domain.model.EvaluateRequest
import com.example.sceneenglish.domain.model.EvaluationResult
import com.example.sceneenglish.domain.model.AppSettings
import com.example.sceneenglish.domain.model.LearningPack
import com.example.sceneenglish.domain.model.LearningPackSummary
import com.example.sceneenglish.domain.model.LocalPackState
import com.example.sceneenglish.domain.model.RoleplayRequest
import com.example.sceneenglish.domain.model.RoleplayResult
import com.example.sceneenglish.domain.model.SpeechSpeed
import com.example.sceneenglish.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class AppUiState(
    val hasApiKey: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val packs: List<LearningPackSummary> = emptyList(),
    val currentPack: LearningPack? = null,
    val currentLocalState: LocalPackState? = null,
    val sceneImagePath: String? = null,
    val sceneImagePaths: Map<String, String> = emptyMap(),
    val vocabularyImagePaths: Map<String, String> = emptyMap(),
    val moduleImagePaths: Map<String, String> = emptyMap(),
    val isImageLoading: Boolean = false,
    val wordImageLoadingItemId: String? = null,
    val isGeneratingWordImages: Boolean = false,
    val moduleImageLoadingId: String? = null,
    val isGeneratingModuleImages: Boolean = false,
    val audioLoadingItemId: String? = null,
    val audioStatus: String? = null,
    val appSettings: AppSettings = AppSettings(),
    val isEnrichingPack: Boolean = false,
    val enrichmentStatus: String? = null,
    val evaluation: EvaluationResult? = null,
    val roleplayResult: RoleplayResult? = null
)

class AppViewModel(
    private val secureSettingsStore: SecureSettingsStore,
    private val appSettingsStore: AppSettingsStore,
    private val learningPackRepository: LearningPackRepository,
    private val audioRepository: AudioRepository,
    private val audioPlayer: AudioPlayer,
    private val imageRepository: ImageRepository,
    private val practiceRepository: PracticeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState(hasApiKey = secureSettingsStore.hasApiKey()))
    val uiState: StateFlow<AppUiState> = _uiState

    init {
        loadSettings()
        refreshPacks()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            runCatching { appSettingsStore.load() }
                .onSuccess { settings -> _uiState.update { it.copy(appSettings = settings) } }
        }
    }

    fun setPronunciationMode(mode: String) {
        viewModelScope.launch {
            val updated = _uiState.value.appSettings.copy(pronunciationMode = mode)
            _uiState.update { it.copy(appSettings = updated) }
            appSettingsStore.save(updated)
        }
    }

    fun saveApiKey(apiKey: String) {
        secureSettingsStore.saveApiKey(apiKey)
        _uiState.update { it.copy(hasApiKey = true) }
    }

    fun clearApiKey() {
        secureSettingsStore.clearApiKey()
        _uiState.update { it.copy(hasApiKey = false) }
    }

    fun refreshPacks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { learningPackRepository.listPacks() }
                .onSuccess { packs -> _uiState.update { it.copy(packs = packs, isLoading = false) } }
                .onFailure { error -> _uiState.update { it.copy(error = error.message, isLoading = false) } }
        }
    }

    fun createPack(sourceInput: String, level: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { withTimeout(GENERATION_TIMEOUT_MS) { learningPackRepository.createPack(sourceInput, level) } }
                .onSuccess { pack ->
                    _uiState.update {
                        it.copy(
                            currentPack = pack,
                            currentLocalState = LocalPackState(packId = pack.id),
                            sceneImagePath = imageRepository.getSceneImageFile(pack.id)?.absolutePath,
                            sceneImagePaths = sceneImagePaths(pack),
                            vocabularyImagePaths = vocabularyImagePaths(pack),
                            moduleImagePaths = moduleImagePaths(pack),
                            isLoading = false
                        )
                    }
                    refreshPacks()
                    loadLocalState(pack.id)
                    onCreated(pack.id)
                    enrichPackInBackground(pack)
                }
                .onFailure { error -> _uiState.update { it.copy(error = friendlyMessage(error), isLoading = false) } }
        }
    }

    fun createMockPack(sourceInput: String, level: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { learningPackRepository.createMockPack(sourceInput, level) }
                .onSuccess { pack ->
                    _uiState.update {
                        it.copy(
                            currentPack = pack,
                            currentLocalState = LocalPackState(packId = pack.id),
                            sceneImagePath = imageRepository.getSceneImageFile(pack.id)?.absolutePath,
                            sceneImagePaths = sceneImagePaths(pack),
                            vocabularyImagePaths = vocabularyImagePaths(pack),
                            moduleImagePaths = moduleImagePaths(pack),
                            isLoading = false
                        )
                    }
                    refreshPacks()
                    loadLocalState(pack.id)
                    onCreated(pack.id)
                }
                .onFailure { error -> _uiState.update { it.copy(error = friendlyMessage(error), isLoading = false) } }
        }
    }

    fun loadPack(packId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { learningPackRepository.getPack(packId) }
                .onSuccess { pack ->
                    _uiState.update {
                        it.copy(
                            currentPack = pack,
                            currentLocalState = LocalPackState(packId = pack.id),
                            sceneImagePath = imageRepository.getSceneImageFile(pack.id)?.absolutePath,
                            sceneImagePaths = sceneImagePaths(pack),
                            vocabularyImagePaths = vocabularyImagePaths(pack),
                            moduleImagePaths = moduleImagePaths(pack),
                            isLoading = false
                        )
                    }
                    loadLocalState(pack.id)
                }
                .onFailure { error -> _uiState.update { it.copy(error = error.message, isLoading = false) } }
        }
    }

    fun deletePack(packId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { learningPackRepository.deletePack(packId) }
                .onSuccess {
                    val current = _uiState.value.currentPack
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentLocalState = current?.takeIf { pack -> pack.id != packId }?.let { _uiState.value.currentLocalState },
                            currentPack = current?.takeIf { pack -> pack.id != packId }
                        )
                    }
                    refreshPacks()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = friendlyMessage(error)) }
                }
        }
    }

    private fun loadLocalState(packId: String) {
        viewModelScope.launch {
            runCatching { learningPackRepository.getState(packId) }
                .onSuccess { localState ->
                    _uiState.update { state ->
                        if (state.currentPack?.id == packId) {
                            state.copy(currentLocalState = localState)
                        } else {
                            state
                        }
                    }
                }
        }
    }

    fun enrichCurrentPack() {
        val pack = _uiState.value.currentPack ?: return
        enrichPackInBackground(pack)
    }

    private fun enrichPackInBackground(pack: LearningPack) {
        if (_uiState.value.isEnrichingPack) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isEnrichingPack = true, enrichmentStatus = "正在后台补充短语、句子和对话...")
            }
            runCatching {
                learningPackRepository.enrichPack(pack) { updated ->
                    _uiState.update { state ->
                        state.copy(
                            currentPack = updated,
                            vocabularyImagePaths = vocabularyImagePaths(updated),
                            moduleImagePaths = moduleImagePaths(updated),
                            enrichmentStatus = enrichmentStatusFor(updated)
                        )
                    }
                    refreshPacks()
                }
            }
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(
                            currentPack = updated,
                            vocabularyImagePaths = vocabularyImagePaths(updated),
                            moduleImagePaths = moduleImagePaths(updated),
                            isEnrichingPack = false,
                            enrichmentStatus = "学习包已补充完成"
                        )
                    }
                    refreshPacks()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isEnrichingPack = false,
                            enrichmentStatus = "网络较慢，已保留当前可学习内容；稍后可继续补充。"
                        )
                    }
                }
        }
    }

    fun refreshSceneImage() {
        val pack = _uiState.value.currentPack ?: return
        _uiState.update {
            it.copy(
                sceneImagePath = imageRepository.getSceneImageFile(pack.id)?.absolutePath,
                sceneImagePaths = sceneImagePaths(pack)
            )
        }
    }

    fun generateSceneImage() {
        val pack = _uiState.value.currentPack ?: return
        val spec = imageRepository.sceneSpecs(pack).first()
        generateSceneImage(spec)
    }

    fun generateSceneImage(spec: ImageSceneSpec) {
        val pack = _uiState.value.currentPack ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isImageLoading = true, error = null) }
            runCatching {
                withTimeout(IMAGE_TIMEOUT_MS) {
                    imageRepository.getOrCreateSceneImage(pack, spec, force = true)
                }
            }
                .onSuccess { file ->
                    _uiState.update {
                        it.copy(
                            sceneImagePath = file.absolutePath,
                            sceneImagePaths = sceneImagePaths(pack),
                            isImageLoading = false
                        )
                    }
                    refreshPacks()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isImageLoading = false, error = friendlyMessage(error))
                    }
                }
        }
    }

    fun generateAllSceneImages() {
        val pack = _uiState.value.currentPack ?: return
        val specs = imageRepository.sceneSpecs(pack)
        viewModelScope.launch {
            _uiState.update { it.copy(isImageLoading = true, error = null) }
            runCatching {
                withTimeout(IMAGE_TIMEOUT_MS * specs.size) {
                    specs.forEach { spec ->
                        imageRepository.getOrCreateSceneImage(pack, spec, force = true)
                    }
                }
            }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            sceneImagePath = imageRepository.getSceneImageFile(pack.id)?.absolutePath,
                            sceneImagePaths = sceneImagePaths(pack),
                            isImageLoading = false
                        )
                    }
                    refreshPacks()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isImageLoading = false, error = friendlyMessage(error))
                    }
                }
        }
    }

    fun imageSceneSpecs(pack: LearningPack): List<ImageSceneSpec> = imageRepository.sceneSpecs(pack)

    fun moduleImageSpecs(pack: LearningPack): List<ModuleImageSpec> = imageRepository.moduleSpecs(pack)

    fun refreshModuleImages() {
        val pack = _uiState.value.currentPack ?: return
        _uiState.update { it.copy(moduleImagePaths = moduleImagePaths(pack)) }
    }

    fun generateModuleImage(moduleId: String, force: Boolean = false) {
        val pack = _uiState.value.currentPack ?: return
        val spec = imageRepository.moduleSpecs(pack).firstOrNull { it.id == moduleId } ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(moduleImageLoadingId = moduleId, error = null) }
            runCatching {
                withTimeout(IMAGE_TIMEOUT_MS) {
                    imageRepository.getOrCreateModuleImage(pack, spec, force)
                }
            }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            moduleImageLoadingId = null,
                            moduleImagePaths = moduleImagePaths(pack)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(moduleImageLoadingId = null, error = friendlyMessage(error))
                    }
                }
        }
    }

    fun generateAllModuleImages(force: Boolean = false) {
        val pack = _uiState.value.currentPack ?: return
        val specs = imageRepository.moduleSpecs(pack)
        viewModelScope.launch {
            _uiState.update {
                it.copy(isGeneratingModuleImages = true, moduleImageLoadingId = null, error = null)
            }
            runCatching {
                withTimeout(IMAGE_TIMEOUT_MS * specs.size.coerceAtLeast(1)) {
                    specs.forEach { spec ->
                        _uiState.update { it.copy(moduleImageLoadingId = spec.id) }
                        imageRepository.getOrCreateModuleImage(pack, spec, force)
                        _uiState.update { it.copy(moduleImagePaths = moduleImagePaths(pack)) }
                    }
                }
            }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isGeneratingModuleImages = false,
                            moduleImageLoadingId = null,
                            moduleImagePaths = moduleImagePaths(pack)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isGeneratingModuleImages = false,
                            moduleImageLoadingId = null,
                            error = friendlyMessage(error)
                        )
                    }
                }
        }
    }

    fun markVocabularyKnown(itemId: String) {
        updateVocabularyProgress(itemId, known = true)
    }

    fun markVocabularyNeedsReview(itemId: String) {
        updateVocabularyProgress(itemId, known = false)
    }

    fun resetVocabularyProgress() {
        val pack = _uiState.value.currentPack ?: return
        val state = _uiState.value.currentLocalState ?: LocalPackState(packId = pack.id)
        val vocabularyIds = pack.vocabulary.map { it.id }.toSet()
        val progress = state.progress.copy(
            learnedItemIds = state.progress.learnedItemIds.filterNot { it in vocabularyIds },
            needsReviewItemIds = state.progress.needsReviewItemIds.filterNot { it in vocabularyIds },
            lastPracticedAt = DateTimeUtils.nowIso()
        )
        saveLocalState(state.copy(progress = progress))
    }

    private fun updateVocabularyProgress(itemId: String, known: Boolean) {
        val pack = _uiState.value.currentPack ?: return
        val state = _uiState.value.currentLocalState ?: LocalPackState(packId = pack.id)
        val progress = if (known) {
            state.progress.copy(
                learnedItemIds = (state.progress.learnedItemIds + itemId).distinct(),
                needsReviewItemIds = state.progress.needsReviewItemIds.filterNot { it == itemId },
                practiceCount = state.progress.practiceCount + 1,
                lastPracticedAt = DateTimeUtils.nowIso()
            )
        } else {
            state.progress.copy(
                learnedItemIds = state.progress.learnedItemIds.filterNot { it == itemId },
                needsReviewItemIds = (state.progress.needsReviewItemIds + itemId).distinct(),
                practiceCount = state.progress.practiceCount + 1,
                lastPracticedAt = DateTimeUtils.nowIso()
            )
        }
        saveLocalState(state.copy(progress = progress))
    }

    private fun saveLocalState(state: LocalPackState) {
        viewModelScope.launch {
            _uiState.update { it.copy(currentLocalState = state) }
            runCatching { learningPackRepository.saveState(state.packId, state) }
                .onFailure { error -> _uiState.update { it.copy(error = friendlyMessage(error)) } }
        }
    }

    fun refreshVocabularyImages() {
        val pack = _uiState.value.currentPack ?: return
        _uiState.update { it.copy(vocabularyImagePaths = vocabularyImagePaths(pack)) }
    }

    fun generateVocabularyImage(itemId: String, force: Boolean = false) {
        val pack = _uiState.value.currentPack ?: return
        val item = pack.vocabulary.firstOrNull { it.id == itemId } ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(wordImageLoadingItemId = itemId, error = null) }
            runCatching {
                withTimeout(IMAGE_TIMEOUT_MS) {
                    imageRepository.getOrCreateVocabularyImage(pack, item, force)
                }
            }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            wordImageLoadingItemId = null,
                            vocabularyImagePaths = vocabularyImagePaths(pack)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(wordImageLoadingItemId = null, error = friendlyMessage(error))
                    }
                }
        }
    }

    fun generateAllVocabularyImages(force: Boolean = false) {
        val pack = _uiState.value.currentPack ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isGeneratingWordImages = true, wordImageLoadingItemId = null, error = null)
            }
            runCatching {
                withTimeout(IMAGE_TIMEOUT_MS * pack.vocabulary.size.coerceAtLeast(1)) {
                    pack.vocabulary.forEach { item ->
                        _uiState.update { it.copy(wordImageLoadingItemId = item.id) }
                        imageRepository.getOrCreateVocabularyImage(pack, item, force)
                        _uiState.update { it.copy(vocabularyImagePaths = vocabularyImagePaths(pack)) }
                    }
                }
            }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isGeneratingWordImages = false,
                            wordImageLoadingItemId = null,
                            vocabularyImagePaths = vocabularyImagePaths(pack)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isGeneratingWordImages = false,
                            wordImageLoadingItemId = null,
                            error = friendlyMessage(error)
                        )
                    }
                }
        }
    }

    fun playVocabularyAudio(itemId: String, text: String, repeat: Boolean = true) {
        val pack = _uiState.value.currentPack ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(audioLoadingItemId = itemId, audioStatus = "正在准备发音...", error = null)
            }
            runCatching {
                val spokenText = if (repeat) "$text. $text." else text
                val file = withTimeout(GENERATION_TIMEOUT_MS) {
                    audioRepository.getOrCreateAudio(pack.id, spokenText, SpeechSpeed.Normal)
                }
                check(file.length() > 0L) { "AI 发音返回了空音频，请检查 API Key 或稍后重试。" }
                Log.i(TAG, "Playing vocabulary audio with ExoPlayer text=$text file=${file.absolutePath} size=${file.length()}")
                audioPlayer.play(
                    file = file,
                    onStarted = {
                        _uiState.update {
                            it.copy(audioLoadingItemId = null, audioStatus = "正在播放：$text")
                        }
                    },
                    onEnded = {
                        _uiState.update { it.copy(audioStatus = null) }
                    },
                    onError = { error ->
                        _uiState.update {
                            it.copy(
                                audioLoadingItemId = null,
                                audioStatus = null,
                                error = "音频播放失败：${error.message ?: "请检查模拟器媒体音量。"}"
                            )
                        }
                    }
                )
            }
                .onSuccess {
                    // Loading state is cleared when ExoPlayer reaches READY.
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(audioLoadingItemId = null, audioStatus = null, error = friendlyMessage(error))
                    }
                }
        }
    }

    fun evaluate(promptZh: String, expectedAnswer: String, userAnswer: String) {
        val pack = _uiState.value.currentPack ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, evaluation = null) }
            runCatching {
                withTimeout(GENERATION_TIMEOUT_MS) {
                    practiceRepository.evaluateTranslation(
                        EvaluateRequest(pack.scenarioTitle, promptZh, expectedAnswer, userAnswer)
                    )
                }
            }
                .onSuccess { result -> _uiState.update { it.copy(isLoading = false, evaluation = result) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, error = friendlyMessage(error)) } }
        }
    }

    fun nextRoleplay(userAnswer: String) {
        val pack = _uiState.value.currentPack ?: return
        val task = pack.roleplayTasks.firstOrNull() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, roleplayResult = null) }
            runCatching {
                withTimeout(GENERATION_TIMEOUT_MS) {
                    practiceRepository.nextRoleplayTurn(
                        RoleplayRequest(
                            scenarioTitle = pack.scenarioTitle,
                            userRole = task.userRole,
                            assistantRole = task.assistantRole,
                            level = pack.level,
                            history = emptyList(),
                            userAnswer = userAnswer
                        )
                    )
                }
            }
                .onSuccess { result -> _uiState.update { it.copy(isLoading = false, roleplayResult = result) } }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, error = friendlyMessage(error)) } }
        }
    }

    private fun friendlyMessage(error: Throwable): String {
        val raw = error.message.orEmpty()
        return when {
            error is kotlinx.coroutines.TimeoutCancellationException ->
                "生成超时。请检查网络/API Key，或先使用“离线测试包”体验。"
            raw.contains("401") || raw.contains("invalid", ignoreCase = true) ->
                "API Key 无效或没有权限，请检查后重试。"
            raw.contains("429") ->
                "API 调用频率或额度受限，请稍后再试。"
            raw.contains("Unable to resolve host", ignoreCase = true) || raw.contains("timeout", ignoreCase = true) ->
                "网络连接失败或响应太慢，请检查网络后重试。"
            raw.contains("Fields [") || raw.contains("required for type", ignoreCase = true) ->
                "AI 返回格式不完整。已优化格式校验，请重新点击生成；如果仍失败，可先用“离线生成测试包”。"
            raw.isNotBlank() -> raw.take(220)
            else -> "生成失败，请稍后重试。"
        }
    }

    private fun sceneImagePaths(pack: LearningPack): Map<String, String> {
        return imageRepository.sceneSpecs(pack).mapNotNull { spec ->
            imageRepository.getSceneImageFile(pack.id, spec.id)?.absolutePath?.let { spec.id to it }
        }.toMap()
    }

    private fun vocabularyImagePaths(pack: LearningPack): Map<String, String> {
        return pack.vocabulary.mapNotNull { item ->
            imageRepository.getVocabularyImageFile(pack.id, item.id)?.absolutePath?.let { item.id to it }
        }.toMap()
    }

    private fun moduleImagePaths(pack: LearningPack): Map<String, String> {
        return imageRepository.moduleSpecs(pack).mapNotNull { spec ->
            imageRepository.getModuleImageFile(pack.id, spec.id)?.absolutePath?.let { spec.id to it }
        }.toMap()
    }

    private fun enrichmentStatusFor(pack: LearningPack): String {
        val done = listOf(
            "短语" to pack.phrases.isNotEmpty(),
            "句子" to pack.sentences.isNotEmpty(),
            "对话" to pack.dialogues.isNotEmpty(),
            "角色扮演" to pack.roleplayTasks.isNotEmpty(),
            "练习" to pack.reviewQuiz.isNotEmpty()
        ).filter { it.second }.joinToString("、") { it.first }
        return if (done.isBlank()) "正在后台补充内容..." else "已补充：$done"
    }

    private companion object {
        private const val TAG = "SceneEnglish"
        const val GENERATION_TIMEOUT_MS = 45_000L
        const val IMAGE_TIMEOUT_MS = 120_000L
    }

    override fun onCleared() {
        audioPlayer.release()
        super.onCleared()
    }
}
