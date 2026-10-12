package com.gmail.vanyadubik.mytaxes.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NbuRateDto(
    @SerialName("cc") val currencyCode: String,
    @SerialName("rate") val rate: Double,
    @SerialName("exchangedate") val exchangeDate: String? = null,
)
