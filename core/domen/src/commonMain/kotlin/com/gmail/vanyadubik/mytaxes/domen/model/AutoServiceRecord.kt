package com.gmail.vanyadubik.myassistant.domen.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** A car maintenance service record. */
@Serializable
data class AutoServiceRecord(
    val id: String = "",
    val carId: String = "",
    val date: LocalDate,
    val odometer: Int,
    val description: String,
    val photos: List<String> = emptyList(),
    val createdAt: Long = 0L,
)
