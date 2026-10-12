package com.gmail.vanyadubik.mytaxes.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun platformDataStoreFactory(): PreferencesDataStoreFactory

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PreferencesDataStoreFactory {
    fun createDataStore(fileName: String): DataStore<Preferences>
}