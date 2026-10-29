package com.gmail.vanyadubik.myassistant.network.repository

import com.gmail.vanyadubik.myassistant.domen.model.Expense
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.repository.ExpenseRepository
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
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Cloud Firestore through its REST API: users/{userId}/expenses/{id}. */
@OptIn(ExperimentalTime::class)
class ExpenseRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val config: FirebaseConfig,
) : ExpenseRepository {

    private fun collectionUrl(userId: String) =
        "https://firestore.googleapis.com/v1/projects/${config.projectId}/databases/(default)/documents/users/$userId/expenses"

    private fun errorMapper(message: String?) = ApiException(message = message ?: "Firestore error")

    override suspend fun save(userId: String, idToken: String, expense: Expense): ResultState<Expense> = apiCall {
        val createdAt = Clock.System.now().toEpochMilliseconds()
        val text = httpClient.post(collectionUrl(userId)) {
            bearerAuth(idToken)
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(JsonObject.serializer(), expense.toDocument(createdAt)))
        }.bodyOrThrow(json, ::errorMapper)
        json.parseToJsonElement(text).jsonObject.toExpense() ?: expense.copy(createdAt = createdAt)
    }

    override suspend fun getAll(userId: String, idToken: String): ResultState<List<Expense>> = apiCall {
        val result = mutableListOf<Expense>()
        var pageToken: String? = null
        do {
            val text = httpClient.get(collectionUrl(userId)) {
                bearerAuth(idToken)
                parameter("pageSize", PAGE_SIZE)
                parameter("orderBy", "createdAt desc")
                pageToken?.let { parameter("pageToken", it) }
            }.bodyOrThrow(json, ::errorMapper)
            val root = json.parseToJsonElement(text).jsonObject
            root["documents"]?.jsonArray?.mapNotNullTo(result) { it.jsonObject.toExpense() }
            pageToken = root.string("nextPageToken")
        } while (pageToken != null)
        result
    }

    override suspend fun delete(userId: String, idToken: String, expenseId: String): ResultState<Unit> = apiCall {
        httpClient.delete("${collectionUrl(userId)}/$expenseId") {
            bearerAuth(idToken)
        }.bodyOrThrow(json, ::errorMapper)
        Unit
    }

    private fun Expense.toDocument(createdAt: Long) = buildJsonObject {
        put("fields", buildJsonObject {
            put("date", buildJsonObject { put("stringValue", date.toString()) })
            put("amountUah", buildJsonObject { put("doubleValue", amountUah) })
            put("note", buildJsonObject { put("stringValue", note) })
            put("createdAt", buildJsonObject { put("integerValue", createdAt.toString()) })
        })
    }

    private fun JsonObject.toExpense(): Expense? {
        val fields = this["fields"]?.jsonObject ?: return null
        return runCatching {
            Expense(
                id = string("name")?.substringAfterLast('/'),
                date = LocalDate.parse(fields["date"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content),
                amountUah = fields["amountUah"]!!.jsonObject.let { (it["doubleValue"] ?: it["integerValue"])!! }
                    .jsonPrimitive.doubleOrNull!!,
                note = fields["note"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content.orEmpty(),
                createdAt = fields["createdAt"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.longOrNull ?: 0L,
            )
        }.getOrNull()
    }

    private companion object {
        const val PAGE_SIZE = 100
    }
}
