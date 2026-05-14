package com.hafsaIlyas.expensetracker.ui.screens.settings

// ui/screens/settings/SettingsScreen.kt

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.data.currency.Currency
import com.hafsaIlyas.expensetracker.data.currency.CurrencyFormatter
import com.hafsaIlyas.expensetracker.data.currency.SUPPORTED_CURRENCIES
import com.hafsaIlyas.expensetracker.ui.theme.AppTheme

// ─────────────────────────────────────────────────────────────────────────────
// Brand constants — identical across all screens
// ─────────────────────────────────────────────────────────────────────────────
private val ColPrimary   = Color(0xFF1A5F7A)
private val ColSecondary = Color(0xFF2C7865)
private val ColGold      = Color(0xFFF9D56E)
private val ColDanger    = Color(0xFFC62828)
private val ColWarning   = Color(0xFFE65100)

private val ColBgLight        = Color(0xFFF0F4F8)
private val ColSurfaceLight   = Color(0xFFFFFFFF)
private val ColBorderLight    = Color(0x1A1A5F7A)
private val ColDividerLight   = Color(0xFFF5F5F5)
private val ColTextDarkLight  = Color(0xFF1A1F2E)
private val ColTextMutedLight = Color(0xFF888888)
private val ColTextLightL     = Color(0xFFAAAAAA)
private val ColSegBgLight     = Color(0xFFF0F4F8)
private val ColTrackLight     = Color(0xFFE8EDF0)

private val ColBgDark        = Color(0xFF0A1112)
private val ColSurfaceDark   = Color(0xFF1A1F2E)
private val ColBorderDark    = Color(0x2E1A5F7A)
private val ColDividerDark   = Color(0xFF2A2F3E)
private val ColTextDarkDark  = Color(0xFFFFFFFF)
private val ColTextMutedDark = Color(0xFFAAAAAA)
private val ColTextLightDark = Color(0xFF666C7A)
private val ColSegBgDark     = Color(0xFF252B3B)
private val ColTrackDark     = Color(0xFF252B3B)

private val GradientAbout  = listOf(ColPrimary, ColSecondary, ColGold)
private val GradientBudget = listOf(ColPrimary, ColSecondary)

// ─────────────────────────────────────────────────────────────────────────────
// Theme-aware colour bundle
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

@Composable
private fun rememberSettingsColors(): SettingsColors {
    val isDark = !MaterialTheme.colorScheme.background.isBright()
    return remember(isDark) {
        if (isDark) SettingsColors(
            bg = ColBgDark, surface = ColSurfaceDark, border = ColBorderDark,
            divider = ColDividerDark, textDark = ColTextDarkDark,
            textMuted = ColTextMutedDark, textLight = ColTextLightDark,
            segBg = ColSegBgDark, budgetTrack = ColTrackDark, isDark = true
        ) else SettingsColors(
            bg = ColBgLight, surface = ColSurfaceLight, border = ColBorderLight,
            divider = ColDividerLight, textDark = ColTextDarkLight,
            textMuted = ColTextMutedLight, textLight = ColTextLightL,
            segBg = ColSegBgLight, budgetTrack = ColTrackLight, isDark = false
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

    var showBudgetDialog by remember { mutableStateOf(false) }

    // Collect the one-time event from ViewModel to auto-open budget dialog
    LaunchedEffect(Unit) {
        viewModel.openBudgetDialogEvent.collect {
            showBudgetDialog = true
        }
    }

    val shareLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.clearExportResult() }

    LaunchedEffect(uiState.exportResult) {
        val r = uiState.exportResult
        if (r is ExportResult.Ready) shareLauncher.launch(r.intent)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.exportResult) {
        val r = uiState.exportResult
        if (r is ExportResult.Error) {
            snackbarHostState.showSnackbar(r.message)
            viewModel.clearExportResult()
        }
    }

    var showClearDialog    by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    // ── Clear-data dialog ─────────────────────────────────────────────────────
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest  = { showClearDialog = false },
            containerColor    = colors.surface,
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

    // ── Currency picker dialog ────────────────────────────────────────────────
    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            currentCurrency = uiState.currentCurrency,
            colors          = colors,
            onSelect        = { currency ->
                viewModel.setCurrency(currency)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    // ── Budget editor dialog ─────────────────────────────────────────────────
    if (showBudgetDialog) {
        BudgetEditorDialog(
            currentInput    = uiState.budgetInput,
            currencySymbol  = uiState.currentCurrency.symbol,
            colors          = colors,
            onInputChange   = viewModel::onBudgetInputChange,
            onSave          = {
                viewModel.saveBudget()
                showBudgetDialog = false
            },
            onDismiss = { showBudgetDialog = false }
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
                        start = 20.dp, top = 52.dp, end = 20.dp, bottom = 16.dp
                    )
                )

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
                                    viewModel.setTheme(when (opt) {
                                        "Light" -> AppTheme.LIGHT
                                        "Dark"  -> AppTheme.DARK
                                        else    -> AppTheme.SYSTEM
                                    })
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

                    // ── 2. CURRENCY ───────────────────────────────────────────
                    SettingsGroup {
                        GroupLabel("Currency")

                        SettingsRow(
                            icon      = Icons.Default.CurrencyExchange,
                            label     = "Display currency",
                            last      = true,
                            clickable = { showCurrencyDialog = true }
                        ) {
                            val cur = uiState.currentCurrency
                            Text(
                                text     = "${cur.symbol}  ${cur.code}",
                                fontSize = 13.sp,
                                color    = colors.textMuted
                            )
                            Icon(
                                Icons.Default.ChevronRight, null,
                                Modifier.size(18.dp), tint = colors.textLight
                            )
                        }
                    }

                    // ── 3. BUDGET ─────────────────────────────────────────────
                    SettingsGroup {
                        GroupLabel("Budget")

                        // Build a formatter for the currently selected currency
                        val budgetFormatter = remember(uiState.currentCurrency) {
                            CurrencyFormatter(uiState.currentCurrency)
                        }

                        SettingsRow(
                            icon      = Icons.Default.AccountBalanceWallet,
                            label     = "Monthly budget",
                            last      = false,
                            clickable = { showBudgetDialog = true }
                        ) {
                            Text(
                                text     = if (uiState.monthlyBudget > 0)
                                    budgetFormatter.format(uiState.monthlyBudget)
                                else "Not set",
                                fontSize = 13.sp,
                                color    = colors.textMuted
                            )
                            Icon(Icons.Default.ChevronRight, null,
                                Modifier.size(18.dp), tint = colors.textLight)
                        }

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
                                    fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                    color = colors.textDark)
                                Text("${(pct * 100).toInt()}% used",
                                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                    color = ColPrimary)
                            }

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
                                Text("${budgetFormatter.format(spent)} spent",
                                    fontSize = 11.sp, color = colors.textMuted)
                                Text("${budgetFormatter.format(left)} remaining",
                                    fontSize = 11.sp, color = colors.textMuted)
                            }
                        }
                    }

                    // ── 4. NOTIFICATIONS ──────────────────────────────────────
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

                    // ── 5. DATA & EXPORT ──────────────────────────────────────
                    SettingsGroup {
                        GroupLabel("Data & Export")

                        SettingsRow(
                            icon      = Icons.Default.TableChart,
                            label     = "Export to CSV",
                            last      = false,
                            clickable = { if (!uiState.isExporting) viewModel.exportAsCsv() }
                        ) { ExportTrailing(uiState.isExporting) }

                        SettingsRow(
                            icon      = Icons.Default.Description,
                            label     = "Export to Text",
                            last      = false,
                            clickable = { if (!uiState.isExporting) viewModel.exportAsText() }
                        ) { ExportTrailing(uiState.isExporting) }

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

                    // ── 6. ABOUT ──────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .border(0.5.dp, colors.border, RoundedCornerShape(18.dp))
                            .background(colors.surface)
                    ) {
                        Column {
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
// Budget editor dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BudgetEditorDialog(
    currentInput   : String,
    currencySymbol : String,
    colors         : SettingsColors,
    onInputChange  : (String) -> Unit,
    onSave         : () -> Unit,
    onDismiss      : () -> Unit
) {
    AlertDialog(
        onDismissRequest  = onDismiss,
        containerColor    = colors.surface,
        titleContentColor = colors.textDark,
        textContentColor  = colors.textMuted,
        title = {
            Text(
                text       = "Set Monthly Budget",
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text     = "Enter the maximum amount you want to spend this month.",
                    fontSize = 13.sp,
                    color    = colors.textMuted
                )
                OutlinedTextField(
                    value         = currentInput,
                    onValueChange = onInputChange,
                    singleLine    = true,
                    placeholder   = { Text("0", color = colors.textLight) },
                    prefix        = {
                        Text(
                            text       = currencySymbol,
                            fontWeight = FontWeight.SemiBold,
                            color      = ColPrimary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = ColPrimary,
                        unfocusedBorderColor = colors.textLight,
                        focusedLabelColor    = ColPrimary,
                        cursorColor          = ColPrimary,
                        focusedTextColor     = colors.textDark,
                        unfocusedTextColor   = colors.textDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = currentInput.isNotBlank()
            ) {
                Text(
                    text       = "Save",
                    color      = if (currentInput.isNotBlank()) ColPrimary else colors.textLight,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textMuted)
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Currency picker dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CurrencyPickerDialog(
    currentCurrency : Currency,
    colors          : SettingsColors,
    onSelect        : (Currency) -> Unit,
    onDismiss       : () -> Unit
) {
    AlertDialog(
        onDismissRequest  = onDismiss,
        containerColor    = colors.surface,
        titleContentColor = colors.textDark,
        title = {
            Text(
                "Select Currency",
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SUPPORTED_CURRENCIES.forEach { currency ->
                    val isSelected = currency.code == currentCurrency.code
                    CurrencyPickerRow(
                        currency   = currency,
                        isSelected = isSelected,
                        colors     = colors,
                        onClick    = { onSelect(currency) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textMuted)
            }
        }
    )
}

@Composable
private fun CurrencyPickerRow(
    currency   : Currency,
    isSelected : Boolean,
    colors     : SettingsColors,
    onClick    : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) ColPrimary.copy(alpha = 0.10f) else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isSelected) ColPrimary.copy(alpha = 0.15f)
                    else colors.segBg
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = currency.symbol,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = if (isSelected) ColPrimary else colors.textDark,
                maxLines   = 1
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = currency.code,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = if (isSelected) ColPrimary else colors.textDark
            )
            Text(
                text     = currency.name,
                fontSize = 11.sp,
                color    = colors.textMuted
            )
        }

        if (isSelected) {
            Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = "Selected",
                tint               = ColPrimary,
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-components
// ─────────────────────────────────────────────────────────────────────────────

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

@Composable
private fun GroupLabel(text: String) {
    Text(
        text          = text.uppercase(),
        fontSize      = 11.sp,
        fontWeight    = FontWeight.Bold,
        color         = ColPrimary,
        letterSpacing = 0.08.sp,
        modifier      = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsRow(
    icon       : ImageVector,
    label      : String,
    last       : Boolean,
    iconTint   : Color             = ColPrimary,
    labelColor : Color?            = null,
    clickable  : (() -> Unit)?     = null,
    trailing   : @Composable RowScope.() -> Unit
) {
    val colors        = LocalSettingsColors.current
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
            Icon(icon, null, Modifier.size(20.dp), tint = iconTint)
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
        Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp), tint = colors.textLight)
    }
}

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
                        active        -> Color.White
                        colors.isDark -> colors.textMuted
                        else          -> ColTextMutedLight
                    }
                )
            }
        }
    }
}

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

private fun Color.isBright(): Boolean =
    (0.2126f * red + 0.7152f * green + 0.0722f * blue) > 0.5f