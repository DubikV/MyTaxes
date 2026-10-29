package com.gmail.vanyadubik.myassistant.commonui.ui.theme.button

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.myassistant.commonui.ui.theme.ThemeAnimationHandler
import com.gmail.vanyadubik.myassistant.domen.model.AppTheme

@Composable
fun ThemeSwitchButton(
    themeHandler: ThemeAnimationHandler,
    buttonIcon: ThemeSwitchIcon? = null,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    iconScale: Float = 1f,
    tint: Color =  LocalContentColor.current
) {
//    val positionProvider = remember { ThemeSwitchPositionProvider() }
    Box(modifier) {
//        Popup(
//            popupPositionProvider = positionProvider,
//            properties = PopupProperties(focusable = false, dismissOnClickOutside = false)
//        ) {
            ThemeSwitchButtonBase(
                themeHandler = themeHandler,
//                positionProvider = positionProvider,
                buttonIcon = buttonIcon ?: ThemeSwitchIcon.Vector(
                    imageVector = when (themeHandler.uiTheme) {
                        AppTheme.System -> Icons.Default.AutoMode
                        AppTheme.Dark -> Icons.Default.DarkMode
                        AppTheme.Light -> Icons.Default.LightMode
                    },
                ),
                iconModifier = iconModifier,
                iconSize = iconSize,
                iconScale = iconScale,
                tint = tint
            )
//        }
    }
}

@Composable
private fun ThemeSwitchButtonBase(
    themeHandler: ThemeAnimationHandler,
//    positionProvider: ThemeSwitchPositionProvider,
    buttonIcon: ThemeSwitchIcon,
    iconModifier: Modifier,
    iconSize: Dp,
    iconScale: Float,
    tint: Color =  LocalContentColor.current
) {
//    IconButton(
//        onClick = {
//            themeHandler.toggleTheme()
//        },
//        modifier = iconModifier
//            .themeAnimationTarget(
//                state = themeHandler,
//                positionProvider = positionProvider
//            )
//    ) {
//        buttonIcon.Icon(
//            state = themeHandler,
//            tint = tint,
//            contentDescription = "Theme switch icon",
//            modifier = Modifier.size(iconSize).scale(iconScale)
//        )
//    }

    Box(
        modifier = iconModifier
            .clip(CircleShape)
            .themeClickWithGlobalOffset(
                state = themeHandler,
            ).clickable{
                themeHandler.toggleTheme()
            },
        contentAlignment = Alignment.Center
    ) {
        buttonIcon.Icon(
            state = themeHandler,
            tint = tint,
            contentDescription = "Theme switch icon",
            modifier = Modifier.size(iconSize).scale(iconScale)
        )
    }
}


@Deprecated("remove")
internal expect fun Modifier.themeAnimationTarget(
    state: ThemeAnimationHandler,
    positionProvider: ThemeSwitchPositionProvider,
): Modifier


fun Modifier.themeClickWithGlobalOffset(
    state: ThemeAnimationHandler,
): Modifier = composed {
    this.onGloballyPositioned { coordinates ->
        val rect = coordinates.boundsInWindow()
        val center = rect.center
        state.updateButtonPosition(center)
    }
}