package com.gmail.vanyadubik.myassistant.featurehome.di

import com.gmail.vanyadubik.myassistant.featurehome.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun featureHomeModule() = module {
    viewModelOf(::HomeViewModel)
}
