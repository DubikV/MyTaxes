package com.gmail.vanyadubik.myassistant.domen.repository

import com.gmail.vanyadubik.myassistant.domen.model.PushDevice
import com.gmail.vanyadubik.myassistant.domen.model.ResultState

interface DeviceRepository {
    suspend fun register(userId: String, idToken: String, device: PushDevice): ResultState<Unit>
    suspend fun updateUnpaidTax(userId: String, idToken: String, token: String, amount: Double): ResultState<Unit>
    suspend fun unregister(userId: String, idToken: String, token: String): ResultState<Unit>
}
