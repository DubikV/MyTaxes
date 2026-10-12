package com.gmail.vanyadubik.mytaxes.network.repository

import com.gmail.vanyadubik.mytaxes.domen.model.Currency
import com.gmail.vanyadubik.mytaxes.domen.model.FopGroup
import com.gmail.vanyadubik.mytaxes.domen.model.IncomeItem
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState
import com.gmail.vanyadubik.mytaxes.domen.model.TaxCalculation
import com.gmail.vanyadubik.mytaxes.domen.repository.TaxHistoryRepository
import com.gmail.vanyadubik.mytaxes.network.ApiException
import com.gmail.vanyadubik.mytaxes.network.FirebaseConfig
import com.gmail.vanyadubik.mytaxes.network.apiCall
import com.gmail.vanyadubik.mytaxes.network.bodyOrThrow
import com.gmail.vanyadubik.mytaxes.network.string
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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Cloud Firestore through its REST API: users/{userId}/calculations/{id}.
 * Access is enforced by Firestore security rules using the user's Firebase ID token.
 */
@OptIn(ExperimentalTime::class)
class TaxHistoryRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val config: FirebaseConfig,
) : TaxHistoryRepository {

    private fun collectionUrl(userId: String) =
        "https://firestore.googleapis.com/v1/projects/${config.projectId}/databases/(default)/documents/users/$userId/calculations"

    private fun errorMapper(message: String?) = ApiException(message = message ?: "Firestore error")

    override suspend fun save(
        userId: String,
        idToken: String,
        calculation: TaxCalculation
    ): ResultState<TaxCalculation> = apiCall {
        val createdAt = Clock.System.now().toEpochMilliseconds()
        val text = httpClient.post(collectionUrl(userId)) {
            bearerAuth(idToken)
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(JsonObject.serializer(), calculation.toDocument(createdAt)))
        }.bodyOrThrow(json, ::errorMapper)
        json.parseToJsonElement(text).jsonObject.toCalculation()
            ?: calculation.copy(createdAt = createdAt)
    }

    override suspend fun getAll(userId: String, idToken: String): ResultState<List<TaxCalculation>> = apiCall {
        val result = mutableListOf<TaxCalculation>()
        var pageToken: String? = null
        do {
            val text = httpClient.get(collectionUrl(userId)) {
                bearerAuth(idToken)
                parameter("pageSize", PAGE_SIZE)
                parameter("orderBy", "createdAt desc")
                pageToken?.let { parameter("pageToken", it) }
            }.bodyOrThrow(json, ::errorMapper)
            val root = json.parseToJsonElement(text).jsonObject
            root["documents"]?.jsonArray?.mapNotNullTo(result) { it.jsonObject.toCalculation() }
            pageToken = root.string("nextPageToken")
        } while (pageToken != null)
        result
    }

    override suspend fun delete(userId: String, idToken: String, calculationId: String): ResultState<Unit> = apiCall {
        httpClient.delete("${collectionUrl(userId)}/$calculationId") {
            bearerAuth(idToken)
        }.bodyOrThrow(json, ::errorMapper)
        Unit
    }

    private fun TaxCalculation.toDocument(createdAt: Long) = buildJsonObject {
        put("fields", buildJsonObject {
            put("incomes", buildJsonObject {
                put("arrayValue", buildJsonObject {
                    put("values", JsonArray(incomes.map { it.toValue() }))
                })
            })
            put("amountUah", doubleValue(amountUah))
            put("singleTax", doubleValue(singleTax))
            put("militaryLevy", doubleValue(militaryLevy))
            put("esv", doubleValue(esv))
            put("totalTax", doubleValue(totalTax))
            put("group", stringValue(group.id))
            put("createdAt", buildJsonObject { put("integerValue", createdAt.toString()) })
        })
    }

    private fun IncomeItem.toValue() = buildJsonObject {
        put("mapValue", buildJsonObject {
            put("fields", buildJsonObject {
                put("date", stringValue(date.toString()))
                put("amount", doubleValue(amount))
                put("currency", stringValue(currency.code))
                put("rate", doubleValue(rate))
                put("amountUah", doubleValue(amountUah))
            })
        })
    }

    private fun stringValue(value: String) = buildJsonObject { put("stringValue", value) }
    private fun doubleValue(value: Double) = buildJsonObject { put("doubleValue", value) }

    private fun JsonObject.str(key: String) = this[key]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content

    private fun JsonObject.num(key: String): Double? = this[key]?.jsonObject?.let { value ->
        (value["doubleValue"] ?: value["integerValue"])?.jsonPrimitive?.doubleOrNull
    }

    private fun JsonObject.toIncome() = IncomeItem(
        date = LocalDate.parse(str(if (containsKey("date")) "date" else "incomeDate")!!),
        amount = num("amount")!!,
        currency = Currency.fromCode(str("currency") ?: ""),
        rate = num("rate")!!,
        amountUah = num("amountUah")!!,
    )

    private fun JsonObject.toCalculation(): TaxCalculation? {
        val fields = this["fields"]?.jsonObject ?: return null
        return runCatching {
            val values = fields["incomes"]?.jsonObject?.get("arrayValue")?.jsonObject?.get("values")?.jsonArray
            // Calculations saved before several incomes were supported hold a single income in the document itself
            val incomes = values?.map { it.jsonObject["mapValue"]!!.jsonObject["fields"]!!.jsonObject.toIncome() }
                ?: listOf(fields.toIncome())
            TaxCalculation(
                id = string("name")?.substringAfterLast('/'),
                incomes = incomes,
                amountUah = fields.num("amountUah")!!,
                singleTax = fields.num("singleTax")!!,
                militaryLevy = fields.num("militaryLevy")!!,
                esv = fields.num("esv") ?: 0.0,
                totalTax = fields.num("totalTax")!!,
                group = FopGroup.fromId(fields.str("group")),
                createdAt = fields["createdAt"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.longOrNull ?: 0L,
            )
        }.getOrNull()
    }

    private companion object {
        const val PAGE_SIZE = 100
    }
}
