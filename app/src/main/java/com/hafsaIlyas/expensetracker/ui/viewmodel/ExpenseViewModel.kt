package com.hafsaIlyas.expensetracker.ui.viewmodel

// ui/viewmodel/ExpenseViewModel.kt
// CHANGES vs original:
//   • Inject UserPreferencesRepository so we can read monthlyBudget Flow
//   • observeExpenses() now combines expense stream with monthlyBudget stream
//   • recomputeDashboard() accepts budget param and populates new budget fields
//   • DashboardUiState extended: monthlyBudget, budgetPercentage, remainingBudget,
//     isOverBudget, budgetStatus (BudgetStatus enum defined here)

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hafsaIlyas.expensetracker.data.local.entity.Expense
import com.hafsaIlyas.expensetracker.data.preferences.UserPreferencesRepository
import com.hafsaIlyas.expensetracker.data.repository.ExpenseRepository
import com.hafsaIlyas.expensetracker.ui.screens.addexpense.AddExpenseUiState
import com.hafsaIlyas.expensetracker.ui.screens.dashboard.*
import com.hafsaIlyas.expensetracker.ui.screens.expenselist.ExpenseListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

// Palette for up to 10 categories (ARGB longs)
private val CATEGORY_COLORS = listOf(
    0xFF4FC3F7L, 0xFFFFB74DL, 0xFFA5D6A7L, 0xFFCE93D8L,
    0xFFFF8A65L, 0xFF80DEEAL, 0xFFF48FB1L, 0xFFFFCC02L,
    0xFF90CAF9L, 0xFFBCAAA4L
)

// ── Budget status enum ────────────────────────────────────────────────────────

/**
 * Represents how the user's current month spending relates to their set budget.
 *
 * | Range (% of budget used) | Status       |
 * |--------------------------|--------------|
 * | 0 – 74 %                 | NORMAL       |
 * | 75 – 89 %                | WARNING      |
 * | 90 – 99 %                | HIGH_ALERT   |
 * | ≥ 100 %                  | OVER_BUDGET  |
 * | No budget set            | NO_BUDGET    |
 */
enum class BudgetStatus { NO_BUDGET, NORMAL, WARNING, HIGH_ALERT, OVER_BUDGET }

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository : ExpenseRepository,
    // ── NEW: injected so dashboard can track budget ───────────────────────────
    private val prefsRepo  : UserPreferencesRepository
) : ViewModel() {

    // ── Raw stream ────────────────────────────────────────────────────────────
    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())

    // ── Dashboard state ───────────────────────────────────────────────────────
    private val _dashboardUiState = MutableStateFlow(DashboardUiState())
    val dashboardUiState: StateFlow<DashboardUiState> = _dashboardUiState.asStateFlow()

    // ── List state ────────────────────────────────────────────────────────────
    private val _listUiState = MutableStateFlow(ExpenseListUiState())
    val listUiState: StateFlow<ExpenseListUiState> = _listUiState.asStateFlow()

    // ── Add state ─────────────────────────────────────────────────────────────
    private val _addUiState = MutableStateFlow(AddExpenseUiState())
    val addUiState: StateFlow<AddExpenseUiState> = _addUiState.asStateFlow()

    private val _pendingDeletion = MutableStateFlow<Expense?>(null)
    val pendingDeletion: StateFlow<Expense?> = _pendingDeletion.asStateFlow()

    private var deletionJob: kotlinx.coroutines.Job? = null

    init {
        observeExpenses()
    }

    // ── Observation & Derivation ──────────────────────────────────────────────

    /**
     * Starts a 4-second countdown before actually deleting.
     * The UI shows a Snackbar during this window.
     */
    fun requestDelete(expense: Expense) {
        deletionJob?.cancel()
        _pendingDeletion.value = expense

        deletionJob = viewModelScope.launch {
            kotlinx.coroutines.delay(4_000)
            repository.deleteExpense(expense)
            _pendingDeletion.value = null
        }
    }

    /** Called when the user taps "Undo" in the Snackbar */
    fun undoDelete() {
        deletionJob?.cancel()
        _pendingDeletion.value = null
    }

    /** Flush immediately (e.g. screen leaves composition) */
    fun commitPendingDelete() {
        val expense = _pendingDeletion.value ?: return
        deletionJob?.cancel()
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _pendingDeletion.value = null
        }
    }

    /**
     * Combines the expense stream with the monthly budget Flow so the dashboard
     * reacts to both expense changes AND budget changes from Settings.
     */
    private fun observeExpenses() {
        viewModelScope.launch {
            combine(
                repository.getAllExpenses(),
                prefsRepo.monthlyBudget           // ← NEW: observe budget from DataStore
            ) { expenses, budget ->
                expenses to budget
            }
                .onStart { /* keep isLoading = true */ }
                .catch { /* TODO: surface error */ }
                .collect { (expenses, budget) ->
                    _allExpenses.value = expenses
                    recomputeListState(expenses)
                    recomputeDashboard(expenses, budget)   // ← passes budget through
                }
        }
    }

    // ── List + Search + Filter ────────────────────────────────────────────────

    private fun recomputeListState(expenses: List<Expense>) {
        val current = _listUiState.value
        val filtered = applyListFilters(expenses, current.searchQuery, current.selectedCategory)
        _listUiState.update {
            it.copy(
                allExpenses      = expenses,
                filteredExpenses = filtered,
                isLoading        = false,
                totalSpending    = expenses.sumOf { e -> e.amount }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _listUiState.update { it.copy(searchQuery = query) }
        applyAndEmitFilters()
    }

    fun onListCategoryFilterChange(category: String) {
        val next = if (_listUiState.value.selectedCategory == category) "" else category
        _listUiState.update { it.copy(selectedCategory = next) }
        applyAndEmitFilters()
    }

    private fun applyAndEmitFilters() {
        val s = _listUiState.value
        _listUiState.update {
            it.copy(filteredExpenses = applyListFilters(s.allExpenses, s.searchQuery, s.selectedCategory))
        }
    }

    private fun applyListFilters(
        expenses: List<Expense>,
        query: String,
        category: String
    ): List<Expense> {
        return expenses.filter { expense ->
            val matchesQuery = query.isBlank() ||
                    expense.note.contains(query, ignoreCase = true) ||
                    expense.category.contains(query, ignoreCase = true)
            val matchesCategory = category.isBlank() ||
                    expense.category == category
            matchesQuery && matchesCategory
        }
    }

    // ── Dashboard Computation ─────────────────────────────────────────────────

    /**
     * @param budget Monthly budget from DataStore (0.0 means not set).
     */
    private fun recomputeDashboard(expenses: List<Expense>, budget: Double) {
        val now      = Calendar.getInstance()
        val thisYear = now.get(Calendar.YEAR)
        val thisMon  = now.get(Calendar.MONTH)

        val prevCal  = Calendar.getInstance().also {
            it.add(Calendar.MONTH, -1)
        }
        val prevYear = prevCal.get(Calendar.YEAR)
        val prevMon  = prevCal.get(Calendar.MONTH)

        val currentMonthExpenses  = expenses.filter { it.belongsToMonth(thisYear, thisMon) }
        val previousMonthExpenses = expenses.filter { it.belongsToMonth(prevYear, prevMon) }

        val currentTotal  = currentMonthExpenses.sumOf { it.amount }
        val previousTotal = previousMonthExpenses.sumOf { it.amount }

        val pctChange = when {
            previousTotal == 0.0 && currentTotal == 0.0 -> 0.0
            previousTotal == 0.0 -> 100.0
            else -> ((currentTotal - previousTotal) / previousTotal) * 100.0
        }

        val trend = when {
            pctChange > 1.0  -> TrendDirection.UP
            pctChange < -1.0 -> TrendDirection.DOWN
            else             -> TrendDirection.NEUTRAL
        }

        // Category breakdown (current month only)
        val grouped = currentMonthExpenses
            .groupBy { it.category }
            .map { (cat, list) -> cat to list.sumOf { it.amount } }
            .sortedByDescending { it.second }

        val categoryShares = grouped.mapIndexed { idx, (cat, amount) ->
            CategoryShare(
                category   = cat,
                amount     = amount,
                percentage = if (currentTotal > 0) ((amount / currentTotal) * 100).toFloat() else 0f,
                color      = CATEGORY_COLORS[idx % CATEGORY_COLORS.size]
            )
        }

        // Recent 5
        val dateFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())
        val recent = expenses.take(5).map { e ->
            RecentExpenseItem(
                id            = e.id,
                category      = e.category,
                note          = e.note,
                amount        = e.amount,
                formattedDate = dateFormatter.format(Date(e.date))
            )
        }

        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            .format(now.time)

        // ── NEW: budget-derived fields ────────────────────────────────────────
        val budgetPct       = if (budget > 0.0) (currentTotal / budget).toFloat() else 0f
        val remainingBudget = if (budget > 0.0) budget - currentTotal else 0.0
        val isOverBudget    = budget > 0.0 && currentTotal > budget

        val budgetStatus = when {
            budget <= 0.0       -> BudgetStatus.NO_BUDGET
            budgetPct >= 1.0f   -> BudgetStatus.OVER_BUDGET
            budgetPct >= 0.90f  -> BudgetStatus.HIGH_ALERT
            budgetPct >= 0.75f  -> BudgetStatus.WARNING
            else                -> BudgetStatus.NORMAL
        }
        // ─────────────────────────────────────────────────────────────────────

        _dashboardUiState.update {
            it.copy(
                isLoading            = false,
                currentMonthTotal    = currentTotal,
                currentMonthName     = monthName,
                previousMonthTotal   = previousTotal,
                percentageChange     = abs(pctChange).roundTo(1),
                trendDirection       = trend,
                categoryBreakdown    = categoryShares,
                recentExpenses       = recent,
                // ── NEW budget fields ────────────────────────────────────────
                monthlyBudget        = budget,
                budgetPercentage     = budgetPct,
                remainingBudget      = remainingBudget,
                isOverBudget         = isOverBudget,
                budgetStatus         = budgetStatus
            )
        }
    }

    // ── Dashboard Filter ──────────────────────────────────────────────────────

    fun onDashboardCategoryFilter(category: String) {
        val next = if (_dashboardUiState.value.selectedFilterCategory == category) "" else category
        _dashboardUiState.update { it.copy(selectedFilterCategory = next) }
    }

    // ── Add Expense ───────────────────────────────────────────────────────────

    fun onAmountChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,8}(\\.\\d{0,2})?\$"))) {
            _addUiState.update { it.copy(amount = value, amountError = null) }
        }
    }

    fun onCategoryChange(category: String) {
        _addUiState.update { it.copy(selectedCategory = category, categoryError = null) }
    }

    fun onNoteChange(note: String) {
        _addUiState.update { it.copy(note = note) }
    }

    fun saveExpense() {
        val state = _addUiState.value
        val amountValue = state.amount.toDoubleOrNull()
        var hasError = false

        if (amountValue == null || amountValue <= 0.0) {
            _addUiState.update { it.copy(amountError = "Enter a valid amount greater than 0") }
            hasError = true
        }
        if (state.selectedCategory.isBlank()) {
            _addUiState.update { it.copy(categoryError = "Please select a category") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            _addUiState.update { it.copy(isSaving = true) }
            repository.insertExpense(
                Expense(
                    amount   = amountValue!!,
                    category = state.selectedCategory,
                    note     = state.note.trim(),
                    date     = System.currentTimeMillis()
                )
            )
            _addUiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    fun resetAddForm() { _addUiState.value = AddExpenseUiState() }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }
}


// ── Extensions ────────────────────────────────────────────────────────────────

private fun Expense.belongsToMonth(year: Int, month: Int): Boolean {
    val cal = Calendar.getInstance().also { it.timeInMillis = date }
    return cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
}

private fun Double.roundTo(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}