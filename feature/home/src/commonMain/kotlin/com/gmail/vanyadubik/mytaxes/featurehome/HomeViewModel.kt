package com.gmail.vanyadubik.mytaxes.featurehome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.mytaxes.domen.model.Expense
import com.gmail.vanyadubik.mytaxes.domen.model.MonthlyStats
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState
import com.gmail.vanyadubik.mytaxes.domen.model.TaxCalculation
import com.gmail.vanyadubik.mytaxes.domen.usecase.AuthUseCase
import com.gmail.vanyadubik.mytaxes.domen.usecase.ExpenseUseCase
import com.gmail.vanyadubik.mytaxes.domen.usecase.MonthlyStatsUseCase
import com.gmail.vanyadubik.mytaxes.domen.usecase.TaxHistoryUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class HomeUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val stats: List<MonthlyStats> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val errorMessage: String? = null,
)

@OptIn(ExperimentalTime::class)
class HomeViewModel(
    private val authUseCase: AuthUseCase,
    private val taxHistory: TaxHistoryUseCase,
    private val expenseUseCase: ExpenseUseCase,
    private val monthlyStats: MonthlyStatsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var calculations: List<TaxCalculation> = emptyList()

    init {
        viewModelScope.launch {
            authUseCase.isLoggedIn.collect { loggedIn ->
                if (!loggedIn) calculations = emptyList()
                _state.update {
                    if (loggedIn) it.copy(isLoggedIn = true) else HomeUiState()
                }
            }
        }
    }

    /** Reloads the data, called every time the screen is shown. */
    fun load() {
        if (!_state.value.isLoggedIn) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val (history, expenses) = coroutineScope {
                val history = async { taxHistory.getAll() }
                val expenses = async { expenseUseCase.getAll() }
                history.await() to expenses.await()
            }
            val error = (history as? ResultState.Error)?.message ?: (expenses as? ResultState.Error)?.message
            if (history is ResultState.Success) calculations = history.data.orEmpty()
            val expenseList = if (expenses is ResultState.Success) expenses.data.orEmpty() else _state.value.expenses
            _state.update {
                if (!it.isLoggedIn) it else it.copy(isLoading = false, errorMessage = error).withData(expenseList)
            }
        }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            when (val result = expenseUseCase.save(expense)) {
                is ResultState.Success -> {
                    val saved = result.data ?: expense
                    _state.update { it.withData(it.expenses + saved) }
                }
                is ResultState.Error -> _state.update { it.copy(errorMessage = result.message ?: "Error") }
                else -> {}
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        val id = expense.id ?: return
        viewModelScope.launch {
            when (val result = expenseUseCase.delete(id)) {
                is ResultState.Success -> _state.update { s -> s.withData(s.expenses.filterNot { it.id == id }) }
                is ResultState.Error -> _state.update { it.copy(errorMessage = result.message ?: "Error") }
                else -> {}
            }
        }
    }

    fun dismissError() = _state.update { it.copy(errorMessage = null) }

    private fun HomeUiState.withData(expenses: List<Expense>): HomeUiState {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return copy(
            expenses = expenses.sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.createdAt }),
            stats = monthlyStats(calculations, expenses, today.year, today.monthNumber),
        )
    }
}
