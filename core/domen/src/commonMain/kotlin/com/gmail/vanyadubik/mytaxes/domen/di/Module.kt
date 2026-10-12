package com.gmail.vanyadubik.mytaxes.domen.di

import com.gmail.vanyadubik.mytaxes.domen.usecase.AuthUseCase
import com.gmail.vanyadubik.mytaxes.domen.usecase.CalculateTaxUseCase
import com.gmail.vanyadubik.mytaxes.domen.usecase.DataStoreUseCase
import com.gmail.vanyadubik.mytaxes.domen.usecase.TaxHistoryUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun domenModule() = module {
    singleOf(::DataStoreUseCase)
    singleOf(::AuthUseCase)
    singleOf(::CalculateTaxUseCase)
    singleOf(::TaxHistoryUseCase)
}
