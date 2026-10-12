package com.gmail.vanyadubik.mytaxes.network.repository

import com.gmail.vanyadubik.mytaxes.domen.model.PushDevice
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState
import com.gmail.vanyadubik.mytaxes.domen.repository.DeviceRepository
import com.gmail.vanyadubik.mytaxes.network.ApiException
import com.gmail.vanyadubik.mytaxes.network.FirebaseConfig
import com.gmail.vanyadubik.mytaxes.network.apiCall
import com.gmail.vanyadubik.mytaxes.network.bodyOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Cloud Firestore through its REST API: users/{userId}/devices/{fcmToken}. */
@OptIn(ExperimentalTime::class)
class DeviceRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val config: FirebaseConfig,
) : DeviceRepository {

    private fun documentUrl(userId: String, token: String) =
        "https://firestore.googleapis.com/v1/projects/${config.projectId}/databases/(default)/documents" +
            "/users/$userId/devices/${token.encodeURLPathPart()}"

    private fun errorMapper(message: String?) = ApiException(message = message ?: "Firestore error")

    // PATCH without an update mask creates the document or replaces it
    override suspend fun register(userId: String, idToken: String, device: PushDevice): ResultState<Unit> = apiCall {
        httpClient.patch(documentUrl(userId, device.token)) {
            bearerAuth(idToken)
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(JsonObject.serializer(), device.toDocument()))
        }.bodyOrThrow(json, ::errorMapper)
        Unit
    }

    override suspend fun unregister(userId: String, idToken: String, token: String): ResultState<Unit> = apiCall {
        httpClient.delete(documentUrl(userId, token)) {
            bearerAuth(idToken)
        }.bodyOrThrow(json, ::errorMapper)
        Unit
    }

    private fun PushDevice.toDocument() = buildJsonObject {
        put("fields", buildJsonObject {
            put("platform", buildJsonObject { put("stringValue", platform) })
            put("language", buildJsonObject { put("stringValue", language) })
            put("group", buildJsonObject { put("stringValue", group.id) })
            put("updatedAt", buildJsonObject { put("integerValue", Clock.System.now().toEpochMilliseconds().toString()) })
        })
    }
}
