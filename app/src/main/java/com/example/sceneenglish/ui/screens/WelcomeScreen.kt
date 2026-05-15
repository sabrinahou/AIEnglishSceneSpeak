package com.example.sceneenglish.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.sceneenglish.ui.components.PrimaryAction
import com.example.sceneenglish.ui.components.SectionCard

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    ScreenScaffold(title = "AIEnglishSceneSpeak") { padding ->
        ScrollContent(padding) {
            SectionCard("欢迎") {
                Text("输入你的真实场景，生成专属英语口语学习包。")
                Text("所有学习数据保存在本机。需要你提供自己的 AI API Key 才能生成内容和语音。")
            }
            Text(
                "无后端、无注册、无云同步。本机保存学习包 JSON、音频缓存和图片缓存。",
                style = MaterialTheme.typography.bodyMedium
            )
            PrimaryAction("开始设置", onClick = onStart)
        }
    }
}
