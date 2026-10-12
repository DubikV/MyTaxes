package com.gmail.vanyadubik.mytaxes.domen.model

enum class Currency(val code: String, val title: String) {
    UAH("UAH", "Ukrainian hryvnia"),
    USD("USD", "US dollar"),
    EUR("EUR", "Euro"),
    GBP("GBP", "Pound sterling"),
    PLN("PLN", "Polish zloty"),
    CHF("CHF", "Swiss franc"),
    CAD("CAD", "Canadian dollar");

    companion object {
        fun fromCode(code: String) = entries.firstOrNull { it.code == code } ?: UAH
    }
}
