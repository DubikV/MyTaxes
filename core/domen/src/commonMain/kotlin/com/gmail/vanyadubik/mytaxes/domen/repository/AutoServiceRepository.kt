package com.gmail.vanyadubik.myassistant.domen.repository

import com.gmail.vanyadubik.myassistant.domen.model.AutoServiceRecord
import com.gmail.vanyadubik.myassistant.domen.model.ResultState

interface AutoServiceRepository {
    suspend fun getRecords(userId: String, idToken: String, carId: String? = null): ResultState<List<AutoServiceRecord>>
    suspend fun saveRecord(userId: String, idToken: String, record: AutoServiceRecord): ResultState<AutoServiceRecord>
    suspend fun deleteRecord(userId: String, idToken: String, recordId: String): ResultState<Unit>
}
