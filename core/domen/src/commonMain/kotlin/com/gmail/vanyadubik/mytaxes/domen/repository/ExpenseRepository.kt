package com.gmail.vanyadubik.myassistant.domen.repository

import com.gmail.vanyadubik.myassistant.domen.model.Expense
import com.gmail.vanyadubik.myassistant.domen.model.ResultState

interface ExpenseRepository {
    suspend fun save(userId: String, idToken: String, expense: Expense): ResultState<Expense>
    suspend fun getAll(userId: String, idToken: String): ResultState<List<Expense>>
    suspend fun delete(userId: String, idToken: String, expenseId: String): ResultState<Unit>
}
