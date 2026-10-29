package com.gmail.vanyadubik.myassistant.common.utils

expect class AppInfo {
    fun getAppVersion(): String
    fun getAppCodeVersion(): Long
}