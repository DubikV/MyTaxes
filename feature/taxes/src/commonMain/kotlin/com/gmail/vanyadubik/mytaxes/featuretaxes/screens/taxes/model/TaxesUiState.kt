package com.gmail.vanyadubik.myassistant.featuretaxes.screens.taxes.model

import com.gmail.vanyadubik.myassistant.domen.model.Currency
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.TaxCalculation
import com.gmail.vanyadubik.myassistant.domen.model.TaxIncomeItem
import kotlinx.datetime.LocalDate

enum class InputError { DATE, AMOUNT, FUTURE_DATE }

data class TaxesUiState(
    val incomes: List<TaxIncomeItem> = emptyList(),
    val group: FopGroup = FopGroup.GROUP_3_5,
    val isGroupSaved: Boolean = false,
    val savedCurrency: Currency? = null,
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val actionError: String? = null,
    val errorMessage: String? = null,
    val calculationResult: TaxCalculation? = null,
    val isLoggedIn: Boolean = false,
    // Income input form
    val inputDate: LocalDate? = null,
    val inputAmount: String = "",
    val inputCurrency: Currency = Currency.USD,
    val inputError: InputError? = null,
) {
    val untaxedIncomes: List<TaxIncomeItem> get() = incomes.filterNot { it.isTaxCalculated }
}
