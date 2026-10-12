package com.gmail.vanyadubik.mytaxes.domen.usecase

import com.gmail.vanyadubik.mytaxes.domen.model.Expense
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState
import com.gmail.vanyadubik.mytaxes.domen.repository.ExpenseRepository

/** Stores the expenses of the signed in user in Firebase Cloud Firestore. */
class ExpenseUseCase(
    private val authUseCase: AuthUseCase,
    private val expenseRepository: ExpenseRepository,
) {
    suspend fun save(expense: Expense): ResultState<Expense> =
        withSession { userId, token -> expenseRepository.save(userId, token, expense) }

    suspend fun getAll(): ResultState<List<Expense>> =
        withSession { userId, token -> expenseRepository.getAll(userId, token) }

    suspend fun delete(id: String): ResultState<Unit> =
        withSession { userId, token -> expenseRepository.delete(userId, token, id) }

    private suspend fun <T> withSession(block: suspend (String, String) -> ResultState<T>): ResultState<T> =
        when (val session = authUseCase.requireSession()) {
            is ResultState.Success -> session.data!!.let { (userId, token) -> block(userId, token) }
            is ResultState.Error -> session
            else -> ResultState.Error(message = "You are not signed in")
        }
}
