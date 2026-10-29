package com.gmail.vanyadubik.myassistant.commonui.language

import com.gmail.vanyadubik.myassistant.domen.model.AppLanguage
import java.util.Locale

// Captured on the first call, before the locale is overridden
private val systemLocale: Locale = Locale.getDefault()

actual fun applyAppLanguage(language: AppLanguage) {
    Locale.setDefault(language.code?.let { Locale.forLanguageTag(it) } ?: systemLocale)
}
