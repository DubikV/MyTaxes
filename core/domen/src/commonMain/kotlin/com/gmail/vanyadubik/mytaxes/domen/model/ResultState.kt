package com.gmail.vanyadubik.myassistant.domen.model

sealed class ResultState<out T> {
    data class Success<out T>(val data: T? = null) : ResultState<T>()
    data class Error(
        val title: String? = null,
        val message: String? = null,
        val throwable: Throwable? = null
    ) : ResultState<Nothing>()
    data object Loading : ResultState<Nothing>()
    data object Inactive : ResultState<Nothing>()

    fun isError() = this is Error
    fun isLoading() = this is Loading
    fun isSuccess() = this is Success
    fun isInactive() = this is Inactive
}

fun <T> ResultState<T>.getData(): T? =
    if (this is ResultState.Success) this.data else null

fun <T> ResultState<T>.getMessage(): String? =
    if (this is ResultState.Error) this.message else null

fun <T> ResultState<T>.getTitle(): String? =
    if (this is ResultState.Error) this.title else null