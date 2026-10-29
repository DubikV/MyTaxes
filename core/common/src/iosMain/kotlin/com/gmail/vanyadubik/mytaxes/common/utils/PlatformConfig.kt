package com.gmail.vanyadubik.myassistant.common.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSBundle
import platform.Foundation.NSDictionary
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientationIsLandscape
import platform.UIKit.UIDeviceOrientationIsPortrait

actual object PlatformConfig {
    @Composable
    actual fun getCurrentOrientation(): ScreenOrientation {

        val orientation = remember { UIDevice.currentDevice.orientation }

        return when {
            UIDeviceOrientationIsLandscape(orientation) -> ScreenOrientation.LANDSCAPE
            UIDeviceOrientationIsPortrait(orientation) -> ScreenOrientation.PORTRAIT
            else -> ScreenOrientation.UNKNOWN
        }
    }

    // Допоміжна функція для визначення режиму збірки iOS
// Ця функція є внутрішньою логікою платформи
    actual fun isDebugBuild(): Boolean {
        // У KMP для iOS найчистіший спосіб визначення режиму збірки
        // вимагає взаємодії з нативним середовищем (наприклад, Info.plist).

        // Приклад логіки:
        // Ми перевіряємо, чи містить словник інформації про збірку ключ,
        // що вказує на Debug (це залежить від конфігурації Xcode).
        val infoDictionary = NSBundle.mainBundle.infoDictionary as? NSDictionary ?: return false
        val configKey = infoDictionary.objectForKey("Configuration") as? String
        return configKey?.contains("Debug", ignoreCase = true) == true
    }
}