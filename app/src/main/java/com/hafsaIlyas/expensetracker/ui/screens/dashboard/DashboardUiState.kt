package com.hafsaIlyas.expensetracker.ui.screens.dashboard

// ui/screens/dashboard/DashboardUiState.kt
// CHANGES vs original:
//   • Added monthlyBudget, budgetPercentage, remainingBudget, isOverBudget, budgetStatus fields
//   • BudgetStatus enum is defined in ExpenseViewModel; imported here via the viewmodel package.
//     If you prefer it here instead, move the enum and remove the import.

import com.hafsaIlyas.expensetracker.ui.viewmodel.BudgetStatus

// ── Supporting data classes ───────────────────────────────────────────────────

data class CategoryShare(
    val category  : String,
    val amount    : Double,
    val percentage: Float,
    val color     : Long        // ARGB long, e.g. 0xFF1A5F7AL
)

data class RecentExpenseItem(
    val id           : Long,
    val category     : String,
    val note         : String,
    val amount       : Double,
    val formattedDate: String
)

enum class TrendDirection { UP, DOWN, NEUTRAL }

// ── Main UI state ─────────────────────────────────────────────────────────────

data class DashboardUiState(
    val isLoading             : Boolean             = true,

    // ── Spending totals ───────────────────────────────────────────────────────
    val currentMonthTotal     : Double              = 0.0,
    val currentMonthName      : String              = "",
    val previousMonthTotal    : Double              = 0.0,
    val percentageChange      : Double              = 0.0,
    val trendDirection        : TrendDirection      = TrendDirection.NEUTRAL,

    // ── Category breakdown ────────────────────────────────────────────────────
    val categoryBreakdown     : List<CategoryShare> = emptyList(),
    val selectedFilterCategory: String              = "",

    // ── Recent expenses ───────────────────────────────────────────────────────
    val recentExpenses        : List<RecentExpenseItem> = emptyList(),

    // ── Budget tracking (NEW) ─────────────────────────────────────────────────
    /** Raw monthly budget from DataStore. 0.0 means no budget is set. */
    val monthlyBudget         : Double              = 0.0,

    /**
     * Ratio of [currentMonthTotal] to [monthlyBudget] (0f … n).
     * Values > 1.0 mean over-budget. 0f when no budget is set.
     */
    val budgetPercentage      : Float               = 0f,

    /**
     * Budget remaining this month. Negative when over-budget.
     * Consumers should use [isOverBudget] to decide whether to show
     * "remaining" vs "over by".
     */
    val remainingBudget       : Double              = 0.0,

    /** True when [currentMonthTotal] exceeds [monthlyBudget] (and budget > 0). */
    val isOverBudget          : Boolean             = false,

    /**
     * Semantic status derived from [budgetPercentage]:
     *   NO_BUDGET   → budget not set
     *   NORMAL      → 0–74 %
     *   WARNING     → 75–89 %
     *   HIGH_ALERT  → 90–99 %
     *   OVER_BUDGET → ≥ 100 %
     */
    val budgetStatus          : BudgetStatus        = BudgetStatus.NO_BUDGET
)