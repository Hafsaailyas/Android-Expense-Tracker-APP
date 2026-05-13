package com.hafsaIlyas.expensetracker.ui.screens.settings

// ui/screens/settings/SettingsScreen.kt
// Pixel-exact match of the HTML SpendSense settings screen — with full dark mode support
// matching the design system used across AiInsightsScreen, InsightCard, DashboardScreen, etc.
//
//  Dark-mode palette mirrors existing screens:
//    bg         #0A1112   (ColorBgDark  in AiInsightsScreen)
//    surface    #1A1F2E   (ContainerDark in InsightCard / --dark-surface)
//    border     rgba(26,95,122,0.18)  (ContainerDarkBorder in InsightCard)
//    divider    #2A2F3E
//    text-dark  #FFFFFF
//    text-muted #AAAAAA   (ColTextLight)
//    text-light #666C7A
//    seg-bg     #252B3B   (slightly lighter than surface)
//    budget-track #252B3B
//
//  Groups (cards, border-radius 18dp, border 0.5dp themed):
//   1. APPEARANCE    — Theme seg-control (Light|Dark|Auto) + Dynamic color toggle
//   2. BUDGET        — Monthly budget row (value + chevron) + Budget usage bar
//   3. NOTIFICATIONS — Budget alerts toggle + Daily reminder (value + chevron)
//   4. DATA & EXPORT — Export CSV + Export Text + Clear all data (red)
//   5. ABOUT         — 4dp gradient strip + name + version + heart

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.ui.theme.AppTheme

// ─────────────────────────────────────────────────────────────────────────────
// Brand constants — identical across all screens
// ─────────────────────────────────────────────────────────────────────────────
private val ColPrimary   = Color(0xFF1A5F7A)   // --primary
private val ColSecondary = Color(0xFF2C7865)   // --secondary
private val ColGold      = Color(0xFFF9D56E)   // --gold
private val ColDanger    = Color(0xFFC62828)   // --danger
private val ColWarning   = Color(0xFFE65100)   // --warning  (heart icon colour)

// Light-mode tokens
private val ColBgLight        = Color(0xFFF0F4F8)   // --bg
private val ColSurfaceLight   = Color(0xFFFFFFFF)   // --surface
private val ColBorderLight    = Color(0x1A1A5F7A)   // rgba(26,95,122,0.10)
private val ColDividerLight   = Color(0xFFF5F5F5)
private val ColTextDarkLight  = Color(0xFF1A1F2E)   // --text-dark
private val ColTextMutedLight = Color(0xFF888888)   // --text-muted
private val ColTextLightL     = Color(0xFFAAAAAA)   // --text-light
private val ColSegBgLight     = Color(0xFFF0F4F8)
private val ColTrackLight     = Color(0xFFE8EDF0)

// Dark-mode tokens — aligned with AiInsightsScreen / InsightCard / DashboardScreen
private val ColBgDark        = Color(0xFF0A1112)   // --dark-bg  (AiInsightsScreen)
private val ColSurfaceDark   = Color(0xFF1A1F2E)   // --dark-surface  (InsightCard)
private val ColBorderDark    = Color(0x2E1A5F7A)   // rgba(26,95,122,0.18)
private val ColDividerDark   = Color(0xFF2A2F3E)
private val ColTextDarkDark  = Color(0xFFFFFFFF)
private val ColTextMutedDark = Color(0xFFAAAAAA)   // --text-light reused as muted in dark
private val ColTextLightDark = Color(0xFF666C7A)
private val ColSegBgDark     = Color(0xFF252B3B)
private val ColTrackDark     = Color(0xFF252B3B)

// Gradients (same in both modes)
private val GradientAbout  = listOf(ColPrimary, ColSecondary, ColGold)
private val GradientBudget = listOf(ColPrimary, ColSecondary)

// ─────────────────────────────────────────────────────────────────────────────
// Theme-aware colour bundle — resolved once, threaded via CompositionLocal
// ─────────────────────────────────────────────────────────────────────────────
private data class SettingsColors(
    val bg         : Color,
    val surface    : Color,
    val border     : Color,
    val divider    : Color,
    val textDark   : Color,
    val textMuted  : Color,
    val textLight  : Color,
    val segBg      : Color,
    val budgetTrack: Color,
    val isDark     : Boolean
)

private val LocalSettingsColors = compositionLocalOf<SettingsColors> {
    error("SettingsColors not provided")
}

/** Resolves the correct colour bundle for the current Material theme. */
@Composable
private fun rememberSettingsColors(): SettingsColors {
    val isDark = !MaterialTheme.colorScheme.background.isBright()
    return remember(isDark) {
        if (isDark) SettingsColors(
            bg          = ColBgDark,
            surface     = ColSurfaceDark,
            border      = ColBorderDark,
            divider     = ColDividerDark,
            textDark    = ColTextDarkDark,
            textMuted   = ColTextMutedDark,
            textLight   = ColTextLightDark,
            segBg       = ColSegBgDark,
            budgetTrack = ColTrackDark,
            isDark      = true
        ) else SettingsColors(
            bg          = ColBgLight,
            surface     = ColSurfaceLight,
            border      = ColBorderLight,
            divider     = ColDividerLight,
            textDark    = ColTextDarkLight,
            textMuted   = ColTextMutedLight,
            textLight   = ColTextLightL,
            segBg       = ColSegBgLight,
            budgetTrack = ColTrackLight,
            isDark      = false
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen root
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    navController : NavController,
    viewModel     : SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors  = rememberSettingsColors()

    // Share-sheet launcher (export)
    val shareLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.clearExportResult() }

    LaunchedEffect(uiState.exportResult) {
        val r = uiState.exportResult
        if (r is ExportResult.Ready) shareLauncher.launch(r.intent)
    }

    // Error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.exportResult) {
        val r = uiState.exportResult
        if (r is ExportResult.Error) {
            snackbarHostState.showSnackbar(r.message)
            viewModel.clearExportResult()
        }
    }

    // Clear-data confirmation dialog
    var showClearDialog by remember { mutableStateOf(false) }
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor   = colors.surface,
            titleContentColor = colors.textDark,
            textContentColor  = colors.textMuted,
            title   = { Text("Clear all data?", fontWeight = FontWeight.Bold) },
            text    = { Text("This will permanently delete every expense. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAllData(); showClearDialog = false }) {
                    Text("Delete", color = ColDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = colors.textMuted)
                }
            }
        )
    }

    CompositionLocalProvider(LocalSettingsColors provides colors) {
        Scaffold(
            snackbarHost   = { SnackbarHost(snackbarHostState) },
            containerColor = colors.bg
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {

                // ── Page title ────────────────────────────────────────────────
                Text(
                    text          = "Settings",
                    fontSize      = 26.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = colors.textDark,
                    letterSpacing = (-0.8).sp,
                    modifier      = Modifier.padding(
                        start  = 20.dp, top    = 52.dp,
                        end    = 20.dp, bottom = 16.dp
                    )
                )

                // ── Group list ────────────────────────────────────────────────
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // ── 1. APPEARANCE ─────────────────────────────────────────
                    SettingsGroup {
                        GroupLabel("Appearance")

                        SettingsRow(icon = Icons.Default.DarkMode, label = "Theme", last = false) {
                            SegControl(
                                options  = listOf("Light", "Dark", "Auto"),
                                selected = when (uiState.appTheme) {
                                    AppTheme.LIGHT  -> "Light"
                                    AppTheme.DARK   -> "Dark"
                                    AppTheme.SYSTEM -> "Auto"
                                },
                                onSelect = { opt ->
                                    viewModel.setTheme(
                                        when (opt) {
                                            "Light" -> AppTheme.LIGHT
                                            "Dark"  -> AppTheme.DARK
                                            else    -> AppTheme.SYSTEM
                                        }
                                    )
                                }
                            )
                        }

                        SettingsRow(icon = Icons.Default.Palette, label = "Dynamic color", last = true) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                HtmlToggle(
                                    checked         = uiState.dynamicColor,
                                    onCheckedChange = viewModel::setDynamicColor
                                )
                            } else {
                                Text("Android 12+", fontSize = 11.sp, color = colors.textLight)
                            }
                        }
                    }

                    // ── 2. BUDGET ─────────────────────────────────────────────
                    SettingsGroup {
                        GroupLabel("Budget")

                        SettingsRow(
                            icon      = Icons.Default.AccountBalanceWallet,
                            label     = "Monthly budget",
                            last      = false,
                            clickable = { /* TODO: open budget editor */ }
                        ) {
                            Text(
                                text     = if (uiState.monthlyBudget > 0)
                                    "$${"%,.0f".format(uiState.monthlyBudget)}" else "Not set",
                                fontSize = 13.sp,
                                color    = colors.textMuted
                            )
                            Icon(Icons.Default.ChevronRight, null,
                                Modifier.size(18.dp), tint = colors.textLight)
                        }

                        // Budget usage bar
                        val budget = uiState.monthlyBudget
                        val spent  = uiState.currentMonthSpent
                        val pct    = if (budget > 0) (spent / budget).coerceIn(0.0, 1.0) else 0.0
                        val left   = (budget - spent).coerceAtLeast(0.0)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text("Budget usage",
                                    fontSize   = 14.sp, fontWeight = FontWeight.Medium,
                                    color      = colors.textDark)
                                Text("${(pct * 100).toInt()}% used",
                                    fontSize   = 14.sp, fontWeight = FontWeight.Bold,
                                    color      = ColPrimary)
                            }

                            // Track + gradient fill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.budgetTrack)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(pct.toFloat())
                                        .fillMaxHeight()
                                        .background(Brush.horizontalGradient(GradientBudget))
                                )
                            }

                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("$${"%,.0f".format(spent)} spent",
                                    fontSize = 11.sp, color = colors.textMuted)
                                Text("$${"%,.0f".format(left)} remaining",
                                    fontSize = 11.sp, color = colors.textMuted)
                            }
                        }
                    }

                    // ── 3. NOTIFICATIONS ──────────────────────────────────────
                    SettingsGroup {
                        GroupLabel("Notifications")

                        SettingsRow(icon = Icons.Default.Notifications,
                            label = "Budget alerts", last = false) {
                            HtmlToggle(
                                checked         = uiState.budgetAlertsEnabled,
                                onCheckedChange = viewModel::setBudgetAlertsEnabled
                            )
                        }

                        SettingsRow(
                            icon      = Icons.Default.Schedule,
                            label     = "Daily reminder",
                            last      = true,
                            clickable = { /* TODO: open time picker */ }
                        ) {
                            Text(
                                text     = uiState.dailyReminderTime.ifBlank { "9:00 PM" },
                                fontSize = 13.sp,
                                color    = colors.textMuted
                            )
                            Icon(Icons.Default.ChevronRight, null,
                                Modifier.size(18.dp), tint = colors.textLight)
                        }
                    }

                    // ── 4. DATA & EXPORT ──────────────────────────────────────
                    SettingsGroup {
                        GroupLabel("Data & Export")

                        SettingsRow(
                            icon      = Icons.Default.TableChart,
                            label     = "Export to CSV",
                            last      = false,
                            clickable = { if (!uiState.isExporting) viewModel.exportAsCsv() }
                        ) {
                            ExportTrailing(uiState.isExporting)
                        }

                        SettingsRow(
                            icon      = Icons.Default.Description,
                            label     = "Export to Text",
                            last      = false,
                            clickable = { if (!uiState.isExporting) viewModel.exportAsText() }
                        ) {
                            ExportTrailing(uiState.isExporting)
                        }

                        SettingsRow(
                            icon       = Icons.Default.DeleteForever,
                            label      = "Clear all data",
                            last       = true,
                            iconTint   = ColDanger,
                            labelColor = ColDanger,
                            clickable  = { showClearDialog = true }
                        ) {
                            Icon(Icons.Default.ChevronRight, null,
                                Modifier.size(18.dp), tint = colors.textLight)
                        }
                    }

                    // ── 5. ABOUT ──────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                            .background(colors.surface)
                    ) {
                        Column {
                            // 4dp gradient strip (same in both modes)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(Brush.horizontalGradient(GradientAbout))
                            )
                            Row(
                                modifier              = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text          = "SpendSense",
                                        fontSize      = 16.sp,
                                        fontWeight    = FontWeight.ExtraBold,
                                        color         = ColPrimary,
                                        letterSpacing = (-0.3).sp
                                    )
                                    Text(
                                        text     = "Version 1.0.0 · Built with ❤\uFE0F",
                                        fontSize = 12.sp,
                                        color    = colors.textMuted
                                    )
                                }
                                Icon(
                                    imageVector        = Icons.Default.Favorite,
                                    contentDescription = null,
                                    modifier           = Modifier.size(24.dp),
                                    tint               = ColWarning
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-components — all consume LocalSettingsColors instead of hardcoded values
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Themed rounded group card.
 * Light: white bg + primary-tinted 0.5dp border.
 * Dark:  #1A1F2E bg + primary-tinted 0.5dp border (matching InsightCard).
 */
@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalSettingsColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
            .background(colors.surface),
        content = content
    )
}

/**
 * Teal uppercase group label — colour unchanged across modes (primary teal reads well on both).
 */
@Composable
private fun GroupLabel(text: String) {
    Text(
        text          = text.uppercase(),
        fontSize      = 11.sp,
        fontWeight    = FontWeight.Bold,
        color         = ColPrimary,
        letterSpacing = 0.08.sp,
        modifier      = Modifier.padding(start = 16.dp, top = 12.dp,
            end = 16.dp, bottom = 4.dp)
    )
}

/**
 * Single settings row: [icon] [label………………] [trailing]
 * Divider colour switches from #F5F5F5 (light) to #2A2F3E (dark).
 */
@Composable
private fun SettingsRow(
    icon       : ImageVector,
    label      : String,
    last       : Boolean,
    iconTint   : Color             = ColPrimary,
    labelColor : Color?            = null,          // null → resolves from LocalSettingsColors
    clickable  : (() -> Unit)?     = null,
    trailing   : @Composable RowScope.() -> Unit
) {
    val colors     = LocalSettingsColors.current
    val resolvedLabel = labelColor ?: colors.textDark

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (clickable != null) Modifier.clickable { clickable() } else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                modifier           = Modifier.size(20.dp),
                tint               = iconTint
            )

            Text(
                text       = label,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = resolvedLabel,
                modifier   = Modifier.weight(1f)
            )

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) { trailing() }
        }

        if (!last) HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
    }
}

/**
 * Chevron or spinner for export rows.
 */
@Composable
private fun RowScope.ExportTrailing(isExporting: Boolean) {
    val colors = LocalSettingsColors.current
    if (isExporting) {
        CircularProgressIndicator(
            modifier    = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color       = colors.textLight
        )
    } else {
        Icon(Icons.Default.ChevronRight, null,
            Modifier.size(18.dp), tint = colors.textLight)
    }
}

/**
 * Three-segment theme control.
 * Dark mode: #252B3B track, inactive label uses dark text-light.
 * Active segment: always primary teal with white label.
 */
@Composable
private fun SegControl(
    options  : List<String>,
    selected : String,
    onSelect : (String) -> Unit
) {
    val colors = LocalSettingsColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.segBg)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEach { opt ->
            val active = opt == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (active) ColPrimary else Color.Transparent)
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = opt,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = when {
                        active            -> Color.White
                        colors.isDark     -> colors.textMuted
                        else              -> ColTextMutedLight
                    }
                )
            }
        }
    }
}

/**
 * Toggle switch — track colour is always primary teal when checked, thumb is always white.
 * Unchecked track: #CCCCCC (light) / #3A3F4E (dark) for better visibility on dark backgrounds.
 */
@Composable
private fun HtmlToggle(
    checked         : Boolean,
    onCheckedChange : (Boolean) -> Unit
) {
    val colors = LocalSettingsColors.current
    Switch(
        checked         = checked,
        onCheckedChange = onCheckedChange,
        colors          = SwitchDefaults.colors(
            checkedTrackColor   = ColPrimary,
            checkedThumbColor   = Color.White,
            uncheckedTrackColor = if (colors.isDark) Color(0xFF3A3F4E) else Color(0xFFCCCCCC),
            uncheckedThumbColor = Color.White
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

/** True when the colour's perceived luminance puts it in "light mode" territory. */
private fun Color.isBright(): Boolean =
    (0.2126f * red + 0.7152f * green + 0.0722f * blue) > 0.5f