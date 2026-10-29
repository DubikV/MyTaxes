package com.gmail.vanyadubik.myassistant.domen.di

import com.gmail.vanyadubik.myassistant.domen.usecase.AccountUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.AuthUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.AutoServiceUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.CalculateTaxUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.DataStoreUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.ExpenseUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.MonthlyStatsUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.PushNotificationUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.TaxHistoryUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.TaxesUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun domenModule() = module {
    singleOf(::DataStoreUseCase)
    singleOf(::AuthUseCase)
    singleOf(::CalculateTaxUseCase)
    singleOf(::TaxHistoryUseCase)
    singleOf(::ExpenseUseCase)
    singleOf(::MonthlyStatsUseCase)
    singleOf(::PushNotificationUseCase)
    singleOf(::AccountUseCase)
    singleOf(::TaxesUseCase)
    singleOf(::AutoServiceUseCase)
}
