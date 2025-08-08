package com.laundryalert.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.laundryalert.receiver.AlarmReceiver
import com.laundryalert.worker.WeatherCheckWorker
import java.util.concurrent.TimeUnit

class AlarmScheduler(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        private const val WEATHER_CHECK_REQUEST_CODE = 1001
        private const val WEATHER_CHECK_WORK_NAME = "weather_check_periodic"
    }
    
    fun scheduleWeatherCheck() {
        // Android 6.0以降ではWorkManagerを使用（バッテリー最適化対応）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scheduleWithWorkManager()
        } else {
            scheduleWithAlarmManager()
        }
    }
    
    fun cancelWeatherCheck() {
        // AlarmManagerのキャンセル
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WEATHER_CHECK
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WEATHER_CHECK_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        
        // WorkManagerのキャンセル
        workManager.cancelUniqueWork(WEATHER_CHECK_WORK_NAME)
    }
    
    private fun scheduleWithWorkManager() {
        // 15分間隔で天気をチェック
        val weatherCheckRequest = PeriodicWorkRequestBuilder<WeatherCheckWorker>(
            15, TimeUnit.MINUTES
        ).build()
        
        workManager.enqueueUniquePeriodicWork(
            WEATHER_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            weatherCheckRequest
        )
    }
    
    private fun scheduleWithAlarmManager() {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WEATHER_CHECK
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WEATHER_CHECK_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val intervalMillis = 15 * 60 * 1000L // 15分
        val triggerAtMillis = System.currentTimeMillis() + intervalMillis
        
        // 繰り返しアラームを設定
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            intervalMillis,
            pendingIntent
        )
    }
    
    fun scheduleOneTimeWeatherCheck(delayMinutes: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WEATHER_CHECK
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WEATHER_CHECK_REQUEST_CODE + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerAtMillis = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }
    
    fun hasScheduledAlarms(): Boolean {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WEATHER_CHECK
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WEATHER_CHECK_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        return pendingIntent != null
    }
}

