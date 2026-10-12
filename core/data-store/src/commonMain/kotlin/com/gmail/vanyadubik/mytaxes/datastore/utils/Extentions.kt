package com.gmail.vanyadubik.mytaxes.datastore.utils

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.gmail.vanyadubik.mytaxes.domen.model.PreferenceKey
import kotlin.reflect.typeOf

fun PreferenceKey<*>.toStoreKey() = when (type) {
    typeOf<String>() -> stringPreferencesKey(name)
    typeOf<Int>() -> intPreferencesKey(name)
    typeOf<Long>() -> longPreferencesKey(name)
    typeOf<Boolean>() -> booleanPreferencesKey(name)
    typeOf<Float>() -> floatPreferencesKey(name)
    typeOf<Double>() -> doublePreferencesKey(name)
    typeOf<Set<String>>() -> stringSetPreferencesKey(name)
    else -> null
}