package com.gmail.vanyadubik.myassistant.commonui.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.gmail.vanyadubik.myassistant.commonui.ui.LocalIsDarkTheme
import com.gmail.vanyadubik.myassistant.domen.model.AppTheme
import com.gmail.vanyadubik.myassistant.resource.AppColors


private val lightScheme = lightColorScheme(
    primary = AppColors.primaryLight,
    onPrimary = AppColors.onPrimaryLight,
    primaryContainer = AppColors.primaryContainerLight,
    onPrimaryContainer = AppColors.onPrimaryContainerLight,
    secondary = AppColors.secondaryLight,
    onSecondary = AppColors.onSecondaryLight,
    secondaryContainer = AppColors.secondaryContainerLight,
    onSecondaryContainer = AppColors.onSecondaryContainerLight,
    tertiary = AppColors.tertiaryLight,
    onTertiary = AppColors.onTertiaryLight,
    tertiaryContainer = AppColors.tertiaryContainerLight,
    onTertiaryContainer = AppColors.onTertiaryContainerLight,
    error = AppColors.errorLight,
    onError = AppColors.onErrorLight,
    errorContainer = AppColors.errorContainerLight,
    onErrorContainer = AppColors.onErrorContainerLight,
    background = AppColors.backgroundLight,
    onBackground = AppColors.onBackgroundLight,
    surface = AppColors.surfaceLight,
    onSurface = AppColors.onSurfaceLight,
    surfaceVariant = AppColors.surfaceVariantLight,
    onSurfaceVariant = AppColors.onSurfaceVariantLight,
    outline = AppColors.outlineLight,
    outlineVariant = AppColors.outlineVariantLight,
    scrim = AppColors.scrimLight,
    inverseSurface = AppColors.inverseSurfaceLight,
    inverseOnSurface = AppColors.inverseOnSurfaceLight,
    inversePrimary = AppColors.inversePrimaryLight,
    surfaceDim = AppColors.surfaceDimLight,
    surfaceBright = AppColors.surfaceBrightLight,
    surfaceContainerLowest = AppColors.surfaceContainerLowestLight,
    surfaceContainerLow = AppColors.surfaceContainerLowLight,
    surfaceContainer = AppColors.surfaceContainerLight,
    surfaceContainerHigh = AppColors.surfaceContainerHighLight,
    surfaceContainerHighest = AppColors.surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = AppColors.primaryDark,
    onPrimary = AppColors.onPrimaryDark,
    primaryContainer = AppColors.primaryContainerDark,
    onPrimaryContainer = AppColors.onPrimaryContainerDark,
    secondary = AppColors.secondaryDark,
    onSecondary = AppColors.onSecondaryDark,
    secondaryContainer = AppColors.secondaryContainerDark,
    onSecondaryContainer = AppColors.onSecondaryContainerDark,
    tertiary = AppColors.tertiaryDark,
    onTertiary = AppColors.onTertiaryDark,
    tertiaryContainer = AppColors.tertiaryContainerDark,
    onTertiaryContainer = AppColors.onTertiaryContainerDark,
    error = AppColors.errorDark,
    onError = AppColors.onErrorDark,
    errorContainer = AppColors.errorContainerDark,
    onErrorContainer = AppColors.onErrorContainerDark,
    background = AppColors.backgroundDark,
    onBackground = AppColors.onBackgroundDark,
    surface = AppColors.surfaceDark,
    onSurface = AppColors.onSurfaceDark,
    surfaceVariant = AppColors.surfaceVariantDark,
    onSurfaceVariant = AppColors.onSurfaceVariantDark,
    outline = AppColors.outlineDark,
    outlineVariant = AppColors.outlineVariantDark,
    scrim = AppColors.scrimDark,
    inverseSurface = AppColors.inverseSurfaceDark,
    inverseOnSurface = AppColors.inverseOnSurfaceDark,
    inversePrimary = AppColors.inversePrimaryDark,
    surfaceDim = AppColors.surfaceDimDark,
    surfaceBright = AppColors.surfaceBrightDark,
    surfaceContainerLowest = AppColors.surfaceContainerLowestDark,
    surfaceContainerLow = AppColors.surfaceContainerLowDark,
    surfaceContainer = AppColors.surfaceContainerDark,
    surfaceContainerHigh = AppColors.surfaceContainerHighDark,
    surfaceContainerHighest = AppColors.surfaceContainerHighestDark,
)

@Composable
expect fun SetSystemBarsAppearance(isDarkTheme: Boolean, isDefault: Boolean = false)

@Composable
fun AppTheme(
    appTheme: AppTheme,
    content: @Composable() () -> Unit
) {

//    val colorScheme = when (appTheme) {
//        AppTheme.Dark -> DarkColors
//        AppTheme.Light -> LightColors
//        AppTheme.System -> if (isSystemInDarkTheme()) DarkColors else LightColors
//    }

    val colorScheme = when (appTheme) {
        AppTheme.Dark -> darkScheme
        AppTheme.Light -> lightScheme
        AppTheme.System -> if (isSystemInDarkTheme()) darkScheme else lightScheme
    }

    val isDarkTheme = appTheme == AppTheme.Dark || (appTheme == AppTheme.System && isSystemInDarkTheme())
//    using in AppNavigation
//    SetSystemBarsAppearance(isDarkTheme = isDarkTheme)
    CompositionLocalProvider(
        LocalIsDarkTheme provides isDarkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}