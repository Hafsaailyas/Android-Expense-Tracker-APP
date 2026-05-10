package com.hafsaIlyas.expensetracker

// MainActivity.kt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
                AppRoot()
            }
        }
    }
}

// ── Root — splash → main flow ─────────────────────────────────────────────────

@Composable
private fun AppRoot() {
    var splashDone by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState  = splashDone,
        label        = "splash_to_main",
        transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) }
    ) { ready ->
        if (!ready) {
            com.hafsaIlyas.expensetracker.ui.screens.splash.SplashScreen(
                onSplashComplete = { splashDone = true }
            )
        } else {
            MainScaffold()
        }
    }
}

// ── Main scaffold ─────────────────────────────────────────────────────────────

private data class BottomNavItem(
    val screen      : Screen,
    val label       : String,
    val selectedIcon: ImageVector,
    val unselIcon   : ImageVector
)

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentDest   = navBackStack?.destination

    val settingsVm    = hiltViewModel<SettingsViewModel>()
    val settingsState by settingsVm.uiState.collectAsState()

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard,   "Dashboard", Icons.Filled.Home,        Icons.Outlined.Home),
        BottomNavItem(Screen.ExpenseList, "History",   Icons.Filled.Receipt,      Icons.Outlined.Receipt),
        BottomNavItem(Screen.AiInsights,  "AI",        Icons.Filled.AutoAwesome,  Icons.Outlined.AutoAwesome),
        BottomNavItem(Screen.Settings,    "Settings",  Icons.Filled.Settings,     Icons.Outlined.Settings),
    )

    val hideBottomBar = currentDest?.route in listOf(Screen.AddExpense.route)

    ExpenseTrackerTheme(
        appTheme     = settingsState.appTheme,
        dynamicColor = settingsState.dynamicColor
    ) {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = !hideBottomBar,
                    enter   = slideInVertically { it } + fadeIn(),
                    exit    = slideOutVertically { it } + fadeOut()
                ) {
                    NavigationBar(tonalElevation = 0.dp) {   // ← fixed: was dp.times(0)
                        bottomNavItems.forEach { item ->
                            val selected = currentDest?.hierarchy?.any {
                                it.route == item.screen.route
                            } == true

                            val iconScale by animateFloatAsState(
                                targetValue   = if (selected) 1.18f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness    = Spring.StiffnessMedium
                                ),
                                label = "nav_scale_${item.label}"
                            )

                            NavigationBarItem(
                                selected = selected,
                                onClick  = {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector        = if (selected) item.selectedIcon else item.unselIcon,
                                        contentDescription = item.label,
                                        modifier           = Modifier.scale(iconScale)
                                    )
                                },
                                label           = { Text(item.label) },
                                alwaysShowLabel = true
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