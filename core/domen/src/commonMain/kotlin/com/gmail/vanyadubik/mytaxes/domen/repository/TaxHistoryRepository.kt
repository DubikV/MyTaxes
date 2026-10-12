package com.gmail.vanyadubik.mytaxes.domen.repository

import com.gmail.vanyadubik.mytaxes.domen.model.ResultState
import com.gmail.vanyadubik.mytaxes.domen.model.TaxCalculation

interface TaxHistoryRepository {
    suspend fun save(userId: String, idToken: String, calculation: TaxCalculation): ResultState<TaxCalculation>
    suspend fun getAll(userId: String, idToken: String): ResultState<List<TaxCalculation>>
    suspend fun delete(userId: String, idToken: String, calculationId: String): ResultState<Unit>
}
