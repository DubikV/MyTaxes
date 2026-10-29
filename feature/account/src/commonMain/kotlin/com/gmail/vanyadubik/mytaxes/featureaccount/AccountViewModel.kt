package com.gmail.vanyadubik.myassistant.featureaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.myassistant.domen.model.Currency
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.model.TaxReminderMode
import com.gmail.vanyadubik.myassistant.domen.model.getData
import com.gmail.vanyadubik.myassistant.domen.usecase.AccountUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.AuthUseCase
import com.gmail.vanyadubik.myassistant.featureaccount.model.AccountUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(
    private val account: AccountUseCase,
    authUseCase: AuthUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    private var lastAction: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            authUseCase.userEmail.collect { email -> _state.update { it.copy(email = email.orEmpty()) } }
        }
        viewModelScope.launch {
            account.savedNickname.collect { nickname ->
                _state.update { it.copy(nickname = nickname, nicknameInput = nickname.orEmpty()) }
            }
        }
        viewModelScope.launch {
            account.savedAvatarUrl.collect { avatarUrl ->
                _state.update { it.copy(avatarUrl = avatarUrl) }
            }
        }
        load()
    }

    fun load() {
        _state.update { it.copy(isGroupLoading = true, loadError = null) }
        viewModelScope.launch {
            when (val result = account.loadProfile()) {
                is ResultState.Error -> _state.update { it.copy(isGroupLoading = false, loadError = result.message) }
                else -> {
                    val p = result.getData()
                    _state.update {
                        it.copy(
                            isGroupLoading = false,
                            loadError = null,
                            nickname = p?.nickname,
                            nicknameInput = p?.nickname.orEmpty(),
                            avatarUrl = p?.avatarUrl,
                            savedGroup = p?.fopGroup,
                            savedCurrency = p?.currency,
                            savedTaxReminderMode = p?.taxReminderMode,
                            cars = p?.cars.orEmpty(),
                        )
                    }
                }
            }
        }
    }

    fun showEditNicknameDialog() {
        _state.update {
            it.copy(
                showEditNicknameDialog = true,
                nicknameInput = it.nickname.orEmpty(),
            )
        }
    }

    fun hideEditNicknameDialog() {
        _state.update { it.copy(showEditNicknameDialog = false) }
    }

    fun onNicknameInputChanged(value: String) {
        _state.update { it.copy(nicknameInput = value) }
    }

    fun saveNickname() {
        val input = _state.value.nicknameInput.trim()
        lastAction = { saveNickname() }
        _state.update { it.copy(actionError = null) }
        viewModelScope.launch {
            val result = account.saveNickname(input)
            if (result is ResultState.Error) {
                _state.update { it.copy(actionError = result.message) }
            }
        }
    }

    fun onAvatarPicked(photoBase64: String) {
        lastAction = { onAvatarPicked(photoBase64) }
        _state.update { it.copy(actionError = null) }
        viewModelScope.launch {
            val result = account.saveAvatarUrl(photoBase64)
            if (result is ResultState.Error) {
                _state.update { it.copy(actionError = result.message) }
            }
        }
    }

    fun onGroupClicked(group: FopGroup) {
        val previous = _state.value.savedGroup
        val new = if (previous == group) null else group
        lastAction = { onGroupClicked(group) }
        _state.update { it.copy(savedGroup = new, actionError = null) }
        viewModelScope.launch {
            val result = account.saveGroup(new)
            if (result is ResultState.Error) {
                _state.update { it.copy(savedGroup = previous, actionError = result.message) }
            }
        }
    }

    fun onCurrencyClicked(currency: Currency) {
        val previous = _state.value.savedCurrency
        val new = if (previous == currency) null else currency
        lastAction = { onCurrencyClicked(currency) }
        _state.update { it.copy(savedCurrency = new, actionError = null) }
        viewModelScope.launch {
            val result = account.saveCurrency(new)
            if (result is ResultState.Error) {
                _state.update { it.copy(savedCurrency = previous, actionError = result.message) }
            }
        }
    }

    fun onTaxReminderModeClicked(mode: TaxReminderMode) {
        val previous = _state.value.savedTaxReminderMode
        val new = if (previous == mode) null else mode
        lastAction = { onTaxReminderModeClicked(mode) }
        _state.update { it.copy(savedTaxReminderMode = new, actionError = null) }
        viewModelScope.launch {
            val result = account.saveTaxReminderMode(new)
            if (result is ResultState.Error) {
                _state.update { it.copy(savedTaxReminderMode = previous, actionError = result.message) }
            }
        }
    }

    fun showAddCarSheet() {
        _state.update { it.copy(showAddCarSheet = true, carNameInput = "", carNumberInput = "", carInputError = null) }
    }

    fun hideAddCarSheet() {
        _state.update { it.copy(showAddCarSheet = false, carNameInput = "", carNumberInput = "", carInputError = null) }
    }

    fun onCarNameChanged(value: String) {
        _state.update { it.copy(carNameInput = value, carInputError = null) }
    }

    fun onCarNumberChanged(value: String) {
        _state.update { it.copy(carNumberInput = value.uppercase(), carInputError = null) }
    }

    fun addCar() {
        val name = _state.value.carNameInput.trim()
        val number = _state.value.carNumberInput.trim()
        if (name.isEmpty() || number.isEmpty()) {
            _state.update { it.copy(carInputError = "empty") }
            return
        }
        lastAction = { addCar() }
        _state.update { it.copy(isCarLoading = true, carInputError = null, actionError = null) }
        viewModelScope.launch {
            val result = account.addCar(name, number)
            if (result is ResultState.Error) {
                _state.update { it.copy(isCarLoading = false, actionError = result.message) }
            } else {
                _state.update {
                    it.copy(
                        isCarLoading = false,
                        showAddCarSheet = false,
                        carNameInput = "",
                        carNumberInput = "",
                        cars = account.savedCars.value,
                    )
                }
            }
        }
    }

    fun deleteCar(carId: String) {
        lastAction = { deleteCar(carId) }
        _state.update { it.copy(actionError = null) }
        viewModelScope.launch {
            val result = account.deleteCar(carId)
            if (result is ResultState.Error) {
                _state.update { it.copy(actionError = result.message) }
            } else {
                _state.update { it.copy(cars = account.savedCars.value) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch { account.logout() }
    }

    fun showDeleteConfirm() = _state.update { it.copy(showDeleteConfirm = true) }

    fun hideDeleteConfirm() = _state.update { it.copy(showDeleteConfirm = false) }

    fun deleteAccount() {
        if (_state.value.isDeleting) return
        lastAction = { deleteAccount() }
        _state.update { it.copy(showDeleteConfirm = false, isDeleting = true, actionError = null) }
        viewModelScope.launch {
            val result = account.deleteAccount()
            _state.update {
                it.copy(isDeleting = false, actionError = (result as? ResultState.Error)?.message)
            }
        }
    }

    fun retryLastAction() {
        dismissActionError()
        lastAction?.invoke()
    }

    fun dismissActionError() = _state.update { it.copy(actionError = null) }
}
