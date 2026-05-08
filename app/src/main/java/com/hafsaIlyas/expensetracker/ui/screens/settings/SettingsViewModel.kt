package com.hafsaIlyas.expensetracker.ui.screens.settings

// ui/screens/settings/SettingsViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hafsaIlyas.expensetracker.data.export.ExportService
import com.hafsaIlyas.expensetracker.data.preferences.UserPreferencesRepository
import com.hafsaIlyas.expensetracker.data.repository.ExpenseRepository
import com.hafsaIlyas.expensetracker.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val monthlyBudget: Double = 0.0,
    val budgetInput: String = "",
    val expenseCount: Int = 0,
    val isExporting: Boolean = false,
    val exportResult: ExportResult? = null
)

sealed class ExportResult {
    data class Ready(val intent: android.content.Intent) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepo: UserPreferencesRepository,
    private val expenseRepo: ExpenseRepository,
    private val exportService: ExportService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Merge all prefs flows
        viewModelScope.launch {
            combine(
                prefsRepo.appTheme,
                prefsRepo.dynamicColor,
                prefsRepo.monthlyBudget,
                expenseRepo.getAllExpenses()
            ) { theme, dynamic, budget, expenses ->
                _uiState.update {
                    it.copy(
                        appTheme      = theme,
                        dynamicColor  = dynamic,
                        monthlyBudget = budget,
                        budgetInput   = if (budget > 0) "%.0f".format(budget) else "",
                        expenseCount  = expenses.size
                    )
                }
            }.collect()
        }
    }

    fun setTheme(theme: AppTheme) = viewModelScope.launch {
        prefsRepo.setAppTheme(theme)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        prefsRepo.setDynamicColor(enabled)
    }

    fun onBudgetInputChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,7}(\\.\\d{0,2})?\$"))) {
            _uiState.update { it.copy(budgetInput = value) }
        }
    }

    fun saveBudget() = viewModelScope.launch {
        val value = _uiState.value.budgetInput.toDoubleOrNull() ?: 0.0
        prefsRepo.setMonthlyBudget(value)
    }

    // ── Export ────────────────────────────────────────────────────────────────

    fun exportAsCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportResult = null) }
            try {
                val expenses = expenseRepo.getAllExpenses().first()
                val csv      = exportService.buildCsv(expenses)
                val intent   = exportService.buildShareIntent(
                    content   = csv,
                    fileName  = "expenses_export.csv",
                    mimeType  = "text/csv"
                )
                _uiState.update { it.copy(isExporting = false, exportResult = ExportResult.Ready(intent)) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isExporting = false,
                        exportResult = ExportResult.Error(e.message ?: "Export failed"))
                }
            }
        }
    }

    fun exportAsText() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportResult = null) }
            try {
                val expenses = expenseRepo.getAllExpenses().first()
                val text     = exportService.buildPlainText(expenses)
                val intent   = exportService.buildShareIntent(
                    content   = text,
                    fileName  = "expenses_report.txt",
                    mimeType  = "text/plain"
                )
                _uiState.update { it.copy(isExporting = false, exportResult = ExportResult.Ready(intent)) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isExporting = false,
                        exportResult = ExportResult.Error(e.message ?: "Export failed"))
                }
            }
        }
    }

    fun clearExportResult() {
        _uiState.update { it.copy(exportResult = null) }
    }
}