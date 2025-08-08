package com.laundryalert.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.laundryalert.WeatherData
import com.laundryalert.model.LaundryStatus
import com.laundryalert.repository.WeatherRepository
import com.laundryalert.repository.LaundryRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val weatherRepository = WeatherRepository()
    private val laundryRepository = LaundryRepository(application)
    
    // 天気データ
    private val _weatherData = MutableLiveData<WeatherData?>()
    val weatherData: LiveData<WeatherData?> = _weatherData
    
    // 洗濯物状態
    private val _laundryStatus = MutableLiveData<LaundryStatus>()
    val laundryStatus: LiveData<LaundryStatus> = _laundryStatus
    
    // エラーメッセージ
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // ローディング状態
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        // 初期状態の設定
        _laundryStatus.value = LaundryStatus.NOT_HANGING
        _isLoading.value = false
    }
    
    fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // 保存された洗濯物状態を読み込み
                val savedStatus = laundryRepository.getCurrentStatus()
                _laundryStatus.value = savedStatus
                
                // 天気データを取得
                loadWeatherData()
                
            } catch (e: Exception) {
                _errorMessage.value = "データの読み込みに失敗しました: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun startLocationUpdates() {
        // 位置情報の更新を開始
        // 実装は後で追加
        loadWeatherData()
    }
    
    private fun loadWeatherData() {
        viewModelScope.launch {
            try {
                // デモ用のダミーデータ
                // 実際の実装では位置情報を使用してAPIから取得
                val dummyWeather = WeatherData(
                    temperature = 25,
                    humidity = 60,
                    precipitationProbability = 20,
                    description = "晴れ時々曇り"
                )
                _weatherData.value = dummyWeather
                
                // 雨の可能性をチェック
                checkRainAlert(dummyWeather)
                
            } catch (e: Exception) {
                _errorMessage.value = "天気情報の取得に失敗しました: ${e.message}"
            }
        }
    }
    
    fun hangLaundry() {
        viewModelScope.launch {
            try {
                _laundryStatus.value = LaundryStatus.HANGING
                laundryRepository.updateStatus(LaundryStatus.HANGING)
                
                // アラーム設定
                scheduleWeatherCheck()
                
            } catch (e: Exception) {
                _errorMessage.value = "状態の更新に失敗しました: ${e.message}"
            }
        }
    }
    
    fun bringInLaundry() {
        viewModelScope.launch {
            try {
                _laundryStatus.value = LaundryStatus.BROUGHT_IN
                laundryRepository.updateStatus(LaundryStatus.BROUGHT_IN)
                
                // アラーム解除
                cancelWeatherCheck()
                
            } catch (e: Exception) {
                _errorMessage.value = "状態の更新に失敗しました: ${e.message}"
            }
        }
    }
    
    private fun checkRainAlert(weather: WeatherData) {
        // 洗濯物が干されていて、雨の可能性が高い場合にアラート
        if (_laundryStatus.value == LaundryStatus.HANGING && 
            weather.precipitationProbability > 30) {
            // 通知を送信（実装は後で追加）
        }
    }
    
    private fun scheduleWeatherCheck() {
        // 定期的な天気チェックのスケジュール（実装は後で追加）
    }
    
    private fun cancelWeatherCheck() {
        // 天気チェックのキャンセル（実装は後で追加）
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

