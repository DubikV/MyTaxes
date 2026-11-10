package com.gmail.vanyadubik.myassistant.domen.usecase

import com.gmail.vanyadubik.myassistant.domen.model.PreferenceKey
import com.gmail.vanyadubik.myassistant.domen.model.PushMessage
import com.gmail.vanyadubik.myassistant.domen.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/** Push messages received by this device, cached in the data store (newest first). */
class PushMessagesUseCase(
    private val dataStoreRepository: DataStoreRepository,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()

    val messages: Flow<List<PushMessage>> =
        dataStoreRepository.getPreference(PreferenceKey.PushMessagesKey)
            .map { raw ->
                if (raw.isNullOrBlank()) emptyList()
                else runCatching { json.decodeFromString<List<PushMessage>>(raw) }.getOrDefault(emptyList())
            }
            .distinctUntilChanged()

    val unreadCount: Flow<Int> = messages.map { list -> list.count { !it.isRead } }.distinctUntilChanged()

    suspend fun add(title: String?, body: String?, messageId: String? = null) {
        if (title.isNullOrBlank() && body.isNullOrBlank()) return
        val receivedAt = Clock.System.now().toEpochMilliseconds()
        val id = messageId?.takeIf { it.isNotBlank() } ?: receivedAt.toString()
        update { list ->
            if (list.any { it.id == id }) list
            else (listOf(PushMessage(id, title.orEmpty(), body.orEmpty(), receivedAt)) + list).take(MAX_MESSAGES)
        }
    }

    suspend fun markAllAsRead() = update { list -> list.map { it.copy(isRead = true) } }

    suspend fun markAsRead(id: String) =
        update { list -> list.map { if (it.id == id) it.copy(isRead = true) else it } }

    suspend fun delete(id: String) = update { list -> list.filterNot { it.id == id } }

    suspend fun clear() = update { emptyList() }

    private suspend fun update(transform: (List<PushMessage>) -> List<PushMessage>) = mutex.withLock {
        val current = messages.first()
        val updated = transform(current)
        if (updated != current) {
            dataStoreRepository.setPreference(PreferenceKey.PushMessagesKey, json.encodeToString(updated))
        }
    }

    private companion object {
        const val MAX_MESSAGES = 100
    }
}
