package com.gmail.vanyadubik.mytaxes.commonui.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.gmail.vanyadubik.mytaxes.domen.model.AppTheme
import com.gmail.vanyadubik.mytaxes.domen.usecase.DataStoreUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Stable
class ThemeAnimationHandler(
    private val dataStoreUseCase: DataStoreUseCase,
    private val coroutineScope: CoroutineScope,
    internal val animationSpec: AnimationSpec<Float> = tween(800),
    internal val format: ThemeAnimationFormat = ThemeAnimationFormat.CircularAroundPress,
    internal val useDynamicContent: Boolean = true,
) {
    var uiTheme: AppTheme by mutableStateOf(AppTheme.System)
        private set
    internal val requestRecord = MutableStateFlow(ThemeRecordStatus.Initial)

    internal var buttonPosition: Offset? by mutableStateOf(null)
        private set

    fun updateButtonPosition(position: Offset) {
        buttonPosition = position
    }

    fun toggleTheme() {
        coroutineScope.launch {
            dataStoreUseCase.toggleTheme()
        }
    }

    init {
        coroutineScope.launch {
            dataStoreUseCase.currentTheme
                .collectIndexed { index, newTheme ->
                    if (index == 0) {
                        uiTheme = newTheme
                    } else {
                        requestRecord.value = ThemeRecordStatus.RecordRequested
                        requestRecord.firstOrNull { it == ThemeRecordStatus.Recorded }
                        uiTheme = newTheme
                        requestRecord.value = ThemeRecordStatus.PrepareForAnimation
                    }
                }
        }
    }
}