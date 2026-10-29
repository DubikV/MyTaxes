package com.gmail.vanyadubik.myassistant.commonui.di

import org.koin.dsl.module
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.setStatusBarStyle

actual fun commonUiPlatformModule() = module {
}

/**
 * Важно для iOS: Чтобы SystemAppearance работало,
 * в Info.plist должен быть установлен ключ View controller-based status bar appearance в значение NO.
 */
@Composable
actual fun SystemAppearance(isDark: Boolean) {
    SideEffect {
        // UIStatusBarStyleLightContent — белые иконки
        // UIStatusBarStyleDarkContent — черные иконки
        val style = if (isDark) {
            UIStatusBarStyleLightContent
        } else {
            UIStatusBarStyleDarkContent
        }
        UIApplication.sharedApplication.setStatusBarStyle(style, animated = true)
    }
}