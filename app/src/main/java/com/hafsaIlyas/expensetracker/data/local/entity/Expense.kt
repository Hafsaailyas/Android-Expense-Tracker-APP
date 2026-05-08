package com.hafsaIlyas.expensetracker.data.local.entity

// data/local/entity/Expense.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val note: String,
    val date: Long = System.currentTimeMillis() // stored as epoch millis
)