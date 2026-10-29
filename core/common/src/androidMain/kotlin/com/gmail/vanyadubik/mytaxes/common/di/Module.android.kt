package com.gmail.vanyadubik.myassistant.common.di

import android.content.Context
import com.gmail.vanyadubik.myassistant.common.utils.*
import org.koin.dsl.module

actual fun commonPlatformModule() = module {
    single { AppInfo(get<Context>()) }
}