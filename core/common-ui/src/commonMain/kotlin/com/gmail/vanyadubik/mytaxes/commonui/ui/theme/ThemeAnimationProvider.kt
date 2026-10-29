package com.gmail.vanyadubik.myassistant.commonui.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.gmail.vanyadubik.myassistant.commonui.utils.isDark
import com.gmail.vanyadubik.myassistant.domen.model.AppTheme

/**
 * A composable scope that enables theme animations for its content.
 *
 * The animation captures the visual state of the content before and after the theme change,
 * creating a smooth transition effect. Place this scope around the composable tree that
 * should be animated during theme toggles.
 *
 * @param state The [ThemeAnimationHandler] instance that controls the theme animation.
 *              This state manages the current theme, animation timing, and format.
 * @param content The composable content to be displayed and animated during theme transitions.
 *                This content will be captured and rendered with theme animation effects.
 */
@Composable
fun ThemeAnimationScope(
    state: ThemeAnimationHandler,
    content: @Composable (AppTheme) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    Box(
        modifier = Modifier.themeAnimation(
            state = state,
            isDark = state.uiTheme.isDark(),
            graphicsLayer = graphicsLayer
        )
    ) {
        content(state.uiTheme)
    }
}