package com.gmail.vanyadubik.myassistant.commonui.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.myassistant.commonui.ui.components.button.AppButton
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.cancel
import org.jetbrains.compose.resources.stringResource

/** A confirmation shown as a bottom sheet; used instead of dialogs for confirmation and destructive actions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmBottomSheet(
    title: String,
    message: String? = null,
    confirmText: String,
    cancelText: String = stringResource(Res.string.cancel),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: (@Composable () -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            content?.invoke()
            AppButton(text = confirmText, onClick = onConfirm)
            AppButton(text = cancelText, onClick = onDismiss, isOutlinedButton = true)
        }
    }
}
