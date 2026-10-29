package com.gmail.vanyadubik.myassistant.featureauto.di

import com.gmail.vanyadubik.myassistant.featureauto.AutoViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun featureAutoModule() = module {
    viewModelOf(::AutoViewModel)
}
