package com.gmail.vanyadubik.myassistant.network.repository

import com.gmail.vanyadubik.myassistant.domen.model.AccountProfile
import com.gmail.vanyadubik.myassistant.domen.model.Car
import com.gmail.vanyadubik.myassistant.domen.model.Currency
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.model.TaxReminderMode
import com.gmail.vanyadubik.myassistant.domen.repository.ProfileRepository
import com.gmail.vanyadubik.myassistant.network.ApiException
import com.gmail.vanyadubik.myassistant.network.FirebaseConfig
import com.gmail.vanyadubik.myassistant.network.apiCall
import com.gmail.vanyadubik.myassistant.network.bodyOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Cloud Firestore through its REST API: users/{userId}/profile/main. */
class ProfileRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val config: FirebaseConfig,
) : ProfileRepository {

    private fun documentUrl(userId: String) =
        "https://firestore.googleapis.com/v1/projects/${config.projectId}/databases/(default)/documents/users/$userId/profile/main"

    private fun errorMapper(message: String?) = ApiException(message = message ?: "Firestore error")

    override suspend fun getProfile(userId: String, idToken: String): ResultState<AccountProfile> = apiCall {
        val response = httpClient.get(documentUrl(userId)) { bearerAuth(idToken) }
        if (response.status == HttpStatusCode.NotFound) return@apiCall AccountProfile()
        val text = response.bodyOrThrow(json, ::errorMapper)
        val fields = json.parseToJsonElement(text).jsonObject["fields"]?.jsonObject
        fun string(name: String) = fields?.get(name)?.jsonObject?.get("stringValue")?.jsonPrimitive?.content
        val carsValues = fields?.get("cars")?.jsonObject?.get("arrayValue")?.jsonObject?.get("values")?.jsonArray
        val cars = carsValues?.mapNotNull { value ->
            val carFields = value.jsonObject["mapValue"]?.jsonObject?.get("fields")?.jsonObject ?: return@mapNotNull null
            fun carStr(name: String) = carFields[name]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty()
            Car(
                id = carStr("id"),
                name = carStr("name"),
                number = carStr("number"),
            )
        }.orEmpty()
        AccountProfile(
            nickname = string("nickname"),
            avatarUrl = string("avatarUrl"),
            fopGroup = FopGroup.entries.firstOrNull { it.id == string("fopGroup") },
            currency = Currency.entries.firstOrNull { it.code == string("currency") },
            taxReminderMode = TaxReminderMode.entries.firstOrNull { it.id == string("taxReminderMode") },
            cars = cars,
        )
    }

    // PATCH creates the document; masked fields that are absent from the body are removed
    override suspend fun saveProfile(userId: String, idToken: String, profile: AccountProfile): ResultState<Unit> = apiCall {
        val document = buildJsonObject {
            put("fields", buildJsonObject {
                profile.nickname?.let { put("nickname", buildJsonObject { put("stringValue", it) }) }
                profile.avatarUrl?.let { put("avatarUrl", buildJsonObject { put("stringValue", it) }) }
                profile.fopGroup?.let { put("fopGroup", buildJsonObject { put("stringValue", it.id) }) }
                profile.currency?.let { put("currency", buildJsonObject { put("stringValue", it.code) }) }
                profile.taxReminderMode?.let { put("taxReminderMode", buildJsonObject { put("stringValue", it.id) }) }
                put("cars", buildJsonObject {
                    put("arrayValue", buildJsonObject {
                        put("values", JsonArray(profile.cars.map { it.toValue() }))
                    })
                })
            })
        }
        httpClient.patch(documentUrl(userId)) {
            bearerAuth(idToken)
            parameter("updateMask.fieldPaths", "nickname")
            parameter("updateMask.fieldPaths", "avatarUrl")
            parameter("updateMask.fieldPaths", "fopGroup")
            parameter("updateMask.fieldPaths", "currency")
            parameter("updateMask.fieldPaths", "taxReminderMode")
            parameter("updateMask.fieldPaths", "cars")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(JsonObject.serializer(), document))
        }.bodyOrThrow(json, ::errorMapper)
        Unit
    }

    override suspend fun delete(userId: String, idToken: String): ResultState<Unit> = apiCall {
        httpClient.delete(documentUrl(userId)) { bearerAuth(idToken) }.bodyOrThrow(json, ::errorMapper)
        Unit
    }

    private fun Car.toValue() = buildJsonObject {
        put("mapValue", buildJsonObject {
            put("fields", buildJsonObject {
                put("id", buildJsonObject { put("stringValue", id) })
                put("name", buildJsonObject { put("stringValue", name) })
                put("number", buildJsonObject { put("stringValue", number) })
            })
        })
    }
}
