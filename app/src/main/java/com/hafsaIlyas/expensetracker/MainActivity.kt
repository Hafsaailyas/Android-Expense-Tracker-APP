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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hafsaIlyas.expensetracker.ui.navigation.ExpenseNavGraph
import com.hafsaIlyas.expensetracker.ui.navigation.Screen
import com.hafsaIlyas.expensetracker.ui.screens.settings.SettingsViewModel
import com.hafsaIlyas.expensetracker.ui.splash.SplashScreen
import com.hafsaIlyas.expensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Outer theme shell needed so SplashScreen has MaterialTheme available
            ExpenseTrackerTheme {
                AppEntry()
            }
        }
    }
}

/**
 * Top-level composable that gates the main app behind the splash screen.
 * Splash is shown once per process; once [splashDone] flips to true the
 * animated crossfade reveals [MainScaffold].
 */
@Composable
private fun AppEntry() {
    var splashDone by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState   = splashDone,
        transitionSpec = {
            fadeIn(tween(400)) togetherWith fadeOut(tween(300))
        },
        label = "splash_to_main"
    ) { done ->
        if (done) {
            MainScaffold()
        } else {
            SplashScreen(onSplashComplete = { splashDone = true })
        }
    }
}

// ─── Data class for bottom-nav items ──────────────────────────────────────
private data class BottomNavItem(
    val screen: Screen,
    val label : String,
    val icon  : androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentDest   = navBackStack?.destination

    val settingsVm    = hiltViewModel<SettingsViewModel>()
    val settingsState by settingsVm.uiState.collectAsState()

    val bottomNavItems = remember {
        listOf(
            BottomNavItem(Screen.Dashboard,   "Dashboard",   Icons.Default.Home),
            BottomNavItem(Screen.ExpenseList, "History",     Icons.Default.List),
            BottomNavItem(Screen.AiInsights,  "AI Insights", Icons.Default.AutoAwesome),
            BottomNavItem(Screen.Settings,    "Settings",    Icons.Default.Settings),
        )
    }

    // Hide bottom bar on screens that provide their own chrome
    val hideBottomBar = currentDest?.route in listOf(Screen.AddExpense.route)

    ExpenseTrackerTheme(
        appTheme     = settingsState.appTheme,
        dynamicColor = settingsState.dynamicColor,
    ) {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = !hideBottomBar,
                    enter   = slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness    = Spring.StiffnessMediumLow,
                        )
                    ) { it },
                    exit    = slideOutVertically(
                        animationSpec = tween(220, easing = FastOutLinearInEasing)
                    ) { it },
                ) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            val selected = currentDest?.hierarchy?.any {
                                it.route == item.screen.route
                            } == true

                            // Spring-driven icon scale on selection
                            val iconScale by animateFloatAsState(
                                targetValue   = if (selected) 1.18f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness    = Spring.StiffnessMedium,
                                ),
                                label = "icon_scale_${item.label}"
                            )

                            NavigationBarItem(
                                icon     = {
                                    Icon(
                                        imageVector        = item.icon,
                                        contentDescription = item.label,
                                        modifier           = Modifier.scale(iconScale),
                                    )
                                },
                                label    = { Text(item.label) },
                                selected = selected,
                                onClick  = {
                                    navController.navigate(item.screen.route) {
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