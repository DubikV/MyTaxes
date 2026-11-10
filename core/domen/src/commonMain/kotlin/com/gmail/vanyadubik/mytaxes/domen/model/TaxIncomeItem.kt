package com.gmail.vanyadubik.myassistant.domen.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class TaxIncomeItem(
    val id: String,
    val dateString: String,
    val amount: Double,
    val currencyCode: String,
    val isTaxCalculated: Boolean = false,
    val rate: Double = 0.0,
    val amountUah: Double = 0.0,
    val singleTax: Double = 0.0,
    val militaryLevy: Double = 0.0,
    val esv: Double = 0.0,
    val totalTax: Double = 0.0,
    val groupId: String = FopGroup.GROUP_3_5.id,
    val calculatedAt: Long = 0L,
    val isSingleTaxPaid: Boolean = false,
    val isMilitaryLevyPaid: Boolean = false,
    val isEsvPaid: Boolean = false,
    val paidAt: Long = 0L,
) {
    fun isPaid(type: TaxType): Boolean = when (type) {
        TaxType.SINGLE_TAX -> isSingleTaxPaid
        TaxType.MILITARY_LEVY -> isMilitaryLevyPaid
        TaxType.ESV -> isEsvPaid
    }

    fun amount(type: TaxType): Double = when (type) {
        TaxType.SINGLE_TAX -> singleTax
        TaxType.MILITARY_LEVY -> militaryLevy
        TaxType.ESV -> esv
    }

    /** Amount of the given tax that is still to be paid. */
    fun unpaidAmount(type: TaxType): Double = if (isPaid(type)) 0.0 else amount(type)

    val unpaidAmount: Double get() = TaxType.entries.sumOf { unpaidAmount(it) }

    /** Calculated and nothing left to pay. */
    val isPaid: Boolean get() = isTaxCalculated && unpaidAmount <= 0.0

    val isPartiallyPaid: Boolean
        get() = isTaxCalculated && !isPaid && TaxType.entries.any { isPaid(it) && amount(it) > 0.0 }

    val date: LocalDate get() = runCatching { LocalDate.parse(dateString) }.getOrDefault(LocalDate(2026, 1, 1))
    val currency: Currency get() = Currency.fromCode(currencyCode)
    val group: FopGroup get() = FopGroup.fromId(groupId)
}
