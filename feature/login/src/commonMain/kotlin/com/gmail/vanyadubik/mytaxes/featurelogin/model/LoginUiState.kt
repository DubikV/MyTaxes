package com.gmail.vanyadubik.myassistant.featurelogin.model

data class LoginUiState(
    val isRegister: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: Boolean = false,
    val passwordError: Boolean = false,
    val nicknameError: Boolean = false,
    val actionError: String? = null,
    val errorMessage: String? = null,
)
