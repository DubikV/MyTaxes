package com.gmail.vanyadubik.myassistant.featurehome.model

import com.gmail.vanyadubik.myassistant.domen.model.Car
import com.gmail.vanyadubik.myassistant.domen.model.TaxIncomeItem

data class HomeUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val untaxedIncomes: List<TaxIncomeItem> = emptyList(),
    val unpaidTaxes: List<TaxIncomeItem> = emptyList(),
    val cars: List<Car> = emptyList(),
    val loadError: String? = null,
    val actionError: String? = null,
) {
    val totalUnpaidTaxAmount: Double get() = unpaidTaxes.sumOf { it.totalTax }
}
