package com.gmail.vanyadubik.mytaxes.commonui.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

class SnackbarVisualsCustom(
    override val message: String,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    val isError: Boolean = false) : SnackbarVisuals