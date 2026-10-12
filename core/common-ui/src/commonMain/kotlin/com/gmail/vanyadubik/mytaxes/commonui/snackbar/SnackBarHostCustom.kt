package com.gmail.vanyadubik.mytaxes.commonui.snackbar

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable


@Composable
fun SnackBarHostCustom(hostState: SnackbarHostState) {
    SnackbarHost(hostState = hostState) { data ->
        val isError = (data.visuals as? SnackbarVisualsCustom)?.isError ?: false

        Snackbar(
            snackbarData = data,
            containerColor = if(isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.secondary
            },
            contentColor = if(isError) {
                MaterialTheme.colorScheme.onError
            } else {
                MaterialTheme.colorScheme.onSecondary
            }
        )
    }
}