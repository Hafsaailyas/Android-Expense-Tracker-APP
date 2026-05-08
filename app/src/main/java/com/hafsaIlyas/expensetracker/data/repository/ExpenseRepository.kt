package com.hafsaIlyas.expensetracker.data.repository

// data/repository/ExpenseRepository.kt

import com.hafsaIlyas.expensetracker.data.local.CategorySpending
import com.hafsaIlyas.expensetracker.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    suspend fun getExpenseById(id: Long): Expense?
    suspend fun insertExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>
    fun getTotalSpendingInRange(startDate: Long, endDate: Long): Flow<Double?>
    fun getSpendingByCategory(): Flow<List<CategorySpending>>
}