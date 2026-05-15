package com.example.sceneenglish.app

import android.app.Application

class SceneEnglishApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
