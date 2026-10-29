package com.gmail.vanyadubik.myassistant.domen.usecase

import com.gmail.vanyadubik.myassistant.domen.model.Currency
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.IncomeInput
import com.gmail.vanyadubik.myassistant.domen.model.IncomeItem
import com.gmail.vanyadubik.myassistant.domen.model.PreferenceKey
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.model.TaxCalculation
import com.gmail.vanyadubik.myassistant.domen.model.TaxIncomeItem
import com.gmail.vanyadubik.myassistant.domen.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.time.Clock

class TaxesUseCase(
    private val dataStoreRepository: DataStoreRepository,
    private val calculateTaxUseCase: CalculateTaxUseCase,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getIncomes(): Flow<List<TaxIncomeItem>> =
        dataStoreRepository.getPreference(PreferenceKey.TaxIncomesKey)
            .map { rawJson ->
                if (rawJson.isNullOrBlank()) emptyList()
                else runCatching { json.decodeFromString<List<TaxIncomeItem>>(rawJson) }.getOrDefault(emptyList())
            }
            .map { list ->
                val needUpdate = list.filter { !it.isTaxCalculated && it.amountUah == 0.0 }
                if (needUpdate.isEmpty()) {
                    list
                } else {
                    val updatedList = list.toMutableList()
                    var changed = false
                    for (item in needUpdate) {
                        if (item.currency == Currency.UAH) {
                            val index = updatedList.indexOfFirst { it.id == item.id }
                            if (index != -1) {
                                updatedList[index] = item.copy(rate = 1.0, amountUah = item.amount)
                                changed = true
                            }
                        } else {
                            val result = calculateTaxUseCase(listOf(IncomeInput(item.date, item.amount, item.currency)))
                            if (result is ResultState.Success) {
                                val incomeItem = result.data?.incomes?.firstOrNull()
                                if (incomeItem != null && incomeItem.amountUah > 0.0) {
                                    val index = updatedList.indexOfFirst { it.id == item.id }
                                    if (index != -1) {
                                        updatedList[index] = item.copy(rate = incomeItem.rate, amountUah = incomeItem.amountUah)
                                        changed = true
                                    }
                                }
                            }
                        }
                    }
                    if (changed) {
                        saveIncomes(updatedList)
                    }
                    updatedList
                }
            }

    suspend fun addIncome(date: LocalDate, amount: Double, currency: Currency) {
        val current = getIncomesList()
        val (rate, amountUah) = if (currency == Currency.UAH) {
            1.0 to amount
        } else {
            when (val rateResult = calculateTaxUseCase(listOf(IncomeInput(date, amount, currency)))) {
                is ResultState.Success -> {
                    val incomeItem = rateResult.data?.incomes?.firstOrNull()
                    (incomeItem?.rate ?: 0.0) to (incomeItem?.amountUah ?: 0.0)
                }
                else -> 0.0 to 0.0
            }
        }
        val newItem = TaxIncomeItem(
            id = "${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(1000, 9999)}",
            dateString = date.toString(),
            amount = amount,
            currencyCode = currency.code,
            isTaxCalculated = false,
            rate = rate,
            amountUah = amountUah,
        )
        saveIncomes(current + newItem)
    }

    suspend fun deleteIncome(id: String) {
        val current = getIncomesList()
        saveIncomes(current.filterNot { it.id == id })
    }

    suspend fun calculateTaxesForUnpaid(group: FopGroup, selectedIds: Set<String> = emptySet()): ResultState<TaxCalculation?> {
        val current = getIncomesList()
        val untaxed = current.filterNot { it.isTaxCalculated }
            .let { list -> if (selectedIds.isNotEmpty()) list.filter { it.id in selectedIds } else list }
        if (untaxed.isEmpty()) return ResultState.Success(null)

        val inputs = untaxed.map { IncomeInput(it.date, it.amount, it.currency) }
        val calcResult = calculateTaxUseCase(inputs, group)
        if (calcResult !is ResultState.Success) {
            return ResultState.Error((calcResult as? ResultState.Error)?.message ?: "Failed to calculate tax")
        }

        val calculation = calcResult.data!!
        val updatedList = current.toMutableList()
        val now = Clock.System.now().toEpochMilliseconds()

        calculation.incomes.forEachIndexed { idx, incomeItem ->
            val origItem = untaxed.getOrNull(idx) ?: return@forEachIndexed
            val index = updatedList.indexOfFirst { it.id == origItem.id }
            if (index != -1) {
                val singleCalc = calculateTaxUseCase.calculate(listOf(incomeItem), group)
                updatedList[index] = origItem.copy(
                    isTaxCalculated = true,
                    rate = incomeItem.rate,
                    amountUah = incomeItem.amountUah,
                    singleTax = singleCalc.singleTax,
                    militaryLevy = singleCalc.militaryLevy,
                    esv = singleCalc.esv,
                    totalTax = singleCalc.totalTax,
                    groupId = group.id,
                    calculatedAt = now,
                )
            }
        }

        saveIncomes(updatedList)
        return ResultState.Success(calculation)
    }

    fun calculatePaymentTotal(selectedIds: Set<String>, incomes: List<TaxIncomeItem>): Double {
        val itemsToPay = if (selectedIds.isNotEmpty()) {
            incomes.filter { it.id in selectedIds && it.isTaxCalculated }
        } else {
            incomes.filter { it.isTaxCalculated }
        }
        return itemsToPay.sumOf { it.totalTax }
    }

    fun getPaymentCalculation(selectedIds: Set<String>, incomes: List<TaxIncomeItem>, group: FopGroup): TaxCalculation? {
        val itemsToPay = if (selectedIds.isNotEmpty()) {
            incomes.filter { it.id in selectedIds && it.isTaxCalculated }
        } else {
            incomes.filter { it.isTaxCalculated }
        }
        if (itemsToPay.isEmpty()) return null

        val incomeItems = itemsToPay.map {
            IncomeItem(
                date = it.date,
                amount = it.amount,
                currency = it.currency,
                rate = it.rate,
                amountUah = it.amountUah,
            )
        }

        val amountUah = itemsToPay.sumOf { it.amountUah }.round2()
        val singleTax = itemsToPay.sumOf { it.singleTax }.round2()
        val militaryLevy = itemsToPay.sumOf { it.militaryLevy }.round2()
        val esv = itemsToPay.sumOf { it.esv }.round2()
        val totalTax = itemsToPay.sumOf { it.totalTax }.round2()

        return TaxCalculation(
            incomes = incomeItems,
            amountUah = amountUah,
            singleTax = singleTax,
            militaryLevy = militaryLevy,
            esv = esv,
            totalTax = totalTax,
            group = group,
        )
    }

    private suspend fun getIncomesList(): List<TaxIncomeItem> {
        val rawJson = dataStoreRepository.getPreference(PreferenceKey.TaxIncomesKey).firstOrNull()
        if (rawJson.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<TaxIncomeItem>>(rawJson) }.getOrDefault(emptyList())
    }

    private suspend fun saveIncomes(incomes: List<TaxIncomeItem>) {
        val rawJson = json.encodeToString(incomes)
        dataStoreRepository.setPreference(PreferenceKey.TaxIncomesKey, rawJson)
    }
}
