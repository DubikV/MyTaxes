package com.gmail.vanyadubik.myassistant.featureaccount.di

import com.gmail.vanyadubik.myassistant.featureaccount.AccountViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun featureAccountModule() = module {
    viewModelOf(::AccountViewModel)
}
