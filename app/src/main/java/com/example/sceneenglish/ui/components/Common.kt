package com.example.sceneenglish.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (actionText != null && onAction != null) {
                    OutlinedButton(onClick = onAction) { Text(actionText) }
                }
            }
            content()
        }
    }
}

@Composable
fun TagChip(label: String, selected: Boolean = false, onClick: () -> Unit = {}) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
fun PrimaryAction(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(enabled = enabled, onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text)
    }
}
