package com.gmail.vanyadubik.mytaxes.domen.repository

import com.gmail.vanyadubik.mytaxes.domen.model.AuthSession
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState

interface AuthRepository {
    suspend fun signIn(email: String, password: String): ResultState<AuthSession>
    suspend fun signUp(email: String, password: String): ResultState<AuthSession>
    suspend fun refresh(refreshToken: String): ResultState<AuthSession>
    suspend fun deleteAccount(idToken: String): ResultState<Unit>
}
