package com.gmail.vanyadubik.mytaxes.domen.usecase

import com.gmail.vanyadubik.mytaxes.domen.model.AuthSession
import com.gmail.vanyadubik.mytaxes.domen.model.PreferenceKey
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState
import com.gmail.vanyadubik.mytaxes.domen.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Handles sign in / sign up and keeps the Firebase session in the data store. */
@OptIn(ExperimentalTime::class)
class AuthUseCase(
    private val authRepository: AuthRepository,
    private val dataStoreUseCase: DataStoreUseCase,
) {
    val userId: Flow<String?> = dataStoreUseCase.getPreference(PreferenceKey.UserId)
    val userEmail: Flow<String?> = dataStoreUseCase.getPreference(PreferenceKey.UserEmail)

    val isLoggedIn: Flow<Boolean> = combine(
        dataStoreUseCase.getPreference(PreferenceKey.UserId),
        dataStoreUseCase.getPreference(PreferenceKey.RefreshToken),
    ) { id, refresh -> !id.isNullOrBlank() && !refresh.isNullOrBlank() }
        .distinctUntilChanged()

    suspend fun signIn(email: String, password: String): ResultState<AuthSession> =
        authRepository.signIn(email.trim(), password).also { saveIfSuccess(it) }

    suspend fun signUp(email: String, password: String): ResultState<AuthSession> =
        authRepository.signUp(email.trim(), password).also { saveIfSuccess(it) }

    suspend fun logout() = dataStoreUseCase.logout()

    /** Returns a non-expired id token and the user id, refreshing the token when needed. */
    suspend fun requireSession(): ResultState<Pair<String, String>> {
        val userId = dataStoreUseCase.getPreference(PreferenceKey.UserId).first()
        val token = dataStoreUseCase.getPreference(PreferenceKey.IdToken).first()
        val refresh = dataStoreUseCase.getPreference(PreferenceKey.RefreshToken).first()
        val expiresAt = dataStoreUseCase.getPreference(PreferenceKey.TokenExpiresAt).first() ?: 0L
        if (userId.isNullOrBlank() || refresh.isNullOrBlank()) {
            return ResultState.Error(message = "You are not signed in")
        }
        if (!token.isNullOrBlank() && expiresAt - TOKEN_LEEWAY_MS > Clock.System.now().toEpochMilliseconds()) {
            return ResultState.Success(userId to token)
        }
        return when (val result = authRepository.refresh(refresh)) {
            is ResultState.Success -> {
                val session = result.data!!
                saveIfSuccess(result)
                ResultState.Success(session.userId to session.idToken)
            }
            is ResultState.Error -> {
                if (result.title == SESSION_EXPIRED) logout()
                result
            }
            else -> ResultState.Error(message = "Unable to refresh session")
        }
    }

    private suspend fun saveIfSuccess(result: ResultState<AuthSession>) {
        val session = (result as? ResultState.Success)?.data ?: return
        dataStoreUseCase.setPreference(PreferenceKey.IdToken, session.idToken)
        dataStoreUseCase.setPreference(PreferenceKey.RefreshToken, session.refreshToken)
        dataStoreUseCase.setPreference(PreferenceKey.TokenExpiresAt, session.expiresAtMillis)
        // The refresh endpoint does not return the email, keep the stored one
        if (session.email.isNotBlank()) dataStoreUseCase.setPreference(PreferenceKey.UserEmail, session.email)
        dataStoreUseCase.setPreference(PreferenceKey.UserId, session.userId)
    }

    companion object {
        const val SESSION_EXPIRED = "SESSION_EXPIRED"
        private const val TOKEN_LEEWAY_MS = 60_000L
    }
}
