package com.gmail.vanyadubik.myassistant.featurehome.model

import com.gmail.vanyadubik.myassistant.domen.model.Car
import com.gmail.vanyadubik.myassistant.domen.model.MonthlyStats
import com.gmail.vanyadubik.myassistant.domen.model.TaxIncomeItem
import com.gmail.vanyadubik.myassistant.domen.model.TaxType

data class HomeUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val untaxedIncomes: List<TaxIncomeItem> = emptyList(),
    val unpaidTaxes: List<TaxIncomeItem> = emptyList(),
    val monthlyStats: List<MonthlyStats> = emptyList(),
    val calculatedTaxes: List<TaxIncomeItem> = emptyList(),
    val cars: List<Car> = emptyList(),
    val hasPushMessages: Boolean = false,
    val hasUnreadPushMessages: Boolean = false,
    val loadError: String? = null,
    val actionError: String? = null,
) {
    val totalUntaxedIncomeAmount: Double
        get() = untaxedIncomes.sumOf { if (it.amountUah > 0.0) it.amountUah else it.amount }

    val totalUnpaidTaxAmount: Double get() = unpaidTaxes.sumOf { it.unpaidAmount }

    val totalTaxAmount: Double get() = calculatedTaxes.sumOf { it.totalTax }

    val totalPaidTaxAmount: Double get() = (totalTaxAmount - totalUnpaidTaxAmount).coerceAtLeast(0.0)

    fun unpaidAmount(type: TaxType): Double = unpaidTaxes.sumOf { it.unpaidAmount(type) }
}
