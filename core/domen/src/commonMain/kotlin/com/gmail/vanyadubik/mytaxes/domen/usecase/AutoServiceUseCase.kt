package com.gmail.vanyadubik.myassistant.domen.usecase

import com.gmail.vanyadubik.myassistant.domen.model.AutoServiceRecord
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.repository.AutoServiceRepository

class AutoServiceUseCase(
    private val authUseCase: AuthUseCase,
    private val autoServiceRepository: AutoServiceRepository,
) {
    suspend fun getRecords(carId: String? = null): ResultState<List<AutoServiceRecord>> =
        withSession { userId, token -> autoServiceRepository.getRecords(userId, token, carId) }

    suspend fun saveRecord(record: AutoServiceRecord): ResultState<AutoServiceRecord> =
        withSession { userId, token -> autoServiceRepository.saveRecord(userId, token, record) }

    suspend fun deleteRecord(recordId: String): ResultState<Unit> =
        withSession { userId, token -> autoServiceRepository.deleteRecord(userId, token, recordId) }

    private suspend fun <T> withSession(block: suspend (String, String) -> ResultState<T>): ResultState<T> =
        when (val session = authUseCase.requireSession()) {
            is ResultState.Success -> session.data!!.let { (userId, token) -> block(userId, token) }
            is ResultState.Error -> ResultState.Error(title = session.title, message = session.message)
            else -> ResultState.Error(message = "You are not signed in")
        }
}
