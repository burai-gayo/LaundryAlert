package com.laundryalert.service

import android.content.Context
import com.laundryalert.WeatherData
import com.laundryalert.repository.HourlyWeather
import com.laundryalert.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherManager(private val context: Context) {
    
    private val weatherRepository = WeatherRepository()
    private val locationService = LocationService(context)
    
    suspend fun getCurrentWeatherWithLocation(): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                // 位置情報を取得
                val location = getLocationForWeather()
                if (location == null) {
                    // 位置情報が取得できない場合はデフォルト位置（東京）を使用
                    return@withContext weatherRepository.getCurrentWeather(35.6762, 139.6503)
                }
                
                // 天気情報を取得
                val weather = weatherRepository.getCurrentWeather(location.first, location.second)
                
                // 位置情報を保存
                locationService.saveLocation(location.first, location.second)
                
                weather
                
            } catch (e: Exception) {
                // エラー時はnullを返す
                null
            }
        }
    }
    
    suspend fun getHourlyForecastWithLocation(): List<HourlyWeather> {
        return withContext(Dispatchers.IO) {
            try {
                // 位置情報を取得
                val location = getLocationForWeather()
                if (location == null) {
                    // 位置情報が取得できない場合はデフォルト位置（東京）を使用
                    return@withContext weatherRepository.getHourlyForecast(35.6762, 139.6503)
                }
                
                // 時間別予報を取得
                weatherRepository.getHourlyForecast(location.first, location.second)
                
            } catch (e: Exception) {
                // エラー時は空のリストを返す
                emptyList()
            }
        }
    }
    
    private suspend fun getLocationForWeather(): Pair<Double, Double>? {
        return try {
            // 保存された位置情報をチェック
            val storedLocation = locationService.getStoredLocation()
            if (storedLocation != null && !locationService.isLocationStale()) {
                return storedLocation
            }
            
            // 新しい位置情報を取得
            val currentLocation = locationService.getCurrentLocation()
            if (currentLocation != null) {
                Pair(currentLocation.latitude, currentLocation.longitude)
            } else {
                storedLocation // 古い位置情報でも使用
            }
            
        } catch (e: Exception) {
            // 権限エラーなどの場合は保存された位置情報を使用
            locationService.getStoredLocation()
        }
    }
    
    suspend fun analyzeRainRisk(hourlyForecast: List<HourlyWeather>): RainRiskAnalysis {
        return withContext(Dispatchers.Default) {
            val currentTime = System.currentTimeMillis()
            val oneHour = 60 * 60 * 1000L
            val twoHours = 2 * oneHour
            val fourHours = 4 * oneHour
            
            // 今後の時間帯別に雨リスクを分析
            val nextHourRisk = hourlyForecast.take(1).maxOfOrNull { it.precipitationProbability } ?: 0
            val next2HoursRisk = hourlyForecast.take(2).maxOfOrNull { it.precipitationProbability } ?: 0
            val next4HoursRisk = hourlyForecast.take(4).maxOfOrNull { it.precipitationProbability } ?: 0
            
            // 雨が始まる予想時刻を計算
            val rainStartTime = calculateRainStartTime(hourlyForecast)
            
            // リスクレベルを決定
            val riskLevel = when {
                nextHourRisk > 70 -> RiskLevel.HIGH
                next2HoursRisk > 50 -> RiskLevel.MEDIUM
                next4HoursRisk > 30 -> RiskLevel.LOW
                else -> RiskLevel.NONE
            }
            
            RainRiskAnalysis(
                riskLevel = riskLevel,
                maxPrecipitationProbability = next4HoursRisk,
                rainStartTime = rainStartTime,
                recommendedAction = getRecommendedAction(riskLevel, rainStartTime)
            )
        }
    }
    
    private fun calculateRainStartTime(hourlyForecast: List<HourlyWeather>): Long? {
        val threshold = getStoredPrecipitationThreshold()
        val rainHour = hourlyForecast.find { it.precipitationProbability > threshold }
        
        return if (rainHour != null) {
            // 簡略化：現在時刻から予報の順番で時間を計算
            val hourIndex = hourlyForecast.indexOf(rainHour)
            System.currentTimeMillis() + (hourIndex * 60 * 60 * 1000L)
        } else {
            null
        }
    }
    
    private fun getRecommendedAction(riskLevel: RiskLevel, rainStartTime: Long?): String {
        return when (riskLevel) {
            RiskLevel.HIGH -> "すぐに洗濯物を取り込んでください"
            RiskLevel.MEDIUM -> "1-2時間以内に取り込むことをお勧めします"
            RiskLevel.LOW -> "念のため天気の変化にご注意ください"
            RiskLevel.NONE -> "現在のところ雨の心配はありません"
        }
    }
    
    private fun getStoredPrecipitationThreshold(): Int {
        val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return sharedPrefs.getInt("precipitation_threshold", 30)
    }
    
    fun cacheWeatherData(weather: WeatherData) {
        val sharedPrefs = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putInt("temperature", weather.temperature)
            .putInt("humidity", weather.humidity)
            .putInt("precipitation", weather.precipitationProbability)
            .putString("description", weather.description)
            .putLong("cache_timestamp", System.currentTimeMillis())
            .apply()
    }
    
    fun getCachedWeatherData(): WeatherData? {
        val sharedPrefs = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
        val timestamp = sharedPrefs.getLong("cache_timestamp", 0L)
        val currentTime = System.currentTimeMillis()
        val fifteenMinutes = 15 * 60 * 1000L
        
        // 15分以内のキャッシュのみ有効
        return if ((currentTime - timestamp) < fifteenMinutes) {
            WeatherData(
                temperature = sharedPrefs.getInt("temperature", 0),
                humidity = sharedPrefs.getInt("humidity", 0),
                precipitationProbability = sharedPrefs.getInt("precipitation", 0),
                description = sharedPrefs.getString("description", "") ?: ""
            )
        } else {
            null
        }
    }
}

// データクラス
data class RainRiskAnalysis(
    val riskLevel: RiskLevel,
    val maxPrecipitationProbability: Int,
    val rainStartTime: Long?,
    val recommendedAction: String
)

enum class RiskLevel {
    NONE,    // リスクなし
    LOW,     // 低リスク
    MEDIUM,  // 中リスク
    HIGH     // 高リスク
}

