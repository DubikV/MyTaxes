package com.gmail.vanyadubik.mytaxes.common.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import com.gmail.vanyadubik.mytaxes.common.BuildConfig

actual object PlatformConfig {

    @Composable
    actual fun getCurrentOrientation(): ScreenOrientation {
        val configuration = LocalConfiguration.current

        return when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> ScreenOrientation.LANDSCAPE
            Configuration.ORIENTATION_PORTRAIT -> ScreenOrientation.PORTRAIT
            Configuration.ORIENTATION_SQUARE -> ScreenOrientation.SQUARE
            else -> ScreenOrientation.UNKNOWN
        }
    }
    actual fun isDebugBuild(): Boolean = BuildConfig.DEBUG
}