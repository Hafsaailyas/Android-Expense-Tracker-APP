package com.hafsaIlyas.expensetracker.ui.screens.settings

// ui/screens/settings/SettingsViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hafsaIlyas.expensetracker.data.currency.Currency
import com.hafsaIlyas.expensetracker.data.currency.CurrencyService
import com.hafsaIlyas.expensetracker.data.currency.getByCode
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
    val currentMonthSpent: Double = 0.0,
    val budgetAlertsEnabled: Boolean = true,
    val dailyReminderTime: String = "9:00 PM",
    val isExporting: Boolean = false,
    val exportResult: ExportResult? = null,
    val currentCurrency: Currency = getByCode("PKR")
)

sealed class ExportResult {
    data class Ready(val intent: android.content.Intent) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepo      : UserPreferencesRepository,
    private val expenseRepo    : ExpenseRepository,
    private val exportService  : ExportService,
    val currencyService        : CurrencyService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // One-time event for opening budget dialog from dashboard
    private val _openBudgetDialogEvent = MutableSharedFlow<Unit>()
    val openBudgetDialogEvent = _openBudgetDialogEvent.asSharedFlow()

    init {
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
                expenseRepo.getTotalSpendingInRange(monthStart, monthEnd),
                currencyService.currentCurrency
            ) { args ->
                val theme       = args[0] as AppTheme
                val dynamic     = args[1] as Boolean
                val budget      = args[2] as Double
                val expenses    = args[3] as List<*>
                val monthTotal  = args[4] as Double?
                val currency    = args[5] as Currency
                _uiState.update {
                    it.copy(
                        appTheme          = theme,
                        dynamicColor      = dynamic,
                        monthlyBudget     = budget,
                        budgetInput       = if (budget > 0) "%.0f".format(budget) else "",
                        expenseCount      = expenses.size,
                        currentMonthSpent = monthTotal ?: 0.0,
                        currentCurrency   = currency
                    )
                }
            }.collect()
        }
    }

    // ── Currency ──────────────────────────────────────────────────────────────

    fun setCurrency(currency: Currency) = viewModelScope.launch {
        currencyService.setCurrency(currency)
    }

    // ── Theme / Color ─────────────────────────────────────────────────────────

    fun setTheme(theme: AppTheme) = viewModelScope.launch {
        prefsRepo.setAppTheme(theme)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        prefsRepo.setDynamicColor(enabled)
    }

    // ── Budget Dialog Control ─────────────────────────────────────────────────

    fun requestOpenBudgetDialog() {
        viewModelScope.launch {
            _openBudgetDialogEvent.emit(Unit)
        }
    }

    // ── Budget ────────────────────────────────────────────────────────────────

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
                    content  = csv,
                    fileName = "expenses_export.csv",
                    mimeType = "text/csv"
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
                    content  = text,
                    fileName = "expenses_report.txt",
                    mimeType = "text/plain"
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

    // ── Notifications ─────────────────────────────────────────────────────────

    fun setBudgetAlertsEnabled(enabled: Boolean) = viewModelScope.launch {
        _uiState.update { it.copy(budgetAlertsEnabled = enabled) }
    }

    fun setDailyReminderTime(time: String) = viewModelScope.launch {
        _uiState.update { it.copy(dailyReminderTime = time) }
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    fun clearAllData() = viewModelScope.launch {
        expenseRepo.getAllExpenses().first().forEach { expenseRepo.deleteExpense(it) }
    }
}