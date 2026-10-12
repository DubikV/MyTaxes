package com.gmail.vanyadubik.mytaxes.datastore.di

import com.gmail.vanyadubik.mytaxes.datastore.platformDataStoreFactory
import com.gmail.vanyadubik.mytaxes.datastore.repository.DataStoreRepositoryImpl
import com.gmail.vanyadubik.mytaxes.domen.repository.DataStoreRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun dataStoreModule() = module {
    single { platformDataStoreFactory().createDataStore("settings") }
    singleOf(::DataStoreRepositoryImpl).bind(DataStoreRepository::class)
}