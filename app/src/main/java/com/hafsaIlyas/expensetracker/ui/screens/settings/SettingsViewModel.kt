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
import java.util.Calendar
import javax.inject.Inject

data class SettingsUiState(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val monthlyBudget: Double = 0.0,
    val budgetInput: String = "",
    val expenseCount: Int = 0,
    val currentMonthSpent: Double = 0.0,      // sum of current-month expenses for budget bar
    val budgetAlertsEnabled: Boolean = true,  // Notifications group — Budget alerts toggle
    val dailyReminderTime: String = "9:00 PM",// Notifications group — Daily reminder value
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
        // Compute start/end of the current calendar month once
        val (monthStart, monthEnd) = run {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0);      cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59);       cal.set(Calendar.MILLISECOND, 999)
            start to cal.timeInMillis
        }

        viewModelScope.launch {
            combine(
                prefsRepo.appTheme,
                prefsRepo.dynamicColor,
                prefsRepo.monthlyBudget,
                expenseRepo.getAllExpenses(),
                // Use the repo's existing range query — Expense.date is the correct field name
                expenseRepo.getTotalSpendingInRange(monthStart, monthEnd)
            ) { theme, dynamic, budget, expenses, monthTotal ->
                _uiState.update {
                    it.copy(
                        appTheme          = theme,
                        dynamicColor      = dynamic,
                        monthlyBudget     = budget,
                        budgetInput       = if (budget > 0) "%.0f".format(budget) else "",
                        expenseCount      = expenses.size,
                        currentMonthSpent = monthTotal ?: 0.0
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

    fun setBudgetAlertsEnabled(enabled: Boolean) = viewModelScope.launch {
        // Persist via prefsRepo when you add that key; for now update local state
        _uiState.update { it.copy(budgetAlertsEnabled = enabled) }
    }

    fun setDailyReminderTime(time: String) = viewModelScope.launch {
        _uiState.update { it.copy(dailyReminderTime = time) }
    }

    fun clearAllData() = viewModelScope.launch {
        expenseRepo.getAllExpenses().first().forEach { expenseRepo.deleteExpense(it) }
    }
}