package com.gmail.vanyadubik.myassistant.domen.usecase

import com.gmail.vanyadubik.myassistant.domen.model.Expense
import com.gmail.vanyadubik.myassistant.domen.model.MonthlyStats
import com.gmail.vanyadubik.myassistant.domen.model.TaxCalculation

/**
 * Groups saved calculations and expenses by calendar month for the dashboard chart.
 * Incomes go to the month of their date, the taxes of a calculation to the month of its first income.
 */
class MonthlyStatsUseCase {

    /** Returns [months] consecutive months ending with [endYear]/[endMonth] (oldest first). */
    operator fun invoke(
        calculations: List<TaxCalculation>,
        expenses: List<Expense>,
        endYear: Int,
        endMonth: Int,
        months: Int = DEFAULT_MONTHS,
    ): List<MonthlyStats> {
        val endIndex = endYear * 12 + (endMonth - 1)
        fun index(year: Int, month: Int) = year * 12 + (month - 1)

        val incomes = DoubleArray(months)
        val taxes = DoubleArray(months)
        val spent = DoubleArray(months)
        fun slot(year: Int, month: Int) = (index(year, month) - (endIndex - months + 1)).takeIf { it in 0 until months }

        calculations.forEach { calculation ->
            calculation.incomes.forEach { income ->
                slot(income.date.year, income.date.monthNumber)?.let { incomes[it] += income.amountUah }
            }
            calculation.incomes.minByOrNull { it.date }?.let { first ->
                slot(first.date.year, first.date.monthNumber)?.let { taxes[it] += calculation.totalTax }
            }
        }
        expenses.forEach { expense ->
            slot(expense.date.year, expense.date.monthNumber)?.let { spent[it] += expense.amountUah }
        }

        return List(months) { i ->
            val position = endIndex - months + 1 + i
            MonthlyStats(
                year = position / 12,
                month = position % 12 + 1,
                income = incomes[i],
                tax = taxes[i],
                expenses = spent[i],
            )
        }
    }

    companion object {
        const val DEFAULT_MONTHS = 6
    }
}
