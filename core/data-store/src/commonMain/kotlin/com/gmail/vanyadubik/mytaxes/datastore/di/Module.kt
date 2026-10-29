package com.gmail.vanyadubik.myassistant.datastore.di

import com.gmail.vanyadubik.myassistant.datastore.platformDataStoreFactory
import com.gmail.vanyadubik.myassistant.datastore.repository.DataStoreRepositoryImpl
import com.gmail.vanyadubik.myassistant.domen.repository.DataStoreRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun dataStoreModule() = module {
    single { platformDataStoreFactory().createDataStore("settings") }
    singleOf(::DataStoreRepositoryImpl).bind(DataStoreRepository::class)
}