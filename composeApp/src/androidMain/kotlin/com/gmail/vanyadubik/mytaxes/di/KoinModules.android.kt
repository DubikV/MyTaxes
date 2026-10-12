package com.gmail.vanyadubik.mytaxes.di

import com.gmail.vanyadubik.mytaxes.domen.repository.PushTokenProvider
import com.gmail.vanyadubik.mytaxes.push.AndroidPushTokenProvider
import com.google.firebase.FirebaseOptions
import org.koin.dsl.module

/** Values of composeApp/google-services.json (Firebase console -> Project settings -> Your apps -> Android). */
val firebaseAndroidOptions: FirebaseOptions = FirebaseOptions.Builder()
    .setApplicationId("1:81447438385:android:2cf1e7ece7e23b7745ed10")
    .setApiKey("AIzaSyDu4pSNq8pKeFbVYjgGXJS1_pcFCT8E21I")
    .setProjectId("mytaxes-53780")
    .setGcmSenderId("81447438385")
    .build()

actual fun platformModule() = module {
    single<PushTokenProvider> { AndroidPushTokenProvider() }
}
