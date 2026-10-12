package com.gmail.vanyadubik.mytaxes.common.utils

import platform.Foundation.NSBundle

actual class AppInfo {
    actual fun getAppVersion(): String {
        val infoDictionary = NSBundle.mainBundle.infoDictionary
        return infoDictionary?.get("CFBundleShortVersionString") as? String ?: "1.0.0"
    }

    actual fun getAppCodeVersion(): Long {
        val infoDictionary = NSBundle.mainBundle.infoDictionary

        // CFBundleVersion -> Build Number (42)
        val versionCodeString = infoDictionary?.get("CFBundleVersion") as? String ?: "1"
        val versionCode = versionCodeString.toLongOrNull() ?: 1L
        return versionCode
    }

}