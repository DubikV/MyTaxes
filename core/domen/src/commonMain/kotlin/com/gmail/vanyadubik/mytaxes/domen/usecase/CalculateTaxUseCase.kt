package com.gmail.vanyadubik.myassistant.domen.usecase

import com.gmail.vanyadubik.myassistant.domen.model.Currency
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.IncomeInput
import com.gmail.vanyadubik.myassistant.domen.model.IncomeItem
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.model.TaxCalculation
import com.gmail.vanyadubik.myassistant.domen.repository.ExchangeRateRepository
import kotlinx.datetime.LocalDate
import kotlin.math.round

/** Fetches the official NBU rate for every income date and calculates the taxes on their total in UAH. */
class CalculateTaxUseCase(
    private val exchangeRateRepository: ExchangeRateRepository,
) {
    suspend operator fun invoke(
        incomes: List<IncomeInput>,
        group: FopGroup = FopGroup.GROUP_3_5,
    ): ResultState<TaxCalculation> {
        if (incomes.isEmpty()) return ResultState.Error(message = "No incomes")
        val rates = mutableMapOf<Pair<Currency, LocalDate>, Double>()
        val items = incomes.sortedBy { it.date }.map { income ->
            val rate = if (income.currency == Currency.UAH) {
                1.0
            } else {
                rates[income.currency to income.date] ?: when (val result = exchangeRateRepository.getRate(income.currency, income.date)) {
                    is ResultState.Success -> result.data ?: return ResultState.Error(message = "Rate not found")
                    is ResultState.Error -> return result
                    else -> return ResultState.Error(message = "Rate not found")
                }.also { rates[income.currency to income.date] = it }
            }
            IncomeItem(
                date = income.date,
                amount = income.amount,
                currency = income.currency,
                rate = rate,
                amountUah = (income.amount.round2() * rate).round2(),
            )
        }
        return ResultState.Success(calculate(items, group))
    }

    suspend operator fun invoke(
        date: LocalDate,
        amount: Double,
        currency: Currency,
        group: FopGroup = FopGroup.GROUP_3_5,
    ): ResultState<TaxCalculation> = invoke(listOf(IncomeInput(date, amount, currency)), group)

    fun calculate(incomes: List<IncomeItem>, group: FopGroup = FopGroup.GROUP_3_5): TaxCalculation {
        val amountUah = incomes.sumOf { it.amountUah }.round2()
        val singleTax = if (group.isFixed) group.fixedSingleTax else (amountUah * group.singleTaxPercent / 100).round2()
        val militaryLevy = if (group.isFixed) group.fixedMilitaryLevy else (amountUah * group.militaryLevyPercent / 100).round2()
        val esv = (group.esv * incomes.map { it.date.year to it.date.monthNumber }.distinct().size.coerceAtLeast(1)).round2()
        return TaxCalculation(
            incomes = incomes,
            amountUah = amountUah,
            singleTax = singleTax,
            militaryLevy = militaryLevy,
            esv = esv,
            totalTax = (singleTax + militaryLevy + esv).round2(),
            group = group,
        )
    }
}

fun Double.round2(): Double = round(this * 100) / 100
