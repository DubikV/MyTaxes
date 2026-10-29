package com.gmail.vanyadubik.myassistant.commonui.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.error
import com.gmail.vanyadubik.myassistant.resource.loading
import com.gmail.vanyadubik.myassistant.resource.ok

@Composable
fun AppDialog(
    title: String?,
    message: String? = null,
    content: @Composable (() -> Unit)? = null,
    confirmButtonName: String? = stringResource(Res.string.ok),
    onConfirm: (() -> Unit)? = null,
    dismissButtonName: String? = null,
    onDismiss: (() -> Unit)? = null,
){
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = if(!title.isNullOrEmpty()){
            { Text(title) }
        }else{
            null
        },
        text = if(!message.isNullOrEmpty()){
            { Text(message) }
        }else{
            content
        },
        confirmButton = if(!confirmButtonName.isNullOrEmpty()){
            {
                Button(onClick = { onConfirm?.invoke() }) {
                    Text(confirmButtonName)
                }
            }
        }else{
            {}
        },
        dismissButton = if(!dismissButtonName.isNullOrEmpty()){
            {
                Button(onClick = { onDismiss?.invoke() }) {
                    Text(dismissButtonName)
                }
            }
        }else{
            {}
        }
    )
}

@Composable
fun ErrorAppDialog(
    title: String = stringResource(Res.string.error),
    message: String,
    confirmButtonName: String? = stringResource(Res.string.ok),
    onDismiss: (() -> Unit)? = null,
){
    AppDialog(
        title = title,
        content = { Text(message) },
        confirmButtonName = confirmButtonName,
        onConfirm = onDismiss,
        onDismiss = onDismiss
    )
}

@Composable
fun LoadingAppDialog(
    message: String = stringResource(Res.string.loading),
    isCancelable: Boolean = true,
    cancelButtonName: String? = null,
    onDismiss: (() -> Unit)? = null,
) {
    AppDialog(
        title = null,
        content = {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(min = 120.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = message,
                )
            }
        },
        confirmButtonName = cancelButtonName,
        onConfirm = {
            if (isCancelable) {
                onDismiss?.invoke()
            }
        },
        onDismiss = {
            if (isCancelable) {
                onDismiss?.invoke()
            }
        },
    )
}


@Composable
fun <T> SelectionAppDialog(
    title: String?,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    onDismiss: () -> Unit,
    itemContent: @Composable (T) -> Unit = { Text(it.toString()) }
) {
    AppDialog(
        title = title,
        content = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemSelected(item)
                            }
                            .padding(vertical = 12.dp, horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemContent(item)
                    }
                }
            }
        },
        confirmButtonName = null,
        onDismiss = onDismiss
    )
}