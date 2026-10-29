package com.gmail.vanyadubik.myassistant.domen.model

import kotlinx.serialization.Serializable

@Serializable
data class Car(
    val id: String = "",
    val name: String = "",
    val number: String = "",
)
