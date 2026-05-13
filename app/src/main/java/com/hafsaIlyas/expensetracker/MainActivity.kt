package com.hafsaIlyas.expensetracker

// MainActivity.kt
//
// App flow:
//   First install  → SplashScreen → OnboardingScreen → MainScaffold
//   Later launches → SplashScreen → MainScaffold
//
// How it works:
//   1. SplashScreen runs its own timed animation then calls onSplashComplete().
//   2. AppRoot then reads OnboardingViewModel.onboardingCompleted (DataStore).
//      While it's null (still loading from disk) we show nothing — the splash
//      has already exited so the transition is instant in practice.
//   3. false → show OnboardingScreen; true → show MainScaffold directly.
//   4. OnboardingScreen calls onFinished() which triggers markCompleted() and
//      advances the state so the main scaffold appears.

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.hafsaIlyas.expensetracker.ui.screens.onboarding.OnboardingScreen
import com.hafsaIlyas.expensetracker.ui.screens.onboarding.OnboardingViewModel
import com.hafsaIlyas.expensetracker.ui.screens.settings.SettingsViewModel
import com.hafsaIlyas.expensetracker.ui.screens.splash.SplashScreen
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

// ── App root ──────────────────────────────────────────────────────────────────
//
// State machine:
//   AppState.Splash       → show SplashScreen
//   AppState.Onboarding   → show OnboardingScreen  (first launch only)
//   AppState.Main         → show MainScaffold

private sealed class AppState {
    object Splash     : AppState()
    object Onboarding : AppState()
    object Main       : AppState()
}

@Composable
private fun AppRoot() {
    val onboardingVm  = hiltViewModel<OnboardingViewModel>()
    val onboardingDone by onboardingVm.onboardingCompleted.collectAsState()

    // Start in Splash; once splash finishes we advance based on the flag
    var appState by remember { mutableStateOf<AppState>(AppState.Splash) }

    // Called when SplashScreen's exit animation completes
    val onSplashComplete: () -> Unit = {
        // onboardingDone: null = loading, false = show onboarding, true = go to main
        appState = when (onboardingDone) {
            true  -> AppState.Main
            false -> AppState.Onboarding
            null  -> AppState.Main   // DataStore loaded before splash ends in practice;
            // fall back to Main to avoid a blank screen
        }
    }

    // If onboardingDone loads *after* splash already completed (rare, disk lag),
    // keep the transition correct:
    LaunchedEffect(onboardingDone, appState) {
        if (appState == AppState.Main && onboardingDone == false) {
            // Splash already done but flag says first launch — show onboarding
            appState = AppState.Onboarding
        }
    }

    AnimatedContent(
        targetState   = appState,
        label         = "app_root_transition",
        transitionSpec = {
            fadeIn(tween(400)) togetherWith fadeOut(tween(300))
        }
    ) { state ->
        when (state) {
            AppState.Splash -> {
                SplashScreen(onSplashComplete = onSplashComplete)
            }

            AppState.Onboarding -> {
                OnboardingScreen(
                    onFinished = {
                        onboardingVm.markCompleted()
                        appState = AppState.Main
                    }
                )
            }

            AppState.Main -> {
                MainScaffold()
            }
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
        BottomNavItem(Screen.Dashboard,   "Dashboard", Icons.Filled.Home,       Icons.Outlined.Home),
        BottomNavItem(Screen.ExpenseList, "History",   Icons.Filled.Receipt,    Icons.Outlined.Receipt),
        BottomNavItem(Screen.AiInsights,  "AI",        Icons.Filled.AutoAwesome,Icons.Outlined.AutoAwesome),
        BottomNavItem(Screen.Settings,    "Settings",  Icons.Filled.Settings,   Icons.Outlined.Settings),
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
                    NavigationBar(tonalElevation = 0.dp) {
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