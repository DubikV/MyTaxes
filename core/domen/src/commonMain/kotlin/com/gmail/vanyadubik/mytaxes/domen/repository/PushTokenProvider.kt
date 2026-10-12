package com.gmail.vanyadubik.mytaxes.domen.repository

/** Platform source of the push (FCM) registration token. */
interface PushTokenProvider {
    val platform: String

    /** Language code of the device, used when the app language follows the system. */
    val systemLanguage: String

    /** Returns null when push notifications are not available (no Google services, not supported yet). */
    suspend fun getToken(): String?
}
