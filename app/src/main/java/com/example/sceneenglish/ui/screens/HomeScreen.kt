package com.example.sceneenglish.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sceneenglish.app.AppViewModel
import com.example.sceneenglish.ui.components.PrimaryAction
import com.example.sceneenglish.ui.components.SectionCard
import com.example.sceneenglish.ui.components.TagChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenPack: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var scenario by remember {
        mutableStateOf("我是网球初学者，最近需要和英语网球教练学习打网球。我需要快速掌握上课时常用的英语。")
    }
    var level by remember { mutableStateOf("A1-A2") }
    val levelOptions = listOf(
        LevelOption("入门", "零基础/初级", "A1-A2"),
        LevelOption("有点基础", "想说得更自然", "A2"),
        LevelOption("中级", "练真实对话", "B1"),
        LevelOption("高级", "提升表达细节", "B2")
    )
    val examples = listOf("餐厅点餐", "看医生", "机场入境", "租房沟通", "健身房私教", "公司会议")

    ScreenScaffold(title = "首页") { padding ->
        ScrollContent(padding) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(modifier = Modifier.weight(1f), onClick = onOpenSettings) { Text("设置") }
                OutlinedButton(modifier = Modifier.weight(1f), onClick = onOpenHistory) { Text("历史学习包") }
            }
            SectionCard("输入场景") {
                OutlinedTextField(
                    value = scenario,
                    onValueChange = { scenario = it },
                    minLines = 4,
                    label = { Text("真实生活场景") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("英语水平")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    levelOptions.forEach { option ->
                        TagChip(
                            label = "${option.title} · ${option.subtitle}",
                            selected = option.value == level,
                            onClick = { level = option.value }
                        )
                    }
                }
                PrimaryAction(
                    text = if (state.isLoading) "生成中..." else "生成学习包",
                    enabled = scenario.isNotBlank() && state.hasApiKey && !state.isLoading,
                    onClick = { viewModel.createPack(scenario, level, onOpenPack) }
                )
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = scenario.isNotBlank() && !state.isLoading,
                    onClick = { viewModel.createMockPack(scenario, level, onOpenPack) }
                ) {
                    Text("离线生成测试包")
                }
                if (!state.hasApiKey) {
                    Text("请先在设置中填写 API Key。")
                }
                Text("先快速生成生词包，短语、句子和对话会在后台继续补充。")
            }
            if (state.isLoading) CircularProgressIndicator()
            state.error?.let { Text("错误：$it") }
            SectionCard("示例场景") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    examples.forEach { label ->
                        TagChip(label = label, onClick = { scenario = label })
                    }
                }
            }
        }
    }
}

private data class LevelOption(
    val title: String,
    val subtitle: String,
    val value: String
)
