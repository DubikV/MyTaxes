package com.gmail.vanyadubik.myassistant.commonui.di

import androidx.compose.animation.core.tween
import com.gmail.vanyadubik.myassistant.commonui.language.LanguageHandler
import com.gmail.vanyadubik.myassistant.commonui.ui.theme.ThemeAnimationFormat
import com.gmail.vanyadubik.myassistant.commonui.ui.theme.ThemeAnimationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

import androidx.compose.runtime.Composable

@Composable
expect fun SystemAppearance(isDark: Boolean)

expect fun commonUiPlatformModule(): Module
fun commonUiModule() = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    single { LanguageHandler(dataStoreUseCase = get(), coroutineScope = get()) }
    single {
        ThemeAnimationHandler(
            dataStoreUseCase = get(),
            coroutineScope = get(),
            animationSpec = tween(800),
            format = ThemeAnimationFormat.CircularAroundPress,
            useDynamicContent = true
        )
    }
}