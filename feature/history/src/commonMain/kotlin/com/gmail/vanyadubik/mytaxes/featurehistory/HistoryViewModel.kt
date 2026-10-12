package com.gmail.vanyadubik.mytaxes.featurehistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState
import com.gmail.vanyadubik.mytaxes.domen.model.TaxCalculation
import com.gmail.vanyadubik.mytaxes.domen.usecase.TaxHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val items: List<TaxCalculation> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class HistoryViewModel(
    private val taxHistory: TaxHistoryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = taxHistory.getAll()) {
                is ResultState.Success -> _state.update {
                    it.copy(items = result.data.orEmpty(), isLoading = false)
                }
                is ResultState.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "Error")
                }
                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun delete(item: TaxCalculation) {
        val id = item.id ?: return
        viewModelScope.launch {
            when (val result = taxHistory.delete(id)) {
                is ResultState.Success -> _state.update { s -> s.copy(items = s.items.filterNot { it.id == id }) }
                is ResultState.Error -> _state.update { it.copy(errorMessage = result.message ?: "Error") }
                else -> {}
            }
        }
    }

    fun dismissError() = _state.update { it.copy(errorMessage = null) }
}
