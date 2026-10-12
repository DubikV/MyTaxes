package com.gmail.vanyadubik.mytaxes

import android.app.Application
import com.gmail.vanyadubik.mytaxes.di.firebaseAndroidOptions
import com.gmail.vanyadubik.mytaxes.di.initKoin
import com.gmail.vanyadubik.mytaxes.push.createReminderChannel
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext

class TaxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialized by hand (no google-services plugin) so that the debug build with its own applicationId works too
        FirebaseApp.initializeApp(this, firebaseAndroidOptions)
        createReminderChannel(this)
        initKoin {
            androidContext(this@TaxApp)
        }
    }
}
