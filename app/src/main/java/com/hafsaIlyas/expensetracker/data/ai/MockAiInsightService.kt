package com.hafsaIlyas.expensetracker.data.ai

// data/ai/MockAiInsightService.kt

import com.hafsaIlyas.expensetracker.data.local.entity.Expense
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

class MockAiInsightService @Inject constructor() : AiInsightService {

    private val fmt = NumberFormat.getCurrencyInstance(Locale.US)

    override suspend fun generateInsights(
        expenses: List<Expense>,
        monthlyBudget: Double
    ): InsightResult {

        // Simulate network latency so the loading state is visible
        delay(2_200)

        if (expenses.isEmpty()) {
            return InsightResult(
                summary  = "No expenses recorded yet.",
                insights = listOf(
                    Insight(
                        type     = InsightType.POSITIVE_REINFORCEMENT,
                        title    = "Clean Slate 🎉",
                        body     = "You haven't recorded any expenses yet. Start tracking to unlock personalised insights.",
                        severity = Severity.INFO
                    )
                )
            )
        }

        val total = expenses.sumOf { it.amount }
        val grouped: Map<String, Double> = expenses
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .toMap()

        val topCategory  = grouped.entries.first()
        val topPct       = ((topCategory.value / total) * 100).roundToInt()
        val insights     = mutableListOf<Insight>()

        // ── Rule 1 — Overspending alert ───────────────────────────────────────
        if (topPct >= 40) {
            insights += Insight(
                type     = InsightType.OVERSPENDING_ALERT,
                title    = "Heavy Spending on ${topCategory.key}",
                body     = "${topCategory.key} accounts for $topPct% of your total " +
                        "spending this month (${fmt.format(topCategory.value)}). " +
                        "This is significantly above the recommended 30% threshold for a single category.",
                severity = if (topPct >= 55) Severity.CRITICAL else Severity.WARNING,
                actionLabel = "See all ${topCategory.key} expenses"
            )
        }

        // ── Rule 2 — Budget forecast ──────────────────────────────────────────
        val cal         = Calendar.getInstance()
        val dayOfMonth  = cal.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dailyAvg    = total / dayOfMonth
        val projected   = dailyAvg * daysInMonth
        val projectedStr = fmt.format(projected)

        insights += Insight(
            type     = InsightType.BUDGET_FORECAST,
            title    = "Month-End Forecast",
            body     = buildString {
                append("Based on your daily average of ${fmt.format(dailyAvg)}, ")
                append("you are on track to spend approximately $projectedStr by the end of the month. ")
                if (monthlyBudget > 0) {
                    val pct = ((projected / monthlyBudget) * 100).roundToInt()
                    if (projected > monthlyBudget) {
                        append("⚠️ This is ${pct - 100}% over your set budget of ${fmt.format(monthlyBudget)}.")
                    } else {
                        append("✅ You're within your budget of ${fmt.format(monthlyBudget)} ($pct% used).")
                    }
                }
            },
            severity = if (monthlyBudget > 0 && projected > monthlyBudget) Severity.WARNING else Severity.INFO
        )

        // ── Rule 3 — Saving suggestion ────────────────────────────────────────
        val savingTips = mapOf(
            "🍔 Food"            to "Meal prepping on weekends can cut food costs by up to 40%. Consider cooking in batches and reducing takeaway orders.",
            "🚌 Transport"       to "Explore monthly transit passes or carpooling — they can reduce transport costs by 25–35% versus daily fares.",
            "🛍️ Shopping"        to "Try a 48-hour rule: wait 2 days before any non-essential purchase. You'll find most impulse urges fade quickly.",
            "🎮 Entertainment"   to "Audit your subscriptions — the average person pays for 3 services they rarely use. Cancelling just one saves ~${fmt.format(12.0)}/month.",
            "✈️ Travel"          to "Booking flights 6–8 weeks in advance and travelling mid-week can save 20–30% on typical airfare costs.",
            "⚡ Utilities"       to "Smart power strips and turning off standby devices can shave 10–15% off your electricity bill each month."
        )

        val tip = savingTips[topCategory.key]
            ?: "Review your ${topCategory.key} spending and identify any recurring costs you could reduce or eliminate."

        insights += Insight(
            type     = InsightType.SAVING_SUGGESTION,
            title    = "Reduce ${topCategory.key} Costs",
            body     = tip,
            severity = Severity.INFO,
            actionLabel = "Explore saving strategies"
        )

        // ── Rule 4 — Positive reinforcement ──────────────────────────────────
        val diversified = grouped.size >= 4
        if (diversified) {
            insights += Insight(
                type     = InsightType.POSITIVE_REINFORCEMENT,
                title    = "Well-Balanced Spending",
                body     = "Your expenses are spread across ${grouped.size} categories, " +
                        "which suggests good financial diversification. Keep it up!",
                severity = Severity.POSITIVE,
                actionLabel = null
            )
        }

        // ── Rule 5 — Category-specific micro-tip ─────────────────────────────
        val secondCategory = grouped.entries.drop(1).firstOrNull()
        if (secondCategory != null) {
            val secondPct = ((secondCategory.value / total) * 100).roundToInt()
            if (secondPct >= 25) {
                insights += Insight(
                    type     = InsightType.CATEGORY_TIP,
                    title    = "Watch ${secondCategory.key}",
                    body     = "${secondCategory.key} is your second-largest expense at $secondPct% " +
                            "(${fmt.format(secondCategory.value)}). Together with ${topCategory.key}, " +
                            "these two categories make up ${topPct + secondPct}% of your total spending.",
                    severity = Severity.INFO
                )
            }
        }

        val summary = "Analysed ${expenses.size} transactions totalling ${fmt.format(total)}"

        return InsightResult(summary = summary, insights = insights)
    }
}