package com.gmail.vanyadubik.myassistant.domen.model

enum class TaxReminderMode(val id: String) {
    AS_NEEDED("as_needed"),
    MONTHLY("monthly");

    companion object {
        fun fromId(id: String?) = entries.firstOrNull { it.id == id } ?: AS_NEEDED
    }
}
