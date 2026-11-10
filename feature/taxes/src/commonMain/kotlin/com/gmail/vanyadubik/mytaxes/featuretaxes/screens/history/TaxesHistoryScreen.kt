package com.gmail.vanyadubik.myassistant.featuretaxes.screens.history

import androidx.compose.foundation.clickable
import com.gmail.vanyadubik.myassistant.resource.selected_to_pay
import com.gmail.vanyadubik.myassistant.resource.what_to_pay
import com.gmail.vanyadubik.myassistant.resource.tax_type_esv
import com.gmail.vanyadubik.myassistant.resource.tax_type_military
import com.gmail.vanyadubik.myassistant.resource.tax_type_single
import com.gmail.vanyadubik.myassistant.resource.partially_paid_status
import com.gmail.vanyadubik.myassistant.domen.model.TaxCalculation
import com.gmail.vanyadubik.myassistant.domen.model.TaxType
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.myassistant.commonui.ui.components.button.AppButton
import com.gmail.vanyadubik.myassistant.commonui.ui.components.card.ErrorContainerCard
import com.gmail.vanyadubik.myassistant.commonui.ui.components.card.ResultCard
import com.gmail.vanyadubik.myassistant.commonui.ui.dialog.ConfirmBottomSheet
import com.gmail.vanyadubik.myassistant.commonui.ui.dialog.ErrorAppDialog
import com.gmail.vanyadubik.myassistant.commonui.utils.formatDate
import com.gmail.vanyadubik.myassistant.commonui.utils.formatNumber
import com.gmail.vanyadubik.myassistant.commonui.utils.title
import com.gmail.vanyadubik.myassistant.domen.model.TaxIncomeItem
import com.gmail.vanyadubik.myassistant.featuretaxes.screens.history.model.TaxesHistoryUiState
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.calculate_taxes
import com.gmail.vanyadubik.myassistant.resource.calculate_untaxed
import com.gmail.vanyadubik.myassistant.resource.close
import com.gmail.vanyadubik.myassistant.resource.currency_uah
import com.gmail.vanyadubik.myassistant.resource.delete
import com.gmail.vanyadubik.myassistant.resource.delete_history_confirm
import com.gmail.vanyadubik.myassistant.resource.delete_history_message
import com.gmail.vanyadubik.myassistant.resource.delete_history_title
import com.gmail.vanyadubik.myassistant.resource.loading
import com.gmail.vanyadubik.myassistant.resource.no_incomes_history
import com.gmail.vanyadubik.myassistant.resource.oops_error_title
import com.gmail.vanyadubik.myassistant.resource.mark_as_paid
import com.gmail.vanyadubik.myassistant.resource.mark_as_unpaid
import com.gmail.vanyadubik.myassistant.resource.paid_status
import com.gmail.vanyadubik.myassistant.resource.pay
import com.gmail.vanyadubik.myassistant.resource.pay_taxes
import com.gmail.vanyadubik.myassistant.resource.pay_unpaid
import com.gmail.vanyadubik.myassistant.resource.retry
import com.gmail.vanyadubik.myassistant.resource.tax_history_title
import com.gmail.vanyadubik.myassistant.resource.total_tax
import com.gmail.vanyadubik.myassistant.resource.unpaid_status
import com.gmail.vanyadubik.myassistant.resource.unpaid_taxes
import com.gmail.vanyadubik.myassistant.resource.untaxed_status
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxesHistoryScreen(
    onBack: () -> Unit,
    viewModel: TaxesHistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var isConfirmDeleteOpen by remember { mutableStateOf(false) }

    TaxesHistoryContent(
        state = state,
        onBack = onBack,
        onToggleSelection = viewModel::toggleSelection,
        onDeleteSelected = { isConfirmDeleteOpen = true },
        onCalculateTaxes = viewModel::calculateTaxes,
        onPayClicked = viewModel::onPayClicked,
        onMarkUnpaid = viewModel::markUnpaid,
        onRetryLoad = viewModel::load,
    )

    if (isConfirmDeleteOpen) {
        ConfirmBottomSheet(
            title = stringResource(Res.string.delete_history_title),
            message = stringResource(Res.string.delete_history_message),
            confirmText = stringResource(Res.string.delete_history_confirm),
            onConfirm = {
                viewModel.deleteSelected()
                isConfirmDeleteOpen = false
            },
            onDismiss = { isConfirmDeleteOpen = false },
        )
    }

    state.calculationResult?.let { result ->
        ConfirmBottomSheet(
            title = stringResource(Res.string.unpaid_taxes),
            confirmText = stringResource(if (state.payIds != null) Res.string.mark_as_paid else Res.string.pay),
            onConfirm = {
                if (state.payIds != null) {
                    viewModel.markPaid()
                } else {
                    viewModel.dismissCalculationResult()
                    viewModel.onPayClicked()
                }
            },
            onDismiss = viewModel::dismissCalculationResult,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResultCard(result = result)
                    if (state.payIds != null) {
                        PayTypesSelector(
                            result = result,
                            selected = state.payTypes,
                            onToggle = viewModel::togglePayType,
                        )
                    }
                }
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
private fun TaxesHistoryContent(
    state: TaxesHistoryUiState,
    onBack: () -> Unit,
    onToggleSelection: (String) -> Unit,
    onDeleteSelected: () -> Unit,
    onCalculateTaxes: () -> Unit,
    onPayClicked: () -> Unit,
    onMarkUnpaid: () -> Unit,
    onRetryLoad: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.tax_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (state.selectedIds.isNotEmpty()) {
                        IconButton(onClick = onDeleteSelected) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.delete),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            val selectedIncomes = state.incomes.filter { it.id in state.selectedIds }
            val showCalculateButton: Boolean
            val showPayButton: Boolean
            val showUnpaidButton: Boolean

            if (state.selectedIds.isNotEmpty()) {
                val hasUntaxedInSelected = selectedIncomes.any { !it.isTaxCalculated }
                showCalculateButton = hasUntaxedInSelected
                showPayButton = !hasUntaxedInSelected && selectedIncomes.any { !it.isPaid }
                showUnpaidButton = !hasUntaxedInSelected && selectedIncomes.any { it.isPaid || it.isPartiallyPaid }
            } else {
                showCalculateButton = state.incomes.any { !it.isTaxCalculated }
                showPayButton = state.incomes.any { it.isTaxCalculated && !it.isPaid }
                showUnpaidButton = false
            }

            if (showCalculateButton || showPayButton || showUnpaidButton) {
                val isBothButtons = showCalculateButton && showPayButton

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (showCalculateButton) {
                        val calculateCount = if (state.selectedIds.isNotEmpty()) {
                            selectedIncomes.count { !it.isTaxCalculated }
                        } else null

                        val calculateText = if (calculateCount != null) {
                            "${stringResource(Res.string.calculate_taxes)} ($calculateCount)"
                        } else {
                            stringResource(Res.string.calculate_untaxed)
                        }

                        AppButton(
                            text = calculateText,
                            onClick = onCalculateTaxes,
                            loadingMessage = if (state.isLoading) stringResource(Res.string.loading) else null,
                        )
                    }

                    if (showPayButton) {
                        val payCount = if (state.selectedIds.isNotEmpty()) {
                            selectedIncomes.count { it.isTaxCalculated && !it.isPaid }
                        } else null

                        val payText = if (payCount != null) {
                            "${stringResource(Res.string.pay_taxes)} ($payCount)"
                        } else {
                            stringResource(Res.string.pay_unpaid)
                        }

                        AppButton(
                            text = payText,
                            onClick = onPayClicked,
                            leftIcon = Icons.Default.Payment,
                            isOutlinedButton = isBothButtons,
                        )
                    }

                    if (showUnpaidButton) {
                        AppButton(
                            text = stringResource(Res.string.mark_as_unpaid),
                            onClick = onMarkUnpaid,
                            isOutlinedButton = true,
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.loadError != null -> ErrorContainerCard(
                    message = state.loadError,
                    onRetry = onRetryLoad,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                )
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.incomes.isEmpty() -> Text(
                    stringResource(Res.string.no_incomes_history),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.incomes, key = { it.id }) { item ->
                        TaxHistoryItemCard(
                            item = item,
                            isSelected = item.id in state.selectedIds,
                            onToggleSelection = { onToggleSelection(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaxHistoryItemCard(
    item: TaxIncomeItem,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelection() },
        colors = if (isSelected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${item.date.formatDate()} · ${item.amount.formatNumber()} ${item.currency.code}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            TaxStatusChip(isTaxCalculated = item.isTaxCalculated, isPaid = item.isPaid, isPartiallyPaid = item.isPartiallyPaid)

            if (item.isTaxCalculated) {
                Text(item.group.title(), style = MaterialTheme.typography.bodySmall)
                Text(
                    "${item.date.formatDate()} · ${item.amount.formatNumber()} ${item.currency.code}" +
                        " × ${item.rate.formatNumber(4)} = ${item.amountUah.formatNumber()} ${stringResource(Res.string.currency_uah)}",
                    style = MaterialTheme.typography.bodySmall,
                )
                TaxType.entries.filter { item.amount(it) > 0.0 }.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${stringResource(type.label())}: ${item.amount(type).formatNumber()} ${stringResource(Res.string.currency_uah)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                        if (item.isPaid(type)) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = stringResource(Res.string.paid_status),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
                Text(
                    "${stringResource(Res.string.total_tax)}: ${item.totalTax.formatNumber()} ${stringResource(Res.string.currency_uah)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun TaxStatusChip(isTaxCalculated: Boolean, isPaid: Boolean, isPartiallyPaid: Boolean) {
    val container = when {
        isPaid -> MaterialTheme.colorScheme.primaryContainer
        isTaxCalculated -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val content = when {
        isPaid -> MaterialTheme.colorScheme.onPrimaryContainer
        isTaxCalculated -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(color = container, shape = MaterialTheme.shapes.small) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = when {
                    isPaid -> Icons.Default.CheckCircle
                    isTaxCalculated -> Icons.Default.Payment
                    else -> Icons.Default.HourglassEmpty
                },
                contentDescription = null,
                tint = content,
                modifier = Modifier.padding(end = 2.dp),
            )
            Text(
                text = stringResource(
                    when {
                        isPaid -> Res.string.paid_status
                        isPartiallyPaid -> Res.string.partially_paid_status
                        isTaxCalculated -> Res.string.unpaid_status
                        else -> Res.string.untaxed_status
                    }
                ),
                style = MaterialTheme.typography.labelSmall,
                color = content,
            )
        }
    }
}

private fun TaxType.label() = when (this) {
    TaxType.SINGLE_TAX -> Res.string.tax_type_single
    TaxType.MILITARY_LEVY -> Res.string.tax_type_military
    TaxType.ESV -> Res.string.tax_type_esv
}

private fun TaxCalculation.amount(type: TaxType) = when (type) {
    TaxType.SINGLE_TAX -> singleTax
    TaxType.MILITARY_LEVY -> militaryLevy
    TaxType.ESV -> esv
}

@Composable
private fun PayTypesSelector(
    result: TaxCalculation,
    selected: Set<TaxType>,
    onToggle: (TaxType) -> Unit,
) {
    val uah = stringResource(Res.string.currency_uah)
    Column {
        Text(stringResource(Res.string.what_to_pay), style = MaterialTheme.typography.titleSmall)
        TaxType.entries.filter { result.amount(it) > 0.0 }.forEach { type ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToggle(type) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = type in selected, onCheckedChange = { onToggle(type) })
                Text(stringResource(type.label()), modifier = Modifier.weight(1f))
                Text("${result.amount(type).formatNumber()} $uah", fontWeight = FontWeight.Medium)
            }
        }
        val total = TaxType.entries.filter { it in selected }.sumOf { result.amount(it) }
        Text(
            "${stringResource(Res.string.selected_to_pay)}: ${total.formatNumber()} $uah",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
