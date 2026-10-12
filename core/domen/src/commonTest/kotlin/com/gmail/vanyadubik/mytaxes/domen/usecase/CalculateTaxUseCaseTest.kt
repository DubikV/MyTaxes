package com.gmail.vanyadubik.mytaxes.domen.usecase

import com.gmail.vanyadubik.mytaxes.domen.model.Currency
import com.gmail.vanyadubik.mytaxes.domen.model.FopGroup
import com.gmail.vanyadubik.mytaxes.domen.model.IncomeInput
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState
import com.gmail.vanyadubik.mytaxes.domen.repository.ExchangeRateRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalculateTaxUseCaseTest {
    private val date = LocalDate(2025, 1, 15)

    private class FakeRates(private val result: ResultState<Double>) : ExchangeRateRepository {
        var calls = 0
        override suspend fun getRate(currency: Currency, date: LocalDate): ResultState<Double> {
            calls++
            return result
        }
    }

    @Test
    fun calculatesTaxesFromNbuRate() = runTest {
        val useCase = CalculateTaxUseCase(FakeRates(ResultState.Success(41.5)))
        val result = (useCase(date, 1000.0, Currency.USD) as ResultState.Success).data!!
        assertEquals(41500.0, result.amountUah)
        assertEquals(2075.0, result.singleTax)
        assertEquals(415.0, result.militaryLevy)
        assertEquals(1760.0, result.esv)
        assertEquals(4250.0, result.totalTax)
    }

    @Test
    fun uahDoesNotRequestRate() = runTest {
        val rates = FakeRates(ResultState.Error(message = "must not be called"))
        val result = (CalculateTaxUseCase(rates)(date, 200.0, Currency.UAH) as ResultState.Success).data!!
        assertEquals(0, rates.calls)
        assertEquals(1.0, result.incomes.single().rate)
        assertEquals(1772.0, result.totalTax)
    }

    @Test
    fun propagatesRateError() = runTest {
        val useCase = CalculateTaxUseCase(FakeRates(ResultState.Error(message = "no rate")))
        assertTrue(useCase(date, 10.0, Currency.EUR) is ResultState.Error)
    }

    @Test
    fun groupThreeVatPayerUsesThreePercent() = runTest {
        val useCase = CalculateTaxUseCase(FakeRates(ResultState.Success(40.0)))
        val result = (useCase(date, 1000.0, Currency.EUR, FopGroup.GROUP_3_3) as ResultState.Success).data!!
        assertEquals(1200.0, result.singleTax)
        assertEquals(400.0, result.militaryLevy)
    }

    @Test
    fun fixedGroupsDoNotDependOnIncome() = runTest {
        val useCase = CalculateTaxUseCase(FakeRates(ResultState.Success(40.0)))
        val small = (useCase(date, 1.0, Currency.USD, FopGroup.GROUP_2) as ResultState.Success).data!!
        val large = (useCase(date, 5000.0, Currency.USD, FopGroup.GROUP_2) as ResultState.Success).data!!
        assertEquals(small.totalTax, large.totalTax)
        assertEquals(FopGroup.GROUP_2.fixedSingleTax + FopGroup.GROUP_2.fixedMilitaryLevy + FopGroup.ESV_MONTHLY, large.totalTax)
    }

    @Test
    fun sumsSeveralIncomesBeforeCalculatingTax() = runTest {
        val rates = FakeRates(ResultState.Success(40.0))
        val incomes = listOf(
            IncomeInput(LocalDate(2025, 1, 20), 500.0, Currency.USD),
            IncomeInput(LocalDate(2025, 1, 5), 1000.0, Currency.UAH),
            IncomeInput(LocalDate(2025, 1, 20), 500.0, Currency.USD),
        )
        val result = (CalculateTaxUseCase(rates)(incomes) as ResultState.Success).data!!
        assertEquals(1, rates.calls)
        assertEquals(LocalDate(2025, 1, 5), result.incomes.first().date)
        assertEquals(41000.0, result.amountUah)
        assertEquals(2050.0, result.singleTax)
        assertEquals(410.0, result.militaryLevy)
    }

    @Test
    fun fixedGroupIsChargedOnceForSeveralIncomes() = runTest {
        val useCase = CalculateTaxUseCase(FakeRates(ResultState.Success(40.0)))
        val incomes = listOf(
            IncomeInput(date, 100.0, Currency.USD),
            IncomeInput(LocalDate(2025, 1, 16), 200.0, Currency.USD),
        )
        val result = (useCase(incomes, FopGroup.GROUP_2) as ResultState.Success).data!!
        assertEquals(FopGroup.GROUP_2.fixedSingleTax + FopGroup.GROUP_2.fixedMilitaryLevy + FopGroup.ESV_MONTHLY, result.totalTax)
    }

    @Test
    fun esvIsChargedForEachMonthOfIncomes() = runTest {
        val useCase = CalculateTaxUseCase(FakeRates(ResultState.Success(40.0)))
        val incomes = listOf(
            IncomeInput(LocalDate(2025, 1, 15), 100.0, Currency.USD),
            IncomeInput(LocalDate(2025, 2, 3), 100.0, Currency.USD),
            IncomeInput(LocalDate(2025, 2, 20), 100.0, Currency.USD),
        )
        val result = (useCase(incomes) as ResultState.Success).data!!
        assertEquals(2, result.months)
        assertEquals(3520.0, result.esv)
    }
}
