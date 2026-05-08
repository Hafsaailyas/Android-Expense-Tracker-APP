package com.hafsaIlyas.expensetracker.ui.screens.dashboard

// ui/screens/dashboard/DashboardUiState.kt

data class DashboardUiState(
    val isLoading: Boolean = true,

    // Current month
    val currentMonthTotal: Double = 0.0,
    val currentMonthName: String = "",

    // Previous month comparison
    val previousMonthTotal: Double = 0.0,
    val percentageChange: Double = 0.0,   // positive = up, negative = down
    val trendDirection: TrendDirection = TrendDirection.NEUTRAL,

    // Category breakdown
    val categoryBreakdown: List<CategoryShare> = emptyList(),

    // Recent 5 expenses for preview
    val recentExpenses: List<RecentExpenseItem> = emptyList(),

    // Filter
    val selectedFilterCategory: String = "" // "" = all
)

enum class TrendDirection { UP, DOWN, NEUTRAL }

data class CategoryShare(
    val category: String,
    val amount: Double,
    val percentage: Float,      // 0..100
    val color: Long             // ARGB packed for Canvas
)

data class RecentExpenseItem(
    val id: Long,
    val category: String,
    val note: String,
    val amount: Double,
    val formattedDate: String
)