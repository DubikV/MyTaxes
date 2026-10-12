package com.gmail.vanyadubik.mytaxes.featurelogin.di

import com.gmail.vanyadubik.mytaxes.featurelogin.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


fun featureLoginModule() = module {
    viewModelOf(::LoginViewModel)
}