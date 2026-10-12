package com.gmail.vanyadubik.mytaxes.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.gmail.vanyadubik.mytaxes.R

const val REMINDER_CHANNEL_ID = "tax_reminders"

/** Android 8+ shows notifications only through a channel; the backend sends to this one by id. */
fun createReminderChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(
        REMINDER_CHANNEL_ID,
        context.getString(R.string.notification_channel_reminders),
        NotificationManager.IMPORTANCE_HIGH,
    )
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
}
