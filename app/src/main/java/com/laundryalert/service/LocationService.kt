package com.laundryalert.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationService(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        10000L // 10秒間隔
    ).apply {
        setMinUpdateDistanceMeters(100f) // 100m移動したら更新
        setMaxUpdateDelayMillis(30000L) // 最大30秒遅延
    }.build()
    
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            throw SecurityException("位置情報の権限がありません")
        }
        
        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            continuation.resume(location)
                        } else {
                            // 最後の位置情報がない場合は新しく取得
                            requestNewLocation(continuation)
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    private fun requestNewLocation(continuation: kotlinx.coroutines.CancellableContinuation<Location?>) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                fusedLocationClient.removeLocationUpdates(this)
                continuation.resume(location)
            }
            
            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                if (!locationAvailability.isLocationAvailable) {
                    fusedLocationClient.removeLocationUpdates(this)
                    continuation.resume(null)
                }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            
            // タイムアウト処理
            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
            
        } catch (e: SecurityException) {
            continuation.resumeWithException(e)
        }
    }
    
    fun startLocationUpdates(callback: LocationCallback) {
        if (!hasLocationPermission()) {
            throw SecurityException("位置情報の権限がありません")
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            throw e
        }
    }
    
    fun stopLocationUpdates(callback: LocationCallback) {
        fusedLocationClient.removeLocationUpdates(callback)
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun getStoredLocation(): Pair<Double, Double>? {
        val sharedPrefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        val latitude = sharedPrefs.getFloat("latitude", Float.NaN).toDouble()
        val longitude = sharedPrefs.getFloat("longitude", Float.NaN).toDouble()
        
        return if (latitude.isNaN() || longitude.isNaN()) {
            null
        } else {
            Pair(latitude, longitude)
        }
    }
    
    fun saveLocation(latitude: Double, longitude: Double) {
        val sharedPrefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putFloat("latitude", latitude.toFloat())
            .putFloat("longitude", longitude.toFloat())
            .putLong("location_timestamp", System.currentTimeMillis())
            .apply()
    }
    
    fun isLocationStale(): Boolean {
        val sharedPrefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        val timestamp = sharedPrefs.getLong("location_timestamp", 0L)
        val currentTime = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000L
        
        return (currentTime - timestamp) > oneHour
    }
}

