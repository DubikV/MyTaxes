package com.gmail.vanyadubik.myassistant.commonui.language

import com.gmail.vanyadubik.myassistant.domen.model.AppLanguage
import platform.Foundation.NSUserDefaults

actual fun applyAppLanguage(language: AppLanguage) {
    val defaults = NSUserDefaults.standardUserDefaults
    val code = language.code
    if (code == null) {
        defaults.removeObjectForKey("AppleLanguages")
    } else {
        defaults.setObject(listOf(code), forKey = "AppleLanguages")
    }
}
