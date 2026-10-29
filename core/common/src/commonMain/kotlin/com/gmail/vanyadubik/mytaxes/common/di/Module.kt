package com.gmail.vanyadubik.myassistant.common.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect fun commonPlatformModule(): Module

fun commonModule() = module {
}