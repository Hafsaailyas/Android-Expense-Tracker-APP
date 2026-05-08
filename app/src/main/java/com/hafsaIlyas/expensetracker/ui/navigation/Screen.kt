// app/src/main/java/com/hafsaIlyas/expensetracker/ui/navigation/Screen.kt
package com.hafsaIlyas.expensetracker.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard   : Screen("dashboard")
    object AddExpense  : Screen("add_expense?expenseId={expenseId}") {
        fun createRoute(expenseId: Long? = null) =
            if (expenseId != null) "add_expense?expenseId=$expenseId" else "add_expense"
    }
    object ExpenseList : Screen("expense_list")
    object AiInsights  : Screen("ai_insights")
    object Settings    : Screen("settings")      // ← new
}