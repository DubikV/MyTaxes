package com.gmail.vanyadubik.mytaxes.domen.usecase

import com.gmail.vanyadubik.mytaxes.domen.model.Currency
import com.gmail.vanyadubik.mytaxes.domen.model.Expense
import com.gmail.vanyadubik.mytaxes.domen.model.IncomeItem
import com.gmail.vanyadubik.mytaxes.domen.model.TaxCalculation
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class MonthlyStatsUseCaseTest {
    private fun income(date: LocalDate, uah: Double) = IncomeItem(date, uah, Currency.UAH, 1.0, uah)

    @Test
    fun groupsByMonthAndCrossesYearBoundary() {
        val calculation = TaxCalculation(
            incomes = listOf(income(LocalDate(2025, 12, 20), 1000.0), income(LocalDate(2026, 1, 5), 500.0)),
            amountUah = 1500.0, singleTax = 75.0, militaryLevy = 15.0, totalTax = 90.0,
        )
        val expense = Expense(date = LocalDate(2026, 1, 10), amountUah = 200.0)

        val stats = MonthlyStatsUseCase()(listOf(calculation), listOf(expense), endYear = 2026, endMonth = 2, months = 3)

        assertEquals(listOf(2025 to 12, 2026 to 1, 2026 to 2), stats.map { it.year to it.month })
        assertEquals(listOf(1000.0, 500.0, 0.0), stats.map { it.income })
        assertEquals(listOf(90.0, 0.0, 0.0), stats.map { it.tax })
        assertEquals(listOf(0.0, 200.0, 0.0), stats.map { it.expenses })
    }

    @Test
    fun ignoresEntriesOutsideTheRange() {
        val expense = Expense(date = LocalDate(2020, 1, 1), amountUah = 10.0)
        val stats = MonthlyStatsUseCase()(emptyList(), listOf(expense), endYear = 2026, endMonth = 6)
        assertEquals(6, stats.size)
        assertEquals(0.0, stats.sumOf { it.expenses })
    }
}
