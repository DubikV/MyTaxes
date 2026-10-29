package com.gmail.vanyadubik.myassistant.network.repository

import com.gmail.vanyadubik.myassistant.domen.model.AutoServiceRecord
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.repository.AutoServiceRepository
import com.gmail.vanyadubik.myassistant.network.ApiException
import com.gmail.vanyadubik.myassistant.network.FirebaseConfig
import com.gmail.vanyadubik.myassistant.network.apiCall
import com.gmail.vanyadubik.myassistant.network.bodyOrThrow
import com.gmail.vanyadubik.myassistant.network.string
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Cloud Firestore through its REST API: users/{userId}/auto_maintenance/{id}. */
@OptIn(ExperimentalTime::class)
class AutoServiceRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val config: FirebaseConfig,
) : AutoServiceRepository {

    private fun collectionUrl(userId: String) =
        "https://firestore.googleapis.com/v1/projects/${config.projectId}/databases/(default)/documents/users/$userId/auto_maintenance"

    private fun errorMapper(message: String?) = ApiException(message = message ?: "Firestore error")

    override suspend fun getRecords(
        userId: String,
        idToken: String,
        carId: String?
    ): ResultState<List<AutoServiceRecord>> = apiCall {
        val result = mutableListOf<AutoServiceRecord>()
        var pageToken: String? = null
        do {
            val text = httpClient.get(collectionUrl(userId)) {
                bearerAuth(idToken)
                parameter("pageSize", PAGE_SIZE)
                parameter("orderBy", "createdAt desc")
                pageToken?.let { parameter("pageToken", it) }
            }.bodyOrThrow(json, ::errorMapper)
            val root = json.parseToJsonElement(text).jsonObject
            root["documents"]?.jsonArray?.mapNotNullTo(result) { it.jsonObject.toRecord() }
            pageToken = root.string("nextPageToken")
        } while (pageToken != null)
        val filtered = if (carId != null) result.filter { it.carId == carId } else result
        filtered.sortedWith(compareByDescending<AutoServiceRecord> { it.date }.thenByDescending { it.createdAt })
    }

    override suspend fun saveRecord(
        userId: String,
        idToken: String,
        record: AutoServiceRecord
    ): ResultState<AutoServiceRecord> = apiCall {
        val createdAt = if (record.createdAt > 0) record.createdAt else Clock.System.now().toEpochMilliseconds()
        val text = if (record.id.isNotBlank()) {
            httpClient.patch("${collectionUrl(userId)}/${record.id}") {
                bearerAuth(idToken)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(JsonObject.serializer(), record.toDocument(createdAt)))
            }.bodyOrThrow(json, ::errorMapper)
        } else {
            httpClient.post(collectionUrl(userId)) {
                bearerAuth(idToken)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(JsonObject.serializer(), record.toDocument(createdAt)))
            }.bodyOrThrow(json, ::errorMapper)
        }
        json.parseToJsonElement(text).jsonObject.toRecord() ?: record.copy(id = record.id, createdAt = createdAt)
    }

    override suspend fun deleteRecord(
        userId: String,
        idToken: String,
        recordId: String
    ): ResultState<Unit> = apiCall {
        httpClient.delete("${collectionUrl(userId)}/$recordId") {
            bearerAuth(idToken)
        }.bodyOrThrow(json, ::errorMapper)
        Unit
    }

    private fun AutoServiceRecord.toDocument(createdAt: Long) = buildJsonObject {
        put("fields", buildJsonObject {
            put("carId", buildJsonObject { put("stringValue", carId) })
            put("date", buildJsonObject { put("stringValue", date.toString()) })
            put("odometer", buildJsonObject { put("integerValue", odometer.toString()) })
            put("description", buildJsonObject { put("stringValue", description) })
            put("photos", buildJsonObject {
                put("arrayValue", buildJsonObject {
                    put("values", JsonArray(photos.map { photo -> buildJsonObject { put("stringValue", photo) } }))
                })
            })
            put("createdAt", buildJsonObject { put("integerValue", createdAt.toString()) })
        })
    }

    private fun JsonObject.toRecord(): AutoServiceRecord? {
        val fields = this["fields"]?.jsonObject ?: return null
        return runCatching {
            val photosValues = fields["photos"]?.jsonObject?.get("arrayValue")?.jsonObject?.get("values")?.jsonArray
            val photoList = photosValues?.mapNotNull { it.jsonObject["stringValue"]?.jsonPrimitive?.content }.orEmpty()
            AutoServiceRecord(
                id = string("name")?.substringAfterLast('/').orEmpty(),
                carId = fields["carId"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty(),
                date = LocalDate.parse(fields["date"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content),
                odometer = fields["odometer"]!!.jsonObject.let { (it["integerValue"] ?: it["doubleValue"])!! }
                    .jsonPrimitive.intOrNull ?: 0,
                description = fields["description"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty(),
                photos = photoList,
                createdAt = fields["createdAt"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.longOrNull ?: 0L,
            )
        }.getOrNull()
    }

    private companion object {
        const val PAGE_SIZE = 100
    }
}
