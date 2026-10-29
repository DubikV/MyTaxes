package com.gmail.vanyadubik.myassistant.commonui.ui.theme.button

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.gmail.vanyadubik.myassistant.commonui.ui.theme.ThemeAnimationHandler

internal actual fun Modifier.themeAnimationTarget(
    state: ThemeAnimationHandler,
    positionProvider: ThemeSwitchPositionProvider,
): Modifier = this.onGloballyPositioned { coordinates ->
    val popup = positionProvider.popupPosition
    val position = popup + coordinates.boundsInRoot().center
    state.updateButtonPosition(position)
}