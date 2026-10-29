package com.gmail.vanyadubik.myassistant.featuretaxes.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.usecase.DataStoreUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.TaxesUseCase
import com.gmail.vanyadubik.myassistant.featuretaxes.screens.history.model.TaxesHistoryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaxesHistoryViewModel(
    private val taxesUseCase: TaxesUseCase,
    private val dataStoreUseCase: DataStoreUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(TaxesHistoryUiState())
    val state: StateFlow<TaxesHistoryUiState> = _state.asStateFlow()

    private var lastAction: (() -> Unit)? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            taxesUseCase.getIncomes().collect { list ->
                _state.update { s ->
                    s.copy(
                        incomes = list,
                        selectedIds = s.selectedIds.filter { id -> list.any { it.id == id } }.toSet(),
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

    fun toggleSelection(id: String) {
        _state.update { s ->
            val updated = if (id in s.selectedIds) s.selectedIds - id else s.selectedIds + id
            s.copy(selectedIds = updated)
        }
    }

    fun deleteSelected() {
        val ids = _state.value.selectedIds
        if (ids.isEmpty()) return
        lastAction = { deleteSelected() }
        _state.update { it.copy(actionError = null) }
        viewModelScope.launch {
            ids.forEach { taxesUseCase.deleteIncome(it) }
            _state.update { it.copy(selectedIds = emptySet()) }
        }
    }

    fun calculateTaxes() {
        val current = _state.value
        if (current.isLoading) return

        lastAction = { calculateTaxes() }
        _state.update { it.copy(isLoading = true, actionError = null, calculationResult = null) }
        viewModelScope.launch {
            when (val result = taxesUseCase.calculateTaxesForUnpaid(current.group, current.selectedIds)) {
                is ResultState.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            calculationResult = result.data,
                            selectedIds = emptySet(),
                        )
                    }
                }
                is ResultState.Error -> {
                    _state.update {
                        it.copy(isLoading = false, actionError = result.message ?: "Calculation error")
                    }
                }
                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onPayClicked() {
        val current = _state.value
        val calculation = taxesUseCase.getPaymentCalculation(current.selectedIds, current.incomes, current.group)
        if (calculation != null) {
            _state.update { it.copy(calculationResult = calculation) }
        }
    }

    fun dismissCalculationResult() {
        _state.update { it.copy(calculationResult = null, selectedIds = emptySet()) }
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
