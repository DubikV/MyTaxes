package com.gmail.vanyadubik.mytaxes.featurelogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.mytaxes.commonui.ui.theme.ThemeAnimationHandler
import com.gmail.vanyadubik.mytaxes.domen.model.ResultState
import com.gmail.vanyadubik.mytaxes.domen.model.getMessage
import com.gmail.vanyadubik.mytaxes.domen.usecase.AuthUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isRegister: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: Boolean = false,
    val passwordError: Boolean = false,
    val errorMessage: String? = null,
)

class LoginViewModel(
    private val authUseCase: AuthUseCase,
    val themeAnimationHandler: ThemeAnimationHandler
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun toggleMode() = _state.update { it.copy(isRegister = !it.isRegister, errorMessage = null) }

    fun submit(email: String, password: String) {
        if (_state.value.isLoading) return
        val emailEmpty = email.isBlank()
        val passwordEmpty = password.isEmpty()
        if (emailEmpty || passwordEmpty) {
            _state.update { it.copy(emailError = emailEmpty, passwordError = passwordEmpty) }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (_state.value.isRegister) {
                authUseCase.signUp(email, password)
            } else {
                authUseCase.signIn(email, password)
            }
            // On success the session is stored and the navigation reacts to the login state
            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage = if (result is ResultState.Error) result.getMessage() ?: "Error" else null
                )
            }
        }
    }

    fun onEmailChanged() = _state.update { it.copy(emailError = false, errorMessage = null) }

    fun onPasswordChanged() = _state.update { it.copy(passwordError = false, errorMessage = null) }

    fun dismissError() = _state.update { it.copy(errorMessage = null) }
}
