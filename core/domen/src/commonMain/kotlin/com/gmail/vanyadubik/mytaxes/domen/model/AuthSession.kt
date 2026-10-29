package com.gmail.vanyadubik.myassistant.domen.model

data class AuthSession(
    val userId: String,
    val email: String,
    val idToken: String,
    val refreshToken: String,
    val expiresAtMillis: Long,
)
