package com.gmail.vanyadubik.myassistant.featurehome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.MonthlyStats
import com.gmail.vanyadubik.myassistant.domen.model.TaxDeadlines
import com.gmail.vanyadubik.myassistant.domen.model.TaxReminderMode
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.model.TaxIncomeItem
import com.gmail.vanyadubik.myassistant.domen.usecase.AccountUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.AuthUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.DataStoreUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.PushMessagesUseCase
import com.gmail.vanyadubik.myassistant.domen.usecase.TaxesUseCase
import com.gmail.vanyadubik.myassistant.featurehome.model.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

private const val MONTHS = 6

class HomeViewModel(
    private val authUseCase: AuthUseCase,
    private val taxesUseCase: TaxesUseCase,
    private val accountUseCase: AccountUseCase,
    private val dataStoreUseCase: DataStoreUseCase,
    private val pushMessagesUseCase: PushMessagesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var fopGroup = FopGroup.GROUP_3_5
    private var reminderMode = TaxReminderMode.AS_NEEDED

    private fun updateDeadline() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val oldest = _state.value.untaxedIncomes.minOfOrNull { it.date }
        val deadline = TaxDeadlines.forUntaxed(today, fopGroup, reminderMode, oldest)
        _state.update { it.copy(daysToDeadline = (deadline.toEpochDays() - today.toEpochDays()).toInt()) }
    }

    private var lastAction: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            authUseCase.isLoggedIn.collect { loggedIn ->
                _state.update { it.copy(isLoggedIn = loggedIn) }
                if (loggedIn) {
                    accountUseCase.loadProfile()
                }
            }
        }

        viewModelScope.launch {
            taxesUseCase.getIncomes().collect { incomes ->
                val untaxed = incomes.filterNot { it.isTaxCalculated }
                val unpaid = incomes.filter { it.isTaxCalculated && !it.isPaid }
                _state.update {
                    it.copy(
                        untaxedIncomes = untaxed,
                        unpaidTaxes = unpaid,
                        monthlyStats = monthlyStats(incomes),
                        calculatedTaxes = incomes.filter { item -> item.isTaxCalculated },
                    )
                }
                updateDeadline()
            }
        }

        viewModelScope.launch {
            dataStoreUseCase.fopGroup.collect { group ->
                fopGroup = group
                updateDeadline()
            }
        }

        viewModelScope.launch {
            accountUseCase.savedTaxReminderMode.collect { mode ->
                reminderMode = mode ?: TaxReminderMode.AS_NEEDED
                updateDeadline()
            }
        }

        viewModelScope.launch {
            accountUseCase.savedCars.collect { cars ->
                _state.update { it.copy(cars = cars) }
            }
        }
    }

    init {
        viewModelScope.launch {
            pushMessagesUseCase.messages.collect { messages ->
                _state.update {
                    it.copy(
                        hasPushMessages = messages.isNotEmpty(),
                        hasUnreadPushMessages = messages.any { message -> !message.isRead },
                    )
                }
            }
        }
    }

    /** Income and calculated taxes of the last [MONTHS] months (oldest first), by the date of the income. */
    private fun monthlyStats(incomes: List<TaxIncomeItem>, months: Int = MONTHS): List<MonthlyStats> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val endIndex = today.year * 12 + today.monthNumber - 1
        return List(months) { i ->
            val position = endIndex - months + 1 + i
            val year = position / 12
            val month = position % 12 + 1
            val inMonth = incomes.filter { it.date.year == year && it.date.monthNumber == month }
            MonthlyStats(
                year = year,
                month = month,
                income = inMonth.sumOf { if (it.amountUah > 0.0) it.amountUah else it.amount },
                tax = inMonth.filter { it.isTaxCalculated }.sumOf { it.totalTax },
                expenses = 0.0,
            )
        }
    }

    fun load() {
        if (!_state.value.isLoggedIn) return
        _state.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            val result = accountUseCase.loadProfile()
            _state.update {
                it.copy(
                    isLoading = false,
                    loadError = if (result is ResultState.Error) result.message else null,
                )
            }
        }
    }

    fun retryLastAction() {
        dismissActionError()
        lastAction?.invoke()
    }

    fun dismissActionError() {
        _state.update { it.copy(actionError = null) }
    }
}
