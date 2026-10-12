package com.gmail.vanyadubik.mytaxes.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual class HttpEngineFactory {
    actual fun createEngine(): HttpClientEngine = OkHttp.create()
}