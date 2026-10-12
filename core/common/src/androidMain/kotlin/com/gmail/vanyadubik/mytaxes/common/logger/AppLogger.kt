package com.gmail.vanyadubik.mytaxes.common.logger

import android.util.Log

actual object Logger {
    private const val DEFAULT_TAG = "KMP_APP" // Тег по умолчанию

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            // Вывод ошибки с исключением (stack trace)
            Log.e(tag.ifEmpty { DEFAULT_TAG }, message, throwable)
        } else {
            // Вывод простого сообщения об ошибке
            Log.e(tag.ifEmpty { DEFAULT_TAG }, message)
        }
    }
}


actual class AppLogger actual constructor(private val tag: String) {

    actual fun log(level: LogLevel, message: String, throwable: Throwable?) {
        when (level) {
            LogLevel.VERBOSE -> Log.v(tag, message, throwable)
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARNING -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
            LogLevel.WTF -> Log.wtf(tag, message, throwable) // Android-специфичный
        }
    }
}