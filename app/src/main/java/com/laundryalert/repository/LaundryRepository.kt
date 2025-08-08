package com.laundryalert.repository

import android.content.Context
import android.content.SharedPreferences
import com.laundryalert.model.LaundryStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LaundryRepository(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("laundry_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_LAUNDRY_STATUS = "laundry_status"
        private const val KEY_HANG_TIME = "hang_time"
        private const val KEY_LAST_UPDATE = "last_update"
    }
    
    suspend fun getCurrentStatus(): LaundryStatus {
        return withContext(Dispatchers.IO) {
            val statusName = sharedPreferences.getString(KEY_LAUNDRY_STATUS, LaundryStatus.NOT_HANGING.name)
            try {
                LaundryStatus.valueOf(statusName ?: LaundryStatus.NOT_HANGING.name)
            } catch (e: IllegalArgumentException) {
                LaundryStatus.NOT_HANGING
            }
        }
    }
    
    suspend fun updateStatus(status: LaundryStatus) {
        withContext(Dispatchers.IO) {
            val editor = sharedPreferences.edit()
            editor.putString(KEY_LAUNDRY_STATUS, status.name)
            editor.putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            
            // 洗濯物を干した時刻を記録
            if (status == LaundryStatus.HANGING) {
                editor.putLong(KEY_HANG_TIME, System.currentTimeMillis())
            } else if (status == LaundryStatus.NOT_HANGING) {
                editor.remove(KEY_HANG_TIME)
            }
            
            editor.apply()
        }
    }
    
    suspend fun getHangTime(): Long? {
        return withContext(Dispatchers.IO) {
            val hangTime = sharedPreferences.getLong(KEY_HANG_TIME, -1L)
            if (hangTime == -1L) null else hangTime
        }
    }
    
    suspend fun getLastUpdateTime(): Long {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getLong(KEY_LAST_UPDATE, 0L)
        }
    }
    
    suspend fun isLaundryHanging(): Boolean {
        return getCurrentStatus() == LaundryStatus.HANGING
    }
    
    suspend fun getHangingDuration(): Long? {
        return withContext(Dispatchers.IO) {
            val hangTime = getHangTime()
            if (hangTime != null && getCurrentStatus() == LaundryStatus.HANGING) {
                System.currentTimeMillis() - hangTime
            } else {
                null
            }
        }
    }
    
    suspend fun saveWeatherAlert(precipitationProbability: Int, alertTime: Long) {
        withContext(Dispatchers.IO) {
            val editor = sharedPreferences.edit()
            editor.putInt("last_precipitation_alert", precipitationProbability)
            editor.putLong("last_alert_time", alertTime)
            editor.apply()
        }
    }
    
    suspend fun getLastAlertTime(): Long {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getLong("last_alert_time", 0L)
        }
    }
}

