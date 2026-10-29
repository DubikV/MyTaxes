package com.gmail.vanyadubik.myassistant

import android.app.Application
import com.gmail.vanyadubik.myassistant.di.firebaseAndroidOptions
import com.gmail.vanyadubik.myassistant.di.initKoin
import com.gmail.vanyadubik.myassistant.push.createReminderChannel
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
