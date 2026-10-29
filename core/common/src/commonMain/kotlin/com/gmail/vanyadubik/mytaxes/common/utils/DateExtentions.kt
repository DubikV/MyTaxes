package com.gmail.vanyadubik.myassistant.common.utils

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