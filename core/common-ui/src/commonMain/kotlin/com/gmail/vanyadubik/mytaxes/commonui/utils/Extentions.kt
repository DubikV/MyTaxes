package com.gmail.vanyadubik.mytaxes.commonui.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.gmail.vanyadubik.mytaxes.domen.model.AppTheme

@Composable
fun AppTheme.isDark(): Boolean = when (this) {
    AppTheme.Light -> false
    AppTheme.Dark -> true
    AppTheme.System -> isSystemInDarkTheme()
}
