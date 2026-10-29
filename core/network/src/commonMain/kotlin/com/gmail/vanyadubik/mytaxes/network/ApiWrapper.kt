package com.gmail.vanyadubik.myassistant.network

import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ApiException(val title: String? = null, message: String) : Exception(message)

suspend fun <T> apiCall(block: suspend () -> T): ResultState<T> = withContext(Dispatchers.IO) {
    try {
        ResultState.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: ApiException) {
        ResultState.Error(title = e.title, message = e.message, throwable = e)
    } catch (e: HttpRequestTimeoutException) {
        ResultState.Error(message = "Request timed out. Please try again.", throwable = e)
    } catch (e: IOException) {
        ResultState.Error(message = "Network connection failed. Check your internet.", throwable = e)
    } catch (e: Exception) {
        ResultState.Error(message = e.message ?: "Unknown error occurred", throwable = e)
    }
}

/** Returns the body of a successful response or throws [ApiException] with the server message. */
suspend fun HttpResponse.bodyOrThrow(json: Json, errorMapper: (String?) -> ApiException): String {
    val text = bodyAsText()
    if (status.isSuccess()) return text
    val serverMessage = runCatching {
        json.parseToJsonElement(text).jsonObject["error"]?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
    }.getOrNull()
    throw errorMapper(serverMessage ?: "HTTP ${status.value}")
}

fun JsonObject.string(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull
