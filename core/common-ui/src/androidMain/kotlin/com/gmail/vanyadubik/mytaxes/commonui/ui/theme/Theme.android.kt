package com.gmail.vanyadubik.myassistant.commonui.ui.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun SetSystemBarsAppearance(isDarkTheme: Boolean, isDefault: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)

            controller.isAppearanceLightStatusBars = if(isDefault){
                !isDarkTheme
            }else{
                isDarkTheme
            }
            controller.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }
}