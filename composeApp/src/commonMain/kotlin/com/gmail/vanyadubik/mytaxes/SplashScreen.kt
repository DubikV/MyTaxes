package com.gmail.vanyadubik.mytaxes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.mytaxes.commonui.ui.LocalIsDarkTheme
import com.gmail.vanyadubik.mytaxes.commonui.ui.theme.SetSystemBarsAppearance
import com.gmail.vanyadubik.mytaxes.resource.AppColors
import com.gmail.vanyadubik.mytaxes.resource.Res
import com.gmail.vanyadubik.mytaxes.resource.ic_splash
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun SplashScreen(
    startRunHidde: Boolean,
    onNavigateToLogin: () -> Unit
) {
    val alpha = remember { Animatable(1f) }

    key(Unit) {
        SetSystemBarsAppearance(
            isDarkTheme = true,
            isDefault = true
        )
    }

    LaunchedEffect(startRunHidde) {
        if(startRunHidde) {
            delay(1500)
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1200)
            )
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.backgroundDark.copy(alpha = alpha.value))
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_splash),
            contentDescription = "Splash Logo",
            modifier = Modifier
                .align(Alignment.Center)
                .width(280.dp)
                .height(280.dp)
//                .fillMaxSize()
                .alpha(alpha.value),
            contentScale = ContentScale.Fit
        )
    }
}