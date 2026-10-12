package com.gmail.vanyadubik.mytaxes.featurehome.di

import com.gmail.vanyadubik.mytaxes.featurehome.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun featureHomeModule() = module {
    viewModelOf(::HomeViewModel)
}
