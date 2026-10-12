package com.gmail.vanyadubik.mytaxes.commonui.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("Can't access SnackbarHostState")
}

val LocalIsDarkTheme = staticCompositionLocalOf<Boolean> {
    error("Can't access IsDarkThemeState")
}

val LocalIsShowSplash = staticCompositionLocalOf<Boolean> {
    error("Can't access IsShowSplash")
}
