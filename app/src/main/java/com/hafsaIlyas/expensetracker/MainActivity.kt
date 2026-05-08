package com.hafsaIlyas.expensetracker
// MainActivity.kt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hafsaIlyas.expensetracker.ui.navigation.ExpenseNavGraph
import com.hafsaIlyas.expensetracker.ui.navigation.Screen
import com.hafsaIlyas.expensetracker.ui.screens.settings.SettingsViewModel
import com.hafsaIlyas.expensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                MainScaffold()
            }
        }
    }
}

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentDest   = navBackStack?.destination

    val prefsRepo     = hiltViewModel<SettingsViewModel>()
    val settingsState by prefsRepo.uiState.collectAsState()

    val bottomNavItems = listOf(
        Triple(Screen.Dashboard,   "Dashboard",   Icons.Default.Home),
        Triple(Screen.ExpenseList, "History",     Icons.Default.List),
        Triple(Screen.AiInsights,  "AI Insights", Icons.Default.AutoAwesome),
        Triple(Screen.Settings,    "Settings",    Icons.Default.Settings),
    )

    // Hide bottom bar only on AddExpense (has its own UI chrome)
    val hideBottomBar = currentDest?.route in listOf(
        Screen.AddExpense.route
    )

    ExpenseTrackerTheme(
        appTheme     = settingsState.appTheme,
        dynamicColor = settingsState.dynamicColor
    ) {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = !hideBottomBar,
                    enter   = slideInVertically { it },
                    exit    = slideOutVertically { it }
                ) {
                    NavigationBar {
                        bottomNavItems.forEach { (screen, label, icon) ->
                            NavigationBarItem(
                                icon     = { Icon(icon, label) },
                                label    = { Text(label) },
                                selected = currentDest?.hierarchy?.any {
                                    it.route == screen.route
                                } == true,
                                onClick  = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            ExpenseNavGraph(navController, Modifier.padding(innerPadding))
        }
    }
}