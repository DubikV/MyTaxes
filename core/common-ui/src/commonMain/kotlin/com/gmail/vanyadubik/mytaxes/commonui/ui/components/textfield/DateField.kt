package com.gmail.vanyadubik.myassistant.commonui.ui.components.textfield

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gmail.vanyadubik.myassistant.commonui.utils.formatDate
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.cancel
import com.gmail.vanyadubik.myassistant.resource.select
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/** Read-only text field that opens a date picker dialog on click. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    label: String,
    date: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null,
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = date?.formatDate() ?: "",
        onValueChange = {},
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
        isError = isError,
        supportingText = errorText?.let { { Text(it) } },
        readOnly = true,
        enabled = false,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            disabledLabelColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledSupportingTextColor = MaterialTheme.colorScheme.error,
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { showPicker = true },
            ),
    )

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date?.let { it.toEpochDays() * MILLIS_IN_DAY }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedDateMillis != null,
                    onClick = {
                        pickerState.selectedDateMillis?.let {
                            onDateSelected(LocalDate.fromEpochDays(it / MILLIS_IN_DAY))
                        }
                        showPicker = false
                    }
                ) { Text(stringResource(Res.string.select)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(Res.string.cancel)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private const val MILLIS_IN_DAY = 86_400_000L
