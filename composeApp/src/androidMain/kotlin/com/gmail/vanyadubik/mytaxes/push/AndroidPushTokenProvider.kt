package com.gmail.vanyadubik.myassistant.push

import com.gmail.vanyadubik.myassistant.domen.repository.PushTokenProvider
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidPushTokenProvider : PushTokenProvider {
    override val platform = "android"
    override val systemLanguage: String get() = Locale.getDefault().language

    override suspend fun getToken(): String? = suspendCancellableCoroutine { continuation ->
        runCatching { FirebaseMessaging.getInstance().token }
            .onSuccess { task -> task.addOnCompleteListener { continuation.resume(it.result.takeIf { _ -> it.isSuccessful }) } }
            .onFailure { continuation.resume(null) }
    }
}
