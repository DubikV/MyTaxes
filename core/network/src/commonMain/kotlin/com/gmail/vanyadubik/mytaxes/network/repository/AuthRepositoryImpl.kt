package com.gmail.vanyadubik.myassistant.network.repository

import com.gmail.vanyadubik.myassistant.domen.model.AuthSession
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.repository.AuthRepository
import com.gmail.vanyadubik.myassistant.domen.usecase.AuthUseCase
import com.gmail.vanyadubik.myassistant.network.ApiException
import com.gmail.vanyadubik.myassistant.network.FirebaseConfig
import com.gmail.vanyadubik.myassistant.network.apiCall
import com.gmail.vanyadubik.myassistant.network.bodyOrThrow
import com.gmail.vanyadubik.myassistant.network.dto.AuthResponse
import com.gmail.vanyadubik.myassistant.network.dto.DeleteAccountRequest
import com.gmail.vanyadubik.myassistant.network.dto.EmailPasswordRequest
import com.gmail.vanyadubik.myassistant.network.dto.RefreshResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.parameters
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Firebase Authentication through the Identity Toolkit REST API. */
@OptIn(ExperimentalTime::class)
class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val config: FirebaseConfig,
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): ResultState<AuthSession> =
        authenticate("accounts:signInWithPassword", email, password)

    override suspend fun signUp(email: String, password: String): ResultState<AuthSession> =
        authenticate("accounts:signUp", email, password)

    override suspend fun refresh(refreshToken: String): ResultState<AuthSession> = apiCall {
        val text = httpClient.submitForm(
            url = "$SECURE_TOKEN_URL/token",
            formParameters = parameters {
                append("grant_type", "refresh_token")
                append("refresh_token", refreshToken)
            }
        ) {
            parameter("key", config.apiKey)
        }.bodyOrThrow(json) { code ->
            val expired = code in SESSION_ERRORS
            ApiException(
                title = if (expired) AuthUseCase.SESSION_EXPIRED else null,
                message = if (expired) "Session expired. Please sign in again." else mapAuthError(code)
            )
        }
        val response = json.decodeFromString<RefreshResponse>(text)
        AuthSession(
            userId = response.userId,
            email = "",
            idToken = response.idToken,
            refreshToken = response.refreshToken,
            expiresAtMillis = expiresAt(response.expiresIn),
        )
    }

    override suspend fun deleteAccount(idToken: String): ResultState<Unit> = apiCall {
        httpClient.post("$IDENTITY_URL/accounts:delete") {
            parameter("key", config.apiKey)
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(DeleteAccountRequest.serializer(), DeleteAccountRequest(idToken)))
        }.bodyOrThrow(json) { code -> ApiException(message = mapAuthError(code)) }
        Unit
    }

    private suspend fun authenticate(path: String, email: String, password: String): ResultState<AuthSession> =
        apiCall {
            val text = httpClient.post("$IDENTITY_URL/$path") {
                parameter("key", config.apiKey)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(EmailPasswordRequest.serializer(), EmailPasswordRequest(email, password)))
            }.bodyOrThrow(json) { code -> ApiException(message = mapAuthError(code)) }
            val response = json.decodeFromString<AuthResponse>(text)
            AuthSession(
                userId = response.localId,
                email = response.email ?: email,
                idToken = response.idToken,
                refreshToken = response.refreshToken,
                expiresAtMillis = expiresAt(response.expiresIn),
            )
        }

    private fun expiresAt(expiresInSeconds: String) =
        Clock.System.now().toEpochMilliseconds() + (expiresInSeconds.toLongOrNull() ?: 3600L) * 1000

    private fun mapAuthError(code: String?): String = when {
        code == null -> "Authentication failed"
        code.startsWith("EMAIL_EXISTS") -> "An account with this email already exists"
        code.startsWith("INVALID_LOGIN_CREDENTIALS") ||
            code.startsWith("INVALID_PASSWORD") ||
            code.startsWith("EMAIL_NOT_FOUND") -> "Wrong email or password"
        code.startsWith("INVALID_EMAIL") -> "Invalid email address"
        code.startsWith("WEAK_PASSWORD") -> "Password must be at least 6 characters"
        code.startsWith("CREDENTIAL_TOO_OLD_LOGIN_AGAIN") || code.startsWith("TOKEN_EXPIRED") -> "Please sign in again to delete the account"
        code.startsWith("USER_DISABLED") -> "This account has been disabled"
        code.startsWith("TOO_MANY_ATTEMPTS") -> "Too many attempts. Try again later"
        code.startsWith("API_KEY") || code.contains("API key") -> "Invalid Firebase API key. Check FirebaseSettings"
        else -> code
    }

    private companion object {
        const val IDENTITY_URL = "https://identitytoolkit.googleapis.com/v1"
        const val SECURE_TOKEN_URL = "https://securetoken.googleapis.com/v1"
        val SESSION_ERRORS = listOf("TOKEN_EXPIRED", "USER_DISABLED", "USER_NOT_FOUND", "INVALID_REFRESH_TOKEN")
    }
}
