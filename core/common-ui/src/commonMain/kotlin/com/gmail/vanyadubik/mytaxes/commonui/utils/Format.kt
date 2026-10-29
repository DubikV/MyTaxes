package com.gmail.vanyadubik.myassistant.commonui.utils

import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

/** Locale independent number formatting: 1 234 567.89 */
fun Double.formatNumber(digits: Int = 2): String {
    val factor = 10.0.pow(digits)
    val scaled = round(abs(this) * factor).toLong()
    val factorLong = factor.toLong()
    val integer = (scaled / factorLong).toString()
        .reversed().chunked(3).joinToString(" ").reversed()
    val fraction = if (digits > 0) "." + (scaled % factorLong).toString().padStart(digits, '0') else ""
    return (if (this < 0 && scaled != 0L) "-" else "") + integer + fraction
}

fun LocalDate.formatDate(): String =
    "${day.toString().padStart(2, '0')}.${(month.ordinal + 1).toString().padStart(2, '0')}.$year"
