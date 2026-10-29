package com.gmail.vanyadubik.myassistant.commonui.ui.components.textfield

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.gmail.vanyadubik.myassistant.common.utils.DateTimeFormat
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.cancel
import com.gmail.vanyadubik.myassistant.resource.select
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldSelectDate(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    valueStyle: TextStyle = MaterialTheme.typography.labelLarge,
    labelStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    placeholder: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label ?: "", style = labelStyle) },
        singleLine = true,
        textStyle = valueStyle,
        colors = colors.copy(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledIndicatorColor = MaterialTheme.colorScheme.outline,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant),
        readOnly = true,
        enabled = false,
        placeholder = placeholder,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    showDatePicker = true
                }
            ),
        trailingIcon = {
            AnimatedVisibility(
                visible = value.isNotBlank(),
                enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth / 2 }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth / 2 }) + fadeOut()
            ) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValueChange(
                            DateTimeFormat.format(
                                time = datePickerState.selectedDateMillis ?: 0,
                                pattern = "yyyy-MM-dd HH:mm",
                                timeZone = "GMT"
                            )
                        )
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text(stringResource(Res.string.select))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis = null
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}