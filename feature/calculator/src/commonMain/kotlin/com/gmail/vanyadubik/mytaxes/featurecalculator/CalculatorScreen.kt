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
import com.gmail.vanyadubik.mytaxes.commonui.language.LanguageSwitchButton
import com.gmail.vanyadubik.mytaxes.commonui.ui.components.button.AppButton
import com.gmail.vanyadubik.mytaxes.commonui.ui.dialog.ErrorAppDialog
import com.gmail.vanyadubik.mytaxes.commonui.ui.theme.button.ThemeSwitchButton
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
import com.gmail.vanyadubik.mytaxes.resource.date_required
import com.gmail.vanyadubik.mytaxes.resource.annual_limit
import com.gmail.vanyadubik.mytaxes.resource.fixed_tax_note
import com.gmail.vanyadubik.mytaxes.resource.fop_group
import com.gmail.vanyadubik.mytaxes.resource.future_date_invalid
import com.gmail.vanyadubik.mytaxes.resource.military_levy_fixed
import com.gmail.vanyadubik.mytaxes.resource.single_tax_fixed
import com.gmail.vanyadubik.mytaxes.resource.history
import com.gmail.vanyadubik.mytaxes.resource.income_amount
import com.gmail.vanyadubik.mytaxes.resource.income_date
import com.gmail.vanyadubik.mytaxes.resource.income_uah
import com.gmail.vanyadubik.mytaxes.resource.loading
import com.gmail.vanyadubik.mytaxes.resource.add_income
import com.gmail.vanyadubik.mytaxes.resource.delete
import com.gmail.vanyadubik.mytaxes.resource.income_number
import com.gmail.vanyadubik.mytaxes.resource.logout
import com.gmail.vanyadubik.mytaxes.resource.sign_in
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
    onOpenHistory: () -> Unit,
    onOpenLogin: () -> Unit,
    viewModel: CalculatorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.calculator_title)) },
                actions = {
                    ThemeSwitchButton(
                        themeHandler = viewModel.themeAnimationHandler,
                        iconModifier = Modifier.padding(12.dp),
                        iconSize = 24.dp,
                    )
                    LanguageSwitchButton()
                    if (state.isLoggedIn) {
                        IconButton(onClick = onOpenHistory) {
                            Icon(Icons.Default.History, contentDescription = stringResource(Res.string.history))
                        }
                        IconButton(onClick = viewModel::logout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(Res.string.logout))
                        }
                    } else {
                        IconButton(onClick = onOpenLogin) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = stringResource(Res.string.sign_in))
                        }
                    }
                }
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
                Text(stringResource(Res.string.fop_group), style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FopGroup.entries) { group ->
                        FilterChip(
                            selected = group == state.group,
                            onClick = { viewModel.onGroupSelected(group) },
                            label = { Text(group.title()) },
                        )
                    }
                }
                state.incomes.forEachIndexed { index, row ->
                    IncomeCard(
                        number = index + 1,
                        row = row,
                        canRemove = state.incomes.size > 1,
                        onDateSelected = { viewModel.onDateSelected(row.id, it) },
                        onAmountChanged = { viewModel.onAmountChanged(row.id, it) },
                        onCurrencySelected = { viewModel.onCurrencySelected(row.id, it) },
                        onRemove = { viewModel.removeIncome(row.id) },
                    )
                }
                AppButton(
                    text = stringResource(Res.string.add_income),
                    onClick = viewModel::addIncome,
                    isOutlinedButton = true,
                    leftIcon = Icons.Default.Add,
                )
                AppButton(
                    text = stringResource(Res.string.calculate),
                    onClick = viewModel::calculate,
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

    state.errorMessage?.let {
        ErrorAppDialog(message = it, onDismiss = viewModel::dismissError)
    }
}

@Composable
private fun IncomeCard(
    number: Int,
    row: IncomeRow,
    canRemove: Boolean,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    date: LocalDate?,
    isError: Boolean,
    errorText: String?,
    onDateSelected: (LocalDate) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = date?.formatDate() ?: "",
        onValueChange = {},
        label = { Text(stringResource(Res.string.income_date)) },
        trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
        isError = isError,
        supportingText = errorText?.let { { Text(it) } },
        readOnly = true,
        enabled = false,
        singleLine = true,
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            disabledLabelColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledSupportingTextColor = MaterialTheme.colorScheme.error,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { showPicker = true },
            ),
    )

    if (showPicker) {
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = date?.let { it.toEpochDays() * MILLIS_IN_DAY }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedDateMillis != null,
                    onClick = {
                        pickerState.selectedDateMillis?.let {
                            onDateSelected(LocalDate.fromEpochDays(it / MILLIS_IN_DAY))
                        }
                        showPicker = false
                    }
                ) { Text(stringResource(Res.string.select)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(Res.string.cancel)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private const val MILLIS_IN_DAY = 86_400_000L

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
