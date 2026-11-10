package com.gmail.vanyadubik.myassistant.di

import com.gmail.vanyadubik.myassistant.BuildConfig
import com.gmail.vanyadubik.myassistant.domen.repository.PushTokenProvider
import com.gmail.vanyadubik.myassistant.push.AndroidPushTokenProvider
import com.google.firebase.FirebaseOptions
import org.koin.dsl.module

/** Values configured via BuildConfig in composeApp/build.gradle.kts */
val firebaseAndroidOptions: FirebaseOptions = FirebaseOptions.Builder()
    .setApplicationId(BuildConfig.FIREBASE_APP_ID)
    .setApiKey(BuildConfig.FIREBASE_API_KEY)
    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
    .setGcmSenderId(BuildConfig.FIREBASE_GCM_SENDER_ID)
    .build()

actual fun platformModule() = module {
    single<PushTokenProvider> { AndroidPushTokenProvider() }
}
