package com.example.sceneenglish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sceneenglish.app.AppNavGraph
import com.example.sceneenglish.app.SceneEnglishApp
import com.example.sceneenglish.ui.theme.SceneEnglishTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as SceneEnglishApp).container
        setContent {
            SceneEnglishTheme {
                AppNavGraph(container = container)
            }
        }
    }
}
