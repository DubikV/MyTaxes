package com.gmail.vanyadubik.myassistant.datastore.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gmail.vanyadubik.myassistant.datastore.utils.toStoreKey
import com.gmail.vanyadubik.myassistant.domen.model.PreferenceKey
import com.gmail.vanyadubik.myassistant.domen.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.io.IOException
import kotlin.reflect.typeOf

class DataStoreRepositoryImpl(private val dataStore: DataStore<Preferences>) : DataStoreRepository {

    private fun <T> getPrefKey(domainKey: PreferenceKey<T>): Preferences.Key<T> {
        return when(domainKey.type) {
            typeOf<Int>() -> intPreferencesKey(domainKey.name)
            typeOf<Long>() -> longPreferencesKey(domainKey.name)
            typeOf<Boolean>() -> booleanPreferencesKey(domainKey.name)
            typeOf<Double>() -> doublePreferencesKey(domainKey.name)
            typeOf<Float>() -> floatPreferencesKey(domainKey.name)
            else -> stringPreferencesKey(domainKey.name)
        } as Preferences.Key<T>
    }

    override suspend fun <T> setPreference(key: PreferenceKey<T>, value: T) {
        val prefKey = getPrefKey(key)
        dataStore.edit { prefs ->
            prefs[prefKey] = value
        }
    }

    override fun <T> getPreference(key: PreferenceKey<T>): Flow<T?> {
        val prefKey = getPrefKey(key)
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { prefs -> prefs[prefKey] }
    }

    override suspend fun <T> clearPreference(key: PreferenceKey<T>) {
        val prefKey = getPrefKey(key)
        dataStore.edit { prefs ->
            prefs.remove(prefKey)
        }
    }

    override suspend fun clearAllPreference() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    override suspend fun logout() {
        dataStore.edit { prefs ->
            val saved = PreferenceKey.getSavedKeys()
            PreferenceKey.getAllKeys().forEach { key ->
                if (key !in saved) {
                    key.toStoreKey()?.let {
                        prefs.remove(it)
                    }
                }
            }
        }
    }
}