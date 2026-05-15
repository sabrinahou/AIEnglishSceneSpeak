package com.example.sceneenglish.data.repository

import com.example.sceneenglish.data.ai.AiClient
import com.example.sceneenglish.domain.model.EvaluateRequest
import com.example.sceneenglish.domain.model.EvaluationResult
import com.example.sceneenglish.domain.model.RoleplayRequest
import com.example.sceneenglish.domain.model.RoleplayResult

class PracticeRepository(private val aiClient: AiClient) {
    suspend fun evaluateTranslation(request: EvaluateRequest): EvaluationResult {
        return aiClient.evaluateTranslation(request)
    }

    suspend fun nextRoleplayTurn(request: RoleplayRequest): RoleplayResult {
        return aiClient.nextRoleplayTurn(request)
    }
}
