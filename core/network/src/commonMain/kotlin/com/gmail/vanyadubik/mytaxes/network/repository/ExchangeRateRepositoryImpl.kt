package com.gmail.vanyadubik.mytaxes.network.repository

import com.gmail.vanyadubik.mytaxes.domen.model.Currency
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState
import com.gmail.vanyadubik.mytaxes.domen.repository.ExchangeRateRepository
import com.gmail.vanyadubik.mytaxes.network.ApiException
import com.gmail.vanyadubik.mytaxes.network.apiCall
import com.gmail.vanyadubik.mytaxes.network.dto.NbuRateDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

/** Official exchange rates of the National Bank of Ukraine. */
class ExchangeRateRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
) : ExchangeRateRepository {

    override suspend fun getRate(currency: Currency, date: LocalDate): ResultState<Double> = apiCall {
        val response = httpClient.get(NBU_URL) {
            parameter("valcode", currency.code)
            parameter("date", date.toNbuDate())
            parameter("json", "")
        }
        if (!response.status.isSuccess()) {
            throw ApiException(message = "NBU service error: HTTP ${response.status.value}")
        }
        val rate = json.decodeFromString<List<NbuRateDto>>(response.bodyAsText())
            .firstOrNull { it.currencyCode.equals(currency.code, ignoreCase = true) }
            ?.rate
        rate ?: throw ApiException(message = "NBU has no ${currency.code} rate for $date")
    }

    private fun LocalDate.toNbuDate(): String =
        "${year.toString().padStart(4, '0')}${month.ordinal.plus(1).toString().padStart(2, '0')}${day.toString().padStart(2, '0')}"

    private companion object {
        const val NBU_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange"
    }
}
