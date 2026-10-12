package com.gmail.vanyadubik.mytaxes.featurehistory

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.gmail.vanyadubik.mytaxes.commonui.ui.components.AppBarActions
import com.gmail.vanyadubik.mytaxes.commonui.ui.dialog.ErrorAppDialog
import com.gmail.vanyadubik.mytaxes.commonui.utils.formatDate
import com.gmail.vanyadubik.mytaxes.commonui.utils.formatNumber
import com.gmail.vanyadubik.mytaxes.commonui.utils.title
import com.gmail.vanyadubik.mytaxes.domen.model.TaxCalculation
import com.gmail.vanyadubik.mytaxes.resource.Res
import com.gmail.vanyadubik.mytaxes.resource.delete
import com.gmail.vanyadubik.mytaxes.resource.history
import com.gmail.vanyadubik.mytaxes.resource.history_empty
import com.gmail.vanyadubik.mytaxes.resource.total_tax
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // The tab keeps its ViewModel between visits, so reload to pick up new calculations
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.history)) },
                actions = {
                    IconButton(onClick = viewModel::load) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                    AppBarActions()
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.items.isEmpty() -> Text(
                    stringResource(Res.string.history_empty),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.items, key = { it.id ?: it.hashCode() }) { item ->
                        HistoryItem(item, onDelete = { viewModel.delete(item) })
                    }
                }
            }
        }
    }

    state.errorMessage?.let {
        ErrorAppDialog(message = it, onDismiss = viewModel::dismissError)
    }
}

@Composable
private fun HistoryItem(item: TaxCalculation, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${item.incomes.first().date.formatDate()} · ${item.amountUah.formatNumber()} UAH",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(item.group.title(), style = MaterialTheme.typography.bodySmall)
                item.incomes.forEach { income ->
                    Text(
                        "${income.date.formatDate()} · ${income.amount.formatNumber()} ${income.currency.code}" +
                            " × ${income.rate.formatNumber(4)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    "${stringResource(Res.string.total_tax)}: ${item.totalTax.formatNumber()} UAH",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.delete))
            }
        }
    }
}
