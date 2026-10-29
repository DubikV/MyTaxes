package com.gmail.vanyadubik.myassistant.featuretaxes.screens.taxes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.myassistant.domen.model.Currency
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.usecase.AccountUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.AuthUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.DataStoreUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.TaxesUseCase
import com.gmail.vanyadubik.myassistant.featuretaxes.screens.taxes.model.InputError
import com.gmail.vanyadubik.myassistant.featuretaxes.screens.taxes.model.TaxesUiState
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

class TaxesViewModel(
    private val taxesUseCase: TaxesUseCase,
    private val authUseCase: AuthUseCase,
    private val dataStoreUseCase: DataStoreUseCase,
    private val accountUseCase: AccountUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        TaxesUiState(inputDate = Clock.System.todayIn(TimeZone.currentSystemDefault()))
    )
    val state: StateFlow<TaxesUiState> = _state.asStateFlow()

    private var lastAction: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            taxesUseCase.getIncomes().collect { list ->
                _state.update { it.copy(incomes = list) }
            }
        }
        viewModelScope.launch {
            authUseCase.isLoggedIn.collect { loggedIn ->
                _state.update { it.copy(isLoggedIn = loggedIn) }
            }
        }
        viewModelScope.launch {
            combine(authUseCase.isLoggedIn, accountUseCase.savedGroup) { loggedIn, saved -> loggedIn && saved != null }
                .collect { saved -> _state.update { it.copy(isGroupSaved = saved) } }
        }
        viewModelScope.launch {
            combine(authUseCase.isLoggedIn, accountUseCase.savedCurrency) { loggedIn, saved -> saved.takeIf { loggedIn } }
                .collect { saved ->
                    _state.update { s ->
                        s.copy(
                            savedCurrency = saved,
                            inputCurrency = saved ?: s.inputCurrency,
                        )
                    }
                }
        }
        viewModelScope.launch {
            dataStoreUseCase.fopGroup.collect { group ->
                _state.update { it.copy(group = group) }
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _state.update { it.copy(inputDate = date, inputError = null) }
    }

    fun onAmountChanged(value: String) {
        val normalized = value.replace(',', '.')
        if (normalized.isNotEmpty() && normalized.toDoubleOrNull() == null && normalized != ".") return
        _state.update { it.copy(inputAmount = normalized, inputError = null) }
    }

    fun onCurrencySelected(currency: Currency) {
        if (_state.value.savedCurrency != null) return
        _state.update { it.copy(inputCurrency = currency) }
    }

    fun onGroupSelected(group: FopGroup) {
        viewModelScope.launch { dataStoreUseCase.setFopGroup(group) }
    }

    fun addIncome() {
        val current = _state.value
        val date = current.inputDate
        val amount = current.inputAmount.toDoubleOrNull()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        val error = when {
            date == null -> InputError.DATE
            date > today -> InputError.FUTURE_DATE
            amount == null || amount <= 0.0 -> InputError.AMOUNT
            else -> null
        }

        if (error != null) {
            _state.update { it.copy(inputError = error) }
            return
        }

        viewModelScope.launch {
            taxesUseCase.addIncome(date!!, amount!!, current.inputCurrency)
            _state.update {
                it.copy(
                    inputAmount = "",
                    inputError = null,
                    calculationResult = null,
                )
            }
        }
    }

    fun deleteIncome(id: String) {
        viewModelScope.launch {
            taxesUseCase.deleteIncome(id)
            _state.update { it.copy(calculationResult = null) }
        }
    }

    fun calculateTaxes() {
        val current = _state.value
        if (current.isLoading || current.untaxedIncomes.isEmpty()) return

        lastAction = { calculateTaxes() }
        _state.update { it.copy(isLoading = true, actionError = null, calculationResult = null) }
        viewModelScope.launch {
            when (val result = taxesUseCase.calculateTaxesForUnpaid(current.group)) {
                is ResultState.Success -> _state.update { it.copy(isLoading = false, calculationResult = result.data) }
                is ResultState.Error -> _state.update {
                    it.copy(isLoading = false, actionError = result.message ?: "Calculation error")
                }
                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun dismissCalculationResult() {
        _state.update { it.copy(calculationResult = null) }
    }

    fun retryLastAction() {
        dismissActionError()
        lastAction?.invoke()
    }

    fun dismissActionError() {
        _state.update { it.copy(actionError = null) }
    }

    fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
