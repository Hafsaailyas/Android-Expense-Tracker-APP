// app/src/main/java/com/hafsaIlyas/expensetracker/data/local/ExpenseDatabase.kt
package com.hafsaIlyas.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hafsaIlyas.expensetracker.data.local.entity.Expense

@Database(
    entities = [Expense::class],
    version = 1,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao

    companion object {
        const val DATABASE_NAME = "expense_tracker_db"
    }
}