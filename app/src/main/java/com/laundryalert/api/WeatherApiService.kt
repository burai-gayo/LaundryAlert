package com.laundryalert.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String? = null,
        @Query("timezone") timezone: String = "Asia/Tokyo"
    ): WeatherResponse
    
    @GET("v1/forecast")
    suspend fun getHourlyForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String,
        @Query("timezone") timezone: String = "Asia/Tokyo"
    ): WeatherResponse
    
    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/"
        
        fun create(): WeatherApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            return retrofit.create(WeatherApiService::class.java)
        }
    }
}

// APIレスポンス用のデータクラス
data class WeatherResponse(
    val current: CurrentWeather,
    val hourly: HourlyWeatherResponse
)

data class CurrentWeather(
    val time: String,
    val temperature_2m: Double,
    val relative_humidity_2m: Double,
    val precipitation_probability: Int?
)

data class HourlyWeatherResponse(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val precipitation_probability: List<Int>
)

