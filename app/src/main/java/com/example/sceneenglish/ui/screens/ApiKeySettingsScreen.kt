package com.example.sceneenglish.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
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
import com.example.sceneenglish.ui.components.SectionCard

@Composable
fun ApiKeySettingsScreen(viewModel: AppViewModel, onDone: () -> Unit, onBack: (() -> Unit)? = null) {
    val state by viewModel.uiState.collectAsState()
    var apiKey by remember { mutableStateOf("") }

    ScreenScaffold(title = "API Key 设置", onBack = onBack) { padding ->
        ScrollContent(padding) {
            SectionCard("本机加密保存") {
                Text("你的 API Key 只保存在本机，不会上传到任何自有服务器。")
                Text("请不要把内置了 API Key 的 APK 公开发布。")
            }
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = apiKey.isNotBlank(),
                    onClick = {
                        viewModel.saveApiKey(apiKey)
                        onDone()
                    }
                ) { Text("保存") }
                OutlinedButton(modifier = Modifier.weight(1f), onClick = { viewModel.clearApiKey() }) {
                    Text("删除")
                }
            }
            Text(if (state.hasApiKey) "当前已保存 API Key。" else "当前未保存 API Key。")
        }
    }
}
