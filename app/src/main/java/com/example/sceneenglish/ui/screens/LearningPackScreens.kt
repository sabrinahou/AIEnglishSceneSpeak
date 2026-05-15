package com.example.sceneenglish.ui.screens

import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sceneenglish.app.AppViewModel
import com.example.sceneenglish.domain.model.ImageLabel
import com.example.sceneenglish.domain.model.LearningPack
import com.example.sceneenglish.domain.model.VocabularyItem
import com.example.sceneenglish.ui.components.SectionCard
import com.example.sceneenglish.ui.components.TagChip
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LearningPackListScreen(viewModel: AppViewModel, onOpenPack: (String) -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var pendingDeletePackId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { viewModel.refreshPacks() }

    ScreenScaffold(title = "本地学习包", onBack = onBack) { padding ->
        ScrollContent(padding) {
            if (state.packs.isEmpty()) {
                Text("还没有学习包。回到首页生成一个网球课测试场景吧。")
            }
            state.packs.forEach { pack ->
                SectionCard(pack.title, actionText = "打开", onAction = { onOpenPack(pack.id) }) {
                    Text(pack.description)
                    Text("${pack.level} · 单词 ${pack.wordCount} · 句子 ${pack.sentenceCount}")
                    Text(pack.createdAt)
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { pendingDeletePackId = pack.id }
                    ) {
                        Text("删除这个学习包")
                    }
                }
            }
            state.error?.let { message ->
                SectionCard("提示") {
                    Text(message)
                }
            }
        }
    }
    val deletePack = state.packs.firstOrNull { it.id == pendingDeletePackId }
    if (deletePack != null) {
        AlertDialog(
            onDismissRequest = { pendingDeletePackId = null },
            title = { Text("删除学习包？") },
            text = { Text("将删除“${deletePack.title}”以及它的本地图片、音频缓存和学习进度。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePack(deletePack.id)
                        pendingDeletePackId = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePackId = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun LearningPackDetailScreen(
    packId: String,
    viewModel: AppViewModel,
    onOpen: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(packId) { viewModel.loadPack(packId) }
    val pack = state.currentPack
    LaunchedEffect(pack?.id) {
        if (pack != null) viewModel.refreshModuleImages()
    }

    ScreenScaffold(title = pack?.scenarioTitle ?: "学习包", onBack = onBack) { padding ->
        ScrollContent(padding) {
            if (pack == null) {
                Text("正在加载学习包...")
                return@ScrollContent
            }
            SectionCard("场景") {
                Text(pack.scenarioDescription)
                Text("等级：${pack.level}")
                Text("创建时间：${pack.createdAt}")
                Text("生词：${pack.vocabulary.size} · 短语：${pack.phrases.size} · 句子：${pack.sentences.size} · 对话：${pack.dialogues.size}")
                state.enrichmentStatus?.let { Text(it) }
                if (pack.phrases.isEmpty() || pack.sentences.isEmpty() || pack.dialogues.isEmpty()) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isEnrichingPack,
                        onClick = { viewModel.enrichCurrentPack() }
                    ) {
                        Text(if (state.isEnrichingPack) "正在补充..." else "继续补充内容")
                    }
                }
            }
            val learnedCount = state.currentLocalState?.progress?.learnedItemIds.orEmpty()
                .count { learnedId -> pack.vocabulary.any { it.id == learnedId } }
            SectionCard("学习路径") {
                Text("建议顺序：先建立图像记忆，再练句子和对话，最后用练习检验能不能说出来。")
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.hasApiKey && !state.isGeneratingModuleImages,
                    onClick = { viewModel.generateAllModuleImages(force = false) }
                ) {
                    Text(if (state.isGeneratingModuleImages) "正在生成模块图片..." else "生成全部功能图片")
                }
                if (!state.hasApiKey) {
                    Text("请先在设置中填写 API Key，才能生成模块图片。")
                }
                LearningModuleCard(
                    title = "单词背诵",
                    subtitle = "图片联想、发音、认识/复习队列",
                    meta = "${learnedCount}/${pack.vocabulary.size} 已掌握",
                    visual = "vocabulary",
                    imagePath = state.moduleImagePaths["vocabulary"],
                    isImageLoading = state.moduleImageLoadingId == "vocabulary",
                    onGenerateImage = { viewModel.generateModuleImage("vocabulary", force = true) },
                    onClick = { onOpen("vocabulary") }
                )
                LearningModuleCard(
                    title = "常用短语",
                    subtitle = "短语搭配、例句发音、场景用法",
                    meta = "${pack.phrases.size} 个短语",
                    visual = "phrases",
                    imagePath = state.moduleImagePaths["phrases"],
                    isImageLoading = state.moduleImageLoadingId == "phrases",
                    onGenerateImage = { viewModel.generateModuleImage("phrases", force = true) },
                    onClick = { onOpen("phrases") }
                )
                LearningModuleCard(
                    title = "实用句子",
                    subtitle = "先听后看、逐词解释、整句跟读",
                    meta = "${pack.sentences.size} 句",
                    visual = "sentences",
                    imagePath = state.moduleImagePaths["sentences"],
                    isImageLoading = state.moduleImageLoadingId == "sentences",
                    onGenerateImage = { viewModel.generateModuleImage("sentences", force = true) },
                    onClick = { onOpen("sentences") }
                )
                LearningModuleCard(
                    title = "真实对话",
                    subtitle = "角色分句听、隐藏中文练理解",
                    meta = "${pack.dialogues.size} 段",
                    visual = "dialogues",
                    imagePath = state.moduleImagePaths["dialogues"],
                    isImageLoading = state.moduleImageLoadingId == "dialogues",
                    onGenerateImage = { viewModel.generateModuleImage("dialogues", force = true) },
                    onClick = { onOpen("dialogues") }
                )
                LearningModuleCard(
                    title = "场景图片",
                    subtitle = "看图点词，建立物体和英文连接",
                    meta = "${viewModel.imageSceneSpecs(pack).size} 组图",
                    visual = "image",
                    imagePath = state.moduleImagePaths["image"],
                    isImageLoading = state.moduleImageLoadingId == "image",
                    onGenerateImage = { viewModel.generateModuleImage("image", force = true) },
                    onClick = { onOpen("image") }
                )
                LearningModuleCard(
                    title = "中译英练习",
                    subtitle = "主动回忆、AI 纠错、更自然表达",
                    meta = "${pack.reviewQuiz.size} 题",
                    visual = "translate",
                    imagePath = state.moduleImagePaths["translate"],
                    isImageLoading = state.moduleImageLoadingId == "translate",
                    onGenerateImage = { viewModel.generateModuleImage("translate", force = true) },
                    onClick = { onOpen("translate") }
                )
                LearningModuleCard(
                    title = "角色扮演",
                    subtitle = "模拟真实交流，边说边纠错",
                    meta = "${pack.roleplayTasks.size} 个任务",
                    visual = "roleplay",
                    imagePath = state.moduleImagePaths["roleplay"],
                    isImageLoading = state.moduleImageLoadingId == "roleplay",
                    onGenerateImage = { viewModel.generateModuleImage("roleplay", force = true) },
                    onClick = { onOpen("roleplay") }
                )
            }
            state.error?.let { message ->
                SectionCard("提示") {
                    Text(message)
                }
            }
        }
    }
}

@Composable
private fun LearningModuleCard(
    title: String,
    subtitle: String,
    meta: String,
    visual: String,
    imagePath: String?,
    isImageLoading: Boolean,
    onGenerateImage: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FBF9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imagePath == null) {
                ModuleVisual(visual, Modifier.size(86.dp))
            } else {
                AsyncImage(
                    model = imagePath,
                    contentDescription = title,
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(meta, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onGenerateImage, enabled = !isImageLoading) {
                    Text(
                        when {
                            isImageLoading -> "图片生成中..."
                            imagePath == null -> "生成图片"
                            else -> "重新生成"
                        }
                    )
                }
            }
            Text("进入", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ModuleVisual(kind: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE6F3EE))
    ) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val w = size.width
            val h = size.height
            drawCircle(Color(0xFF0B8B71), radius = w * 0.14f, center = Offset(w * 0.26f, h * 0.28f))
            drawRoundRect(
                color = Color(0xFFFFC857),
                topLeft = Offset(w * 0.42f, h * 0.18f),
                size = Size(w * 0.34f, h * 0.24f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f, w * 0.06f)
            )
            when (kind) {
                "vocabulary" -> {
                    drawRoundRect(Color(0xFFFFFFFF), Offset(w * 0.20f, h * 0.52f), Size(w * 0.60f, h * 0.10f))
                    drawCircle(Color(0xFFD7193F), radius = w * 0.08f, center = Offset(w * 0.72f, h * 0.72f))
                }
                "phrases" -> {
                    drawLine(Color(0xFF263238), Offset(w * 0.20f, h * 0.58f), Offset(w * 0.78f, h * 0.58f), strokeWidth = w * 0.04f)
                    drawLine(Color(0xFF263238), Offset(w * 0.32f, h * 0.72f), Offset(w * 0.68f, h * 0.72f), strokeWidth = w * 0.04f)
                }
                "sentences" -> {
                    drawRoundRect(Color.White, Offset(w * 0.16f, h * 0.52f), Size(w * 0.68f, h * 0.26f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f, w * 0.04f))
                    drawLine(Color(0xFF0B8B71), Offset(w * 0.24f, h * 0.62f), Offset(w * 0.74f, h * 0.62f), strokeWidth = w * 0.025f)
                }
                "dialogues" -> {
                    drawCircle(Color(0xFF263238), radius = w * 0.10f, center = Offset(w * 0.34f, h * 0.62f))
                    drawCircle(Color(0xFF0B8B71), radius = w * 0.10f, center = Offset(w * 0.66f, h * 0.62f))
                }
                "image" -> {
                    drawRoundRect(Color(0xFF8ED1B2), Offset(w * 0.16f, h * 0.52f), Size(w * 0.68f, h * 0.28f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.05f, w * 0.05f))
                    drawCircle(Color(0xFFD8F21B), radius = w * 0.07f, center = Offset(w * 0.65f, h * 0.60f))
                }
                "translate" -> {
                    drawLine(Color(0xFF0B8B71), Offset(w * 0.22f, h * 0.64f), Offset(w * 0.78f, h * 0.64f), strokeWidth = w * 0.05f)
                    drawLine(Color(0xFF0B8B71), Offset(w * 0.65f, h * 0.52f), Offset(w * 0.78f, h * 0.64f), strokeWidth = w * 0.05f)
                    drawLine(Color(0xFF0B8B71), Offset(w * 0.65f, h * 0.76f), Offset(w * 0.78f, h * 0.64f), strokeWidth = w * 0.05f)
                }
                else -> {
                    drawCircle(Color(0xFF0B8B71), radius = w * 0.09f, center = Offset(w * 0.42f, h * 0.64f))
                    drawCircle(Color(0xFFD7193F), radius = w * 0.09f, center = Offset(w * 0.62f, h * 0.64f))
                }
            }
        }
    }
}

@Composable
fun VocabularyScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val systemSpeech = rememberSystemSpeechController()
    PackContentScreen(viewModel, "必备单词", onBack) { pack ->
        LaunchedEffect(pack.id, pack.vocabulary.size) {
            viewModel.refreshVocabularyImages()
        }
        SectionCard("发音模式") {
            Text("省钱模式不消耗 API；高质量模式使用 AI 美式发音并缓存。")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TagChip(
                    label = "省钱模式",
                    selected = state.appSettings.pronunciationMode == "system",
                    onClick = { viewModel.setPronunciationMode("system") }
                )
                TagChip(
                    label = "高质量模式",
                    selected = state.appSettings.pronunciationMode == "ai",
                    onClick = { viewModel.setPronunciationMode("ai") }
                )
            }
            if (state.appSettings.pronunciationMode == "ai") {
                Text("当前声音为 AI 生成的拟真人美式发音。")
            }
        }
        val learnedIds = state.currentLocalState?.progress?.learnedItemIds.orEmpty().toSet()
        val reviewIds = state.currentLocalState?.progress?.needsReviewItemIds.orEmpty().toSet()
        val speakText: (String, String, Boolean) -> Unit = { id, text, repeat ->
            if (state.appSettings.pronunciationMode == "system") {
                systemSpeech.speak(text, id)
            } else {
                viewModel.playVocabularyAudio(id, text, repeat = repeat)
            }
        }
        VocabularyStudyCard(
            pack = pack,
            imagePaths = state.vocabularyImagePaths,
            learnedIds = learnedIds,
            reviewIds = reviewIds,
            isAudioLoading = state.audioLoadingItemId != null,
            onSpeakWord = { item -> speakText(item.id, item.english, true) },
            onSpeakExample = { item -> speakText("${item.id}_example", item.exampleEn, false) },
            onKnown = viewModel::markVocabularyKnown,
            onNeedsReview = viewModel::markVocabularyNeedsReview,
            onReset = viewModel::resetVocabularyProgress
        )
        SectionCard("单词图片") {
            Text("为每个单词生成独立图片，已生成的图片会保存在本地。")
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = state.hasApiKey && !state.isGeneratingWordImages,
                onClick = { viewModel.generateAllVocabularyImages(force = false) }
            ) {
                Text(if (state.isGeneratingWordImages) "正在生成..." else "生成全部单词图片")
            }
            if (!state.hasApiKey) {
                Text("请先在设置中填写 API Key，才能生成图片和 AI 发音。")
            }
        }
        Text("全部单词", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        pack.vocabulary
            .sortedWith(compareBy<VocabularyItem> { it.priority }.thenBy { it.categoryZh }.thenBy { it.english })
            .groupBy { it.categoryZh }
            .forEach { (category, items) ->
                Text("$category · ${items.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                items.forEach { item ->
                    val speakWord = {
                        speakText(item.id, item.english, true)
                    }
                    val speakExample = {
                        speakText("${item.id}_example", item.exampleEn, false)
                    }
                    VocabularyFlashCard(
                        item = item,
                        imagePath = state.vocabularyImagePaths[item.id],
                        isImageLoading = state.wordImageLoadingItemId == item.id,
                        isAudioLoading = state.audioLoadingItemId == item.id,
                        onGenerateImage = { viewModel.generateVocabularyImage(item.id, force = true) },
                        onSpeak = speakWord,
                        onSpeakExample = speakExample
                    )
                }
        }
        state.audioStatus?.let { message ->
            SectionCard("发音") {
                Text(message)
            }
        }
        state.error?.let { message ->
            SectionCard("提示") {
                Text(message)
            }
        }
    }
}

@Composable
private fun VocabularyStudyCard(
    pack: LearningPack,
    imagePaths: Map<String, String>,
    learnedIds: Set<String>,
    reviewIds: Set<String>,
    isAudioLoading: Boolean,
    onSpeakWord: (VocabularyItem) -> Unit,
    onSpeakExample: (VocabularyItem) -> Unit,
    onKnown: (String) -> Unit,
    onNeedsReview: (String) -> Unit,
    onReset: () -> Unit
) {
    var index by remember(pack.id) { mutableStateOf(0) }
    var showAnswer by remember(pack.id) { mutableStateOf(false) }
    val sortedVocabulary = pack.vocabulary.sortedWith(compareBy<VocabularyItem> { it.priority }.thenBy { it.categoryZh }.thenBy { it.english })
    val reviewItems = sortedVocabulary.filter { it.id in reviewIds }
    val newItems = sortedVocabulary.filter { it.id !in reviewIds && it.id !in learnedIds }
    val learnedItems = sortedVocabulary.filter { it.id in learnedIds && it.id !in reviewIds }
    val queue = reviewItems + newItems + learnedItems
    LaunchedEffect(queue.map { it.id }.joinToString("|")) {
        if (queue.isNotEmpty()) {
            index = index.coerceIn(0, queue.lastIndex)
        } else {
            index = 0
        }
        showAnswer = false
    }
    val item = queue.getOrNull(index)
    SectionCard("背诵模式") {
        Text("先看英文、听发音、回想中文；再点显示答案。错词会自动排到前面复习。")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TagChip("未学 ${newItems.size}")
            TagChip("需复习 ${reviewItems.size}", selected = reviewItems.isNotEmpty())
            TagChip("已掌握 ${learnedItems.size}")
        }
        if (item == null) {
            Text("还没有单词。")
            return@SectionCard
        }
        val imagePath = imagePaths[item.id]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFF7FBF8))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (imagePath == null) {
                        VocabularyCartoonImage(
                            english = item.english,
                            modifier = Modifier.size(116.dp)
                        )
                    } else {
                        AsyncImage(
                            model = imagePath,
                            contentDescription = item.english,
                            modifier = Modifier
                                .size(116.dp)
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(item.english, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(item.phonetic.ifBlank { "American English" }, color = MaterialTheme.colorScheme.primary)
                        Text("${index + 1}/${queue.size} · ${item.categoryZh} · ${studyStatusText(item.id, learnedIds, reviewIds)}")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(enabled = !isAudioLoading, onClick = { onSpeakWord(item) }) {
                        Text("听单词")
                    }
                    OutlinedButton(onClick = { showAnswer = !showAnswer }) {
                        Text(if (showAnswer) "隐藏答案" else "显示答案")
                    }
                }
                if (showAnswer) {
                    Text(item.chinese, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${item.partOfSpeech} · ${item.usageNoteZh}")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.exampleEn, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        TextButton(enabled = !isAudioLoading, onClick = { onSpeakExample(item) }) {
                            Text("例句发音")
                        }
                    }
                    Text(item.exampleZh)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onNeedsReview(item.id)
                            showAnswer = false
                            if (queue.isNotEmpty()) index = (index + 1).coerceAtMost(queue.lastIndex)
                        }
                    ) {
                        Text("需要复习")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onKnown(item.id)
                            showAnswer = false
                            if (queue.isNotEmpty()) index = (index + 1).coerceAtMost(queue.lastIndex)
                        }
                    ) {
                        Text("认识")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = index > 0,
                        onClick = {
                            index = (index - 1).coerceAtLeast(0)
                            showAnswer = false
                        }
                    ) {
                        Text("上一张")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = index < queue.lastIndex,
                        onClick = {
                            index = (index + 1).coerceAtMost(queue.lastIndex)
                            showAnswer = false
                        }
                    ) {
                        Text("下一张")
                    }
                }
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onReset) {
                    Text("重置本学习包单词进度")
                }
            }
        }
    }
}

private fun studyStatusText(itemId: String, learnedIds: Set<String>, reviewIds: Set<String>): String {
    return when {
        itemId in reviewIds -> "需要复习"
        itemId in learnedIds -> "已掌握"
        else -> "新词"
    }
}

@Composable
private fun VocabularyFlashCard(
    item: VocabularyItem,
    imagePath: String?,
    isImageLoading: Boolean,
    isAudioLoading: Boolean,
    onGenerateImage: () -> Unit,
    onSpeak: () -> Unit,
    onSpeakExample: () -> Unit
) {
    var expanded by remember(item.id) { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(104.dp)) {
                if (imagePath == null) {
                    VocabularyCartoonImage(
                        english = item.english,
                        modifier = Modifier.size(104.dp)
                    )
                } else {
                    AsyncImage(
                        model = imagePath,
                        contentDescription = item.english,
                        modifier = Modifier
                            .size(104.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.english,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(enabled = !isAudioLoading, onClick = onSpeak) {
                        Text(if (isAudioLoading) "加载中" else "发音")
                    }
                }
                Text(
                    text = item.phonetic.ifBlank { "American English" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                if (expanded) {
                    Spacer(Modifier.height(4.dp))
                    Text("${item.chinese} · ${item.partOfSpeech}", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            item.exampleEn,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(enabled = !isAudioLoading, onClick = onSpeakExample) {
                            Text("例句发音")
                        }
                    }
                    Text(item.exampleZh)
                    Text(item.usageNoteZh, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isImageLoading,
                        onClick = onGenerateImage
                    ) {
                        Text(
                            when {
                                isImageLoading -> "图片生成中..."
                                imagePath == null -> "生成这个单词的图片"
                                else -> "重新生成图片"
                            }
                        )
                    }
                } else {
                    Text(
                        "点击查看中文和例句",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VocabularyCartoonImage(english: String, modifier: Modifier = Modifier) {
    val kind = remember(english) { cartoonKindFor(english) }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFEAF8F1), Color(0xFFD8EEE4))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            val w = size.width
            val h = size.height
            drawRoundRect(
                color = Color(0xFFBFE4D2),
                topLeft = Offset(w * 0.08f, h * 0.68f),
                size = Size(w * 0.84f, h * 0.20f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f, w * 0.04f)
            )
            when (kind) {
                CartoonKind.Person -> {
                    drawCircle(Color(0xFFFFD2A6), radius = w * 0.17f, center = Offset(w * 0.50f, h * 0.30f))
                    drawRoundRect(
                        Color(0xFF0B8B71),
                        topLeft = Offset(w * 0.34f, h * 0.46f),
                        size = Size(w * 0.32f, h * 0.30f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.07f, w * 0.07f)
                    )
                    drawLine(Color(0xFF263238), Offset(w * 0.40f, h * 0.24f), Offset(w * 0.60f, h * 0.22f), strokeWidth = w * 0.05f)
                    drawLine(Color(0xFF263238), Offset(w * 0.38f, h * 0.76f), Offset(w * 0.28f, h * 0.90f), strokeWidth = w * 0.04f)
                    drawLine(Color(0xFF263238), Offset(w * 0.62f, h * 0.76f), Offset(w * 0.72f, h * 0.90f), strokeWidth = w * 0.04f)
                }
                CartoonKind.Racket -> {
                    drawLine(Color(0xFF4E342E), Offset(w * 0.45f, h * 0.56f), Offset(w * 0.24f, h * 0.86f), strokeWidth = w * 0.08f)
                    drawOval(
                        color = Color(0xFF263238),
                        topLeft = Offset(w * 0.34f, h * 0.12f),
                        size = Size(w * 0.44f, h * 0.50f),
                        style = Stroke(width = w * 0.05f)
                    )
                    repeat(4) { index ->
                        val x = w * (0.40f + index * 0.09f)
                        drawLine(Color(0xFF78909C), Offset(x, h * 0.18f), Offset(x, h * 0.56f), strokeWidth = w * 0.01f)
                    }
                    repeat(3) { index ->
                        val y = h * (0.25f + index * 0.10f)
                        drawLine(Color(0xFF78909C), Offset(w * 0.38f, y), Offset(w * 0.74f, y), strokeWidth = w * 0.01f)
                    }
                }
                CartoonKind.Ball -> {
                    drawCircle(Color(0xFFD8F21B), radius = w * 0.28f, center = Offset(w * 0.52f, h * 0.48f))
                    drawArc(
                        color = Color.White,
                        startAngle = 110f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(w * 0.25f, h * 0.22f),
                        size = Size(w * 0.36f, h * 0.52f),
                        style = Stroke(width = w * 0.035f)
                    )
                    drawArc(
                        color = Color.White,
                        startAngle = -70f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(w * 0.44f, h * 0.22f),
                        size = Size(w * 0.36f, h * 0.52f),
                        style = Stroke(width = w * 0.035f)
                    )
                }
                CartoonKind.Net -> {
                    drawLine(Color.White, Offset(w * 0.10f, h * 0.38f), Offset(w * 0.90f, h * 0.38f), strokeWidth = w * 0.04f)
                    repeat(5) { index ->
                        val x = w * (0.14f + index * 0.16f)
                        drawLine(Color(0xFF455A64), Offset(x, h * 0.38f), Offset(x, h * 0.74f), strokeWidth = w * 0.015f)
                    }
                    repeat(4) { index ->
                        val y = h * (0.46f + index * 0.08f)
                        drawLine(Color(0xFF455A64), Offset(w * 0.10f, y), Offset(w * 0.90f, y), strokeWidth = w * 0.015f)
                    }
                }
                CartoonKind.Court -> {
                    drawRoundRect(
                        Color(0xFF4DBA7A),
                        topLeft = Offset(w * 0.16f, h * 0.18f),
                        size = Size(w * 0.68f, h * 0.64f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.03f, w * 0.03f)
                    )
                    drawLine(Color.White, Offset(w * 0.18f, h * 0.50f), Offset(w * 0.82f, h * 0.50f), strokeWidth = w * 0.025f)
                    drawLine(Color.White, Offset(w * 0.34f, h * 0.20f), Offset(w * 0.34f, h * 0.80f), strokeWidth = w * 0.02f)
                    drawLine(Color.White, Offset(w * 0.66f, h * 0.20f), Offset(w * 0.66f, h * 0.80f), strokeWidth = w * 0.02f)
                }
                CartoonKind.Action -> {
                    drawCircle(Color(0xFFD8F21B), radius = w * 0.08f, center = Offset(w * 0.72f, h * 0.20f))
                    drawLine(Color(0xFF4E342E), Offset(w * 0.28f, h * 0.72f), Offset(w * 0.48f, h * 0.48f), strokeWidth = w * 0.06f)
                    drawOval(Color(0xFF263238), Offset(w * 0.46f, h * 0.25f), Size(w * 0.34f, h * 0.30f), style = Stroke(width = w * 0.04f))
                    drawArc(
                        color = Color(0xFF00856B),
                        startAngle = 210f,
                        sweepAngle = 110f,
                        useCenter = false,
                        topLeft = Offset(w * 0.20f, h * 0.12f),
                        size = Size(w * 0.55f, h * 0.55f),
                        style = Stroke(width = w * 0.035f)
                    )
                }
                CartoonKind.Object -> {
                    drawRoundRect(
                        Color(0xFFFFC857),
                        topLeft = Offset(w * 0.28f, h * 0.24f),
                        size = Size(w * 0.44f, h * 0.44f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.10f, w * 0.10f)
                    )
                    drawCircle(Color(0xFF0B8B71), radius = w * 0.10f, center = Offset(w * 0.34f, h * 0.72f))
                    drawCircle(Color(0xFFD7193F), radius = w * 0.07f, center = Offset(w * 0.68f, h * 0.28f))
                }
            }
        }
    }
}

private enum class CartoonKind {
    Person,
    Racket,
    Ball,
    Net,
    Court,
    Action,
    Object
}

private fun cartoonKindFor(english: String): CartoonKind {
    val text = english.lowercase()
    return when {
        listOf("coach", "student", "player", "beginner", "server", "instructor").any { text.contains(it) } -> CartoonKind.Person
        listOf("racket", "racquet", "grip").any { text.contains(it) } -> CartoonKind.Racket
        listOf("ball", "deuce", "point", "love", "fifteen", "thirty", "forty").any { text.contains(it) } -> CartoonKind.Ball
        listOf("net", "cord").any { text.contains(it) } -> CartoonKind.Net
        listOf("court", "baseline", "sideline", "line", "box", "alley").any { text.contains(it) } -> CartoonKind.Court
        listOf("forehand", "backhand", "serve", "volley", "swing", "footwork", "stance", "step", "return", "slice", "topspin", "rally").any { text.contains(it) } -> CartoonKind.Action
        else -> CartoonKind.Object
    }
}

internal data class SystemSpeechController(
    val ready: Boolean,
    val speak: (text: String, utteranceId: String) -> Unit
)

@Composable
internal fun rememberSystemSpeechController(): SystemSpeechController {
    val context = LocalContext.current
    val ttsState = remember { mutableStateOf<TextToSpeech?>(null) }
    val readyState = remember { mutableStateOf(false) }
    val messageState = remember { mutableStateOf("系统发音正在准备，请稍后再试。") }

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val languageResult = engine?.setLanguage(Locale.US)
                engine?.setSpeechRate(0.88f)
                engine?.setPitch(1.0f)
                engine?.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                val supported = languageResult != TextToSpeech.LANG_MISSING_DATA &&
                    languageResult != TextToSpeech.LANG_NOT_SUPPORTED
                readyState.value = supported
                messageState.value = if (supported) {
                    ""
                } else {
                    "系统缺少英文发音包。可切换到高质量模式，或在系统设置中安装英文 TTS。"
                }
                ttsState.value = engine
            } else {
                readyState.value = false
                messageState.value = "系统发音初始化失败。可切换到高质量模式。"
            }
        }
        onDispose {
            engine?.stop()
            engine?.shutdown()
            ttsState.value = null
            readyState.value = false
        }
    }

    return SystemSpeechController(
        ready = readyState.value,
        speak = { text, utteranceId ->
            val engine = ttsState.value
            when {
                engine == null -> Toast.makeText(context, messageState.value, Toast.LENGTH_SHORT).show()
                !readyState.value -> Toast.makeText(context, messageState.value, Toast.LENGTH_LONG).show()
                else -> {
                    val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                    if (result == TextToSpeech.ERROR) {
                        Toast.makeText(context, "系统发音播放失败，可切换到高质量模式。", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )
}

@Composable
fun PhraseScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val systemSpeech = rememberSystemSpeechController()
    val speakText: (String, String) -> Unit = { id, text ->
        if (state.appSettings.pronunciationMode == "system") {
            systemSpeech.speak(text, id)
        } else {
            viewModel.playVocabularyAudio(id, text, repeat = false)
        }
    }
    PackContentScreen(viewModel, "常用短语", onBack) { pack ->
        SectionCard("短语学习") {
            Text("短语比单词更接近真实表达。先听短语，再看例句，最后盖住中文复述。")
            Text("${pack.phrases.size} 个短语 · 点击卡片展开中文和用法")
        }
        pack.phrases
            .sortedWith(compareBy<com.example.sceneenglish.domain.model.PhraseItem> { it.priority }.thenBy { it.categoryZh }.thenBy { it.english })
            .groupBy { it.categoryZh }
            .forEach { (category, items) ->
                Text("$category · ${items.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                items.forEach {
                    PhraseLearningCard(
                        english = it.english,
                        chinese = it.chinese,
                        exampleEn = it.exampleEn,
                        exampleZh = it.exampleZh,
                        usageNoteZh = it.usageNoteZh,
                        onSpeak = { speakText(it.id, it.english) },
                        onSpeakExample = { speakText("${it.id}_example", it.exampleEn) }
                    )
                }
            }
        state.audioStatus?.let { SectionCard("发音") { Text(it) } }
        state.error?.let { SectionCard("提示") { Text(it) } }
    }
}

@Composable
fun SentenceScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val systemSpeech = rememberSystemSpeechController()
    val speakText: (String, String) -> Unit = { id, text ->
        if (state.appSettings.pronunciationMode == "system") {
            systemSpeech.speak(text, id)
        } else {
            viewModel.playVocabularyAudio(id, text, repeat = false)
        }
    }
    PackContentScreen(viewModel, "实用句子", onBack) { pack ->
        SectionCard("句子训练") {
            Text("按“听句子 → 猜中文 → 看逐词解释 → 跟读”的顺序练，记得更牢。")
            Text("${pack.sentences.size} 句 · 每句都能播放发音")
        }
        pack.sentences
            .sortedWith(compareBy<com.example.sceneenglish.domain.model.SentenceItem> { it.priority }.thenBy { it.categoryZh }.thenBy { it.english })
            .groupBy { it.categoryZh }
            .forEach { (category, items) ->
                Text("$category · ${items.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                items.forEach {
                    SentenceLearningCard(
                        english = it.english,
                        chinese = it.chinese,
                        wordByWord = it.wordByWord.map { word -> "${word.text} = ${word.meaningZh}" },
                        usageContextZh = it.usageContextZh,
                        onSpeak = { speakText(it.id, it.english) }
                    )
                }
            }
        state.audioStatus?.let { SectionCard("发音") { Text(it) } }
        state.error?.let { SectionCard("提示") { Text(it) } }
    }
}

@Composable
fun DialogueScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val systemSpeech = rememberSystemSpeechController()
    var showChinese by remember { mutableStateOf(true) }
    val speakText: (String, String) -> Unit = { id, text ->
        if (state.appSettings.pronunciationMode == "system") {
            systemSpeech.speak(text, id)
        } else {
            viewModel.playVocabularyAudio(id, text, repeat = false)
        }
    }
    PackContentScreen(viewModel, "真实对话", onBack) { pack ->
        SectionCard("对话练习") {
            Text("先整段听懂，再隐藏中文按角色复述。每一句都可以单独播放。")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TagChip("显示中文", selected = showChinese, onClick = { showChinese = true })
                TagChip("隐藏中文", selected = !showChinese, onClick = { showChinese = false })
            }
        }
        pack.dialogues.forEach { dialogue ->
            SectionCard(dialogue.title) {
                Text(dialogue.descriptionZh)
                dialogue.turns.forEach { turn ->
                    DialogueTurnCard(
                        speaker = turn.speaker,
                        english = turn.english,
                        chinese = turn.chinese,
                        showChinese = showChinese,
                        onSpeak = { speakText(turn.id, turn.english) }
                    )
                }
            }
        }
        state.audioStatus?.let { SectionCard("发音") { Text(it) } }
        state.error?.let { SectionCard("提示") { Text(it) } }
    }
}

@Composable
private fun PhraseLearningCard(
    english: String,
    chinese: String,
    exampleEn: String,
    exampleZh: String,
    usageNoteZh: String,
    onSpeak: () -> Unit,
    onSpeakExample: () -> Unit
) {
    var expanded by remember(english) { mutableStateOf(false) }
    SectionCard(english, actionText = if (expanded) "收起" else "展开", onAction = { expanded = !expanded }) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onSpeak) { Text("短语发音") }
            OutlinedButton(onClick = onSpeakExample) { Text("例句发音") }
        }
        Text(exampleEn, fontWeight = FontWeight.SemiBold)
        if (expanded) {
            Text(chinese, style = MaterialTheme.typography.titleMedium)
            Text(exampleZh)
            Text(usageNoteZh, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text("先听和复述，再展开看中文。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SentenceLearningCard(
    english: String,
    chinese: String,
    wordByWord: List<String>,
    usageContextZh: String,
    onSpeak: () -> Unit
) {
    var expanded by remember(english) { mutableStateOf(false) }
    SectionCard(english, actionText = if (expanded) "收起" else "解析", onAction = { expanded = !expanded }) {
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onSpeak) {
            Text("播放整句")
        }
        if (expanded) {
            Text(chinese, style = MaterialTheme.typography.titleMedium)
            Text("逐词解释：", fontWeight = FontWeight.Bold)
            wordByWord.forEach { Text(it) }
            Text("使用场景：$usageContextZh")
        } else {
            Text("听完后先在心里翻译，再点解析。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DialogueTurnCard(
    speaker: String,
    english: String,
    chinese: String,
    showChinese: Boolean,
    onSpeak: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FBF9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(speaker, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                TextButton(onClick = onSpeak) { Text("播放") }
            }
            Text(english, fontWeight = FontWeight.SemiBold)
            if (showChinese) Text(chinese, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun SceneImageScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val selectedLabel = remember { mutableStateOf<ImageLabel?>(null) }
    val systemSpeech = rememberSystemSpeechController()
    PackContentScreen(viewModel, "场景图片", onBack) { pack ->
        LaunchedEffect(pack.id) {
            viewModel.refreshSceneImage()
        }
        val specs = remember(pack) { viewModel.imageSceneSpecs(pack) }
        val pagerState = rememberPagerState(pageCount = { specs.size })
        val currentSpec = specs[pagerState.currentPage]
        val overlayLabels = currentSpec.labels
        SectionCard("场景图") {
            Text("${currentSpec.title} · ${pagerState.currentPage + 1}/${specs.size}")
            Text("动画卡通教学风格；图片本身不含文字，英文标签由 App 叠加。")
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) { page ->
                val spec = specs[page]
                val imagePath = state.sceneImagePaths[spec.id]
                if (imagePath == null) {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                        Text("这一组还没有图片。请生成当前场景或生成全部场景。")
                    }
                } else {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        AsyncImage(
                            model = imagePath,
                            contentDescription = "${pack.scenarioTitle} ${spec.title}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                        spec.labels.forEachIndexed { index, label ->
                            val (fallbackX, fallbackY) = fallbackLabelPosition(index)
                            val x = label.x ?: fallbackX
                            val y = label.y ?: fallbackY
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = maxWidth * x.coerceIn(0.03f, 0.80f),
                                        y = maxHeight * y.coerceIn(0.05f, 0.90f)
                                    )
                            ) {
                                TextButtonLabel(
                                    text = label.english,
                                    onClick = {
                                        selectedLabel.value = label
                                        if (state.appSettings.pronunciationMode == "system") {
                                            systemSpeech.speak(label.english, label.id)
                                        } else {
                                            viewModel.playVocabularyAudio(label.id, label.english)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = state.hasApiKey && !state.isImageLoading,
                onClick = { viewModel.generateSceneImage(currentSpec) }
            ) {
                Text(if (state.isImageLoading) "生成中..." else "生成当前场景图片")
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = state.hasApiKey && !state.isImageLoading,
                onClick = { viewModel.generateAllSceneImages() }
            ) {
                Text("生成全部场景图片")
            }
            if (!state.hasApiKey) {
                Text("请先在设置中填写 API Key。")
            }
            if (state.isImageLoading) {
                CircularProgressIndicator()
            }
        }
        selectedLabel.value?.let { label ->
            SectionCard("单词") {
                Text(label.english)
                Text(label.chinese)
                Text(label.descriptionZh)
            }
        }
        state.error?.let { message ->
            SectionCard("提示") {
                Text(message)
            }
        }
        SectionCard("标签") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                overlayLabels.forEach { TagChip("${it.english} ${it.chinese}") }
            }
        }
    }
}

private fun fallbackLabelPosition(index: Int): Pair<Float, Float> {
    val positions = listOf(
        0.06f to 0.08f,
        0.56f to 0.08f,
        0.20f to 0.22f,
        0.70f to 0.22f,
        0.05f to 0.44f,
        0.72f to 0.46f,
        0.08f to 0.62f,
        0.58f to 0.64f,
        0.08f to 0.78f,
        0.68f to 0.82f
    )
    return positions[index % positions.size]
}

@Composable
private fun TextButtonLabel(text: String, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick, modifier = Modifier.widthIn(max = 150.dp)) {
        Text(
            text = text,
            color = androidx.compose.ui.graphics.Color(0xFFD7193F),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PackContentScreen(
    viewModel: AppViewModel,
    title: String,
    onBack: () -> Unit,
    content: @Composable (LearningPack) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    ScreenScaffold(title = title, onBack = onBack) { padding ->
        ScrollContent(padding) {
            val pack = state.currentPack
            if (pack == null) {
                Text("请先打开一个学习包。")
            } else {
                content(pack)
            }
        }
    }
}
