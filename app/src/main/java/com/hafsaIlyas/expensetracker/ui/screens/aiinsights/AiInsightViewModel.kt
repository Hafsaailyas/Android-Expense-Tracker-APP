package com.hafsaIlyas.expensetracker.ui.screens.aiinsights

// ui/screens/aiinsights/AiInsightViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hafsaIlyas.expensetracker.data.ai.AiInsightService
import com.hafsaIlyas.expensetracker.data.local.entity.Expense
import com.hafsaIlyas.expensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private val THINKING_LABELS = listOf(
    "Reading your transactions…",
    "Crunching the numbers…",
    "Detecting spending patterns…",
    "Generating personalised advice…",
    "Almost there…"
)

@HiltViewModel
class AiInsightViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val aiService: AiInsightService
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiInsightUiState>(AiInsightUiState.Idle)
    val uiState: StateFlow<AiInsightUiState> = _uiState.asStateFlow()

    // Typewriter cycling label
    private val _thinkingLabel = MutableStateFlow(THINKING_LABELS.first())
    val thinkingLabel: StateFlow<String> = _thinkingLabel.asStateFlow()

    init {
        generateInsights()
    }

    fun generateInsights() {
        viewModelScope.launch {
            _uiState.value = AiInsightUiState.Loading()
            startThinkingLabelCycle()

            try {
                // Fetch current month's expenses
                val expenses = currentMonthExpenses()

                val result = aiService.generateInsights(expenses)

                _uiState.value = AiInsightUiState.Success(
                    summary      = result.summary,
                    insights     = result.insights,
                    generatedAt  = result.generatedAt
                )
            } catch (e: Exception) {
                _uiState.value = AiInsightUiState.Error(
                    e.message ?: "Something went wrong while generating insights."
                )
            }
        }
    }

    private fun startThinkingLabelCycle() {
        viewModelScope.launch {
            var idx = 0
            while (_uiState.value is AiInsightUiState.Loading) {
                _thinkingLabel.value = THINKING_LABELS[idx % THINKING_LABELS.size]
                idx++
                delay(750)
            }
        }
    }

    private suspend fun currentMonthExpenses(): List<Expense> {
        val now       = Calendar.getInstance()
        val year      = now.get(Calendar.YEAR)
        val month     = now.get(Calendar.MONTH)

        val startCal  = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0)
        }
        val endCal    = Calendar.getInstance().apply {
            set(year, month, now.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        }

        return repository
            .getExpensesByDateRange(startCal.timeInMillis, endCal.timeInMillis)
            .first()
    }
}