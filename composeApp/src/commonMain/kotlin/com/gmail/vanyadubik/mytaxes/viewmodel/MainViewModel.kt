package com.gmail.vanyadubik.mytaxes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.mytaxes.commonui.ui.theme.ThemeAnimationHandler
import com.gmail.vanyadubik.mytaxes.domen.usecase.AuthUseCase
import com.gmail.vanyadubik.mytaxes.domen.usecase.DataStoreUseCase
import com.gmail.vanyadubik.mytaxes.domen.usecase.PushNotificationUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val authUseCase: AuthUseCase,
    private val dataStoreUseCase: DataStoreUseCase,
    private val pushNotifications: PushNotificationUseCase,
    val themeAnimationHandler: ThemeAnimationHandler
) : ViewModel() {

    /** null until the stored session has been read */
    val isLoggedIn: StateFlow<Boolean?> = authUseCase.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        // Keep this device's push token (with the FOP group and language the reminders depend on) up to date
        viewModelScope.launch {
            combine(authUseCase.isLoggedIn, dataStoreUseCase.fopGroup, dataStoreUseCase.language) { loggedIn, _, _ -> loggedIn }
                .collect { loggedIn -> if (loggedIn) pushNotifications.register() }
        }
    }

    fun logout() {
        viewModelScope.launch {
            pushNotifications.unregister()
            authUseCase.logout()
        }
    }
}
