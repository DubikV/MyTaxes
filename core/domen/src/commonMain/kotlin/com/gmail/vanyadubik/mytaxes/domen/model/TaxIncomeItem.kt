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
) {
    val date: LocalDate get() = runCatching { LocalDate.parse(dateString) }.getOrDefault(LocalDate(2026, 1, 1))
    val currency: Currency get() = Currency.fromCode(currencyCode)
    val group: FopGroup get() = FopGroup.fromId(groupId)
}
