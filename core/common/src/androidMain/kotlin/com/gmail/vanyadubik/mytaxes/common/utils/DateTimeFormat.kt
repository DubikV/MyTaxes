package com.gmail.vanyadubik.myassistant.common.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

actual object DateTimeFormat {

    actual fun format(time: Long,
                      pattern: String,
                      timeZone: String): String {
        val df = SimpleDateFormat(pattern, Locale.getDefault()).apply {
            this.timeZone = TimeZone.getTimeZone(timeZone)
        }
        return df.format(Date(time))
    }

    actual fun parse(time: String,
                     pattern: String,
                     timeZone: String): Long {

        val df = SimpleDateFormat(pattern, Locale.getDefault()).apply {
            this.timeZone = TimeZone.getTimeZone(timeZone)
            isLenient = false
        }

        return try {
            val date = df.parse(time)
            date?.time ?: 0L
        } catch (e: java.text.ParseException) {
            e.printStackTrace()
            0L
        }
    }
}