package com.gmail.vanyadubik.myassistant.commonui.utils

import androidx.compose.runtime.Composable
import com.gmail.vanyadubik.myassistant.domen.model.TaxReminderMode
import com.gmail.vanyadubik.myassistant.resource.Res
import com.gmail.vanyadubik.myassistant.resource.tax_reminder_as_needed
import com.gmail.vanyadubik.myassistant.resource.tax_reminder_monthly
import org.jetbrains.compose.resources.stringResource

@Composable
fun TaxReminderMode.title(): String = stringResource(
    when (this) {
        TaxReminderMode.AS_NEEDED -> Res.string.tax_reminder_as_needed
        TaxReminderMode.MONTHLY -> Res.string.tax_reminder_monthly
    }
)
