package com.gmail.vanyadubik.mytaxes.common.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect fun commonPlatformModule(): Module

fun commonModule() = module {
}