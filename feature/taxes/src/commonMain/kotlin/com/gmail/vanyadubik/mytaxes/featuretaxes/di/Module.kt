package com.gmail.vanyadubik.myassistant.featuretaxes.di

import com.gmail.vanyadubik.myassistant.featuretaxes.screens.history.TaxesHistoryViewModel
import com.gmail.vanyadubik.myassistant.featuretaxes.screens.taxes.TaxesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun featureTaxesModule() = module {
    viewModelOf(::TaxesViewModel)
    viewModelOf(::TaxesHistoryViewModel)
}
