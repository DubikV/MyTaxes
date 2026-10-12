package com.gmail.vanyadubik.mytaxes.network.di

import com.gmail.vanyadubik.mytaxes.common.logger.LogI
import com.gmail.vanyadubik.mytaxes.common.utils.PlatformConfig.isDebugBuild
import com.gmail.vanyadubik.mytaxes.domen.repository.AuthRepository
import com.gmail.vanyadubik.mytaxes.domen.repository.ExchangeRateRepository
import com.gmail.vanyadubik.mytaxes.domen.repository.DeviceRepository
import com.gmail.vanyadubik.mytaxes.domen.repository.ExpenseRepository
import com.gmail.vanyadubik.mytaxes.domen.repository.TaxHistoryRepository
import com.gmail.vanyadubik.mytaxes.network.FirebaseConfig
import com.gmail.vanyadubik.mytaxes.network.HttpEngineFactory
import com.gmail.vanyadubik.mytaxes.network.repository.AuthRepositoryImpl
import com.gmail.vanyadubik.mytaxes.network.repository.ExchangeRateRepositoryImpl
import com.gmail.vanyadubik.mytaxes.network.repository.DeviceRepositoryImpl
import com.gmail.vanyadubik.mytaxes.network.repository.ExpenseRepositoryImpl
import com.gmail.vanyadubik.mytaxes.network.repository.TaxHistoryRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun networkModule(
    firebaseConfig: FirebaseConfig,
    enableNetworkLogs: Boolean = isDebugBuild(),
) = module {

    single { createJson() }
    single { firebaseConfig }
    single { createHttpClient(enableNetworkLogs = enableNetworkLogs, json = get()) }

    singleOf(::AuthRepositoryImpl).bind(AuthRepository::class)
    singleOf(::ExchangeRateRepositoryImpl).bind(ExchangeRateRepository::class)
    singleOf(::TaxHistoryRepositoryImpl).bind(TaxHistoryRepository::class)
    singleOf(::ExpenseRepositoryImpl).bind(ExpenseRepository::class)
    singleOf(::DeviceRepositoryImpl).bind(DeviceRepository::class)
}

fun createHttpClient(
    enableNetworkLogs: Boolean,
    json: Json,
) = HttpClient(HttpEngineFactory().createEngine()) {
    install(ContentNegotiation) {
        json(json, contentType = ContentType.Any)
    }

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, request ->
            if (isDebugBuild()) {
                LogI("HttpClient", "Request error on ${request.url}: ${cause.message}")
            }
        }
    }

    install(DefaultRequest) {
        header("Accept", "application/json")
    }

    if (enableNetworkLogs) {
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.BODY
        }
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30000
        connectTimeoutMillis = 15000
        socketTimeoutMillis = 30000
    }
}

fun createJson() = Json {
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
}
