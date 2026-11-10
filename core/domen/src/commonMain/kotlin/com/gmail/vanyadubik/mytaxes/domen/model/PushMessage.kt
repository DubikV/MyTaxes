package com.gmail.vanyadubik.myassistant.domen.model

import kotlinx.serialization.Serializable

@Serializable
data class PushMessage(
    val id: String,
    val title: String = "",
    val body: String = "",
    val receivedAt: Long,
    val isRead: Boolean = false,
)
