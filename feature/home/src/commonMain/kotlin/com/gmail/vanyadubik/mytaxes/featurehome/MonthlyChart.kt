package com.gmail.vanyadubik.myassistant.featurehome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gmail.vanyadubik.myassistant.commonui.utils.formatNumber
import com.gmail.vanyadubik.myassistant.domen.model.MonthlyStats
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.chart_expenses
import com.gmail.vanyadubik.myassistant.resource.chart_income
import com.gmail.vanyadubik.myassistant.resource.chart_tax
import org.jetbrains.compose.resources.stringResource

private val IncomeColor = Color(0xFF2E9E5B)
private val TaxColor = Color(0xFFE08A1E)
private val ExpenseColor = Color(0xFFD9534F)
private val ChartHeight = 160.dp

/** Grouped bar chart: income, taxes and expenses for every month. */
@Composable
fun MonthlyChart(stats: List<MonthlyStats>, modifier: Modifier = Modifier) {
    val maxValue = stats.maxOf { maxOf(it.income, it.tax, it.expenses) }.coerceAtLeast(1.0)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(IncomeColor, stringResource(Res.string.chart_income))
            LegendItem(TaxColor, stringResource(Res.string.chart_tax))
            LegendItem(ExpenseColor, stringResource(Res.string.chart_expenses))
        }
        Text("${maxValue.formatNumber(0)} UAH", style = MaterialTheme.typography.labelSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            stats.forEach { month ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.height(ChartHeight),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Bar(month.income / maxValue, IncomeColor)
                        Bar(month.tax / maxValue, TaxColor)
                        Bar(month.expenses / maxValue, ExpenseColor)
                    }
                    Text(
                        "${month.month.toString().padStart(2, '0')}.${(month.year % 100).toString().padStart(2, '0')}",
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun Bar(fraction: Double, color: Color) {
    Box(
        modifier = Modifier
            .width(10.dp)
            // A tiny non-zero value stays visible as a thin line
            .height(if (fraction > 0.0) (ChartHeight * fraction.toFloat()).coerceAtLeast(2.dp) else 0.dp)
            .background(color, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
    )
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
