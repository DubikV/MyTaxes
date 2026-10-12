package com.gmail.vanyadubik.mytaxes.featurehistory.di

import com.gmail.vanyadubik.mytaxes.featurehistory.HistoryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun featureHistoryModule() = module {
    viewModelOf(::HistoryViewModel)
}
