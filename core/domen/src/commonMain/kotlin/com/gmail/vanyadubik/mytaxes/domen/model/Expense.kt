package com.gmail.vanyadubik.mytaxes.domen.model

import kotlinx.datetime.LocalDate

/** A business expense in UAH entered by the user. */
data class Expense(
    val id: String? = null,
    val date: LocalDate,
    val amountUah: Double,
    val note: String = "",
    val createdAt: Long = 0L,
)
