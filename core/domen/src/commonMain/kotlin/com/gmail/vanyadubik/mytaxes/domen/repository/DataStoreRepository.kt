package com.gmail.vanyadubik.mytaxes.domen.repository


import com.gmail.vanyadubik.mytaxes.domen.model.PreferenceKey
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    suspend fun <T> setPreference(key: PreferenceKey<T>, value: T)
    fun <T> getPreference(key: PreferenceKey<T>): Flow<T?>
    suspend fun <T> clearPreference(key: PreferenceKey<T>)
    suspend fun clearAllPreference()
    suspend fun logout()
}