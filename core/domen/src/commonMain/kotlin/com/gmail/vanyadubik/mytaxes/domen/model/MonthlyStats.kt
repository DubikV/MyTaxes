package com.gmail.vanyadubik.myassistant.domen.model

/** Incomes, taxes and expenses (all in UAH) of one calendar month. */
data class MonthlyStats(
    val year: Int,
    val month: Int,
    val income: Double,
    val tax: Double,
    val expenses: Double,
)
