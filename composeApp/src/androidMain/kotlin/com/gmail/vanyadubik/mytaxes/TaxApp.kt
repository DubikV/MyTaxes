package com.gmail.vanyadubik.mytaxes

import android.app.Application
import com.gmail.vanyadubik.mytaxes.di.initKoin
import org.koin.android.ext.koin.androidContext

class TaxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@TaxApp)
        }
    }
}