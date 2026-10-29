package com.gmail.vanyadubik.myassistant.commonui.ui.components.image

import androidx.compose.runtime.Composable

interface PhotoPickerLauncher {
    fun launch()
}

@Composable
expect fun rememberPhotoPickerLauncher(
    onPhotoPicked: (String) -> Unit,
): PhotoPickerLauncher
