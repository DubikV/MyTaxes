package com.gmail.vanyadubik.mytaxes.common.utils

fun String?.capitalizeFirstLetter() = if (this.isNullOrEmpty()) {
    this
}else{
    val first = this[0].uppercaseChar()
    if (this.length == 1) {
        first.toString()
    }
    first + this.substring(1)
}