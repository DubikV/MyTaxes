package com.gmail.vanyadubik.mytaxes.common.di

import android.content.Context
import com.gmail.vanyadubik.mytaxes.common.utils.*
import org.koin.dsl.module

actual fun commonPlatformModule() = module {
    single { AppInfo(get<Context>()) }
}