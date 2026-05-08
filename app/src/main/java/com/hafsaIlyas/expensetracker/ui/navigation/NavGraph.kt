// app/src/main/java/com/hafsaIlyas/expensetracker/ui/navigation/NavGraph.kt
package com.hafsaIlyas.expensetracker.ui.navigation
// ui/navigation/NavGraph.kt

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hafsaIlyas.expensetracker.ui.screens.addexpense.AddExpenseScreen
import com.hafsaIlyas.expensetracker.ui.screens.aiinsights.AiInsightsScreen
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.DashboardScreen
import com.hafsaIlyas.expensetracker.ui.screens.expenselist.ExpenseListScreen
import com.hafsaIlyas.expensetracker.ui.screens.settings.SettingsScreen

@Composable
fun ExpenseNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Dashboard.route,
        modifier         = modifier
    ) {

        // Dashboard — placeholder until Day 3
        // ui/navigation/NavGraph.kt  — one-line change: wire DashboardScreen
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)  // ← was PlaceholderScreen
        }
// All other routes unchanged from Step 2

        // Add / Edit Expense
        composable(
            route     = Screen.AddExpense.route,
            arguments = listOf(
                navArgument("expenseId") {
                    type         = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddExpenseScreen(navController = navController)
        }

        // Expense List
        composable(Screen.ExpenseList.route) {
            ExpenseListScreen(navController = navController)
        }

        // AI Insights — placeholder until Day 5
        // In NavGraph.kt — replace the AI placeholder:
        composable(Screen.AiInsights.route) {
            AiInsightsScreen(navController = navController)  // ← was PlaceholderScreen
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    androidx.compose.foundation.layout.Box(
        modifier            = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment    = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text  = label,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )
    }
}