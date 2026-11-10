package com.gmail.vanyadubik.myassistant.featurehome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.myassistant.commonui.utils.formatNumber
import com.gmail.vanyadubik.myassistant.domen.model.TaxType
import com.gmail.vanyadubik.myassistant.featurehome.model.HomeUiState
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.all_taxes_paid
import com.gmail.vanyadubik.myassistant.resource.income_net
import com.gmail.vanyadubik.myassistant.resource.income_total_label
import com.gmail.vanyadubik.myassistant.resource.currency_uah
import com.gmail.vanyadubik.myassistant.resource.tax_type_esv
import com.gmail.vanyadubik.myassistant.resource.tax_type_military
import com.gmail.vanyadubik.myassistant.resource.tax_type_single
import com.gmail.vanyadubik.myassistant.resource.taxes_paid_of_format
import com.gmail.vanyadubik.myassistant.resource.days_to_deadline_format
import com.gmail.vanyadubik.myassistant.resource.total_amount_format
import com.gmail.vanyadubik.myassistant.resource.unpaid_taxes
import com.gmail.vanyadubik.myassistant.resource.unregistered_incomes
import org.jetbrains.compose.resources.stringResource

private val SingleTaxColor = Color(0xFF3F7DD8)
private val MilitaryColor = Color(0xFFE08A1E)
private val EsvColor = Color(0xFF8E5BD0)
private val UntaxedColor = Color(0xFFB3261E)
private val PaidColor = Color(0xFF2E9E5B)

private fun TaxType.color() = when (this) {
    TaxType.SINGLE_TAX -> SingleTaxColor
    TaxType.MILITARY_LEVY -> MilitaryColor
    TaxType.ESV -> EsvColor
}

/** Taxes dashboard: donut of what is still unpaid by tax type, paid progress and the untaxed incomes. */
@Composable
internal fun TaxesOverviewCard(
    state: HomeUiState,
    onOpenTaxes: () -> Unit,
    onOpenTaxesHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uah = stringResource(Res.string.currency_uah)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (state.calculatedTaxes.isNotEmpty() || state.untaxedIncomes.isNotEmpty()) {
                IncomeDistribution(state, uah)
            }

            if (state.calculatedTaxes.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenTaxesHistory() },
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (state.unpaidTaxes.isEmpty()) {
                        AllPaid()
                    } else {
                        UnpaidBreakdown(state, uah)
                    }
                    PaidProgress(state, uah)
                }
            }

            if (state.untaxedIncomes.isNotEmpty()) {
                UntaxedRow(state, uah, onOpenTaxes)
            }
        }
    }
}

/** Pie of the calculated incomes: share of every tax and what remains after them. */
@Composable
private fun IncomeDistribution(state: HomeUiState, uah: String) {
    val items = state.calculatedTaxes
    val calculatedIncome = items.sumOf { if (it.amountUah > 0.0) it.amountUah else it.amount }
    val untaxed = state.totalUntaxedIncomeAmount
    val income = calculatedIncome + untaxed
    if (income <= 0.0) return
    val taxes = TaxType.entries.map { type ->
        type to items.sumOf {
            when (type) {
                TaxType.SINGLE_TAX -> it.singleTax
                TaxType.MILITARY_LEVY -> it.militaryLevy
                TaxType.ESV -> it.esv
            }
        }
    }
    val net = (calculatedIncome - taxes.sumOf { it.second }).coerceAtLeast(0.0)
    val netLabel = stringResource(Res.string.income_net)
    val slices = taxes.map { (type, amount) -> Triple(type.label(), type.color(), amount) } +
        Triple(netLabel, PaidColor, net) +
        Triple(stringResource(Res.string.unregistered_incomes), UntaxedColor, untaxed)
    val visible = slices.filter { it.third > 0.0 }
    val sum = visible.sumOf { it.third }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(132.dp)) {
                Canvas(modifier = Modifier.size(132.dp)) {
                    var start = -90f
                    visible.forEach { (_, color, amount) ->
                        val sweep = (amount / sum * 360.0).toFloat()
                        val gap = if (visible.size > 1) 2f else 0f
                        drawArc(color, start + gap / 2, (sweep - gap).coerceAtLeast(0.5f), true)
                        start += sweep
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                Text(
                    "${stringResource(Res.string.income_total_label)}: ${income.formatNumber()} $uah",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                visible.forEach { (label, color, amount) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                        Text(
                            "$label · ${(amount / sum * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnpaidBreakdown(state: HomeUiState, uah: String) {
    val parts = TaxType.entries.map { it to state.unpaidAmount(it) }
    val total = state.totalUnpaidTaxAmount

    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(Res.string.unpaid_taxes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(112.dp)) {
            val track = MaterialTheme.colorScheme.outlineVariant
            Canvas(modifier = Modifier.size(112.dp)) {
                val stroke = 12.dp.toPx()
                val topLeft = Offset(stroke / 2, stroke / 2)
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
                var start = -90f
                parts.filter { it.second > 0.0 }.forEach { (type, amount) ->
                    val sweep = (amount / total * 360.0).toFloat()
                    // A small gap between the segments, none for a single one
                    val gap = if (parts.count { it.second > 0.0 } > 1) 2f else 0f
                    drawArc(
                        type.color(), start + gap / 2, (sweep - gap).coerceAtLeast(0.5f), false, topLeft, arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Butt),
                    )
                    start += sweep
                }
            }
            // Inner diameter is 112 - 2 * 12 = 88dp; keep the text inside it
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    total.formatNumber(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(uah, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            parts.filter { it.second > 0.0 }.forEach { (type, amount) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(type.color(), CircleShape))
                    Column {
                        Text(type.label(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${amount.formatNumber()} $uah",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllPaid() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PaidColor, modifier = Modifier.size(32.dp))
        Text(
            stringResource(Res.string.all_taxes_paid),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PaidProgress(state: HomeUiState, uah: String) {
    val total = state.totalTaxAmount
    if (total <= 0.0) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LinearProgressIndicator(
            progress = { (state.totalPaidTaxAmount / total).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = PaidColor,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
            strokeCap = StrokeCap.Round,
        )
        Text(
            stringResource(
                Res.string.taxes_paid_of_format,
                state.totalPaidTaxAmount.formatNumber(),
                total.formatNumber(),
                uah,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Green when the deadline is a month away or more, through yellow and orange to red on the deadline day. */
private fun urgencyColor(daysLeft: Int?): Color {
    val fraction = ((daysLeft ?: 30).coerceIn(0, 30)) / 30f
    return Color.hsv(fraction * 125f, 0.75f, 0.55f)
}

@Composable
private fun UntaxedRow(state: HomeUiState, uah: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = urgencyColor(state.daysToDeadline),
        contentColor = Color.White,
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Default.HourglassEmpty, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${stringResource(Res.string.unregistered_incomes)} (${state.untaxedIncomes.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(Res.string.total_amount_format, state.totalUntaxedIncomeAmount.formatNumber(), uah),
                    style = MaterialTheme.typography.bodySmall,
                )
                state.daysToDeadline?.let {
                    Text(
                        stringResource(Res.string.days_to_deadline_format, it),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun TaxType.label() = stringResource(
    when (this) {
        TaxType.SINGLE_TAX -> Res.string.tax_type_single
        TaxType.MILITARY_LEVY -> Res.string.tax_type_military
        TaxType.ESV -> Res.string.tax_type_esv
    },
)
