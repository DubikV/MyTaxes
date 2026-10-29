package com.gmail.vanyadubik.myassistant.di

import com.gmail.vanyadubik.myassistant.common.di.commonModule
import com.gmail.vanyadubik.myassistant.common.di.commonPlatformModule
import com.gmail.vanyadubik.myassistant.commonui.di.commonUiModule
import com.gmail.vanyadubik.myassistant.commonui.di.commonUiPlatformModule
import com.gmail.vanyadubik.myassistant.datastore.di.dataStoreModule
import com.gmail.vanyadubik.myassistant.domen.di.domenModule
import com.gmail.vanyadubik.myassistant.featureaccount.di.featureAccountModule
import com.gmail.vanyadubik.myassistant.featureauto.di.featureAutoModule
import com.gmail.vanyadubik.myassistant.featurehome.di.featureHomeModule
import com.gmail.vanyadubik.myassistant.featurelogin.di.featureLoginModule
import com.gmail.vanyadubik.myassistant.featuretaxes.di.featureTaxesModule
import com.gmail.vanyadubik.myassistant.network.di.networkModule
import com.gmail.vanyadubik.myassistant.viewmodel.MainViewModel
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
                featureTaxesModule(),
                featureAccountModule(),
                featureHomeModule(),
                featureAutoModule(),
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
