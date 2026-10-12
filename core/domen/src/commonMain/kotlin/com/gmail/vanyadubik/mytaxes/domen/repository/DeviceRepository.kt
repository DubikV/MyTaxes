package com.gmail.vanyadubik.mytaxes.domen.repository

import com.gmail.vanyadubik.mytaxes.domen.model.PushDevice
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState

interface DeviceRepository {
    suspend fun register(userId: String, idToken: String, device: PushDevice): ResultState<Unit>
    suspend fun unregister(userId: String, idToken: String, token: String): ResultState<Unit>
}
