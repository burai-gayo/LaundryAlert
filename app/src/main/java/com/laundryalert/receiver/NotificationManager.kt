package com.laundryalert.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.laundryalert.MainActivity
import com.laundryalert.R

class NotificationManager(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID_RAIN_ALERT = "rain_alert_channel"
        private const val CHANNEL_ID_GENERAL = "general_channel"
        private const val NOTIFICATION_ID_RAIN_ALERT = 1001
        private const val NOTIFICATION_ID_GENERAL = 1002
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // 雨アラート用チャンネル
            val rainAlertChannel = NotificationChannel(
                CHANNEL_ID_RAIN_ALERT,
                "雨アラート",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "洗濯物の取り込みアラート"
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
            }
            
            // 一般通知用チャンネル
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "一般通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "アプリの一般的な通知"
            }
            
            notificationManager.createNotificationChannel(rainAlertChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
    
    fun showRainAlert(minutesUntilRain: Int = 30) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 取り込み完了アクション
        val bringInIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_BRING_IN
        }
        val bringInPendingIntent = PendingIntent.getBroadcast(
            context, 0, bringInIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // スヌーズアクション
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, 1, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_RAIN_ALERT)
            .setSmallIcon(R.drawable.ic_rain_notification)
            .setContentTitle(context.getString(R.string.rain_alert))
            .setContentText(context.getString(R.string.rain_expected_in, minutesUntilRain))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(context.getString(R.string.bring_in_now)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_check,
                context.getString(R.string.mark_as_brought_in),
                bringInPendingIntent
            )
            .addAction(
                R.drawable.ic_snooze,
                context.getString(R.string.snooze_10_min),
                snoozePendingIntent
            )
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_RAIN_ALERT, notification)
        }
    }
    
    fun showGeneralNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_GENERAL, notification)
        }
    }
    
    fun cancelRainAlert() {
        with(NotificationManagerCompat.from(context)) {
            cancel(NOTIFICATION_ID_RAIN_ALERT)
        }
    }
    
    fun cancelAllNotifications() {
        with(NotificationManagerCompat.from(context)) {
            cancelAll()
        }
    }
}

