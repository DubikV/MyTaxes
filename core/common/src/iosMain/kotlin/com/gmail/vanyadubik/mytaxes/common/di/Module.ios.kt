package com.gmail.vanyadubik.mytaxes.common.di

import com.gmail.vanyadubik.mytaxes.common.utils.*
import org.koin.dsl.module

actual fun commonPlatformModule() = module {
    single { AppInfo() }
}