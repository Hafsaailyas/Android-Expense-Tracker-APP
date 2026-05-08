package com.hafsaIlyas.expensetracker.data.repository

// data/repository/ExpenseRepositoryImpl.kt

import com.hafsaIlyas.expensetracker.data.local.CategorySpending
import com.hafsaIlyas.expensetracker.data.local.ExpenseDao
import com.hafsaIlyas.expensetracker.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> =
        dao.getAllExpenses()

    override suspend fun getExpenseById(id: Long): Expense? =
        dao.getExpenseById(id)

    override suspend fun insertExpense(expense: Expense): Long =
        dao.insertExpense(expense)

    override suspend fun updateExpense(expense: Expense) =
        dao.updateExpense(expense)

    override suspend fun deleteExpense(expense: Expense) =
        dao.deleteExpense(expense)

    override fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> =
        dao.getExpensesByDateRange(startDate, endDate)

    override fun getTotalSpendingInRange(startDate: Long, endDate: Long): Flow<Double?> =
        dao.getTotalSpendingInRange(startDate, endDate)

    override fun getSpendingByCategory(): Flow<List<CategorySpending>> =
        dao.getSpendingByCategory()
}