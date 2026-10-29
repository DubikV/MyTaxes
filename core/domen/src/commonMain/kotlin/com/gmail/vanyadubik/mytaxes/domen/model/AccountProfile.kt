package com.gmail.vanyadubik.myassistant.domen.model

/** The settings saved in the user's account; null means the setting is not saved. */
data class AccountProfile(
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val fopGroup: FopGroup? = null,
    val currency: Currency? = null,
    val taxReminderMode: TaxReminderMode? = null,
    val cars: List<Car> = emptyList(),
)
