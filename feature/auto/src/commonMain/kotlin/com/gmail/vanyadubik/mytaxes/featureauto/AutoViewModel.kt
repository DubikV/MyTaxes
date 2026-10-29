package com.gmail.vanyadubik.myassistant.featureauto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.myassistant.domen.model.AutoServiceRecord
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.usecase.AccountUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.AuthUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.AutoServiceUseCase
import com.gmail.vanyadubik.myassistant.featureauto.model.AutoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class AutoViewModel(
    private val authUseCase: AuthUseCase,
    private val accountUseCase: AccountUseCase,
    private val autoServiceUseCase: AutoServiceUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AutoUiState())
    val state: StateFlow<AutoUiState> = _state.asStateFlow()

    private var lastAction: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            authUseCase.isLoggedIn.collect { loggedIn ->
                _state.update { it.copy(isLoggedIn = loggedIn) }
                if (loggedIn) {
                    accountUseCase.loadProfile()
                    loadRecords()
                } else {
                    _state.update { it.copy(records = emptyList()) }
                }
            }
        }

        viewModelScope.launch {
            accountUseCase.savedCars.collect { cars ->
                _state.update { s ->
                    val currentSelected = s.selectedCarId
                    val newSelected = if (currentSelected != null && cars.any { it.id == currentSelected }) {
                        currentSelected
                    } else {
                        cars.firstOrNull()?.id
                    }
                    s.copy(cars = cars, selectedCarId = newSelected)
                }
                loadRecords()
            }
        }
    }

    fun onCarSelected(carId: String) {
        if (_state.value.selectedCarId == carId) return
        _state.update { it.copy(selectedCarId = carId) }
        loadRecords()
    }

    fun loadRecords() {
        val carId = _state.value.selectedCarId ?: return
        if (!_state.value.isLoggedIn) return
        _state.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            when (val result = autoServiceUseCase.getRecords(carId)) {
                is ResultState.Success -> _state.update {
                    it.copy(isLoading = false, records = result.data.orEmpty(), loadError = null)
                }
                is ResultState.Error -> _state.update {
                    it.copy(isLoading = false, loadError = result.message)
                }
                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showAddCarSheet() {
        _state.update {
            it.copy(
                showAddCarSheet = true,
                newCarNameInput = "",
                newCarNumberInput = "",
                newCarInputError = null,
            )
        }
    }

    fun hideAddCarSheet() {
        _state.update {
            it.copy(
                showAddCarSheet = false,
                newCarNameInput = "",
                newCarNumberInput = "",
                newCarInputError = null,
            )
        }
    }

    fun onNewCarNameChanged(value: String) {
        _state.update { it.copy(newCarNameInput = value, newCarInputError = null) }
    }

    fun onNewCarNumberChanged(value: String) {
        _state.update { it.copy(newCarNumberInput = value.uppercase(), newCarInputError = null) }
    }

    fun addCar() {
        val name = _state.value.newCarNameInput.trim()
        val number = _state.value.newCarNumberInput.trim()
        if (name.isEmpty() || number.isEmpty()) {
            _state.update { it.copy(newCarInputError = "empty") }
            return
        }
        lastAction = { addCar() }
        _state.update { it.copy(isSavingCar = true, newCarInputError = null, actionError = null) }
        viewModelScope.launch {
            val result = accountUseCase.addCar(name, number)
            if (result is ResultState.Error) {
                _state.update { it.copy(isSavingCar = false, actionError = result.message) }
            } else {
                val updatedCars = accountUseCase.savedCars.value
                val newlyAddedId = updatedCars.lastOrNull()?.id
                _state.update {
                    it.copy(
                        isSavingCar = false,
                        showAddCarSheet = false,
                        newCarNameInput = "",
                        newCarNumberInput = "",
                        cars = updatedCars,
                        selectedCarId = newlyAddedId ?: it.selectedCarId,
                    )
                }
                loadRecords()
            }
        }
    }

    fun showAddRecordSheet() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        _state.update {
            it.copy(
                showAddRecordSheet = true,
                editingRecordId = null,
                inputDate = today,
                inputOdometer = "",
                inputDescription = "",
                inputPhotos = emptyList(),
                inputError = null,
            )
        }
    }

    fun showEditRecordSheet(record: AutoServiceRecord) {
        _state.update {
            it.copy(
                showAddRecordSheet = true,
                editingRecordId = record.id,
                inputDate = record.date,
                inputOdometer = record.odometer.toString(),
                inputDescription = record.description,
                inputPhotos = record.photos,
                inputError = null,
            )
        }
    }

    fun hideAddRecordSheet() {
        _state.update {
            it.copy(
                showAddRecordSheet = false,
                editingRecordId = null,
                inputDate = null,
                inputOdometer = "",
                inputDescription = "",
                inputPhotos = emptyList(),
                inputError = null,
            )
        }
    }

    fun onDateSelected(date: LocalDate) {
        _state.update { it.copy(inputDate = date, inputError = null) }
    }

    fun onOdometerChanged(value: String) {
        val digits = value.filter { it.isDigit() }
        _state.update { it.copy(inputOdometer = digits, inputError = null) }
    }

    fun onDescriptionChanged(value: String) {
        _state.update { it.copy(inputDescription = value, inputError = null) }
    }

    fun onPhotoAdded(photoBase64: String) {
        val currentPhotos = _state.value.inputPhotos
        if (currentPhotos.size >= 3) return
        _state.update { it.copy(inputPhotos = currentPhotos + photoBase64) }
    }

    fun onPhotoRemoved(index: Int) {
        val currentPhotos = _state.value.inputPhotos.toMutableList()
        if (index in currentPhotos.indices) {
            currentPhotos.removeAt(index)
            _state.update { it.copy(inputPhotos = currentPhotos) }
        }
    }

    fun addRecord() {
        val current = _state.value
        val carId = current.selectedCarId ?: return
        val date = current.inputDate
        val odometer = current.inputOdometer.toIntOrNull()
        val description = current.inputDescription.trim()

        if (date == null || odometer == null || odometer <= 0 || description.isEmpty()) {
            _state.update { it.copy(inputError = "empty") }
            return
        }

        lastAction = { addRecord() }
        _state.update { it.copy(isSavingRecord = true, inputError = null, actionError = null) }
        viewModelScope.launch {
            val recordToSave = AutoServiceRecord(
                id = current.editingRecordId.orEmpty(),
                carId = carId,
                date = date,
                odometer = odometer,
                description = description,
                photos = current.inputPhotos,
            )
            val result = autoServiceUseCase.saveRecord(recordToSave)
            if (result is ResultState.Error) {
                _state.update { it.copy(isSavingRecord = false, actionError = result.message) }
            } else {
                _state.update {
                    it.copy(
                        isSavingRecord = false,
                        showAddRecordSheet = false,
                        editingRecordId = null,
                    )
                }
                loadRecords()
            }
        }
    }

    fun confirmDeleteRecord(recordId: String) {
        _state.update { it.copy(recordToDeleteId = recordId) }
    }

    fun hideDeleteRecordConfirm() {
        _state.update { it.copy(recordToDeleteId = null) }
    }

    fun deleteRecord() {
        val id = _state.value.recordToDeleteId ?: return
        lastAction = { deleteRecord() }
        _state.update { it.copy(recordToDeleteId = null, isLoading = true, actionError = null) }
        viewModelScope.launch {
            val result = autoServiceUseCase.deleteRecord(id)
            if (result is ResultState.Error) {
                _state.update { it.copy(isLoading = false, actionError = result.message) }
            } else {
                loadRecords()
            }
        }
    }

    fun retryLastAction() {
        dismissActionError()
        lastAction?.invoke()
    }

    fun dismissActionError() = _state.update { it.copy(actionError = null) }
}
