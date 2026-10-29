package com.gmail.vanyadubik.myassistant.featurehome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.usecase.AccountUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.AuthUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.TaxesUseCase
import com.gmail.vanyadubik.myassistant.featurehome.model.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authUseCase: AuthUseCase,
    private val taxesUseCase: TaxesUseCase,
    private val accountUseCase: AccountUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var lastAction: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            authUseCase.isLoggedIn.collect { loggedIn ->
                _state.update { it.copy(isLoggedIn = loggedIn) }
                if (loggedIn) {
                    accountUseCase.loadProfile()
                }
            }
        }

        viewModelScope.launch {
            taxesUseCase.getIncomes().collect { incomes ->
                val untaxed = incomes.filterNot { it.isTaxCalculated }
                val unpaid = incomes.filter { it.isTaxCalculated }
                _state.update {
                    it.copy(
                        untaxedIncomes = untaxed,
                        unpaidTaxes = unpaid,
                    )
                }
            }
        }

        viewModelScope.launch {
            accountUseCase.savedCars.collect { cars ->
                _state.update { it.copy(cars = cars) }
            }
        }
    }

    fun load() {
        if (!_state.value.isLoggedIn) return
        _state.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            val result = accountUseCase.loadProfile()
            _state.update {
                it.copy(
                    isLoading = false,
                    loadError = if (result is ResultState.Error) result.message else null,
                )
            }
        }
    }

    fun retryLastAction() {
        dismissActionError()
        lastAction?.invoke()
    }

    fun dismissActionError() {
        _state.update { it.copy(actionError = null) }
    }
}
