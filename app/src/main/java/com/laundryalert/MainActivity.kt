package com.laundryalert

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.laundryalert.databinding.ActivityMainBinding
import com.laundryalert.model.LaundryStatus
import com.laundryalert.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    
    // 位置情報権限のリクエスト
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // 精密な位置情報が許可された
                viewModel.startLocationUpdates()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // おおよその位置情報が許可された
                viewModel.startLocationUpdates()
            }
            else -> {
                // 位置情報が拒否された
                showToast(getString(R.string.error_permission_location))
            }
        }
    }
    
    // 通知権限のリクエスト（Android 13+）
    private val notificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showToast(getString(R.string.error_permission_notification))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // ViewModelの初期化
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // UI初期化
        setupUI()
        
        // 権限チェック
        checkPermissions()
        
        // データ観察
        observeViewModel()
        
        // 初期データ読み込み
        viewModel.loadInitialData()
    }
    
    private fun setupUI() {
        // 洗濯物を干すボタン
        binding.hangLaundryButton.setOnClickListener {
            viewModel.hangLaundry()
        }
        
        // 取り込むボタン
        binding.bringInButton.setOnClickListener {
            viewModel.bringInLaundry()
        }
        
        // 設定ボタン
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        // 履歴ボタン
        binding.historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }
    
    private fun checkPermissions() {
        // 位置情報権限のチェック
        if (!hasLocationPermission()) {
            requestLocationPermission()
        } else {
            viewModel.startLocationUpdates()
        }
        
        // 通知権限のチェック（Android 13+）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    
    private fun observeViewModel() {
        // 天気情報の観察
        viewModel.weatherData.observe(this) { weather ->
            weather?.let {
                updateWeatherUI(it)
            }
        }
        
        // 洗濯物状態の観察
        viewModel.laundryStatus.observe(this) { status ->
            updateLaundryStatusUI(status)
        }
        
        // エラーメッセージの観察
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                showToast(it)
                viewModel.clearErrorMessage()
            }
        }
        
        // ローディング状態の観察
        viewModel.isLoading.observe(this) { isLoading ->
            // ローディングインジケーターの表示/非表示
            // 今回は簡単のため省略
        }
    }
    
    private fun updateWeatherUI(weather: WeatherData) {
        binding.temperatureText.text = getString(R.string.temperature, weather.temperature)
        binding.humidityText.text = getString(R.string.humidity, weather.humidity)
        binding.precipitationText.text = getString(R.string.precipitation, weather.precipitationProbability)
        
        // 天気アイコンの更新
        val iconRes = when {
            weather.precipitationProbability > 70 -> R.drawable.ic_rainy
            weather.precipitationProbability > 30 -> R.drawable.ic_cloudy
            else -> R.drawable.ic_sunny
        }
        binding.weatherIcon.setImageResource(iconRes)
    }
    
    private fun updateLaundryStatusUI(status: LaundryStatus) {
        val statusText = when (status) {
            LaundryStatus.NOT_HANGING -> getString(R.string.not_hanging)
            LaundryStatus.HANGING -> getString(R.string.currently_drying)
            LaundryStatus.BROUGHT_IN -> getString(R.string.brought_in)
        }
        binding.laundryStatusText.text = statusText
        
        // ボタンの有効/無効状態の更新
        binding.hangLaundryButton.isEnabled = status != LaundryStatus.HANGING
        binding.bringInButton.isEnabled = status == LaundryStatus.HANGING
        
        // アイコンの更新
        val iconRes = when (status) {
            LaundryStatus.NOT_HANGING -> R.drawable.ic_laundry_basket
            LaundryStatus.HANGING -> R.drawable.ic_laundry_hanging
            LaundryStatus.BROUGHT_IN -> R.drawable.ic_laundry_done
        }
        binding.laundryIcon.setImageResource(iconRes)
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

// データクラス
data class WeatherData(
    val temperature: Int,
    val humidity: Int,
    val precipitationProbability: Int,
    val description: String
)

