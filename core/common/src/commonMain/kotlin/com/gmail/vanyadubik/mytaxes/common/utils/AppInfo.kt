package com.gmail.vanyadubik.mytaxes.common.utils

expect class AppInfo {
    fun getAppVersion(): String
    fun getAppCodeVersion(): Long
}