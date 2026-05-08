package com.hafsaIlyas.expensetracker.data.ai

// data/ai/AiInsightService.kt

import com.hafsaIlyas.expensetracker.data.local.entity.Expense

// ── Domain models ─────────────────────────────────────────────────────────────

enum class InsightType {
    OVERSPENDING_ALERT,
    SAVING_SUGGESTION,
    BUDGET_FORECAST,
    POSITIVE_REINFORCEMENT,
    CATEGORY_TIP
}

data class Insight(
    val type: InsightType,
    val title: String,
    val body: String,
    val actionLabel: String? = null,  // optional CTA
    val severity: Severity = Severity.INFO
)

enum class Severity { INFO, WARNING, CRITICAL, POSITIVE }

data class InsightResult(
    val summary: String,              // 1-liner "You spent X this month"
    val insights: List<Insight>,
    val generatedAt: Long = System.currentTimeMillis()
)

// ── Contract ──────────────────────────────────────────────────────────────────

interface AiInsightService {
    /**
     * Analyse [expenses] (pre-filtered to the relevant period) and
     * return a set of human-readable financial insights.
     */
    suspend fun generateInsights(
        expenses: List<Expense>,
        monthlyBudget: Double = 0.0   // 0 = no budget set
    ): InsightResult
}