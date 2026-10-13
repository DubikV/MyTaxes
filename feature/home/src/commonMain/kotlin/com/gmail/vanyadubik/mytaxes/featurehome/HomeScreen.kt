package com.gmail.vanyadubik.mytaxes.featurehome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.gmail.vanyadubik.mytaxes.domen.model.Expense
import com.gmail.vanyadubik.mytaxes.domen.usecase.MonthlyStatsUseCase
import com.gmail.vanyadubik.mytaxes.resource.Res
import com.gmail.vanyadubik.mytaxes.resource.add_expense
import com.gmail.vanyadubik.mytaxes.resource.amount_invalid
import com.gmail.vanyadubik.mytaxes.resource.cancel
import com.gmail.vanyadubik.mytaxes.resource.chart_empty
import com.gmail.vanyadubik.mytaxes.resource.chart_net
import com.gmail.vanyadubik.mytaxes.resource.chart_period
import com.gmail.vanyadubik.mytaxes.resource.chart_title
import com.gmail.vanyadubik.mytaxes.resource.delete
import com.gmail.vanyadubik.mytaxes.resource.expense_amount
import com.gmail.vanyadubik.mytaxes.resource.expense_date
import com.gmail.vanyadubik.mytaxes.resource.expense_note
import com.gmail.vanyadubik.mytaxes.resource.expenses_empty
import com.gmail.vanyadubik.mytaxes.resource.expenses_title
import com.gmail.vanyadubik.mytaxes.resource.future_date_invalid
import com.gmail.vanyadubik.mytaxes.resource.home_sign_in_prompt
import com.gmail.vanyadubik.mytaxes.resource.home_title
import com.gmail.vanyadubik.mytaxes.resource.save
import com.gmail.vanyadubik.mytaxes.resource.sign_in
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenLogin: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showAddExpense by remember { mutableStateOf(false) }

    HomeContent(
        state = state,
        onOpenLogin = onOpenLogin,
        onRefresh = viewModel::load,
        onAddExpense = { showAddExpense = true },
        onDeleteExpense = viewModel::deleteExpense,
    )

    if (showAddExpense) {
        AddExpenseDialog(
            onDismiss = { showAddExpense = false },
            onSave = {
                viewModel.addExpense(it)
                showAddExpense = false
            },
        )
    }

    state.errorMessage?.let {
        ErrorAppDialog(message = it, onDismiss = viewModel::dismissError)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState,
    onOpenLogin: () -> Unit,
    onRefresh: () -> Unit,
    onAddExpense: () -> Unit,
    onDeleteExpense: (Expense) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(
            title = { Text(stringResource(Res.string.home_title)) },
            actions = { AppBarActions() },
        ) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (!state.isLoggedIn) {
                    Text(stringResource(Res.string.home_sign_in_prompt), style = MaterialTheme.typography.bodyLarge)
                    AppButton(text = stringResource(Res.string.sign_in), onClick = onOpenLogin)
                } else {
                    ChartCard(state)
                    ExpensesSection(
                        expenses = state.expenses,
                        onAdd = onAddExpense,
                        onDelete = onDeleteExpense,
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun ChartCard(state: HomeUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(Res.string.chart_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(Res.string.chart_period, MonthlyStatsUseCase.DEFAULT_MONTHS),
                style = MaterialTheme.typography.bodySmall,
            )
            when {
                state.isLoading && state.stats.isEmpty() -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                state.stats.none { it.income > 0.0 || it.tax > 0.0 || it.expenses > 0.0 } ->
                    Text(stringResource(Res.string.chart_empty), style = MaterialTheme.typography.bodyMedium)
                else -> {
                    MonthlyChart(state.stats)
                    HorizontalDivider()
                    val net = state.stats.sumOf { it.income - it.tax - it.expenses }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            stringResource(Res.string.chart_net),
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "${net.formatNumber()} UAH",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (net >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpensesSection(expenses: List<Expense>, onAdd: () -> Unit, onDelete: (Expense) -> Unit) {
    Text(stringResource(Res.string.expenses_title), style = MaterialTheme.typography.titleMedium)
    AppButton(
        text = stringResource(Res.string.add_expense),
        onClick = onAdd,
        isOutlinedButton = true,
        leftIcon = Icons.Default.Add,
    )
    if (expenses.isEmpty()) {
        Text(stringResource(Res.string.expenses_empty), style = MaterialTheme.typography.bodyMedium)
    }
    expenses.forEach { expense ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${expense.amountUah.formatNumber()} UAH", style = MaterialTheme.typography.titleSmall)
                    Text(
                        listOf(expense.date.formatDate(), expense.note).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                IconButton(onClick = { onDelete(expense) }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.delete))
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun AddExpenseDialog(onDismiss: () -> Unit, onSave: (Expense) -> Unit) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var date by remember { mutableStateOf<LocalDate>(today) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val parsedAmount = amount.toDoubleOrNull()?.takeIf { it > 0.0 }
    val futureDate = date > today

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.add_expense)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField(
                    label = stringResource(Res.string.expense_date),
                    date = date,
                    onDateSelected = { date = it },
                    isError = submitted && futureDate,
                    errorText = if (submitted && futureDate) stringResource(Res.string.future_date_invalid) else null,
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { value ->
                        val normalized = value.replace(',', '.')
                        if (normalized.isEmpty() || normalized == "." || normalized.toDoubleOrNull() != null) {
                            amount = normalized
                        }
                    },
                    label = { Text(stringResource(Res.string.expense_amount)) },
                    isError = submitted && parsedAmount == null,
                    supportingText = if (submitted && parsedAmount == null) {
                        { Text(stringResource(Res.string.amount_invalid)) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(Res.string.expense_note)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                submitted = true
                if (parsedAmount != null && !futureDate) {
                    onSave(Expense(date = date, amountUah = parsedAmount, note = note.trim()))
                }
            }) { Text(stringResource(Res.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
    )
}
