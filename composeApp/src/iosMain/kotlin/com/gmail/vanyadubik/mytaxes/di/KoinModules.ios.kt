package com.gmail.vanyadubik.mytaxes.di

import com.gmail.vanyadubik.mytaxes.domen.repository.PushTokenProvider
import org.koin.dsl.module
import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

/** Push notifications are not wired on iOS yet (needs the Firebase iOS SDK and an APNs key), so there is no token. */
private class IosPushTokenProvider : PushTokenProvider {
    override val platform = "ios"
    override val systemLanguage: String
        get() = (NSLocale.preferredLanguages.firstOrNull() as? String)?.substringBefore('-') ?: "en"

    override suspend fun getToken(): String? = null
}

actual fun platformModule() = module {
    single<PushTokenProvider> { IosPushTokenProvider() }
}
