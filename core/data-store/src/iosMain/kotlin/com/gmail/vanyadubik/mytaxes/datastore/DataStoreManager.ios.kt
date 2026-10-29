package com.gmail.vanyadubik.myassistant.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun platformDataStoreFactory(): PreferencesDataStoreFactory =
    PreferencesDataStoreFactory()
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class PreferencesDataStoreFactory {

    @OptIn(ExperimentalForeignApi::class)
    actual fun createDataStore(fileName: String): DataStore<Preferences> {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        ) ?: error("Documents directory not found")

        return PreferenceDataStoreFactory.create(
            storage =  OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = PreferencesSerializer,
                producePath = { ("${directory.path}/$fileName.preferences_pb").toPath() }
            ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
    }
}