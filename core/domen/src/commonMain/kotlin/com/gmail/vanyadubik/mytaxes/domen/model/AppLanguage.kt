package com.gmail.vanyadubik.mytaxes.domen.model

/** [code] is null for the system language. */
enum class AppLanguage(val id: String, val code: String?, val nativeName: String?) {
    System("system", null, null),
    English("en", "en", "English"),
    Ukrainian("uk", "uk", "Українська");

    companion object {
        fun fromId(id: String?) = entries.firstOrNull { it.id == id } ?: System
    }
}
