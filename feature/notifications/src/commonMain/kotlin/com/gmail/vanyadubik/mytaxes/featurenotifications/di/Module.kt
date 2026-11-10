package com.gmail.vanyadubik.myassistant.featurenotifications.di

import com.gmail.vanyadubik.myassistant.featurenotifications.NotificationsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun featureNotificationsModule() = module {
    viewModelOf(::NotificationsViewModel)
}
