package com.gmail.vanyadubik.myassistant.common.utils

import androidx.compose.runtime.Composable

enum class ScreenOrientation {
    PORTRAIT,
    LANDSCAPE,
    SQUARE,
    UNKNOWN
}

expect object PlatformConfig {
    @Composable
    fun getCurrentOrientation(): ScreenOrientation
    fun isDebugBuild(): Boolean
}
