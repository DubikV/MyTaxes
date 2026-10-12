package com.gmail.vanyadubik.mytaxes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.mytaxes.commonui.ui.theme.ThemeAnimationHandler
import com.gmail.vanyadubik.mytaxes.domen.usecase.AuthUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    authUseCase: AuthUseCase,
    val themeAnimationHandler: ThemeAnimationHandler
) : ViewModel() {

    /** null until the stored session has been read */
    val isLoggedIn: StateFlow<Boolean?> = authUseCase.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
