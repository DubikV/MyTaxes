package com.gmail.vanyadubik.mytaxes.featurecalculator.di

import com.gmail.vanyadubik.mytaxes.featurecalculator.CalculatorViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun featureCalculatorModule() = module {
    viewModelOf(::CalculatorViewModel)
}
