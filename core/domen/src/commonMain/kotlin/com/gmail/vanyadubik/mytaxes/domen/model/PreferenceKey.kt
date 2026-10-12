package com.gmail.vanyadubik.mytaxes.domen.model

import kotlin.reflect.KType
import kotlin.reflect.typeOf

sealed class PreferenceKey<T>(
    val name: String,
    val type: KType
) {
    data object Theme : PreferenceKey<Int>("THEME_PREFERENCE", typeOf<Int>())
    data object Language : PreferenceKey<String>("LANGUAGE_PREFERENCE", typeOf<String>())
    data object FopGroupKey : PreferenceKey<String>("FOP_GROUP_PREFERENCE", typeOf<String>())
    data object IdToken : PreferenceKey<String>("ID_TOKEN_PREFERENCE", typeOf<String>())
    data object RefreshToken : PreferenceKey<String>("REFRESH_TOKEN_PREFERENCE", typeOf<String>())
    data object TokenExpiresAt : PreferenceKey<Long>("TOKEN_EXPIRES_AT_PREFERENCE", typeOf<Long>())
    data object UserId : PreferenceKey<String>("USER_ID_PREFERENCE", typeOf<String>())
    data object UserEmail : PreferenceKey<String>("USER_EMAIL_PREFERENCE", typeOf<String>())

    companion object {
        fun getAllKeys() = listOf(Theme, Language, FopGroupKey, IdToken, RefreshToken, TokenExpiresAt, UserId, UserEmail)

        fun getSavedKeys() = listOf(Theme, Language, FopGroupKey)
    }
}
