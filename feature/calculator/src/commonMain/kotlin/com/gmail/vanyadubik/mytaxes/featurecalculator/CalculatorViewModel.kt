package com.gmail.vanyadubik.mytaxes.featurecalculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.mytaxes.domen.model.Currency
import com.gmail.vanyadubik.mytaxes.domen.model.FopGroup
import com.gmail.vanyadubik.mytaxes.domen.model.IncomeInput
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState
import com.gmail.vanyadubik.mytaxes.domen.model.TaxCalculation
import com.gmail.vanyadubik.mytaxes.domen.usecase.AccountUseCase
import com.gmail.vanyadubik.mytaxes.domen.usecase.AuthUseCase
import com.gmail.vanyadubik.mytaxes.domen.usecase.CalculateTaxUseCase
import com.gmail.vanyadubik.mytaxes.domen.usecase.DataStoreUseCase
import com.gmail.vanyadubik.mytaxes.domen.usecase.TaxHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class InputError { DATE, AMOUNT, FUTURE_DATE }

data class IncomeRow(
    val id: Int,
    val date: LocalDate?,
    val amount: String = "",
    val currency: Currency = Currency.USD,
    val error: InputError? = null,
)

data class CalculatorUiState(
    val incomes: List<IncomeRow> = emptyList(),
    val group: FopGroup = FopGroup.GROUP_3_5,
    val isLoading: Boolean = false,
    val result: TaxCalculation? = null,
    val errorMessage: String? = null,
    val savedToHistory: Boolean = false,
    val saveError: String? = null,
    val isLoggedIn: Boolean = false,
    /** The group is saved in the account, so it is changed there and not in the calculator. */
    val isGroupSaved: Boolean = false,
    /** The currency saved in the account; it is used for every income and changed only there. */
    val savedCurrency: Currency? = null,
)

@OptIn(ExperimentalTime::class)
class CalculatorViewModel(
    private val calculateTax: CalculateTaxUseCase,
    private val taxHistory: TaxHistoryUseCase,
    private val authUseCase: AuthUseCase,
    private val dataStoreUseCase: DataStoreUseCase,
    private val accountUseCase: AccountUseCase,
) : ViewModel() {

    private var nextRowId = 0

    private fun newRow(currency: Currency = Currency.USD) =
        IncomeRow(id = nextRowId++, date = Clock.System.todayIn(TimeZone.currentSystemDefault()), currency = currency)

    private val _state = MutableStateFlow(CalculatorUiState(incomes = listOf(newRow())))
    val state: StateFlow<CalculatorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authUseCase.isLoggedIn.collect { loggedIn ->
                _state.update { it.copy(isLoggedIn = loggedIn, savedToHistory = false, saveError = null) }
            }
        }
        viewModelScope.launch {
            combine(authUseCase.isLoggedIn, accountUseCase.savedGroup) { loggedIn, saved -> loggedIn && saved != null }
                .collect { saved -> _state.update { it.copy(isGroupSaved = saved) } }
        }
        viewModelScope.launch {
            combine(authUseCase.isLoggedIn, accountUseCase.savedCurrency) { loggedIn, saved -> saved.takeIf { loggedIn } }
                .collect { saved ->
                    _state.update { state ->
                        state.copy(
                            savedCurrency = saved,
                            incomes = if (saved == null) state.incomes else state.incomes.map { it.copy(currency = saved) },
                            result = null,
                        )
                    }
                }
        }
        viewModelScope.launch {
            dataStoreUseCase.fopGroup.collect { group ->
                _state.update { it.copy(group = group, result = null) }
            }
        }
    }

    fun onGroupSelected(group: FopGroup) {
        viewModelScope.launch { dataStoreUseCase.setFopGroup(group) }
    }

    private fun updateRow(id: Int, transform: (IncomeRow) -> IncomeRow) = _state.update { state ->
        state.copy(incomes = state.incomes.map { if (it.id == id) transform(it) else it }, result = null)
    }

    fun onDateSelected(id: Int, date: LocalDate) = updateRow(id) { it.copy(date = date, error = null) }

    fun onAmountChanged(id: Int, value: String) {
        // Allow only digits and a single decimal separator
        val normalized = value.replace(',', '.')
        if (normalized.isNotEmpty() && normalized.toDoubleOrNull() == null && normalized != ".") return
        updateRow(id) { it.copy(amount = normalized, error = null) }
    }

    fun onCurrencySelected(id: Int, currency: Currency) {
        if (_state.value.savedCurrency != null) return
        updateRow(id) { it.copy(currency = currency) }
    }

    fun addIncome() = _state.update { state ->
        state.copy(incomes = state.incomes + newRow(state.incomes.lastOrNull()?.currency ?: Currency.USD), result = null)
    }

    fun removeIncome(id: Int) = _state.update { state ->
        if (state.incomes.size <= 1) state else state.copy(incomes = state.incomes.filterNot { it.id == id }, result = null)
    }

    fun dismissError() = _state.update { it.copy(errorMessage = null) }

    fun calculate() {
        val current = _state.value
        if (current.isLoading) return
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val validated = current.incomes.map { row ->
            val amount = row.amount.toDoubleOrNull()
            row.copy(
                error = when {
                    row.date == null -> InputError.DATE
                    row.date > today -> InputError.FUTURE_DATE
                    amount == null || amount <= 0.0 -> InputError.AMOUNT
                    else -> null
                }
            )
        }
        if (validated.any { it.error != null }) {
            _state.update { it.copy(incomes = validated) }
            return
        }
        val inputs = validated.map { IncomeInput(it.date!!, it.amount.toDouble(), it.currency) }

        _state.update {
            it.copy(isLoading = true, result = null, errorMessage = null, savedToHistory = false, saveError = null)
        }
        viewModelScope.launch {
            when (val result = calculateTax(inputs, current.group)) {
                is ResultState.Success -> {
                    val calculation = result.data!!
                    _state.update { it.copy(result = calculation) }
                    // History is a bonus for signed in users, guests only get the calculation
                    if (_state.value.isLoggedIn) save(calculation) else _state.update { it.copy(isLoading = false) }
                }
                is ResultState.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "Error")
                }
                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun save(calculation: TaxCalculation) {
        when (val saved = taxHistory.save(calculation)) {
            is ResultState.Success -> _state.update { it.copy(isLoading = false, savedToHistory = true) }
            is ResultState.Error -> _state.update { it.copy(isLoading = false, saveError = saved.message) }
            else -> _state.update { it.copy(isLoading = false) }
        }
    }
}
