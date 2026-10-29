package com.gmail.vanyadubik.myassistant.domen.repository

import com.gmail.vanyadubik.myassistant.domen.model.AccountProfile
import com.gmail.vanyadubik.myassistant.domen.model.ResultState

interface ProfileRepository {
    /** The settings saved in the account; empty when the user has not saved any. */
    suspend fun getProfile(userId: String, idToken: String): ResultState<AccountProfile>

    /** Replaces the saved settings; null settings are removed. */
    suspend fun saveProfile(userId: String, idToken: String, profile: AccountProfile): ResultState<Unit>
    suspend fun delete(userId: String, idToken: String): ResultState<Unit>
}
