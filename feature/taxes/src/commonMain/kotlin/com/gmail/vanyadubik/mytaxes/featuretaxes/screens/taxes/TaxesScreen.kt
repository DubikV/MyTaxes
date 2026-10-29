package com.gmail.vanyadubik.myassistant.featuretaxes.screens.taxes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.myassistant.commonui.ui.components.button.AppButton
import com.gmail.vanyadubik.myassistant.commonui.ui.components.card.ResultCard
import com.gmail.vanyadubik.myassistant.commonui.ui.components.textfield.DateField
import com.gmail.vanyadubik.myassistant.commonui.ui.dialog.ConfirmBottomSheet
import com.gmail.vanyadubik.myassistant.commonui.ui.dialog.ErrorAppDialog
import com.gmail.vanyadubik.myassistant.commonui.utils.formatDate
import com.gmail.vanyadubik.myassistant.commonui.utils.formatNumber
import com.gmail.vanyadubik.myassistant.commonui.utils.title
import com.gmail.vanyadubik.myassistant.domen.model.Currency
import com.gmail.vanyadubik.myassistant.domen.model.FopGroup
import com.gmail.vanyadubik.myassistant.domen.model.TaxIncomeItem
import com.gmail.vanyadubik.myassistant.featuretaxes.screens.taxes.model.InputError
import com.gmail.vanyadubik.myassistant.featuretaxes.screens.taxes.model.TaxesUiState
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.add_income
import com.gmail.vanyadubik.myassistant.resource.amount_invalid
import com.gmail.vanyadubik.myassistant.resource.calculate_tax
import com.gmail.vanyadubik.myassistant.resource.currency
import com.gmail.vanyadubik.myassistant.resource.currency_change_in_account
import com.gmail.vanyadubik.myassistant.resource.currency_uah
import com.gmail.vanyadubik.myassistant.resource.date_required
import com.gmail.vanyadubik.myassistant.resource.delete
import com.gmail.vanyadubik.myassistant.resource.fop_group
import com.gmail.vanyadubik.myassistant.resource.fop_group_account_info_guest
import com.gmail.vanyadubik.myassistant.resource.fop_group_account_info_signed_in
import com.gmail.vanyadubik.myassistant.resource.fop_group_change_in_account
import com.gmail.vanyadubik.myassistant.resource.future_date_invalid
import com.gmail.vanyadubik.myassistant.resource.income_amount
import com.gmail.vanyadubik.myassistant.resource.income_date
import com.gmail.vanyadubik.myassistant.resource.loading
import com.gmail.vanyadubik.myassistant.resource.oops_error_title
import com.gmail.vanyadubik.myassistant.resource.pay
import com.gmail.vanyadubik.myassistant.resource.retry
import com.gmail.vanyadubik.myassistant.resource.tax_history
import com.gmail.vanyadubik.myassistant.resource.taxes_title
import com.gmail.vanyadubik.myassistant.resource.unpaid_taxes
import com.gmail.vanyadubik.myassistant.resource.unregistered_incomes
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxesScreen(
    onOpenHistory: () -> Unit,
    viewModel: TaxesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    TaxesContent(
        state = state,
        onOpenHistory = onOpenHistory,
        onGroupSelected = viewModel::onGroupSelected,
        onDateSelected = viewModel::onDateSelected,
        onAmountChanged = viewModel::onAmountChanged,
        onCurrencySelected = viewModel::onCurrencySelected,
        onAddIncome = viewModel::addIncome,
        onDeleteIncome = viewModel::deleteIncome,
        onCalculateTaxes = viewModel::calculateTaxes,
    )

    state.calculationResult?.let { result ->
        ConfirmBottomSheet(
            title = stringResource(Res.string.unpaid_taxes),
            confirmText = stringResource(Res.string.pay),
            onConfirm = viewModel::dismissCalculationResult,
            onDismiss = viewModel::dismissCalculationResult,
            content = {
                ResultCard(result = result)
            }
        )
    }

    if (state.actionError != null) {
        ConfirmBottomSheet(
            title = stringResource(Res.string.oops_error_title),
            message = state.actionError,
            confirmText = stringResource(Res.string.retry),
            onConfirm = viewModel::retryLastAction,
            onDismiss = viewModel::dismissActionError,
        )
    }

    state.errorMessage?.let {
        ErrorAppDialog(message = it, onDismiss = viewModel::dismissError)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaxesContent(
    state: TaxesUiState,
    onOpenHistory: () -> Unit,
    onGroupSelected: (FopGroup) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onAmountChanged: (String) -> Unit,
    onCurrencySelected: (Currency) -> Unit,
    onAddIncome: () -> Unit,
    onDeleteIncome: (String) -> Unit,
    onCalculateTaxes: () -> Unit,
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(state.untaxedIncomes.size) {
        if (state.untaxedIncomes.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.taxes_title)) },
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(Res.string.tax_history),
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // FOP Group info
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

                // Untaxed Incomes Container (above "Add Income" form)
                if (state.untaxedIncomes.isNotEmpty()) {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                stringResource(Res.string.unregistered_incomes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            state.untaxedIncomes.forEach { item ->
                                IncomeItemRow(
                                    item = item,
                                    onDelete = { onDeleteIncome(item.id) },
                                )
                            }
                        }
                    }
                }

                // Input Form Card (Add Income)
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            stringResource(Res.string.add_income),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        DateField(
                            label = stringResource(Res.string.income_date),
                            date = state.inputDate,
                            isError = state.inputError == InputError.DATE || state.inputError == InputError.FUTURE_DATE,
                            errorText = when (state.inputError) {
                                InputError.FUTURE_DATE -> stringResource(Res.string.future_date_invalid)
                                InputError.DATE -> stringResource(Res.string.date_required)
                                else -> null
                            },
                            onDateSelected = onDateSelected,
                        )
                        OutlinedTextField(
                            value = state.inputAmount,
                            onValueChange = onAmountChanged,
                            label = { Text(stringResource(Res.string.income_amount)) },
                            isError = state.inputError == InputError.AMOUNT,
                            supportingText = if (state.inputError == InputError.AMOUNT) {
                                { Text(stringResource(Res.string.amount_invalid)) }
                            } else null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (state.savedCurrency != null) {
                            Text(
                                "${stringResource(Res.string.currency)}: ${state.inputCurrency.code}",
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
                                        selected = currency == state.inputCurrency,
                                        onClick = { onCurrencySelected(currency) },
                                        label = { Text(currency.code) },
                                    )
                                }
                            }
                        }
                        AppButton(
                            text = stringResource(Res.string.add_income),
                            onClick = onAddIncome,
                            isOutlinedButton = true,
                            leftIcon = Icons.Default.Add,
                        )
                    }
                }

                if (state.untaxedIncomes.isNotEmpty()) {
                    AppButton(
                        text = stringResource(Res.string.calculate_tax),
                        onClick = onCalculateTaxes,
                        loadingMessage = if (state.isLoading) stringResource(Res.string.loading) else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeItemRow(
    item: TaxIncomeItem,
    onDelete: () -> Unit,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${item.date.formatDate()} · ${item.amount.formatNumber()} ${item.currency.code}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                if (item.amountUah > 0.0) {
                    Text(
                        "${item.amountUah.formatNumber()} ${stringResource(Res.string.currency_uah)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.delete))
            }
        }
    }
}
