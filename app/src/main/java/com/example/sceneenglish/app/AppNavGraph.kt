package com.example.sceneenglish.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sceneenglish.ui.screens.ApiKeySettingsScreen
import com.example.sceneenglish.ui.screens.DialogueScreen
import com.example.sceneenglish.ui.screens.HomeScreen
import com.example.sceneenglish.ui.screens.LearningPackDetailScreen
import com.example.sceneenglish.ui.screens.LearningPackListScreen
import com.example.sceneenglish.ui.screens.PhraseScreen
import com.example.sceneenglish.ui.screens.RoleplayScreen
import com.example.sceneenglish.ui.screens.SceneImageScreen
import com.example.sceneenglish.ui.screens.SentenceScreen
import com.example.sceneenglish.ui.screens.TranslatePracticeScreen
import com.example.sceneenglish.ui.screens.VocabularyScreen
import com.example.sceneenglish.ui.screens.WelcomeScreen

@Composable
fun AppNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    val viewModel: AppViewModel = viewModel(factory = AppViewModelFactory(container))
    val start = if (container.secureSettingsStore.hasApiKey()) "home" else "welcome"

    NavHost(navController = navController, startDestination = start) {
        composable("welcome") {
            WelcomeScreen(onStart = { navController.navigate("settings") })
        }
        composable("settings") {
            ApiKeySettingsScreen(
                viewModel = viewModel,
                onDone = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onOpenSettings = { navController.navigate("settings") },
                onOpenHistory = { navController.navigate("packs") },
                onOpenPack = { navController.navigate("pack/$it") }
            )
        }
        composable("packs") {
            LearningPackListScreen(
                viewModel = viewModel,
                onOpenPack = { navController.navigate("pack/$it") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("pack/{packId}", arguments = listOf(navArgument("packId") { type = NavType.StringType })) {
            val packId = it.arguments?.getString("packId").orEmpty()
            LearningPackDetailScreen(
                packId = packId,
                viewModel = viewModel,
                onOpen = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable("vocabulary") { VocabularyScreen(viewModel, onBack = { navController.popBackStack() }) }
        composable("phrases") { PhraseScreen(viewModel, onBack = { navController.popBackStack() }) }
        composable("sentences") { SentenceScreen(viewModel, onBack = { navController.popBackStack() }) }
        composable("dialogues") { DialogueScreen(viewModel, onBack = { navController.popBackStack() }) }
        composable("image") { SceneImageScreen(viewModel, onBack = { navController.popBackStack() }) }
        composable("translate") { TranslatePracticeScreen(viewModel, onBack = { navController.popBackStack() }) }
        composable("roleplay") { RoleplayScreen(viewModel, onBack = { navController.popBackStack() }) }
    }
}
