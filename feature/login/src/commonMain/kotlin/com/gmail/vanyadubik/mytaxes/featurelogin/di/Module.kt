package com.gmail.vanyadubik.myassistant.featurelogin.di

import com.gmail.vanyadubik.myassistant.featurelogin.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


fun featureLoginModule() = module {
    viewModelOf(::LoginViewModel)
}