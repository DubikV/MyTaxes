package com.gmail.vanyadubik.mytaxes.domen.model

/** A device that receives push notifications; the fields let the backend pick the reminders and their language. */
data class PushDevice(
    val token: String,
    val platform: String,
    val language: String,
    val group: FopGroup,
)
