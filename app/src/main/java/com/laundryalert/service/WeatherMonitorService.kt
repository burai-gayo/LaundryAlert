package com.laundryalert.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.laundryalert.worker.WeatherCheckWorker
import java.util.concurrent.TimeUnit

class WeatherMonitorService : Service() {
    
    companion object {
        private const val WEATHER_MONITOR_WORK_NAME = "weather_monitor_periodic"
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startWeatherMonitoring()
        return START_STICKY
    }
    
    private fun startWeatherMonitoring() {
        // 15分間隔で天気をチェック
        val weatherCheckRequest = PeriodicWorkRequestBuilder<WeatherCheckWorker>(
            15, TimeUnit.MINUTES
        ).build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WEATHER_MONITOR_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            weatherCheckRequest
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // サービス終了時にワーカーをキャンセル
        WorkManager.getInstance(this).cancelUniqueWork(WEATHER_MONITOR_WORK_NAME)
    }
}

