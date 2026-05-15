package com.example.sceneenglish.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppViewModel(
            secureSettingsStore = container.secureSettingsStore,
            appSettingsStore = container.appSettingsStore,
            learningPackRepository = container.learningPackRepository,
            audioRepository = container.audioRepository,
            audioPlayer = container.audioPlayer,
            imageRepository = container.imageRepository,
            practiceRepository = container.practiceRepository
        ) as T
    }
}
