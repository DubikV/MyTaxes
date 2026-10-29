package com.gmail.vanyadubik.myassistant.domen.repository

import com.gmail.vanyadubik.myassistant.domen.model.Currency
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import kotlinx.datetime.LocalDate

interface ExchangeRateRepository {
    /** Official NBU rate (UAH per 1 unit of [currency]) for [date]. */
    suspend fun getRate(currency: Currency, date: LocalDate): ResultState<Double>
}
