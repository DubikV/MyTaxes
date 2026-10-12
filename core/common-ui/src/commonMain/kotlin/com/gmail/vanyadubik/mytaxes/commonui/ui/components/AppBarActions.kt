package com.gmail.vanyadubik.mytaxes.commonui.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.mytaxes.commonui.language.LanguageSwitchButton
import com.gmail.vanyadubik.mytaxes.commonui.ui.theme.ThemeAnimationHandler
import com.gmail.vanyadubik.mytaxes.commonui.ui.theme.button.ThemeSwitchButton
import org.koin.compose.koinInject

/** Theme and language switches shown in the top bar of every screen with the bottom menu. */
@Composable
fun AppBarActions() {
    ThemeSwitchButton(
        themeHandler = koinInject<ThemeAnimationHandler>(),
        iconModifier = Modifier.padding(12.dp),
        iconSize = 24.dp,
    )
    LanguageSwitchButton()
}
