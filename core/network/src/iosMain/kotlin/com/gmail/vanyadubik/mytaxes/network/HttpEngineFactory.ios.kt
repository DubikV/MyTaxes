package com.gmail.vanyadubik.myassistant.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual class HttpEngineFactory {
    actual fun createEngine(): HttpClientEngine = Darwin.create()
}