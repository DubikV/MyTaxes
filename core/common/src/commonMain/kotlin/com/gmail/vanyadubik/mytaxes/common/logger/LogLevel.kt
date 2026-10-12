package com.gmail.vanyadubik.mytaxes.common.logger

enum class LogLevel(val priority: Int) {
    VERBOSE(2),
    DEBUG(3),
    INFO(4),
    WARNING(5),
    ERROR(6),
    WTF(7) //What a Terrible Failure, а не общепринятая интерпретация
}