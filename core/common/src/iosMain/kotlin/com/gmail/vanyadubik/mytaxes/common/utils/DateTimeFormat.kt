package com.gmail.vanyadubik.mytaxes.common.utils

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeIntervalSince1970
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.systemTimeZone
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.timeZoneWithAbbreviation

actual object DateTimeFormat {
    actual fun format(time: Long,
                      pattern: String,
                      timeZone: String): String {
        val date = NSDate(timeIntervalSinceReferenceDate = (time.toDouble() / 1000.0) - NSTimeIntervalSince1970)
        val df = NSDateFormatter().apply {
            dateFormat = pattern
            this.timeZone = NSTimeZone.timeZoneWithAbbreviation(timeZone) ?: NSTimeZone.systemTimeZone()
            locale = NSLocale.currentLocale
        }

        return df.stringFromDate(date)
    }

    actual fun parse(time: String,
                     pattern: String,
                     timeZone: String): Long {

        val df = NSDateFormatter().apply {
            dateFormat = pattern
            this.timeZone = NSTimeZone.timeZoneWithAbbreviation(timeZone) ?: NSTimeZone.systemTimeZone()
            locale = NSLocale.currentLocale
        }
        val date = df.dateFromString(time)
        return date?.timeIntervalSince1970?.times(1000)?.toLong() ?: 0L
    }
}