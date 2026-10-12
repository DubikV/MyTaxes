package com.gmail.vanyadubik.mytaxes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gmail.vanyadubik.mytaxes.commonui.di.SystemAppearance
import com.gmail.vanyadubik.mytaxes.commonui.language.LanguageHandler
import com.gmail.vanyadubik.mytaxes.commonui.language.applyAppLanguage
import com.gmail.vanyadubik.mytaxes.commonui.ui.LocalIsDarkTheme
import com.gmail.vanyadubik.mytaxes.commonui.ui.LocalIsShowSplash
import com.gmail.vanyadubik.mytaxes.commonui.ui.theme.AppTheme
import com.gmail.vanyadubik.mytaxes.commonui.ui.theme.ThemeAnimationScope
import com.gmail.vanyadubik.mytaxes.navigation.AppNavigation
import com.gmail.vanyadubik.mytaxes.viewmodel.MainViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    val viewModel = koinViewModel<MainViewModel>()
    val themeHandler = viewModel.themeAnimationHandler
    val languageHandler = koinInject<LanguageHandler>()
    var showSplash by rememberSaveable { mutableStateOf(true) }

    CompositionLocalProvider(
        LocalIsShowSplash provides showSplash
    ) {
        ThemeAnimationScope(state = themeHandler) { currentTheme ->
            AppTheme(appTheme = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Recreate the screens when the language changes so that all strings are re-read
                    key(languageHandler.language) {
                        remember(languageHandler.language) { applyAppLanguage(languageHandler.language) }
                        AppNavigation(viewModel = viewModel)
                    }
                    if (showSplash) {
                        SplashScreen(
                            startRunHidde = true,
                            onNavigateToLogin = { showSplash = false }
                        )
                    } else {
                        // The splash forces light icons over its dark background, afterwards follow the app theme
                        SystemAppearance(isDark = LocalIsDarkTheme.current)
                    }
                }
            }
        }
    }
}
