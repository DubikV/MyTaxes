package com.gmail.vanyadubik.mytaxes.push

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gmail.vanyadubik.mytaxes.MainActivity
import com.gmail.vanyadubik.mytaxes.R
import com.gmail.vanyadubik.mytaxes.domen.usecase.PushNotificationUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class TaxMessagingService : FirebaseMessagingService() {

    /** FCM rotated the token: store the new one for the signed in user (ignored for guests). */
    override fun onNewToken(token: String) {
        val koin = GlobalContext.getOrNull() ?: return
        koin.get<CoroutineScope>().launch { koin.get<PushNotificationUseCase>().register(token) }
    }

    /**
     * Called only while the app is in the foreground; in the background the system tray shows
     * notification messages by itself.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.notification ?: return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return
        val intent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val built = NotificationCompat.Builder(this, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setAutoCancel(true)
            .setContentIntent(intent)
            .build()
        NotificationManagerCompat.from(this).notify(message.messageId.hashCode(), built)
    }
}
