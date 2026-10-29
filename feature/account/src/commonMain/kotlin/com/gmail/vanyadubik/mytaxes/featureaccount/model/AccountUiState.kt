package com.gmail.vanyadubik.myassistant.featureaccount.model

import com.gmail.vanyadubik.myassistant.domen.model.Car
import com.gmail.vanyadubik.myassistant.domen.model.Currency
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.TaxReminderMode

data class AccountUiState(
    val email: String = "",
    val nickname: String? = null,
    val nicknameInput: String = "",
    val avatarUrl: String? = null,
    val showEditNicknameDialog: Boolean = false,
    val savedGroup: FopGroup? = null,
    val savedCurrency: Currency? = null,
    val savedTaxReminderMode: TaxReminderMode? = null,
    val cars: List<Car> = emptyList(),
    val carNameInput: String = "",
    val carNumberInput: String = "",
    val carInputError: String? = null,
    val showAddCarSheet: Boolean = false,
    val isCarLoading: Boolean = false,
    val isGroupLoading: Boolean = true,
    val showDeleteConfirm: Boolean = false,
    val isDeleting: Boolean = false,
    val loadError: String? = null,
    val actionError: String? = null,
)
