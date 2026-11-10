package com.gmail.vanyadubik.myassistant.featurenotifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.myassistant.domen.usecase.PushMessagesUseCase
import com.gmail.vanyadubik.myassistant.featurenotifications.model.NotificationsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val pushMessages: PushMessagesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            pushMessages.messages.collect { list -> _state.update { it.copy(messages = list) } }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch { pushMessages.markAsRead(id) }
    }

    fun markAllAsRead() {
        viewModelScope.launch { pushMessages.markAllAsRead() }
    }

    fun delete(id: String) {
        viewModelScope.launch { pushMessages.delete(id) }
    }

    fun clear() {
        viewModelScope.launch { pushMessages.clear() }
    }
}
