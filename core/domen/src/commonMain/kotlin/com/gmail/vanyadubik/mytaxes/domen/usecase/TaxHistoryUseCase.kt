package com.gmail.vanyadubik.myassistant.domen.usecase

import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.model.TaxCalculation
import com.gmail.vanyadubik.myassistant.domen.repository.TaxHistoryRepository

/** Stores the calculation history of the signed in user in Firebase Cloud Firestore. */
class TaxHistoryUseCase(
    private val authUseCase: AuthUseCase,
    private val taxHistoryRepository: TaxHistoryRepository,
) {
    suspend fun save(calculation: TaxCalculation): ResultState<TaxCalculation> =
        withSession { userId, token -> taxHistoryRepository.save(userId, token, calculation) }

    suspend fun getAll(): ResultState<List<TaxCalculation>> =
        withSession { userId, token -> taxHistoryRepository.getAll(userId, token) }

    suspend fun delete(id: String): ResultState<Unit> =
        withSession { userId, token -> taxHistoryRepository.delete(userId, token, id) }

    private suspend fun <T> withSession(block: suspend (String, String) -> ResultState<T>): ResultState<T> =
        when (val session = authUseCase.requireSession()) {
            is ResultState.Success -> session.data!!.let { (userId, token) -> block(userId, token) }
            is ResultState.Error -> session
            else -> ResultState.Error(message = "You are not signed in")
        }
}
