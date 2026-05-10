// app/src/main/java/com/hafsaIlyas/expensetracker/ui/navigation/NavGraph.kt
package com.hafsaIlyas.expensetracker.ui.navigation

// ui/navigation/NavGraph.kt
// Unchanged functionally — splash is handled in MainActivity, not NavGraph

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
    navController    : NavHostController,
    modifier         : Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Dashboard.route,
        modifier         = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

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

        composable(Screen.ExpenseList.route) {
            ExpenseListScreen(navController = navController)
        }

        composable(Screen.AiInsights.route) {
            AiInsightsScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}