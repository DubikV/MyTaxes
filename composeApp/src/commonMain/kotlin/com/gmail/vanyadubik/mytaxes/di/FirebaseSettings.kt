package com.gmail.vanyadubik.mytaxes.di

import com.gmail.vanyadubik.mytaxes.network.FirebaseConfig

/**
 * Fill in the values of your Firebase project (Firebase console -> Project settings -> General).
 * The Web API key is not a secret; access to the data is protected by Firestore security rules.
 */
val firebaseConfig = FirebaseConfig(
    apiKey = "YOUR_FIREBASE_WEB_API_KEY",
    projectId = "YOUR_FIREBASE_PROJECT_ID",
)
