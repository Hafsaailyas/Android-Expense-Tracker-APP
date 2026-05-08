package com.hafsaIlyas.expensetracker.ui.screens.settings

// ui/screens/settings/SettingsScreen.kt

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hafsaIlyas.expensetracker.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboard = LocalSoftwareKeyboardController.current

    // Launch the OS share sheet when an export intent is ready
    val shareLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.clearExportResult() }

    LaunchedEffect(uiState.exportResult) {
        val result = uiState.exportResult
        if (result is ExportResult.Ready) {
            shareLauncher.launch(result.intent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── APPEARANCE ────────────────────────────────────────────────────
            SettingsSectionHeader("Appearance")

            // Theme picker
            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SettingsLabel("App Theme", Icons.Default.Palette)
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppTheme.entries.forEach { theme ->
                            FilterChip(
                                modifier  = Modifier.weight(1f),
                                selected  = uiState.appTheme == theme,
                                onClick   = { viewModel.setTheme(theme) },
                                label     = {
                                    Text(
                                        theme.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Dynamic color (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SettingsCard {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            SettingsLabel("Dynamic Color", Icons.Default.ColorLens)
                            Text(
                                "Use your wallpaper colors",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked         = uiState.dynamicColor,
                            onCheckedChange = viewModel::setDynamicColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── BUDGET ────────────────────────────────────────────────────────
            SettingsSectionHeader("Budget")

            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsLabel("Monthly Budget", Icons.Default.Savings)
                    Text(
                        "Set a target to track overspending on the Dashboard.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value         = uiState.budgetInput,
                            onValueChange = viewModel::onBudgetInputChange,
                            modifier      = Modifier.weight(1f),
                            placeholder   = { Text("e.g. 2000") },
                            prefix        = { Text("$  ") },
                            singleLine    = true,
                            shape         = MaterialTheme.shapes.medium,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction    = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboard?.hide(); viewModel.saveBudget() }
                            )
                        )
                        Button(onClick = {
                            keyboard?.hide()
                            viewModel.saveBudget()
                        }) {
                            Text("Save")
                        }
                    }
                    AnimatedVisibility(uiState.monthlyBudget > 0) {
                        Text(
                            "Current budget: ${"$%.2f".format(uiState.monthlyBudget)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── DATA EXPORT ───────────────────────────────────────────────────
            SettingsSectionHeader("Data & Export")

            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsLabel("Export Expenses", Icons.Default.Share)
                    Text(
                        "${uiState.expenseCount} expenses ready to export",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Export error banner
                    val result = uiState.exportResult
                    if (result is ExportResult.Error) {
                        Surface(
                            color  = MaterialTheme.colorScheme.errorContainer,
                            shape  = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Error, null,
                                    tint     = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp))
                                Text(result.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // CSV
                        OutlinedButton(
                            onClick  = viewModel::exportAsCsv,
                            enabled  = !uiState.isExporting && uiState.expenseCount > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isExporting) {
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.TableChart, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("CSV")
                            }
                        }
                        // Plain text
                        OutlinedButton(
                            onClick  = viewModel::exportAsText,
                            enabled  = !uiState.isExporting && uiState.expenseCount > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isExporting) {
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Description, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Text")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── ABOUT ─────────────────────────────────────────────────────────
            SettingsSectionHeader("About")
            SettingsCard {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        SettingsLabel("Expense Tracker", Icons.Default.Info)
                        Text(
                            "Version 1.0.0 · Portfolio Project",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Reusable sub-components ───────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text     = text.uppercase(),
        style    = MaterialTheme.typography.labelMedium,
        color    = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.large,
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SettingsLabel(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint     = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}