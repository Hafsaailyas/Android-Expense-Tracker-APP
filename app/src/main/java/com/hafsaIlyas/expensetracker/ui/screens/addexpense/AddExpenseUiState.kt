package com.hafsaIlyas.expensetracker.ui.screens.addexpense

data class AddExpenseUiState(
    val amount: String = "",
    val selectedCategory: String = "",
    val note: String = "",
    val amountError: String? = null,
    val categoryError: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)