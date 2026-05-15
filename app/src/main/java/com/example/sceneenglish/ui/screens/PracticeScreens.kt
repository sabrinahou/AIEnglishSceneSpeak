package com.example.sceneenglish.ui.screens

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp
import com.example.sceneenglish.app.AppViewModel
import com.example.sceneenglish.ui.components.PrimaryAction
import com.example.sceneenglish.ui.components.SectionCard
import com.example.sceneenglish.ui.components.TagChip

@Composable
fun TranslatePracticeScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val quizzes = state.currentPack?.reviewQuiz.orEmpty()
    var index by remember(state.currentPack?.id) { mutableStateOf(0) }
    val quiz = quizzes.getOrNull(index)
    var answer by remember(index) { mutableStateOf("") }
    val systemSpeech = rememberSystemSpeechController()
    val speak: (String, String) -> Unit = { id, text ->
        if (state.appSettings.pronunciationMode == "system") {
            systemSpeech.speak(text, id)
        } else {
            viewModel.playVocabularyAudio(id, text, repeat = false)
        }
    }

    ScreenScaffold(title = "中译英练习", onBack = onBack) { padding ->
        ScrollContent(padding) {
            if (quiz == null) {
                Text("当前学习包没有练习题。")
                return@ScrollContent
            }
            SectionCard("主动回忆") {
                Text("先自己写，再看纠错。主动回忆比只看答案记得更牢。")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TagChip("${index + 1}/${quizzes.size}")
                    TagChip("中译英")
                }
            }
            SectionCard("题目 ${index + 1}") {
                Text(quiz.promptZh)
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("你的英文") },
                    modifier = Modifier.fillMaxWidth()
                )
                PrimaryAction("检查", enabled = answer.isNotBlank()) {
                    viewModel.evaluate(quiz.promptZh, quiz.expectedAnswer, answer)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(enabled = index > 0, onClick = { index -= 1 }) { Text("上一题") }
                    TextButton(enabled = index < quizzes.lastIndex, onClick = { index += 1 }) { Text("下一题") }
                }
            }
            state.evaluation?.let {
                SectionCard("反馈") {
                    Text("正确表达：${it.correctedAnswer}", fontWeight = FontWeight.Bold)
                    Text("更自然：${it.naturalAnswer}", fontWeight = FontWeight.SemiBold)
                    Text(it.feedbackZh)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { speak("corrected_${quiz.id}", it.correctedAnswer) }) {
                            Text("听正确表达")
                        }
                        OutlinedButton(onClick = { speak("natural_${quiz.id}", it.naturalAnswer) }) {
                            Text("听自然表达")
                        }
                    }
                }
            }
            state.audioStatus?.let { SectionCard("发音") { Text(it) } }
            state.error?.let { SectionCard("提示") { Text(it) } }
        }
    }
}

@Composable
fun RoleplayScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val tasks = state.currentPack?.roleplayTasks.orEmpty()
    var index by remember(state.currentPack?.id) { mutableStateOf(0) }
    val task = tasks.getOrNull(index)
    var answer by remember(index) { mutableStateOf("") }
    val systemSpeech = rememberSystemSpeechController()
    val speak: (String, String) -> Unit = { id, text ->
        if (state.appSettings.pronunciationMode == "system") {
            systemSpeech.speak(text, id)
        } else {
            viewModel.playVocabularyAudio(id, text, repeat = false)
        }
    }

    ScreenScaffold(title = "角色扮演", onBack = onBack) { padding ->
        ScrollContent(padding) {
            if (task == null) {
                Text("当前学习包没有角色扮演任务。")
                return@ScrollContent
            }
            SectionCard("口语模拟") {
                Text("听对方一句话，输入你的回答。AI 会纠错并继续下一轮。")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TagChip("${index + 1}/${tasks.size}")
                    TagChip("${task.userRole} 练习")
                }
            }
            SectionCard("${task.assistantRole} → ${task.userRole}") {
                Text("${task.assistantRole}: ${task.starterEnglish}", fontWeight = FontWeight.Bold)
                Text(task.starterChinese)
                OutlinedButton(onClick = { speak("starter_${task.id}", task.starterEnglish) }) {
                    Text("播放对方开场")
                }
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("你的回答") },
                    modifier = Modifier.fillMaxWidth()
                )
                PrimaryAction("发送并纠错", enabled = answer.isNotBlank()) {
                    viewModel.nextRoleplay(answer)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(enabled = index > 0, onClick = { index -= 1 }) { Text("上一个任务") }
                    TextButton(enabled = index < tasks.lastIndex, onClick = { index += 1 }) { Text("下一个任务") }
                }
            }
            state.roleplayResult?.let {
                SectionCard("纠错") {
                    Text("正确表达：${it.feedback.correctedAnswer}", fontWeight = FontWeight.Bold)
                    Text("更自然：${it.feedback.naturalAnswer}", fontWeight = FontWeight.SemiBold)
                    Text(it.feedback.feedbackZh)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { speak("roleplay_corrected", it.feedback.correctedAnswer) }) {
                            Text("听正确表达")
                        }
                        OutlinedButton(onClick = { speak("roleplay_natural", it.feedback.naturalAnswer) }) {
                            Text("听自然表达")
                        }
                    }
                }
                SectionCard("下一句") {
                    Text("${it.nextMessage.speaker}: ${it.nextMessage.english}", fontWeight = FontWeight.Bold)
                    Text(it.nextMessage.chinese)
                    OutlinedButton(onClick = { speak(it.nextMessage.id, it.nextMessage.english) }) {
                        Text("播放下一句")
                    }
                }
            }
            state.audioStatus?.let { SectionCard("发音") { Text(it) } }
            state.error?.let { SectionCard("提示") { Text(it) } }
        }
    }
}
