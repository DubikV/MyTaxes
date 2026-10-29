package com.gmail.vanyadubik.myassistant.commonui.ui.components.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPhotoPickerLauncher(
    onPhotoPicked: (String) -> Unit,
): PhotoPickerLauncher {
    return remember {
        object : PhotoPickerLauncher {
            override fun launch() {}
        }
    }
}
