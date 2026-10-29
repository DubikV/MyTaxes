package com.gmail.vanyadubik.myassistant.common.logger

// iosMain/kotlin/AppLogger.kt

import platform.Foundation.NSLog

actual object Logger {
    private const val DEFAULT_TAG = "KMP_APP"

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        val fullMessage = buildString {
            append("ERROR: [${tag.ifEmpty { DEFAULT_TAG }}] $message")
            if (throwable != null) {
                append(". Throwable: $throwable")
            }
        }
        NSLog(fullMessage)
    }
}


actual class AppLogger actual constructor(private val tag: String) {

    actual fun log(level: LogLevel, message: String, throwable: Throwable?) {
        val levelTag = when (level) {
            LogLevel.VERBOSE -> "[V]"
            LogLevel.DEBUG -> "[D]"
            LogLevel.INFO -> "[I]"
            LogLevel.WARNING -> "[W]"
            LogLevel.ERROR -> "[E]"
            LogLevel.WTF -> "[WTF]"
        }

        // Форматирование вывода: [Уровень] ТЕГ: Сообщение
        // На iOS часто не используют трейс исключения в логах, а выводят только сообщение.
        val fullMessage = "$levelTag $tag: $message"

        NSLog(fullMessage)

        // Можно добавить вывод исключения, overlay:
        // if (throwable != null) {
        //     NSLog("Exception details: ${throwable.getStackTrace()}")
        // }
    }
}