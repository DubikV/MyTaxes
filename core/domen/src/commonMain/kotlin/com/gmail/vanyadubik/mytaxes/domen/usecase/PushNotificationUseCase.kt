package com.gmail.vanyadubik.mytaxes.domen.usecase

import com.gmail.vanyadubik.mytaxes.domen.model.PushDevice
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState
import com.gmail.vanyadubik.mytaxes.domen.repository.DeviceRepository
import com.gmail.vanyadubik.mytaxes.domen.repository.PushTokenProvider
import kotlinx.coroutines.flow.first

/**
 * Keeps the FCM token of this device in Cloud Firestore (users/{uid}/devices/{token}) for signed in users,
 * so the backend can send them tax reminders even when the app is not running.
 */
class PushNotificationUseCase(
    private val authUseCase: AuthUseCase,
    private val dataStoreUseCase: DataStoreUseCase,
    private val deviceRepository: DeviceRepository,
    private val tokenProvider: PushTokenProvider,
) {
    /** Registers (or refreshes the group / language of) the device; [token] is the new token when FCM rotated it. */
    suspend fun register(token: String? = null) {
        val fcmToken = token ?: tokenProvider.getToken() ?: return
        val language = dataStoreUseCase.language.first().let { it.code ?: tokenProvider.systemLanguage }
        val device = PushDevice(
            token = fcmToken,
            platform = tokenProvider.platform,
            language = language,
            group = dataStoreUseCase.fopGroup.first(),
        )
        withSession { userId, idToken -> deviceRepository.register(userId, idToken, device) }
    }

    /** Stops the reminders for the current user on this device; call before logging out. */
    suspend fun unregister() {
        val token = tokenProvider.getToken() ?: return
        withSession { userId, idToken -> deviceRepository.unregister(userId, idToken, token) }
    }

    private suspend fun withSession(block: suspend (String, String) -> ResultState<Unit>) {
        val session = authUseCase.requireSession()
        if (session is ResultState.Success) session.data!!.let { (userId, token) -> block(userId, token) }
    }
}
