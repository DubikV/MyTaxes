package com.gmail.vanyadubik.mytaxes.commonui.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.setStatusBarStyle

@Composable
actual fun SetSystemBarsAppearance(isDarkTheme: Boolean, isDefault: Boolean) {
    SideEffect {
        val style = if(isDefault) {
            if (isDarkTheme) {
                UIStatusBarStyleLightContent
            } else {
                UIStatusBarStyleDarkContent
            }
        }else{
            if (isDarkTheme) {
                UIStatusBarStyleDarkContent
            } else {
                UIStatusBarStyleLightContent
            }
        }

        UIApplication.sharedApplication.setStatusBarStyle(style, animated = true)
    }
}