package com.hafsaIlyas.expensetracker.ui.screens.aiinsights

// ui/screens/aiinsights/AiInsightUiState.kt

import com.hafsaIlyas.expensetracker.data.ai.Insight

sealed class AiInsightUiState {
    object Idle : AiInsightUiState()
    data class Loading(val thinkingLabel: String = "Analysing your spending…") : AiInsightUiState()
    data class Success(
        val summary: String,
        val insights: List<Insight>,
        val generatedAt: Long
    ) : AiInsightUiState()
    data class Error(val message: String) : AiInsightUiState()
}