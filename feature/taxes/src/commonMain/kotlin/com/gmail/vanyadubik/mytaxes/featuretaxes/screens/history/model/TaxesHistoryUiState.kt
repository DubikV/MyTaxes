package com.gmail.vanyadubik.myassistant.featuretaxes.screens.history.model

import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.TaxCalculation
import com.gmail.vanyadubik.myassistant.domen.model.TaxIncomeItem

data class TaxesHistoryUiState(
    val incomes: List<TaxIncomeItem> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val group: FopGroup = FopGroup.GROUP_3_5,
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val actionError: String? = null,
    val errorMessage: String? = null,
    val calculationResult: TaxCalculation? = null,
)
