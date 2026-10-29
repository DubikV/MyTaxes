package com.gmail.vanyadubik.myassistant.commonui.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.myassistant.commonui.utils.formatDate
import com.gmail.vanyadubik.myassistant.commonui.utils.formatNumber
import com.gmail.vanyadubik.myassistant.domen.model.TaxCalculation
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.annual_limit
import com.gmail.vanyadubik.myassistant.resource.currency_uah
import com.gmail.vanyadubik.myassistant.resource.esv
import com.gmail.vanyadubik.myassistant.resource.fixed_tax_note
import com.gmail.vanyadubik.myassistant.resource.income_uah
import com.gmail.vanyadubik.myassistant.resource.military_levy
import com.gmail.vanyadubik.myassistant.resource.military_levy_fixed
import com.gmail.vanyadubik.myassistant.resource.nbu_rate
import com.gmail.vanyadubik.myassistant.resource.single_tax
import com.gmail.vanyadubik.myassistant.resource.single_tax_fixed
import com.gmail.vanyadubik.myassistant.resource.total_tax
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResultCard(result: TaxCalculation, modifier: Modifier = Modifier) {
    val uahSymbol = stringResource(Res.string.currency_uah)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.nbu_rate), style = MaterialTheme.typography.labelLarge)
            result.incomes.forEach { income ->
                ResultRow(
                    "${income.date.formatDate()} · ${income.amount.formatNumber()} ${income.currency.code}" +
                        " × ${income.rate.formatNumber(4)}",
                    "${income.amountUah.formatNumber()} $uahSymbol",
                )
            }
            HorizontalDivider()
            ResultRow(stringResource(Res.string.income_uah), "${result.amountUah.formatNumber()} $uahSymbol")
            HorizontalDivider()
            ResultRow(
                if (result.group.isFixed) {
                    stringResource(Res.string.single_tax_fixed)
                } else {
                    stringResource(Res.string.single_tax, "${result.group.singleTaxPercent.formatNumber(0)}%")
                },
                "${result.singleTax.formatNumber()} $uahSymbol",
            )
            ResultRow(
                if (result.group.isFixed) {
                    stringResource(Res.string.military_levy_fixed)
                } else {
                    stringResource(Res.string.military_levy, "${result.group.militaryLevyPercent.formatNumber(0)}%")
                },
                "${result.militaryLevy.formatNumber()} $uahSymbol",
            )
            if (result.esv > 0.0) {
                ResultRow(stringResource(Res.string.esv, result.months), "${result.esv.formatNumber()} $uahSymbol")
            }
            HorizontalDivider()
            ResultRow(stringResource(Res.string.total_tax), "${result.totalTax.formatNumber()} $uahSymbol", bold = true)
            ResultRow(stringResource(Res.string.annual_limit), "${result.group.annualIncomeLimit.formatNumber(0)} $uahSymbol")
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
