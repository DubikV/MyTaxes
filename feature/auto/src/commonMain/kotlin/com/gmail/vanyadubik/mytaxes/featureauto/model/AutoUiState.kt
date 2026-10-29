package com.gmail.vanyadubik.myassistant.featureauto.model

import com.gmail.vanyadubik.myassistant.domen.model.AutoServiceRecord
import com.gmail.vanyadubik.myassistant.domen.model.Car
import kotlinx.datetime.LocalDate

data class AutoUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val isSavingCar: Boolean = false,
    val isSavingRecord: Boolean = false,
    val cars: List<Car> = emptyList(),
    val selectedCarId: String? = null,
    val records: List<AutoServiceRecord> = emptyList(),
    val showAddCarSheet: Boolean = false,
    val newCarNameInput: String = "",
    val newCarNumberInput: String = "",
    val newCarInputError: String? = null,
    val showAddRecordSheet: Boolean = false,
    val editingRecordId: String? = null,
    val inputDate: LocalDate? = null,
    val inputOdometer: String = "",
    val inputDescription: String = "",
    val inputPhotos: List<String> = emptyList(),
    val inputError: String? = null,
    val recordToDeleteId: String? = null,
    val loadError: String? = null,
    val actionError: String? = null,
)
