package com.gmail.vanyadubik.myassistant.featurelogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.myassistant.commonui.ui.theme.ThemeAnimationHandler
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.model.getMessage
import com.gmail.vanyadubik.myassistant.domen.usecase.AccountUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.AuthUseCase
import com.gmail.vanyadubik.myassistant.featurelogin.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authUseCase: AuthUseCase,
    private val accountUseCase: AccountUseCase,
    val themeAnimationHandler: ThemeAnimationHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private var lastAction: (() -> Unit)? = null

    fun toggleMode() = _state.update { it.copy(isRegister = !it.isRegister, actionError = null) }

    fun submit(email: String, password: String, nickname: String = "") {
        if (_state.value.isLoading) return
        val emailEmpty = email.isBlank()
        val passwordEmpty = password.isEmpty()
        val nicknameEmpty = _state.value.isRegister && nickname.isBlank()
        if (emailEmpty || passwordEmpty || nicknameEmpty) {
            _state.update {
                it.copy(
                    emailError = emailEmpty,
                    passwordError = passwordEmpty,
                    nicknameError = nicknameEmpty,
                )
            }
            return
        }
        lastAction = { submit(email, password, nickname) }
        _state.update { it.copy(isLoading = true, actionError = null) }
        viewModelScope.launch {
            val result = if (_state.value.isRegister) {
                val signUpResult = authUseCase.signUp(email, password)
                if (signUpResult is ResultState.Success && nickname.isNotBlank()) {
                    accountUseCase.saveNickname(nickname)
                }
                signUpResult
            } else {
                authUseCase.signIn(email, password)
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    actionError = if (result is ResultState.Error) result.getMessage() ?: "Error" else null,
                )
            }
        }
    }

    fun onEmailChanged() = _state.update { it.copy(emailError = false, actionError = null) }

    fun onPasswordChanged() = _state.update { it.copy(passwordError = false, actionError = null) }

    fun onNicknameChanged() = _state.update { it.copy(nicknameError = false, actionError = null) }

    fun retryLastAction() {
        dismissActionError()
        lastAction?.invoke()
    }

    fun dismissActionError() = _state.update { it.copy(actionError = null) }

    fun dismissError() = _state.update { it.copy(errorMessage = null) }
}
