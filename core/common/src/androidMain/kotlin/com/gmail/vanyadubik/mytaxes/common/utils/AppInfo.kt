package com.gmail.vanyadubik.mytaxes.common.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

actual class AppInfo(private val context: Context) {
    //BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE

    fun getPackageInfo(): PackageInfo{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
    }

    actual fun getAppVersion(): String {
        return try {
            getPackageInfo().versionName ?: "unknown"
        } catch (_: Exception) {
            "unknown"
        }
    }

    actual fun getAppCodeVersion(): Long {
        val packageInfo = getPackageInfo()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    }
}