package com.hafsaIlyas.expensetracker.ui.screens.settings

// ui/screens/settings/SettingsScreen.kt
// Pixel-exact match of the HTML SpendSense settings screen (second revision):
//
//  Groups (white cards, border-radius 18dp, border 0.5dp rgba(26,95,122,0.1)):
//   1. APPEARANCE    — Theme seg-control (Light|Dark|Auto) + Dynamic color toggle
//   2. BUDGET        — Monthly budget row (value + chevron) + Budget usage bar
//   3. NOTIFICATIONS — Budget alerts toggle + Daily reminder (value + chevron)
//   4. DATA & EXPORT — Export CSV + Export Text + Clear all data (red)
//   5. ABOUT         — 4dp gradient strip + name + version + heart
//
//  Row anatomy : 20dp teal icon | 14sp medium label (weight 1) | trailing
//  Row padding : vertical 14dp, horizontal 16dp, gap 12dp
//  Divider     : 0.5dp #f5f5f5 between rows (not after last)
//  Group label : 11sp bold teal uppercase, letter-spacing 0.08em, pad 12/16/4

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
// Colours — taken verbatim from HTML :root variables & class rules
// ─────────────────────────────────────────────────────────────────────────────
private val ColBg        = Color(0xFFF0F4F8)   // --bg
private val ColSurface   = Color(0xFFFFFFFF)   // --surface / .settings-group bg
private val ColBorder    = Color(0x1A1A5F7A)   // --border  rgba(26,95,122,0.10)
private val ColDivider   = Color(0xFFF5F5F5)   // .settings-row border-bottom
private val ColPrimary   = Color(0xFF1A5F7A)   // --primary
private val ColSecondary = Color(0xFF2C7865)   // --secondary
private val ColGold      = Color(0xFFF9D56E)   // --gold
private val ColTextDark  = Color(0xFF1A1F2E)   // --text-dark
private val ColTextMuted = Color(0xFF888888)   // --text-muted
private val ColTextLight = Color(0xFFAAAAAA)   // --text-light
private val ColDanger    = Color(0xFFC62828)   // --danger
private val ColWarning   = Color(0xFFE65100)   // --warning  (heart icon colour)

// About strip : linear-gradient(90deg, primary, secondary, gold)
private val GradientAbout  = listOf(ColPrimary, ColSecondary, ColGold)
// Budget fill : linear-gradient(90deg, primary, secondary)
private val GradientBudget = listOf(ColPrimary, ColSecondary)

// ─────────────────────────────────────────────────────────────────────────────
// Screen root
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    navController : NavController,
    viewModel     : SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            title   = { Text("Clear all data?", fontWeight = FontWeight.Bold) },
            text    = { Text("This will permanently delete every expense. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAllData(); showClearDialog = false }) {
                    Text("Delete", color = ColDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = ColBg
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Page title  (.settings-topbar h2) ────────────────────────────
            // HTML: padding 52px top (below notch), 20px sides, 16px bottom
            // font: 26px / 800 / letter-spacing -0.8px
            Text(
                text          = "Settings",
                fontSize      = 26.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = ColTextDark,
                letterSpacing = (-0.8).sp,
                modifier      = Modifier.padding(
                    start  = 20.dp, top    = 52.dp,
                    end    = 20.dp, bottom = 16.dp
                )
            )

            // ── Group list  (.settings-body) ─────────────────────────────────
            // HTML: padding 0 16px, flex-direction column, gap 14px
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── 1. APPEARANCE ─────────────────────────────────────────────
                SettingsGroup {
                    GroupLabel("Appearance")

                    // Theme — segmented control Light | Dark | Auto
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

                    // Dynamic color — toggle (only functional on API 31+)
                    SettingsRow(icon = Icons.Default.Palette, label = "Dynamic color", last = true) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            HtmlToggle(
                                checked         = uiState.dynamicColor,
                                onCheckedChange = viewModel::setDynamicColor
                            )
                        } else {
                            Text("Android 12+", fontSize = 11.sp, color = ColTextLight)
                        }
                    }
                }

                // ── 2. BUDGET ─────────────────────────────────────────────────
                SettingsGroup {
                    GroupLabel("Budget")

                    // Monthly budget row: muted value + chevron
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
                            color    = ColTextMuted
                        )
                        Icon(Icons.Default.ChevronRight, null,
                            Modifier.size(18.dp), tint = ColTextLight)
                    }

                    // Budget usage bar — full-width, no leading icon
                    // HTML: .settings-row.budget-section  flex-direction:column, gap:8px
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
                        // "Budget usage"  |  "68% used"
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("Budget usage",
                                fontSize   = 14.sp, fontWeight = FontWeight.Medium,
                                color      = ColTextDark)
                            Text("${(pct * 100).toInt()}% used",
                                fontSize   = 14.sp, fontWeight = FontWeight.Bold,
                                color      = ColPrimary)
                        }

                        // Track + fill  (height 8dp, radius 4dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE8EDF0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(pct.toFloat())
                                    .fillMaxHeight()
                                    .background(Brush.horizontalGradient(GradientBudget))
                            )
                        }

                        // "$X spent"  |  "$Y remaining"
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$${"%,.0f".format(spent)} spent",
                                fontSize = 11.sp, color = ColTextMuted)
                            Text("$${"%,.0f".format(left)} remaining",
                                fontSize = 11.sp, color = ColTextMuted)
                        }
                    }
                }

                // ── 3. NOTIFICATIONS ──────────────────────────────────────────
                SettingsGroup {
                    GroupLabel("Notifications")

                    // Budget alerts toggle
                    SettingsRow(icon = Icons.Default.Notifications,
                        label = "Budget alerts", last = false) {
                        HtmlToggle(
                            checked         = uiState.budgetAlertsEnabled,
                            onCheckedChange = viewModel::setBudgetAlertsEnabled
                        )
                    }

                    // Daily reminder — muted time value + chevron
                    SettingsRow(
                        icon      = Icons.Default.Schedule,
                        label     = "Daily reminder",
                        last      = true,
                        clickable = { /* TODO: open time picker */ }
                    ) {
                        Text(
                            text     = uiState.dailyReminderTime.ifBlank { "9:00 PM" },
                            fontSize = 13.sp,
                            color    = ColTextMuted
                        )
                        Icon(Icons.Default.ChevronRight, null,
                            Modifier.size(18.dp), tint = ColTextLight)
                    }
                }

                // ── 4. DATA & EXPORT ──────────────────────────────────────────
                SettingsGroup {
                    GroupLabel("Data & Export")

                    // Export CSV
                    SettingsRow(
                        icon      = Icons.Default.TableChart,
                        label     = "Export to CSV",
                        last      = false,
                        clickable = { if (!uiState.isExporting) viewModel.exportAsCsv() }
                    ) {
                        ExportTrailing(uiState.isExporting)
                    }

                    // Export Text
                    SettingsRow(
                        icon      = Icons.Default.Description,
                        label     = "Export to Text",
                        last      = false,
                        clickable = { if (!uiState.isExporting) viewModel.exportAsText() }
                    ) {
                        ExportTrailing(uiState.isExporting)
                    }

                    // Clear all data — danger colour on both icon and label
                    SettingsRow(
                        icon       = Icons.Default.DeleteForever,
                        label      = "Clear all data",
                        last       = true,
                        iconTint   = ColDanger,
                        labelColor = ColDanger,
                        clickable  = { showClearDialog = true }
                    ) {
                        Icon(Icons.Default.ChevronRight, null,
                            Modifier.size(18.dp), tint = ColTextLight)
                    }
                }

                // ── 5. ABOUT ──────────────────────────────────────────────────
                // HTML: .settings-group > .about-strip (4dp) + .about-row (16dp pad)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(0.5.dp, ColBorder, RoundedCornerShape(18.dp))
                        .background(ColSurface)
                ) {
                    Column {
                        // 4dp gradient strip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Brush.horizontalGradient(GradientAbout))
                        )
                        // Name + version + heart
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
                                    color    = ColTextMuted
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

// ─────────────────────────────────────────────────────────────────────────────
// Sub-components
// ─────────────────────────────────────────────────────────────────────────────

/**
 * White rounded group card.
 * HTML: .settings-group  background:#fff, border-radius:18px, border:0.5px solid var(--border)
 */
@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(0.5.dp, ColBorder, RoundedCornerShape(18.dp))
            .background(ColSurface),
        content = content
    )
}

/**
 * Teal uppercase group label.
 * HTML: .group-label  font-size:11px, font-weight:700, color:primary,
 *                     padding:12px 16px 4px, letter-spacing:0.08em
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
 * HTML: .settings-row  padding:14px 16px, gap:12px, border-bottom:0.5dp #f5f5f5
 *
 * @param iconTint   Leading icon tint; defaults to teal, pass [ColDanger] for destructive rows.
 * @param labelColor Label text colour; defaults to dark, pass [ColDanger] for destructive rows.
 */
@Composable
private fun SettingsRow(
    icon       : ImageVector,
    label      : String,
    last       : Boolean,
    iconTint   : Color             = ColPrimary,
    labelColor : Color             = ColTextDark,
    clickable  : (() -> Unit)?     = null,
    trailing   : @Composable RowScope.() -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (clickable != null) Modifier.clickable { clickable() } else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Leading icon (24dp column keeps text-alignment consistent)
            Icon(
                imageVector        = icon,
                contentDescription = null,
                modifier           = Modifier.size(20.dp),
                tint               = iconTint
            )

            // Label — flex 1
            Text(
                text       = label,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = labelColor,
                modifier   = Modifier.weight(1f)
            )

            // Trailing content
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) { trailing() }
        }

        if (!last) HorizontalDivider(color = ColDivider, thickness = 0.5.dp)
    }
}

/**
 * Chevron or spinner used as the trailing control on export rows.
 */
@Composable
private fun RowScope.ExportTrailing(isExporting: Boolean) {
    if (isExporting) {
        CircularProgressIndicator(
            modifier    = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color       = ColTextLight
        )
    } else {
        Icon(Icons.Default.ChevronRight, null,
            Modifier.size(18.dp), tint = ColTextLight)
    }
}

/**
 * Three-segment theme control.
 * HTML: .seg-control  background:#f0f4f8, border-radius:10px, padding:3px
 *       .seg-opt      font-size:11px, font-weight:600, padding:5px 10px, radius:7px
 *       .seg-opt.active  background:primary, color:#fff
 */
@Composable
private fun SegControl(
    options  : List<String>,
    selected : String,
    onSelect : (String) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF0F4F8))
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
                    color      = if (active) Color.White else ColTextMuted
                )
            }
        }
    }
}

/**
 * Toggle switch sized to match the HTML .toggle-switch (44×26dp track).
 * HTML: background:primary, knob:#fff, border-radius:13px
 */
@Composable
private fun HtmlToggle(
    checked         : Boolean,
    onCheckedChange : (Boolean) -> Unit
) {
    Switch(
        checked         = checked,
        onCheckedChange = onCheckedChange,
        colors          = SwitchDefaults.colors(
            checkedTrackColor   = ColPrimary,
            checkedThumbColor   = Color.White,
            uncheckedTrackColor = Color(0xFFCCCCCC),
            uncheckedThumbColor = Color.White
        )
    )
}