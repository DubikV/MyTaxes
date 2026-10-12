package com.gmail.vanyadubik.mytaxes.common.logger


expect object Logger {
    /**
     * Выводит сообщение об ошибке (Error level) в консоль/Logcat.
     * @param tag Метка (тег) для сообщения.
     * @param message Сообщение об ошибке.
     * @param throwable Исключение (опционально), которое нужно вывести.
     */
    fun e(tag: String, message: String, throwable: Throwable? = null)

    // Могут быть добавлены другие уровни: d (Debug), i (Info) и т.д.
}

/**
 * Ожидаемый интерфейс для кросс-платформенного логгирования.
 * Реализуется с помощью expect/actual.
 */
expect class AppLogger(tag: String) {
    fun log(level: LogLevel, message: String, throwable: Throwable? = null)
}

// Удобные функции-обертки для общего использования
fun LogV(tag: String, message: String, throwable: Throwable? = null) {
    AppLogger(tag).log(LogLevel.VERBOSE, message, throwable)
}

fun LogD(tag: String, message: String, throwable: Throwable? = null) {
    AppLogger(tag).log(LogLevel.DEBUG, message, throwable)
}

fun LogI(tag: String, message: String, throwable: Throwable? = null) {
    AppLogger(tag).log(LogLevel.INFO, message, throwable)
}

fun LogW(tag: String, message: String, throwable: Throwable? = null) {
    AppLogger(tag).log(LogLevel.WARNING, message, throwable)
}

fun LogE(tag: String, message: String, throwable: Throwable? = null) {
    AppLogger(tag).log(LogLevel.ERROR, message, throwable)
}