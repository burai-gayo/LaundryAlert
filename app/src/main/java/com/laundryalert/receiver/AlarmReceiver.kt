package com.laundryalert.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.laundryalert.worker.WeatherCheckWorker

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_WEATHER_CHECK = "com.laundryalert.ACTION_WEATHER_CHECK"
        const val ACTION_RAIN_ALERT = "com.laundryalert.ACTION_RAIN_ALERT"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_WEATHER_CHECK -> {
                // 天気チェックのワーカーを開始
                val workRequest = OneTimeWorkRequestBuilder<WeatherCheckWorker>().build()
                WorkManager.getInstance(context).enqueue(workRequest)
            }
            
            ACTION_RAIN_ALERT -> {
                // 雨アラートの通知を表示
                val notificationManager = NotificationManager(context)
                notificationManager.showRainAlert()
            }
        }
    }
}

