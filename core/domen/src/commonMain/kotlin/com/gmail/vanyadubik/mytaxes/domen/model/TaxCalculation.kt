package com.gmail.vanyadubik.mytaxes.domen.model

import kotlinx.datetime.LocalDate

/** A single income as entered by the user. */
data class IncomeInput(
    val date: LocalDate,
    val amount: Double,
    val currency: Currency,
)

/** An income converted to UAH with the NBU rate of its date. */
data class IncomeItem(
    val date: LocalDate,
    val amount: Double,
    val currency: Currency,
    val rate: Double,
    val amountUah: Double,
)

/** Taxes for one or several incomes of the same period, calculated from their total in UAH. */
data class TaxCalculation(
    val id: String? = null,
    val incomes: List<IncomeItem>,
    val amountUah: Double,
    val singleTax: Double,
    val militaryLevy: Double,
    val esv: Double = 0.0,
    val totalTax: Double,
    val group: FopGroup = FopGroup.GROUP_3_5,
    val createdAt: Long = 0L,
) {
    /** Number of distinct calendar months the incomes fall into; the ESV is paid for each of them. */
    val months: Int get() = incomes.map { it.date.year to it.date.monthNumber }.distinct().size.coerceAtLeast(1)
}
