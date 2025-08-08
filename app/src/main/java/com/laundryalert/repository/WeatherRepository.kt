package com.laundryalert.repository

import com.laundryalert.WeatherData
import com.laundryalert.api.WeatherApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository {
    
    private val apiService = WeatherApiService.create()
    
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherData {
        return withContext(Dispatchers.IO) {
            try {
                // Open-Meteo APIを使用して天気データを取得
                val response = apiService.getCurrentWeather(
                    latitude = latitude,
                    longitude = longitude,
                    current = "temperature_2m,relative_humidity_2m,precipitation_probability",
                    hourly = "precipitation_probability",
                    timezone = "Asia/Tokyo"
                )
                
                // APIレスポンスをWeatherDataに変換
                WeatherData(
                    temperature = response.current.temperature_2m.toInt(),
                    humidity = response.current.relative_humidity_2m.toInt(),
                    precipitationProbability = response.current.precipitation_probability ?: 0,
                    description = getWeatherDescription(response.current.precipitation_probability ?: 0)
                )
                
            } catch (e: Exception) {
                // エラー時はダミーデータを返す（実際のアプリでは適切なエラーハンドリングが必要）
                WeatherData(
                    temperature = 25,
                    humidity = 60,
                    precipitationProbability = 20,
                    description = "天気情報取得エラー"
                )
            }
        }
    }
    
    suspend fun getHourlyForecast(latitude: Double, longitude: Double): List<HourlyWeather> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getHourlyForecast(
                    latitude = latitude,
                    longitude = longitude,
                    hourly = "temperature_2m,precipitation_probability",
                    timezone = "Asia/Tokyo"
                )
                
                // 今後24時間のデータを取得
                val times = response.hourly.time.take(24)
                val temperatures = response.hourly.temperature_2m.take(24)
                val precipitations = response.hourly.precipitation_probability.take(24)
                
                times.mapIndexed { index, time ->
                    HourlyWeather(
                        time = time,
                        temperature = temperatures[index].toInt(),
                        precipitationProbability = precipitations[index]
                    )
                }
                
            } catch (e: Exception) {
                // エラー時は空のリストを返す
                emptyList()
            }
        }
    }
    
    private fun getWeatherDescription(precipitationProbability: Int): String {
        return when {
            precipitationProbability > 70 -> "雨"
            precipitationProbability > 30 -> "曇り"
            else -> "晴れ"
        }
    }
}

// データクラス
data class HourlyWeather(
    val time: String,
    val temperature: Int,
    val precipitationProbability: Int
)

