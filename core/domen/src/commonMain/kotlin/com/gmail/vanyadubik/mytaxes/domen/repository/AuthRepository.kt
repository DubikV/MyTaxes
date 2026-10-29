package com.gmail.vanyadubik.myassistant.domen.repository

import com.gmail.vanyadubik.myassistant.domen.model.AuthSession
import com.gmail.vanyadubik.myassistant.domen.model.ResultState

interface AuthRepository {
    suspend fun signIn(email: String, password: String): ResultState<AuthSession>
    suspend fun signUp(email: String, password: String): ResultState<AuthSession>
    suspend fun refresh(refreshToken: String): ResultState<AuthSession>
    suspend fun deleteAccount(idToken: String): ResultState<Unit>
}
