package com.gmail.vanyadubik.myassistant.featurenotifications.model

import com.gmail.vanyadubik.myassistant.domen.model.PushMessage

data class NotificationsUiState(
    val messages: List<PushMessage> = emptyList(),
) {
    val unreadCount: Int get() = messages.count { !it.isRead }
}
