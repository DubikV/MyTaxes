package com.gmail.vanyadubik.mytaxes.di

import com.gmail.vanyadubik.mytaxes.common.di.commonModule
import com.gmail.vanyadubik.mytaxes.common.di.commonPlatformModule
import com.gmail.vanyadubik.mytaxes.commonui.di.commonUiModule
import com.gmail.vanyadubik.mytaxes.commonui.di.commonUiPlatformModule
import com.gmail.vanyadubik.mytaxes.datastore.di.dataStoreModule
import com.gmail.vanyadubik.mytaxes.domen.di.domenModule
import com.gmail.vanyadubik.mytaxes.featurecalculator.di.featureCalculatorModule
import com.gmail.vanyadubik.mytaxes.featurehistory.di.featureHistoryModule
import com.gmail.vanyadubik.mytaxes.featurehome.di.featureHomeModule
import com.gmail.vanyadubik.mytaxes.featurelogin.di.featureLoginModule
import com.gmail.vanyadubik.mytaxes.network.di.networkModule
import com.gmail.vanyadubik.mytaxes.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.dsl.onClose

expect fun platformModule(): Module

public fun doInitKoin() {
    initKoin()
}

public fun initKoin() {
    initKoin(enableNetworkLogs = true) {}
}

public fun initKoin(
    enableNetworkLogs: Boolean = true,
    appDeclaration: KoinAppDeclaration = {}
) =
    startKoin {
        appDeclaration()
        modules(
            listOf(
                appScopeModule(),
                domenModule(),
                dataStoreModule(),
                networkModule(
                    firebaseConfig = firebaseConfig,
                    enableNetworkLogs = enableNetworkLogs
                ),
                featureLoginModule(),
                featureCalculatorModule(),
                featureHistoryModule(),
                featureHomeModule(),
                platformModule(),
                mainModule(),
                commonUiModule(),
                commonUiPlatformModule(),
                commonModule(),
                commonPlatformModule(),
            )
        )
    }

fun mainModule() = module {
    viewModelOf(::MainViewModel)
}

fun appScopeModule() = module {
    single<CoroutineScope>(createdAtStart = true) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    } onClose { scope ->
        scope?.cancel()
    }
}
