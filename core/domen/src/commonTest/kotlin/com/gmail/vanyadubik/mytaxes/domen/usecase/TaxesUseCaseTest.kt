package com.gmail.vanyadubik.myassistant.domen.usecase

import com.gmail.vanyadubik.myassistant.domen.model.Currency
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.PreferenceKey
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.repository.DataStoreRepository
import com.gmail.vanyadubik.myassistant.domen.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class TaxesUseCaseTest {

    private class FakeStore : DataStoreRepository {
        val values = MutableStateFlow<Map<String, Any?>>(emptyMap())

        @Suppress("UNCHECKED_CAST")
        override fun <T> getPreference(key: PreferenceKey<T>): Flow<T?> = values.map { it[key.name] as T? }
        override suspend fun <T> setPreference(key: PreferenceKey<T>, value: T) {
            values.value = values.value + (key.name to value)
        }
        override suspend fun <T> clearPreference(key: PreferenceKey<T>) = Unit
        override suspend fun clearAllPreference() = Unit
        override suspend fun logout() = Unit
    }

    private val rates = object : ExchangeRateRepository {
        override suspend fun getRate(currency: Currency, date: LocalDate) = ResultState.Success(40.0)
    }

    private fun useCase() = TaxesUseCase(FakeStore(), CalculateTaxUseCase(rates))

    @Test
    fun esvIsChargedOncePerMonth() = runTest {
        val useCase = useCase()
        useCase.addIncome(LocalDate(2026, 10, 3), 100.0, Currency.UAH)
        useCase.addIncome(LocalDate(2026, 10, 20), 200.0, Currency.UAH)
        useCase.addIncome(LocalDate(2026, 11, 2), 300.0, Currency.UAH)

        useCase.calculateTaxesForUnpaid(FopGroup.GROUP_3_5)

        val items = useCase.getIncomes().first()
        assertEquals(2 * FopGroup.ESV_MONTHLY, items.sumOf { it.esv })
        assertEquals(1, items.count { it.esv > 0.0 && it.date.monthNumber == 10 })
    }

    @Test
    fun esvIsNotRepeatedForMonthCalculatedEarlier() = runTest {
        val useCase = useCase()
        useCase.addIncome(LocalDate(2026, 10, 3), 100.0, Currency.UAH)
        useCase.calculateTaxesForUnpaid(FopGroup.GROUP_3_5)
        useCase.addIncome(LocalDate(2026, 10, 25), 200.0, Currency.UAH)
        val result = useCase.calculateTaxesForUnpaid(FopGroup.GROUP_3_5)

        assertEquals(0.0, (result as ResultState.Success).data!!.esv)
        assertEquals(FopGroup.ESV_MONTHLY, useCase.getIncomes().first().sumOf { it.esv })
    }

    @Test
    fun fixedTaxOfGroupTwoIsChargedOncePerMonth() = runTest {
        val useCase = useCase()
        useCase.addIncome(LocalDate(2026, 10, 3), 100.0, Currency.UAH)
        useCase.addIncome(LocalDate(2026, 10, 20), 200.0, Currency.UAH)

        useCase.calculateTaxesForUnpaid(FopGroup.GROUP_2)

        val items = useCase.getIncomes().first()
        assertEquals(FopGroup.GROUP_2.fixedSingleTax, items.sumOf { it.singleTax })
        assertEquals(FopGroup.GROUP_2.fixedMilitaryLevy, items.sumOf { it.militaryLevy })
    }

    @Test
    fun duplicatedEsvOfOlderCalculationsIsRemoved() = runTest {
        val store = FakeStore()
        val useCase = TaxesUseCase(store, CalculateTaxUseCase(rates))
        fun item(id: String, day: Int) =
            """{"id":"$id","dateString":"2026-10-${day.toString().padStart(2, '0')}","amount":100.0,"currencyCode":"UAH","isTaxCalculated":true,""" +
                """"rate":1.0,"amountUah":100.0,"singleTax":5.0,"militaryLevy":1.0,"esv":1760.0,"totalTax":1766.0}"""
        store.setPreference(PreferenceKey.TaxIncomesKey, "[${item("a", 3)},${item("b", 20)}]")

        val items = useCase.getIncomes().first()

        assertEquals(FopGroup.ESV_MONTHLY, items.sumOf { it.esv })
        assertEquals(1766.0 + 6.0, items.sumOf { it.totalTax })
    }
}
