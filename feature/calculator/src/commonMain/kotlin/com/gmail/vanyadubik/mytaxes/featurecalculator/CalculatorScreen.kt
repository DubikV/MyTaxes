package com.gmail.vanyadubik.mytaxes.featurecalculator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.mytaxes.commonui.ui.components.AppBarActions
import com.gmail.vanyadubik.mytaxes.commonui.ui.components.button.AppButton
import com.gmail.vanyadubik.mytaxes.commonui.ui.components.textfield.DateField
import com.gmail.vanyadubik.mytaxes.commonui.ui.dialog.ErrorAppDialog
import com.gmail.vanyadubik.mytaxes.commonui.utils.formatDate
import com.gmail.vanyadubik.mytaxes.commonui.utils.formatNumber
import com.gmail.vanyadubik.mytaxes.commonui.utils.title
import com.gmail.vanyadubik.mytaxes.domen.model.FopGroup
import com.gmail.vanyadubik.mytaxes.domen.model.Currency
import com.gmail.vanyadubik.mytaxes.domen.model.TaxCalculation
import com.gmail.vanyadubik.mytaxes.resource.Res
import com.gmail.vanyadubik.mytaxes.resource.amount_invalid
import com.gmail.vanyadubik.mytaxes.resource.calculate
import com.gmail.vanyadubik.mytaxes.resource.calculator_title
import com.gmail.vanyadubik.mytaxes.resource.cancel
import com.gmail.vanyadubik.mytaxes.resource.currency
import com.gmail.vanyadubik.mytaxes.resource.currency_change_in_account
import com.gmail.vanyadubik.mytaxes.resource.date_required
import com.gmail.vanyadubik.mytaxes.resource.annual_limit
import com.gmail.vanyadubik.mytaxes.resource.fixed_tax_note
import com.gmail.vanyadubik.mytaxes.resource.fop_group
import com.gmail.vanyadubik.mytaxes.resource.fop_group_account_info_guest
import com.gmail.vanyadubik.mytaxes.resource.fop_group_change_in_account
import com.gmail.vanyadubik.mytaxes.resource.fop_group_account_info_signed_in
import com.gmail.vanyadubik.mytaxes.resource.future_date_invalid
import com.gmail.vanyadubik.mytaxes.resource.military_levy_fixed
import com.gmail.vanyadubik.mytaxes.resource.single_tax_fixed
import com.gmail.vanyadubik.mytaxes.resource.income_amount
import com.gmail.vanyadubik.mytaxes.resource.income_date
import com.gmail.vanyadubik.mytaxes.resource.income_uah
import com.gmail.vanyadubik.mytaxes.resource.loading
import com.gmail.vanyadubik.mytaxes.resource.add_income
import com.gmail.vanyadubik.mytaxes.resource.delete
import com.gmail.vanyadubik.mytaxes.resource.income_number
import com.gmail.vanyadubik.mytaxes.resource.sign_in_to_save
import com.gmail.vanyadubik.mytaxes.resource.esv
import com.gmail.vanyadubik.mytaxes.resource.military_levy
import com.gmail.vanyadubik.mytaxes.resource.nbu_rate
import com.gmail.vanyadubik.mytaxes.resource.result_saved
import com.gmail.vanyadubik.mytaxes.resource.save_failed
import com.gmail.vanyadubik.mytaxes.resource.select
import com.gmail.vanyadubik.mytaxes.resource.single_tax
import com.gmail.vanyadubik.mytaxes.resource.total_tax
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onOpenLogin: () -> Unit,
    viewModel: CalculatorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    CalculatorContent(
        state = state,
        onOpenLogin = onOpenLogin,
        onGroupSelected = viewModel::onGroupSelected,
        onDateSelected = viewModel::onDateSelected,
        onAmountChanged = viewModel::onAmountChanged,
        onCurrencySelected = viewModel::onCurrencySelected,
        onAddIncome = viewModel::addIncome,
        onRemoveIncome = viewModel::removeIncome,
        onCalculate = viewModel::calculate,
    )

    state.errorMessage?.let {
        ErrorAppDialog(message = it, onDismiss = viewModel::dismissError)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorContent(
    state: CalculatorUiState,
    onOpenLogin: () -> Unit,
    onGroupSelected: (FopGroup) -> Unit,
    onDateSelected: (Int, LocalDate) -> Unit,
    onAmountChanged: (Int, String) -> Unit,
    onCurrencySelected: (Int, Currency) -> Unit,
    onAddIncome: () -> Unit,
    onRemoveIncome: (Int) -> Unit,
    onCalculate: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.calculator_title)) },
                actions = { AppBarActions() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.isGroupSaved) {
                    Text(
                        "${stringResource(Res.string.fop_group)}: ${state.group.title()}",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        stringResource(Res.string.fop_group_change_in_account),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(stringResource(Res.string.fop_group), style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(FopGroup.entries) { group ->
                            FilterChip(
                                selected = group == state.group,
                                onClick = { onGroupSelected(group) },
                                label = { Text(group.title()) },
                            )
                        }
                    }
                    Text(
                        stringResource(
                            if (state.isLoggedIn) Res.string.fop_group_account_info_signed_in
                            else Res.string.fop_group_account_info_guest
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                state.incomes.forEachIndexed { index, row ->
                    IncomeCard(
                        number = index + 1,
                        row = row,
                        canRemove = state.incomes.size > 1,
                        currencyLocked = state.savedCurrency != null,
                        onDateSelected = { onDateSelected(row.id, it) },
                        onAmountChanged = { onAmountChanged(row.id, it) },
                        onCurrencySelected = { onCurrencySelected(row.id, it) },
                        onRemove = { onRemoveIncome(row.id) },
                    )
                }
                AppButton(
                    text = stringResource(Res.string.add_income),
                    onClick = onAddIncome,
                    isOutlinedButton = true,
                    leftIcon = Icons.Default.Add,
                )
                AppButton(
                    text = stringResource(Res.string.calculate),
                    onClick = onCalculate,
                    loadingMessage = if (state.isLoading) stringResource(Res.string.loading) else null,
                )
                state.result?.let { ResultCard(it) }
                if (state.result != null && !state.isLoggedIn) {
                    TextButton(onClick = onOpenLogin) {
                        Text(stringResource(Res.string.sign_in_to_save))
                    }
                }
                if (state.savedToHistory) {
                    Text(
                        stringResource(Res.string.result_saved),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                state.saveError?.let {
                    Text(
                        stringResource(Res.string.save_failed, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeCard(
    number: Int,
    row: IncomeRow,
    canRemove: Boolean,
    currencyLocked: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onAmountChanged: (String) -> Unit,
    onCurrencySelected: (Currency) -> Unit,
    onRemove: () -> Unit,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.income_number, number),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                )
                if (canRemove) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.delete))
                    }
                }
            }
            DateField(
                label = stringResource(Res.string.income_date),
                date = row.date,
                isError = row.error == InputError.DATE || row.error == InputError.FUTURE_DATE,
                errorText = when (row.error) {
                    InputError.FUTURE_DATE -> stringResource(Res.string.future_date_invalid)
                    InputError.DATE -> stringResource(Res.string.date_required)
                    else -> null
                },
                onDateSelected = onDateSelected,
            )
            OutlinedTextField(
                value = row.amount,
                onValueChange = onAmountChanged,
                label = { Text(stringResource(Res.string.income_amount)) },
                isError = row.error == InputError.AMOUNT,
                supportingText = if (row.error == InputError.AMOUNT) {
                    { Text(stringResource(Res.string.amount_invalid)) }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            if (currencyLocked) {
                Text(
                    "${stringResource(Res.string.currency)}: ${row.currency.code}",
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    stringResource(Res.string.currency_change_in_account),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Currency.entries) { currency ->
                        FilterChip(
                            selected = currency == row.currency,
                            onClick = { onCurrencySelected(currency) },
                            label = { Text(currency.code) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: TaxCalculation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.nbu_rate), style = MaterialTheme.typography.labelLarge)
            result.incomes.forEach { income ->
                ResultRow(
                    "${income.date.formatDate()} · ${income.amount.formatNumber()} ${income.currency.code}" +
                        " × ${income.rate.formatNumber(4)}",
                    "${income.amountUah.formatNumber()} UAH",
                )
            }
            HorizontalDivider()
            ResultRow(stringResource(Res.string.income_uah), "${result.amountUah.formatNumber()} UAH")
            HorizontalDivider()
            ResultRow(
                if (result.group.isFixed) {
                    stringResource(Res.string.single_tax_fixed)
                } else {
                    stringResource(Res.string.single_tax, "${result.group.singleTaxPercent.formatNumber(0)}%")
                },
                "${result.singleTax.formatNumber()} UAH",
            )
            ResultRow(
                if (result.group.isFixed) {
                    stringResource(Res.string.military_levy_fixed)
                } else {
                    stringResource(Res.string.military_levy, "${result.group.militaryLevyPercent.formatNumber(0)}%")
                },
                "${result.militaryLevy.formatNumber()} UAH",
            )
            if (result.esv > 0.0) {
                ResultRow(stringResource(Res.string.esv, result.months), "${result.esv.formatNumber()} UAH")
            }
            HorizontalDivider()
            ResultRow(stringResource(Res.string.total_tax), "${result.totalTax.formatNumber()} UAH", bold = true)
            ResultRow(stringResource(Res.string.annual_limit), "${result.group.annualIncomeLimit.formatNumber(0)} UAH")
            if (result.group.isFixed) {
                Text(stringResource(Res.string.fixed_tax_note), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else null,
        )
        Text(
            value,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
        )
    }
}
