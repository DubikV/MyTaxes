package com.gmail.vanyadubik.mytaxes.common.utils

import kotlinx.datetime.LocalDateTime

fun String?.parseDateTime() = if(!this.isNullOrEmpty()) {
    try {
        LocalDateTime.parse(this.replace(' ', 'T'))
    } catch (e: IllegalArgumentException) {
        null
    }
}else {
    null
}