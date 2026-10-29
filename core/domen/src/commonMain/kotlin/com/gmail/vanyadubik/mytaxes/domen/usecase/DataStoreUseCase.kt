package com.gmail.vanyadubik.myassistant.domen.usecase

import com.gmail.vanyadubik.myassistant.domen.model.AppLanguage
import com.gmail.vanyadubik.myassistant.domen.model.AppTheme
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.PreferenceKey
import com.gmail.vanyadubik.myassistant.domen.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DataStoreUseCase(
    private val dataStoreRepository: DataStoreRepository
) {
    suspend fun <T> setPreference(key: PreferenceKey<T>, value: T) =
        dataStoreRepository.setPreference(key = key, value = value)

    suspend fun <T> clearPreference(key: PreferenceKey<T>) =
        dataStoreRepository.clearPreference(key = key)

    fun <T> getPreference(key: PreferenceKey<T>): Flow<T?> =
        dataStoreRepository
            .getPreference(key)
            .distinctUntilChanged()

    private val themes = listOf(AppTheme.System, AppTheme.Dark, AppTheme.Light)

    val currentTheme: Flow<AppTheme> =
        dataStoreRepository.getPreference(PreferenceKey.Theme)
            .distinctUntilChanged()
            .map { themeId -> themes.firstOrNull { it.id == themeId } ?: themes.first() }

    suspend fun toggleTheme() {
        val currentId = dataStoreRepository.getPreference(PreferenceKey.Theme).firstOrNull()
        val nextThemeId = when (currentId) {
            AppTheme.System.id -> AppTheme.Dark.id
            AppTheme.Dark.id -> AppTheme.Light.id
            AppTheme.Light.id -> AppTheme.System.id
            else -> AppTheme.Dark.id
        }
        setPreference(PreferenceKey.Theme, nextThemeId)
    }

    val language: Flow<AppLanguage> =
        dataStoreRepository.getPreference(PreferenceKey.Language)
            .map { AppLanguage.fromId(it) }
            .distinctUntilChanged()

    suspend fun setLanguage(language: AppLanguage) = setPreference(PreferenceKey.Language, language.id)

    val fopGroup: Flow<FopGroup> =
        dataStoreRepository.getPreference(PreferenceKey.FopGroupKey)
            .map { FopGroup.fromId(it) }
            .distinctUntilChanged()

    suspend fun setFopGroup(group: FopGroup) = setPreference(PreferenceKey.FopGroupKey, group.id)

    suspend fun logout() {
        dataStoreRepository.logout()
    }
}
