package com.laundryalert.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.laundryalert.model.LaundryStatus
import com.laundryalert.receiver.NotificationManager
import com.laundryalert.repository.LaundryRepository
import com.laundryalert.repository.WeatherRepository

class WeatherCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val laundryRepository = LaundryRepository(applicationContext)
    private val weatherRepository = WeatherRepository()
    private val notificationManager = NotificationManager(applicationContext)
    
    override suspend fun doWork(): Result {
        return try {
            // 洗濯物が干されているかチェック
            val isHanging = laundryRepository.isLaundryHanging()
            if (!isHanging) {
                return Result.success()
            }
            
            // 位置情報を取得（簡略化のため固定値を使用）
            val latitude = 35.6762 // 東京の緯度
            val longitude = 139.6503 // 東京の経度
            
            // 天気情報を取得
            val weather = weatherRepository.getCurrentWeather(latitude, longitude)
            
            // 雨の可能性をチェック
            val precipitationThreshold = getStoredPrecipitationThreshold()
            if (weather.precipitationProbability > precipitationThreshold) {
                // 前回のアラート時刻をチェック（重複アラートを防ぐ）
                val lastAlertTime = laundryRepository.getLastAlertTime()
                val currentTime = System.currentTimeMillis()
                val timeSinceLastAlert = currentTime - lastAlertTime
                
                // 30分以内に同じアラートを送信しない
                if (timeSinceLastAlert > 30 * 60 * 1000) {
                    // 雨アラートを送信
                    notificationManager.showRainAlert(30) // 30分後に雨予想
                    
                    // アラート時刻を保存
                    laundryRepository.saveWeatherAlert(
                        weather.precipitationProbability,
                        currentTime
                    )
                }
            }
            
            Result.success()
            
        } catch (e: Exception) {
            // エラー時は再試行
            Result.retry()
        }
    }
    
    private fun getStoredPrecipitationThreshold(): Int {
        // SharedPreferencesから閾値を取得（デフォルト30%）
        val sharedPrefs = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return sharedPrefs.getInt("precipitation_threshold", 30)
    }
}

