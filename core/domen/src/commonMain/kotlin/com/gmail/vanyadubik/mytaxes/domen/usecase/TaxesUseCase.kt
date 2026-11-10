package com.gmail.vanyadubik.myassistant.domen.usecase

import com.gmail.vanyadubik.myassistant.domen.model.Currency
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.IncomeInput
import com.gmail.vanyadubik.myassistant.domen.model.IncomeItem
import com.gmail.vanyadubik.myassistant.domen.model.PreferenceKey
import com.gmail.vanyadubik.myassistant.domen.model.ResultState
import com.gmail.vanyadubik.myassistant.domen.model.TaxCalculation
import com.gmail.vanyadubik.myassistant.domen.model.TaxIncomeItem
import com.gmail.vanyadubik.myassistant.domen.model.TaxType
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
            .map { list -> list.withMonthlyTaxesChargedOnce().also { fixed -> if (fixed != list) saveIncomes(fixed) } }
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
        // Sorted like CalculateTaxUseCase sorts its incomes, so that results match items by index
        val untaxed = current.filterNot { it.isTaxCalculated }
            .let { list -> if (selectedIds.isNotEmpty()) list.filter { it.id in selectedIds } else list }
            .sortedBy { it.date }
        if (untaxed.isEmpty()) return ResultState.Success(null)

        val inputs = untaxed.map { IncomeInput(it.date, it.amount, it.currency) }
        val calcResult = calculateTaxUseCase(inputs, group)
        if (calcResult !is ResultState.Success) {
            return ResultState.Error((calcResult as? ResultState.Error)?.message ?: "Failed to calculate tax")
        }

        val calculation = calcResult.data!!
        val updatedList = current.toMutableList()
        val now = Clock.System.now().toEpochMilliseconds()

        // ESV (and the fixed tax of groups 1 and 2) is charged once per month, not once per income
        val calculated = current.filter { it.isTaxCalculated }
        val esvMonths = calculated.filter { it.esv > 0.0 }.map { it.monthKey() }.toMutableSet()
        val fixedMonths = calculated.filter { it.group.isFixed && it.singleTax > 0.0 }.map { it.monthKey() }.toMutableSet()
        val calculatedItems = mutableListOf<TaxIncomeItem>()

        calculation.incomes.forEachIndexed { idx, incomeItem ->
            val origItem = untaxed.getOrNull(idx) ?: return@forEachIndexed
            val index = updatedList.indexOfFirst { it.id == origItem.id }
            if (index != -1) {
                val month = incomeItem.date.year to incomeItem.date.monthNumber
                val chargeFixed = group.isFixed && fixedMonths.add(month)
                val singleTax = when {
                    !group.isFixed -> (incomeItem.amountUah * group.singleTaxPercent / 100).round2()
                    chargeFixed -> group.fixedSingleTax
                    else -> 0.0
                }
                val militaryLevy = when {
                    !group.isFixed -> (incomeItem.amountUah * group.militaryLevyPercent / 100).round2()
                    chargeFixed -> group.fixedMilitaryLevy
                    else -> 0.0
                }
                val esv = if (esvMonths.add(month)) group.esv else 0.0
                val item = origItem.copy(
                    isTaxCalculated = true,
                    rate = incomeItem.rate,
                    amountUah = incomeItem.amountUah,
                    singleTax = singleTax,
                    militaryLevy = militaryLevy,
                    esv = esv,
                    totalTax = (singleTax + militaryLevy + esv).round2(),
                    groupId = group.id,
                    calculatedAt = now,
                )
                updatedList[index] = item
                calculatedItems += item
            }
        }

        saveIncomes(updatedList)
        return ResultState.Success(
            calculation.copy(
                singleTax = calculatedItems.sumOf { it.singleTax }.round2(),
                militaryLevy = calculatedItems.sumOf { it.militaryLevy }.round2(),
                esv = calculatedItems.sumOf { it.esv }.round2(),
                totalTax = calculatedItems.sumOf { it.totalTax }.round2(),
            )
        )
    }

    /**
     * Older versions charged ESV (and the fixed tax of groups 1 and 2) for every income instead of once per month;
     * keeps the charge on the first calculated income of each month only.
     */
    private fun List<TaxIncomeItem>.withMonthlyTaxesChargedOnce(): List<TaxIncomeItem> {
        val esvMonths = mutableSetOf<Pair<Int, Int>>()
        val fixedMonths = mutableSetOf<Pair<Int, Int>>()
        val fixes = filter { it.isTaxCalculated }.sortedWith(compareBy({ it.date }, { it.calculatedAt }, { it.id }))
            .mapNotNull { item ->
                val month = item.monthKey()
                val keepEsv = item.esv <= 0.0 || esvMonths.add(month)
                val keepFixed = !item.group.isFixed || item.singleTax <= 0.0 || fixedMonths.add(month)
                if (keepEsv && keepFixed) return@mapNotNull null
                val esv = if (keepEsv) item.esv else 0.0
                val singleTax = if (keepFixed) item.singleTax else 0.0
                val militaryLevy = if (keepFixed) item.militaryLevy else 0.0
                item.copy(
                    esv = esv,
                    singleTax = singleTax,
                    militaryLevy = militaryLevy,
                    totalTax = (singleTax + militaryLevy + esv).round2(),
                )
            }.associateBy { it.id }
        return if (fixes.isEmpty()) this else map { fixes[it.id] ?: it }
    }

    private fun TaxIncomeItem.monthKey() = date.year to date.monthNumber

    /**
     * Marks the given tax types of calculated incomes as paid (or unpaid again);
     * with an empty selection it applies to all calculated incomes.
     */
    suspend fun setPaid(selectedIds: Set<String>, types: Set<TaxType>, paid: Boolean) {
        val now = Clock.System.now().toEpochMilliseconds()
        val updated = getIncomesList().map { item ->
            if (!item.isTaxCalculated || (selectedIds.isNotEmpty() && item.id !in selectedIds)) return@map item
            val changed = item.copy(
                isSingleTaxPaid = if (TaxType.SINGLE_TAX in types) paid else item.isSingleTaxPaid,
                isMilitaryLevyPaid = if (TaxType.MILITARY_LEVY in types) paid else item.isMilitaryLevyPaid,
                isEsvPaid = if (TaxType.ESV in types) paid else item.isEsvPaid,
            )
            changed.copy(paidAt = if (changed.isPaid) now else 0L)
        }
        saveIncomes(updated)
    }

    fun calculatePaymentTotal(selectedIds: Set<String>, incomes: List<TaxIncomeItem>): Double {
        val itemsToPay = if (selectedIds.isNotEmpty()) {
            incomes.filter { it.id in selectedIds && it.isTaxCalculated && !it.isPaid }
        } else {
            incomes.filter { it.isTaxCalculated && !it.isPaid }
        }
        return itemsToPay.sumOf { it.unpaidAmount }
    }

    fun getPaymentCalculation(selectedIds: Set<String>, incomes: List<TaxIncomeItem>, group: FopGroup): TaxCalculation? {
        val itemsToPay = if (selectedIds.isNotEmpty()) {
            incomes.filter { it.id in selectedIds && it.isTaxCalculated && !it.isPaid }
        } else {
            incomes.filter { it.isTaxCalculated && !it.isPaid }
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
        val singleTax = itemsToPay.sumOf { it.unpaidAmount(TaxType.SINGLE_TAX) }.round2()
        val militaryLevy = itemsToPay.sumOf { it.unpaidAmount(TaxType.MILITARY_LEVY) }.round2()
        val esv = itemsToPay.sumOf { it.unpaidAmount(TaxType.ESV) }.round2()
        val totalTax = itemsToPay.sumOf { it.unpaidAmount }.round2()

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
