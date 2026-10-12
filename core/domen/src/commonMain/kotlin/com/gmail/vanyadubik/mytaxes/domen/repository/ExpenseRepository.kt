package com.gmail.vanyadubik.mytaxes.domen.repository

import com.gmail.vanyadubik.mytaxes.domen.model.Expense
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState

interface ExpenseRepository {
    suspend fun save(userId: String, idToken: String, expense: Expense): ResultState<Expense>
    suspend fun getAll(userId: String, idToken: String): ResultState<List<Expense>>
    suspend fun delete(userId: String, idToken: String, expenseId: String): ResultState<Unit>
}
