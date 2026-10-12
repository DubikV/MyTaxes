package com.gmail.vanyadubik.mytaxes.common.utils

expect object DateTimeFormat {
    fun format(time: Long,
               pattern: String,
               timeZone: String): String

    fun parse(time: String,
               pattern: String,
               timeZone: String): Long
}