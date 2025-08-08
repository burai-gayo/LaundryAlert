package com.laundryalert.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.laundryalert.model.LaundryStatus
import com.laundryalert.receiver.NotificationManager
import com.laundryalert.repository.LaundryRepository
import com.laundryalert.repository.WeatherRepository

class SnoozeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val laundryRepository = LaundryRepository(applicationContext)
    private val weatherRepository = WeatherRepository()
    private val notificationManager = NotificationManager(applicationContext)
    
    override suspend fun doWork(): Result {
        return try {
            // 洗濯物がまだ干されているかチェック
            val currentStatus = laundryRepository.getCurrentStatus()
            if (currentStatus != LaundryStatus.HANGING) {
                // 既に取り込まれている場合は何もしない
                return Result.success()
            }
            
            // 位置情報を取得（簡略化のため固定値を使用）
            val latitude = 35.6762 // 東京の緯度
            val longitude = 139.6503 // 東京の経度
            
            // 最新の天気情報を取得
            val weather = weatherRepository.getCurrentWeather(latitude, longitude)
            
            // まだ雨の可能性が高いかチェック
            val precipitationThreshold = getStoredPrecipitationThreshold()
            if (weather.precipitationProbability > precipitationThreshold) {
                // 再度雨アラートを送信
                notificationManager.showRainAlert(20) // 20分後に雨予想（時間が経過したため短縮）
                
                // アラート時刻を更新
                laundryRepository.saveWeatherAlert(
                    weather.precipitationProbability,
                    System.currentTimeMillis()
                )
            } else {
                // 雨の可能性が低くなった場合は安全通知
                notificationManager.showGeneralNotification(
                    "天気回復",
                    "雨の可能性が低くなりました"
                )
            }
            
            Result.success()
            
        } catch (e: Exception) {
            // エラー時は失敗として処理（再試行しない）
            Result.failure()
        }
    }
    
    private fun getStoredPrecipitationThreshold(): Int {
        // SharedPreferencesから閾値を取得（デフォルト30%）
        val sharedPrefs = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return sharedPrefs.getInt("precipitation_threshold", 30)
    }
}

