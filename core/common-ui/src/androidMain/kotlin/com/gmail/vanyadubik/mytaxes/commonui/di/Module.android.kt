package com.gmail.vanyadubik.myassistant.commonui.di

import org.koin.dsl.module
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

actual fun commonUiPlatformModule() = module {
}

@Composable
actual fun SystemAppearance(isDark: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)

            // Если в приложении ТЕМНО (isDark = true),
            // иконки должны быть СВЕТЛЫМИ (isAppearanceLight... = false)
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
        }
    }
}