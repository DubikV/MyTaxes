package com.gmail.vanyadubik.myassistant.domen.usecase

import com.gmail.vanyadubik.myassistant.domen.model.AccountProfile
import com.gmail.vanyadubik.myassistant.domen.model.Car
import com.gmail.vanyadubik.myassistant.domen.model.Currency
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.model.TaxReminderMode
import com.gmail.vanyadubik.myassistant.domen.model.getData
import com.gmail.vanyadubik.myassistant.domen.repository.AuthRepository
import com.gmail.vanyadubik.myassistant.domen.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random
import kotlin.time.Clock

/** The account of the signed in user: the FOP group, currency, tax reminder settings and cars saved in Cloud Firestore, sign out and account deletion. */
class AccountUseCase(
    private val authUseCase: AuthUseCase,
    private val dataStoreUseCase: DataStoreUseCase,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val taxHistory: TaxHistoryUseCase,
    private val expenses: ExpenseUseCase,
    private val pushNotifications: PushNotificationUseCase,
) {
    private val _savedNickname = MutableStateFlow<String?>(null)
    private val _savedAvatarUrl = MutableStateFlow<String?>(null)
    private val _savedGroup = MutableStateFlow<FopGroup?>(null)
    private val _savedCurrency = MutableStateFlow<Currency?>(null)
    private val _savedTaxReminderMode = MutableStateFlow<TaxReminderMode?>(null)
    private val _savedCars = MutableStateFlow<List<Car>>(emptyList())

    /** The nickname saved in the account; valid for a signed in user. */
    val savedNickname: StateFlow<String?> = _savedNickname.asStateFlow()

    /** The avatar URL / Base64 saved in the account; valid for a signed in user. */
    val savedAvatarUrl: StateFlow<String?> = _savedAvatarUrl.asStateFlow()

    /** The group saved in the account (null for guests or when none is saved); valid for a signed in user. */
    val savedGroup: StateFlow<FopGroup?> = _savedGroup.asStateFlow()

    /** The currency saved in the account (null for guests or when none is saved); valid for a signed in user. */
    val savedCurrency: StateFlow<Currency?> = _savedCurrency.asStateFlow()

    /** The tax reminder mode saved in the account (null for guests or when none is saved); valid for a signed in user. */
    val savedTaxReminderMode: StateFlow<TaxReminderMode?> = _savedTaxReminderMode.asStateFlow()

    /** The cars saved in the account; valid for a signed in user. */
    val savedCars: StateFlow<List<Car>> = _savedCars.asStateFlow()

    /** Loads the settings saved in the account and applies the group as the current one. */
    suspend fun loadProfile(): ResultState<AccountProfile> {
        val result = withSession { userId, token -> profileRepository.getProfile(userId, token) }
        if (result is ResultState.Success) {
            val profile = result.data ?: AccountProfile()
            _savedNickname.value = profile.nickname
            _savedAvatarUrl.value = profile.avatarUrl
            _savedGroup.value = profile.fopGroup
            _savedCurrency.value = profile.currency
            _savedTaxReminderMode.value = profile.taxReminderMode
            _savedCars.value = profile.cars
            profile.fopGroup?.let { dataStoreUseCase.setFopGroup(it) }
            pushNotifications.register(taxReminderMode = profile.taxReminderMode)
        }
        return result
    }

    /** Saves [nickname] in the account. */
    suspend fun saveNickname(nickname: String?): ResultState<Unit> {
        val cleanNickname = nickname?.trim()?.ifBlank { null }
        val result = saveProfile(
            AccountProfile(
                nickname = cleanNickname,
                avatarUrl = _savedAvatarUrl.value,
                fopGroup = _savedGroup.value,
                currency = _savedCurrency.value,
                taxReminderMode = _savedTaxReminderMode.value,
                cars = _savedCars.value,
            )
        )
        if (result is ResultState.Success) _savedNickname.value = cleanNickname
        return result
    }

    /** Saves [avatarUrl] in the account. */
    suspend fun saveAvatarUrl(avatarUrl: String?): ResultState<Unit> {
        val result = saveProfile(
            AccountProfile(
                nickname = _savedNickname.value,
                avatarUrl = avatarUrl,
                fopGroup = _savedGroup.value,
                currency = _savedCurrency.value,
                taxReminderMode = _savedTaxReminderMode.value,
                cars = _savedCars.value,
            )
        )
        if (result is ResultState.Success) _savedAvatarUrl.value = avatarUrl
        return result
    }

    /** Saves [group] in the account, or removes the saved group when it is null. */
    suspend fun saveGroup(group: FopGroup?): ResultState<Unit> {
        val result = saveProfile(
            AccountProfile(
                nickname = _savedNickname.value,
                avatarUrl = _savedAvatarUrl.value,
                fopGroup = group,
                currency = _savedCurrency.value,
                taxReminderMode = _savedTaxReminderMode.value,
                cars = _savedCars.value,
            )
        )
        if (result is ResultState.Success) {
            _savedGroup.value = group
            if (group != null) dataStoreUseCase.setFopGroup(group)
            pushNotifications.register(taxReminderMode = _savedTaxReminderMode.value)
        }
        return result
    }

    /** Saves [currency] in the account, or removes the saved currency when it is null. */
    suspend fun saveCurrency(currency: Currency?): ResultState<Unit> {
        val result = saveProfile(
            AccountProfile(
                nickname = _savedNickname.value,
                avatarUrl = _savedAvatarUrl.value,
                fopGroup = _savedGroup.value,
                currency = currency,
                taxReminderMode = _savedTaxReminderMode.value,
                cars = _savedCars.value,
            )
        )
        if (result is ResultState.Success) _savedCurrency.value = currency
        return result
    }

    /** Saves [mode] in the account, or removes the saved tax reminder mode when it is null. */
    suspend fun saveTaxReminderMode(mode: TaxReminderMode?): ResultState<Unit> {
        val result = saveProfile(
            AccountProfile(
                nickname = _savedNickname.value,
                avatarUrl = _savedAvatarUrl.value,
                fopGroup = _savedGroup.value,
                currency = _savedCurrency.value,
                taxReminderMode = mode,
                cars = _savedCars.value,
            )
        )
        if (result is ResultState.Success) {
            _savedTaxReminderMode.value = mode
            pushNotifications.register(taxReminderMode = mode)
        }
        return result
    }

    /** Adds a car with [name] and [number] to the account profile. */
    suspend fun addCar(name: String, number: String): ResultState<Unit> {
        val carId = "${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(1000, 9999)}"
        val newCar = Car(id = carId, name = name.trim(), number = number.trim())
        val updatedCars = _savedCars.value + newCar
        val result = saveProfile(
            AccountProfile(
                nickname = _savedNickname.value,
                avatarUrl = _savedAvatarUrl.value,
                fopGroup = _savedGroup.value,
                currency = _savedCurrency.value,
                taxReminderMode = _savedTaxReminderMode.value,
                cars = updatedCars,
            )
        )
        if (result is ResultState.Success) {
            _savedCars.value = updatedCars
        }
        return result
    }

    /** Deletes the car with [carId] from the account profile. */
    suspend fun deleteCar(carId: String): ResultState<Unit> {
        val updatedCars = _savedCars.value.filterNot { it.id == carId }
        val result = saveProfile(
            AccountProfile(
                nickname = _savedNickname.value,
                avatarUrl = _savedAvatarUrl.value,
                fopGroup = _savedGroup.value,
                currency = _savedCurrency.value,
                taxReminderMode = _savedTaxReminderMode.value,
                cars = updatedCars,
            )
        )
        if (result is ResultState.Success) {
            _savedCars.value = updatedCars
        }
        return result
    }

    private suspend fun saveProfile(profile: AccountProfile): ResultState<Unit> =
        withSession { userId, token ->
            if (profile.nickname == null && profile.avatarUrl == null && profile.fopGroup == null && profile.currency == null && profile.taxReminderMode == null && profile.cars.isEmpty()) {
                profileRepository.delete(userId, token)
            } else {
                profileRepository.saveProfile(userId, token, profile)
            }
        }

    suspend fun logout() {
        pushNotifications.unregister()
        _savedNickname.value = null
        _savedAvatarUrl.value = null
        _savedGroup.value = null
        _savedCurrency.value = null
        _savedTaxReminderMode.value = null
        _savedCars.value = emptyList()
        authUseCase.logout()
    }

    /** Deletes the user's data from Cloud Firestore and then the Firebase account itself. */
    suspend fun deleteAccount(): ResultState<Unit> {
        val calculations = taxHistory.getAll()
        if (calculations is ResultState.Error) return calculations
        calculations.getData().orEmpty().forEach { item ->
            val id = item.id ?: return@forEach
            taxHistory.delete(id).let { if (it is ResultState.Error) return it }
        }
        val expenseList = expenses.getAll()
        if (expenseList is ResultState.Error) return expenseList
        expenseList.getData().orEmpty().forEach { item ->
            val id = item.id ?: return@forEach
            expenses.delete(id).let { if (it is ResultState.Error) return it }
        }
        withSession { userId, token -> profileRepository.delete(userId, token) }
            .let { if (it is ResultState.Error) return it }
        pushNotifications.unregister()
        val deleted = withSession { _, token -> authRepository.deleteAccount(token) }
        if (deleted is ResultState.Success) {
            _savedGroup.value = null
            _savedCurrency.value = null
            _savedTaxReminderMode.value = null
            _savedCars.value = emptyList()
            authUseCase.logout()
        }
        return deleted
    }

    private suspend fun <T> withSession(block: suspend (String, String) -> ResultState<T>): ResultState<T> =
        when (val session = authUseCase.requireSession()) {
            is ResultState.Success -> session.data!!.let { (userId, token) -> block(userId, token) }
            is ResultState.Error -> ResultState.Error(title = session.title, message = session.message)
            else -> ResultState.Error(message = "You are not signed in")
        }
}
