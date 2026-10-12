package com.gmail.vanyadubik.mytaxes.commonui.language

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gmail.vanyadubik.mytaxes.domen.model.AppLanguage
import com.gmail.vanyadubik.mytaxes.resource.Res
import com.gmail.vanyadubik.mytaxes.resource.language
import com.gmail.vanyadubik.mytaxes.resource.language_system
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun LanguageSwitchButton(handler: LanguageHandler = koinInject()) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.Language, contentDescription = stringResource(Res.string.language))
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        AppLanguage.entries.forEach { item ->
            DropdownMenuItem(
                text = { Text(item.nativeName ?: stringResource(Res.string.language_system)) },
                trailingIcon = {
                    if (item == handler.language) Icon(Icons.Default.Check, contentDescription = null)
                },
                onClick = {
                    expanded = false
                    handler.changeLanguage(item)
                },
            )
        }
    }
}
