package com.hafsaIlyas.expensetracker.ui.screens.expenselist

import com.hafsaIlyas.expensetracker.data.local.entity.Expense

data class ExpenseListUiState(
    val allExpenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true,
    val totalSpending: Double = 0.0,

    // Search & filter
    val searchQuery: String = "",
    val selectedCategory: String = ""  // "" = all
)